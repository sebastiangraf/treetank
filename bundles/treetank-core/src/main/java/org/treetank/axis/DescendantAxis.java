/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.axis;

import java.util.Stack;

import org.treetank.api.IReadTransaction;
import org.treetank.api.IStructuralItem;
import org.treetank.settings.EFixed;

/**
 * <h1>DescendantAxis</h1>
 * 
 * <p>
 * Iterate over all descendants of kind ELEMENT or TEXT starting at a given node. Self is not included.
 * </p>
 */
public final class DescendantAxis extends AbsAxis {

    /** Stack for remembering next nodeKey in document order. */
    private Stack<Long> mRightSiblingKeyStack;

    /** The nodeKey of the next node to visit. */
    private long mNextKey;

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     */
    public DescendantAxis(final IReadTransaction rtx) {
        super(rtx);
    }

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mIncludeSelf
     *            Is self included?
     */
    public DescendantAxis(final IReadTransaction rtx, final boolean mIncludeSelf) {
        super(rtx, mIncludeSelf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final long mNodeKey) {
        super.reset(mNodeKey);
        mRightSiblingKeyStack = new Stack<Long>();
        if (isSelfIncluded()) {
            mNextKey = getTransaction().getNode().getNodeKey();
        } else {
            mNextKey = ((IStructuralItem)getTransaction().getNode()).getFirstChildKey();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        resetToLastKey();

        // Fail if there is no node anymore.
        if (mNextKey == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
            resetToStartKey();
            return false;
        }

        getTransaction().moveTo(mNextKey);

        // Fail if the subtree is finished.
        if (((IStructuralItem)getTransaction().getNode()).getLeftSiblingKey() == getStartKey()) {
            resetToStartKey();
            return false;
        }

        // Always follow first child if there is one.
        if (((IStructuralItem)getTransaction().getNode()).hasFirstChild()) {
            mNextKey = ((IStructuralItem)getTransaction().getNode()).getFirstChildKey();
            if (((IStructuralItem)getTransaction().getNode()).hasRightSibling()) {
                mRightSiblingKeyStack
                    .push(((IStructuralItem)getTransaction().getNode()).getRightSiblingKey());
            }
            return true;
        }

        // Then follow right sibling if there is one.
        if (((IStructuralItem)getTransaction().getNode()).hasRightSibling()) {
            mNextKey = ((IStructuralItem)getTransaction().getNode()).getRightSiblingKey();
            return true;
        }

        // Then follow right sibling on stack.
        if (mRightSiblingKeyStack.size() > 0) {
            mNextKey = mRightSiblingKeyStack.pop();
            return true;
        }

        // Then end.
        mNextKey = (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        return true;
    }

}

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
import org.treetank.node.AbsStructNode;
import org.treetank.settings.EFixed;

/**
 * <h1>PostOrder</h1>
 * 
 * <p>
 * Iterate over the whole tree starting with the last node.
 * </p>
 */
public class PostOrderAxis extends AbsAxis {

    /** For remembering last parent. */
    private Stack<Long> mLastParent;

    /** The nodeKey of the next node to visit. */
    private long mNextKey;

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     */
    public PostOrderAxis(final IReadTransaction rtx) {
        super(rtx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void reset(final long mNodeKey) {
        super.reset(mNodeKey);
        mLastParent = new Stack<Long>();
        mLastParent.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
        mNextKey = mNodeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        resetToLastKey();
        long key = mNextKey;
        if (key != (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
            getTransaction().moveTo(mNextKey);
            while (((AbsStructNode)getTransaction().getNode()).hasFirstChild() && key != mLastParent.peek()) {
                mLastParent.push(key);
                key = ((AbsStructNode)getTransaction().getNode()).getFirstChildKey();
                getTransaction().moveToFirstChild();
            }
            if (key == mLastParent.peek()) {
                mLastParent.pop();
            }

            if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                mNextKey = ((AbsStructNode)getTransaction().getNode()).getRightSiblingKey();

            } else {
                mNextKey = mLastParent.peek();
            }

            return true;

        } else {
            resetToStartKey();
            return false;
        }
    }

}

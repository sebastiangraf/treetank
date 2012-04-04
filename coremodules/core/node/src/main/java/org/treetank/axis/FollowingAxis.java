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

import org.treetank.api.INodeReadTrx;
import org.treetank.node.ENode;
import org.treetank.node.interfaces.IStructNode;

/**
 * <h1>FollowingAxis</h1>
 * 
 * <p>
 * Iterate over all following nodes of kind ELEMENT or TEXT starting at a given
 * node. Self is not included.
 * </p>
 */
public class FollowingAxis extends AbsAxis {

    private boolean mIsFirst;

    private Stack<Long> mRightSiblingStack;

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     */
    public FollowingAxis(final INodeReadTrx rtx) {

        super(rtx);
        mIsFirst = true;
        mRightSiblingStack = new Stack<Long>();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void reset(final long mNodeKey) {

        super.reset(mNodeKey);
        mIsFirst = true;
        mRightSiblingStack = new Stack<Long>();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasNext() {

        // assure, that preceding is not evaluated on an attribute or a
        // namespace
        if (mIsFirst && getNode().getKind() == ENode.ATTRIBUTE_KIND) {
            // || getTransaction().isNamespaceKind() {
            resetToStartKey();
            return false;

        }

        resetToLastKey();

        if (mIsFirst) {
            mIsFirst = false;
            // the first following is either a right sibling, or the right
            // sibling of
            // the first ancestor that has a right sibling.
            // note: ancestors and descendants are no following node!
            if ((((IStructNode) getNode()).hasRightSibling())) {
                moveTo(((IStructNode) getNode()).getRightSiblingKey());

                if ((((IStructNode) getNode()).hasRightSibling())) {
                    // push right sibling on a stack to reduce path traversal
                    mRightSiblingStack.push(((IStructNode) getNode())
                            .getRightSiblingKey());
                }
                return true;
            }
            // Try to find the right sibling of one of the ancestors.
            while (getNode().hasParent()) {
                moveTo(getNode().getParentKey());
                if ((((IStructNode) getNode()).hasRightSibling())) {
                    moveTo(((IStructNode) getNode()).getRightSiblingKey());
                    if ((((IStructNode) getNode()).hasRightSibling())) {
                        mRightSiblingStack.push(((IStructNode) getNode())
                                .getRightSiblingKey());
                    }
                    return true;
                }
            }
            // currentNode is last key in the document order
            resetToStartKey();
            return false;

        }
        // step down the tree in document order
        if ((((IStructNode) getNode()).hasFirstChild())) {
            moveTo(((IStructNode) getNode()).getFirstChildKey());
            if ((((IStructNode) getNode()).hasRightSibling())) {
                // push right sibling on a stack to reduce path traversal
                mRightSiblingStack.push(((IStructNode) getNode())
                        .getRightSiblingKey());
            }

            return true;
        }
        if (mRightSiblingStack.empty()) {

            // Try to find the right sibling of one of the ancestors.
            while (getNode().hasParent()) {
                moveTo(getNode().getParentKey());
                if (((IStructNode) getNode()).hasRightSibling()) {
                    moveTo(((IStructNode) getNode()).getRightSiblingKey());
                    if (((IStructNode) getNode()).hasRightSibling()) {
                        // push right sibling on a stack to reduce path
                        // traversal
                        mRightSiblingStack.push(((IStructNode) getNode())
                                .getRightSiblingKey());
                    }
                    return true;
                }
            }

        } else {

            // get root key of sibling subtree
            moveTo(mRightSiblingStack.pop());
            if (((IStructNode) getNode()).hasRightSibling()) {
                // push right sibling on a stack to reduce path traversal
                mRightSiblingStack.push(((IStructNode) getNode())
                        .getRightSiblingKey());
            }
            return true;

        }
        resetToStartKey();
        return false;
    }
}

/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.axis;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.utils.FastStack;

/**
 * <h1>FollowingAxis</h1>
 * 
 * <p>
 * Iterate over all following nodes of kind ELEMENT or TEXT starting at a given node. Self is not included.
 * </p>
 */
public class FollowingAxis extends AbstractAxis implements IAxis {

    private boolean mIsFirst;

    private FastStack<Long> mRightSiblingStack;

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     */
    public FollowingAxis(final IReadTransaction rtx) {

        super(rtx);
        mIsFirst = true;
        mRightSiblingStack = new FastStack<Long>();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void reset(final long mNodeKey) {

        super.reset(mNodeKey);
        mIsFirst = true;
        mRightSiblingStack = new FastStack<Long>();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasNext() {

        // assure, that preceding is not evaluated on an attribute or a
        // namespace
        if (mIsFirst && getTransaction().getNode().getKind() == ENodes.ATTRIBUTE_KIND) {
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
            if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                getTransaction().moveToRightSibling();

                if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                    // push right sibling on a stack to reduce path traversal
                    mRightSiblingStack.push(((AbsStructNode)getTransaction().getNode()).getRightSiblingKey());
                }
                return true;
            }
            // Try to find the right sibling of one of the ancestors.
            while (getTransaction().getNode().hasParent()) {
                getTransaction().moveToParent();
                if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                    getTransaction().moveToRightSibling();
                    if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                        mRightSiblingStack.push(((AbsStructNode)getTransaction().getNode())
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
        if (((AbsStructNode)getTransaction().getNode()).hasFirstChild()) {
            getTransaction().moveToFirstChild();
            if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                // push right sibling on a stack to reduce path traversal
                mRightSiblingStack.push(((AbsStructNode)getTransaction().getNode()).getRightSiblingKey());
            }

            return true;
        }
        if (mRightSiblingStack.empty()) {

            // Try to find the right sibling of one of the ancestors.
            while (getTransaction().getNode().hasParent()) {
                getTransaction().moveToParent();
                if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                    getTransaction().moveToRightSibling();
                    if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                        // push right sibling on a stack to reduce path
                        // traversal
                        mRightSiblingStack.push(((AbsStructNode)getTransaction().getNode())
                            .getRightSiblingKey());
                    }
                    return true;
                }
            }

        } else {

            // get root key of sibling subtree
            getTransaction().moveTo(mRightSiblingStack.pop());
            if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                // push right sibling on a stack to reduce path traversal
                mRightSiblingStack.push(((AbsStructNode)getTransaction().getNode()).getRightSiblingKey());
            }
            return true;

        }
        resetToStartKey();
        return false;
    }
}

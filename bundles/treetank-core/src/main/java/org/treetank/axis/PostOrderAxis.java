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

package org.treetank.axis;

import org.treetank.api.IReadTransaction;
import org.treetank.node.AbsStructNode;
import org.treetank.settings.EFixed;
import org.treetank.utils.FastStack;

/**
 * <h1>PostOrder</h1>
 * 
 * <p>
 * Iterate over the whole tree starting with the last node.
 * </p>
 */
public class PostOrderAxis extends AbsAxis  {

    /** For remembering last parent. */
    private FastStack<Long> mLastParent;

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
        mLastParent = new FastStack<Long>();
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

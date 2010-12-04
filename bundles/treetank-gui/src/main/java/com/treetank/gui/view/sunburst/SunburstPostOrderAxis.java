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

package com.treetank.gui.view.sunburst;

import java.util.Stack;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.node.AbsStructNode;
import com.treetank.settings.EFixed;
import com.treetank.utils.FastStack;

/**
 * <h1>PostOrder</h1>
 * 
 * <p>
 * Iterate over the whole tree starting with the last node.
 * </p>
 */
public class SunburstPostOrderAxis extends AbsAxis implements IAxis {

    /** For remembering last parent. */
    private FastStack<Long> mLastParent;
    
    /** Descendants {@link Stack}. */
    private Stack<Integer> mDescendantsStack;

    /** The nodeKey of the next node to visit. */
    private long mNextKey;

    /** Depth in the tree starting at 0. */
    private int mDepth;
    
    /** Determines if cursor moved to child of parent node. */
    public enum CursorMoved {
        /** Start of traversal. */
        START,

        /** Next node is a child of the current node. */
        CHILD,
        
        /** Next node is the rightsibling of the first anchestor node which has one. */
        SIBL,
        
        /** Next node is a parent node. */
        PARENT
    }
    
    /** Determines movement of cursor. */
    private CursorMoved mMoved = CursorMoved.START;

    /**
     * Constructor initializing internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     */
    public SunburstPostOrderAxis(final IReadTransaction rtx) {
        super(rtx);
        mMoved = CursorMoved.START;
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
        mMoved = CursorMoved.START;
        mDescendantsStack = new Stack<Integer>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasNext() {
        resetToLastKey();
        long key = mNextKey;
        if (key != (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
            getTransaction().moveTo(mNextKey);
            while (((AbsStructNode)getTransaction().getNode()).hasFirstChild() && key != mLastParent.peek()) {
                mMoved = CursorMoved.CHILD;
                mLastParent.push(key);
                key = ((AbsStructNode)getTransaction().getNode()).getFirstChildKey();
                getTransaction().moveToFirstChild();
                mDepth++;
            }
            if (key == mLastParent.peek()) {
                mLastParent.pop();
            }

            if (((AbsStructNode)getTransaction().getNode()).hasRightSibling()) {
                mNextKey = ((AbsStructNode)getTransaction().getNode()).getRightSiblingKey();
                mMoved = CursorMoved.SIBL;
            } else {
                mNextKey = mLastParent.peek();
                mMoved = CursorMoved.PARENT;
                mDepth--;
            }

            return true;

        } else {
            resetToStartKey();
            return false;
        }
    }
    
    /**
     * Get depth.
     * 
     * @return depth
     */
    public final int getDepth() {
        return mDepth;
    }
    
    /**
     * Get movement.
     * 
     * @return moved
     */
    public final CursorMoved getMoved() {
        return mMoved;
    }

}

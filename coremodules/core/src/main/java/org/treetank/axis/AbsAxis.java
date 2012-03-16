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

import java.util.Iterator;

import org.treetank.api.INodeReadTransaction;
import org.treetank.exception.TTXPathException;

/**
 * <h1>AbstractAxis</h1>
 * 
 * <p>
 * Provide standard Java iterator capability compatible with the new enhanced for loop available since Java 5.
 * </p>
 * 
 * <p>
 * All implementations must make sure to call super.hasNext() as the first thing in hasNext().
 * </p>
 * 
 * <p>
 * All users must make sure to call next() after hasNext() evaluated to true.
 * </p>
 */
public abstract class AbsAxis implements Iterator<Long>, Iterable<Long> {

    /** Iterate over transaction exclusive to this step. */
    private final INodeReadTransaction mRTX;

    /** Key of last found node. */
    private long mKey;

    /** Make sure next() can only be called after hasNext(). */
    private boolean mNext;

    /** Key of node where axis started. */
    private long mStartKey;

    /** Include self? */
    private final boolean mIncludeSelf;

    /**
     * Bind axis step to transaction.
     * 
     * @param paramRtx
     *            transaction to operate with
     */
    public AbsAxis(final INodeReadTransaction paramRtx) {
        if (paramRtx == null) {
            throw new IllegalArgumentException("Transaction may not be null!");
        }
        mRTX = paramRtx;
        mIncludeSelf = false;
        reset(paramRtx.getNode().getNodeKey());
    }

    /**
     * Bind axis step to transaction.
     * 
     * @param paramRtx
     *            transaction to operate with
     * @param paramIncludeSelf
     *            determines if self is included
     */
    public AbsAxis(final INodeReadTransaction paramRtx, final boolean paramIncludeSelf) {
        if (paramRtx == null) {
            throw new IllegalArgumentException("Transaction may not be null!");
        }
        mRTX = paramRtx;
        mIncludeSelf = paramIncludeSelf;
        reset(paramRtx.getNode().getNodeKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Iterator<Long> iterator() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Long next() {
        if (!mNext) {
            throw new IllegalStateException("IAxis.next() must be called exactely once after hasNext()"
                + " evaluated to true.");
        }
        mKey = mRTX.getNode().getNodeKey();
        mNext = false;
        return mKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Resetting the nodekey of this axis to a given nodekey.
     * 
     * @param paramNodeKey
     *            the nodekey where the reset should occur to.
     */
    public void reset(final long paramNodeKey) {
        mStartKey = paramNodeKey;
        mKey = paramNodeKey;
        mNext = false;
    }

    /**
     * Get current {@link INodeReadTransaction}.
     * 
     * @return the {@link INodeReadTransaction} used
     */
    public final INodeReadTransaction getTransaction() {
        return mRTX;
    }

    /**
     * Make sure the transaction points to the node it started with. This must
     * be called just before hasNext() == false.
     * 
     * @return Key of node where transaction was before the first call of
     *         hasNext().
     */
    protected final long resetToStartKey() {
        // No check because of IAxis Convention 4.
        mRTX.moveTo(mStartKey);
        mNext = false;
        return mStartKey;
    }

    /**
     * Make sure the transaction points to the node after the last hasNext().
     * This must be called first in hasNext().
     * 
     * @return Key of node where transaction was after the last call of
     *         hasNext().
     */
    protected final long resetToLastKey() {
        // No check because of IAxis Convention 4.
        mRTX.moveTo(mKey);
        mNext = true;
        return mKey;
    }

    /**
     * Get start key.
     * 
     * @return Start key.
     */
    protected final long getStartKey() {
        return mStartKey;
    }

    /**
     * Is self included?
     * 
     * @return True if self is included. False else.
     */
    protected final boolean isSelfIncluded() {
        return mIncludeSelf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean hasNext();

    /**
     * {@inheritDoc}
     */
    public void evaluate() throws TTXPathException {
        while (hasNext()) {
            next();
        }
    }

}

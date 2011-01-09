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

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.treetank.api.IExpression;
import com.treetank.api.IReadTransaction;
import com.treetank.exception.TTXPathException;

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
public abstract class AbsAxis implements IExpression, Iterator<Long>, Iterable<Long> {

    /** Iterate over transaction exclusive to this step. */
    private IReadTransaction mRTX;

    /** Key of last found node. */
    private long mKey;

    /** Make sure next() can only be called after hasNext(). */
    private boolean mNext;

    /** Key of node where axis started. */
    private long mStartKey;

    /** Include self? */
    private final boolean mIncludeSelf;
    
    /** Size of thread pool for executor service. */
    private static int THREADPOOLSIZE = 2;
    
    /** Executor Service holding the execution plan for future tasks. */
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREADPOOLSIZE);
   
    /**
     * Bind axis step to transaction.
     * 
     * @param rtx
     *            Transaction to operate with.
     */
    public AbsAxis(final IReadTransaction rtx) {
        mRTX = rtx;
        mIncludeSelf = false;
        reset(rtx.getNode().getNodeKey());
    }

    /**
     * Bind axis step to transaction.
     * 
     * @param rtx
     *            Transaction to operate with.
     * @param mIncludeSelf
     *            Is self included?
     */
    public AbsAxis(final IReadTransaction rtx, final boolean mIncludeSelf) {
        this.mRTX = rtx;
        this.mIncludeSelf = mIncludeSelf;
        reset(rtx.getNode().getNodeKey());
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
     * {@inheritDoc}
     */
    public void reset(final long paramNodeKey) {
        mStartKey = paramNodeKey;
        mKey = paramNodeKey;
        mNext = false;
    }

    /**
     * {@inheritDoc}
     */
    public final IReadTransaction getTransaction() {
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
     * Resets the transaction.
     * 
     * @param rtx
     *            read transaction which is bound to transaction.
     */
    public synchronized void setTransaction(final IReadTransaction rtx) {
        mRTX = rtx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evaluate() throws TTXPathException {
        while (hasNext()) {
            next();
        }
    }

}

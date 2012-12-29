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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.treetank.api.INodeReadTrx;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.node.AtomicValue;
import org.treetank.node.interfaces.INode;

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
    private final INodeReadTrx mRTX;

    /** Key of last found node. */
    private long mKey;

    /** Make sure next() can only be called after hasNext(). */
    private boolean mNext;

    /** Key of node where axis started. */
    private long mStartKey;

    /** Include self? */
    private final boolean mIncludeSelf;

    /** Map with ItemList to each transaction. */
    private final static Map<INodeReadTrx, ItemList> atomics =
        new ConcurrentHashMap<INodeReadTrx, ItemList>();

    /** Map with ItemList to each transaction. */
    private final static Map<INodeReadTrx, Long> lastPointer = new ConcurrentHashMap<INodeReadTrx, Long>();

    /**
     * Bind axis step to transaction.
     * 
     * @param paramRtx
     *            transaction to operate with
     */
    public AbsAxis(final INodeReadTrx paramRtx) {
        this(paramRtx, false);
    }

    /**
     * Bind axis step to transaction.
     * 
     * @param paramRtx
     *            transaction to operate with
     * @param paramIncludeSelf
     *            determines if self is included
     */
    public AbsAxis(final INodeReadTrx paramRtx, final boolean paramIncludeSelf) {
        checkNotNull(paramRtx);
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
        checkState(mNext, "IAxis.next() must be called exactely once after hasNext()" + " evaluated to true.");
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
        lastPointer.remove(mRTX);
    }

    /**
     * Move cursor to a node by its node key.
     * 
     * @param pKey
     *            Key of node to select.
     * @return True if the node with the given node key is selected.
     */
    public boolean moveTo(final long pKey) {
        try {
            if (pKey < 0 || mRTX.moveTo(pKey)) {
                lastPointer.put(mRTX, pKey);
                return true;
            } else {
                return false;
            }
        } catch (TTIOException exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Closing the Transaction
     * 
     * @throws TTException
     */
    public void close() throws TTException {
        atomics.remove(mRTX);
        lastPointer.remove(mRTX);
        mRTX.close();
    }

    /**
     * Getting the current node (including items from the ItemList)
     * 
     * @return Getting the node.
     */
    public INode getNode() {
        if (lastPointer.get(mRTX) != null && lastPointer.get(mRTX) < 0) {
            return atomics.get(mRTX).getItem(lastPointer.get(mRTX));
        } else {
            return mRTX.getNode();
        }
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
        moveTo(mStartKey);
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
        moveTo(mKey);
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
     * Getting the ItemList.
     * 
     * @return the Itemlist
     */
    public ItemList getItemList() {
        if (!atomics.containsKey(mRTX)) {
            atomics.put(mRTX, new ItemList());
        }
        return atomics.get(mRTX);
    }

    /**
     * Adding any AtomicVal to any ItemList staticly.
     * 
     * @param pRtx
     *            as key
     * @param pVal
     *            to be added
     * @return the index in the ItemList
     */
    public static int addAtomicToItemList(final INodeReadTrx pRtx, final AtomicValue pVal) {
        if (!atomics.containsKey(pRtx)) {
            atomics.put(pRtx, new ItemList());
        }
        return atomics.get(pRtx).addItem(pVal);

    }
}

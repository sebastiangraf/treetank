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

package org.treetank.page;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.treetank.exception.TTIOException;
import org.treetank.page.interfaces.IReferencePage;

/**
 * <h1>UberPage</h1>
 * 
 * <p>
 * Uber page holds a reference to the static revision root page tree.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 * @author Marc Kramis, University of Konstanz
 */
public final class UberPage implements IReferencePage {

    /** Number of revisions. */
    private final long mRevisionCount;

    /** Reference key for first indirect page. */
    private final long mReferenceKeys[];

    /** Key of this UberPage. */
    private final long mPageKey;

    /** Counter for new pages. */
    private long mPageCounter;

    /**
     * New uber page
     * 
     * @param pPageKey
     *            key of this page
     * @param pRevisionCount
     *            count of all revisions in this storage
     * @param pPageCounter
     *            Counter for all pages
     */
    public UberPage(final long pPageKey, final long pRevisionCount, final long pPageCounter) {
        mRevisionCount = pRevisionCount;
        mReferenceKeys = new long[1];
        mPageKey = pPageKey;
        mPageCounter = pPageCounter;
    }

    /**
     * Get revision key of current in-memory state.
     * 
     * @return Revision key.
     */
    public long getRevisionNumber() {
        return mRevisionCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeInt(IConstants.UBERPAGE);
            pOutput.writeLong(mPageKey);
            pOutput.writeLong(mRevisionCount);
            pOutput.writeLong(mPageCounter);
            pOutput.writeLong(mReferenceKeys[0]);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPageKey() {
        return mPageKey;
    }

    /**
     * Incrementing the counter.
     * 
     * @return the incremented counter
     */
    public long incrementPageCounter() {
        return mPageCounter++;
    }

    /**
     * Getter for mPageCounter.
     * 
     * @return the mPageCounter
     */
    public long getPageCounter() {
        return mPageCounter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long[] getReferenceKeys() {
        return mReferenceKeys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReferenceKey(int pIndex, long pKey) {
        mReferenceKeys[pIndex] = pKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mPageKey", mPageKey).add("mPageCounter", mPageCounter).add(
            "mReferenceKeys", mReferenceKeys).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(mPageKey, mPageCounter, Arrays.hashCode(mReferenceKeys));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

}

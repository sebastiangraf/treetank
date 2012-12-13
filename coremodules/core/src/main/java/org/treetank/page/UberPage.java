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

import org.treetank.access.PageWriteTrx;
import org.treetank.exception.TTException;
import org.treetank.page.interfaces.IReferencePage;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * <h1>UberPage</h1>
 * 
 * <p>
 * Uber page holds a reference to the static revision root page tree.
 * </p>
 */
public final class UberPage implements IReferencePage {

    /** Number of revisions. */
    private final long mRevisionCount;

    /** Page references. */
    private final PageReference mReference;

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
     * @param pReference
     *            Reference for the indirect page
     */
    public UberPage(final long pPageKey, final long pRevisionCount, final long pPageCounter,
        final PageReference pReference) {
        mRevisionCount = pRevisionCount;
        mReference = pReference;
        // TODO This can be a single value only but first, kick out the PageReferences
        mReferenceKeys = new long[1];
        mPageKey = pPageKey;
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
    public byte[] getByteRepresentation() {
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        pOutput.writeInt(IConstants.UBERPAGE);
        pOutput.writeLong(mPageKey);
        pOutput.writeLong(mRevisionCount);
        pOutput.writeLong(mPageCounter);
        pOutput.writeLong(mReferenceKeys[0]);
        pOutput.writeLong(mReference.getKey());
        return pOutput.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UberPage [mPageKey=");
        builder.append(mPageKey);
        builder.append("mRevisionCount=");
        builder.append(mRevisionCount);
        builder.append(", mReference=");
        builder.append(mReference.toString());
        return builder.toString();
    }

    @Override
    public void commit(PageWriteTrx paramState) throws TTException {
        paramState.commit(mReference);
    }

    @Override
    public PageReference[] getReferences() {
        return new PageReference[] {
            mReference
        };
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

    @Override
    public long[] getReferenceKeys() {
        return mReferenceKeys;
    }

    @Override
    public void setReferenceKey(int pIndex, long pKey) {
        mReferenceKeys[pIndex] = pKey;
    }

}

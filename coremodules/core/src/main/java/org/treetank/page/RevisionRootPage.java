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

import org.treetank.page.interfaces.IReferencePage;
import org.treetank.page.interfaces.IRevisionPage;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * <h1>RevisionRootPage</h1>
 * 
 * <p>
 * Revision root page holds a reference to the name page as well as the static node page tree.
 * </p>
 */
public final class RevisionRootPage implements IRevisionPage, IReferencePage {

    /** Offset of name page reference. */
    public static final int NAME_REFERENCE_OFFSET = 1;

    /** Last allocated node key. */
    private long mMaxNodeKey;

    /** Revision of this page. */
    private final long mRevision;

    /** Reference keys. */
    private final long[] mReferenceKeys;

    /** Page Key of this page. */
    private final long mPageKey;

    /**
     * Constructor of RevisionRootPages.
     * 
     * @param pPageKey
     *            Key of this page
     * @param pRevision
     *            to be created
     * @param pMaxNodeKey
     *            maximal node key given
     */
    public RevisionRootPage(final long pPageKey, final long pRevision, final long pMaxNodeKey) {
        mRevision = pRevision;
        mReferenceKeys = new long[2];
        mMaxNodeKey = pMaxNodeKey;
        mPageKey = pPageKey;
    }

    /**
     * Get last allocated node key.
     * 
     * @return Last allocated node key.
     */
    public long getMaxNodeKey() {
        return mMaxNodeKey;
    }

    /**
     * Increment number of nodes by one while allocating another key.
     */
    public void incrementMaxNodeKey() {
        mMaxNodeKey += 1;
    }

    @Override
    public long getRevision() {
        return mRevision;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getByteRepresentation() {
        final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
        pOutput.writeInt(IConstants.REVISIONROOTPAGE);
        pOutput.writeLong(mPageKey);
        pOutput.writeLong(mRevision);
        pOutput.writeLong(mMaxNodeKey);
        for (long key : mReferenceKeys) {
            pOutput.writeLong(key);
        }
        return pOutput.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPageKey() {
        return mPageKey;
    }

    @Override
    public long[] getReferenceKeys() {
        return mReferenceKeys;
    }

    @Override
    public void setReferenceKey(int pIndex, long pKey) {
        mReferenceKeys[pIndex] = pKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mPageKey", mPageKey).add("mRevision", mRevision).add(
            "mReferenceKeys", mReferenceKeys).add("mMaxNodeKey", mMaxNodeKey).toString();
    }

}

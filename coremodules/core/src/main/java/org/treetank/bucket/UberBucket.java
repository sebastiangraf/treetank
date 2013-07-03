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

package org.treetank.bucket;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.treetank.access.conf.StandardSettings;
import org.treetank.bucket.interfaces.IReferenceBucket;
import org.treetank.exception.TTIOException;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;

/**
 * <h1>UberBucket</h1>
 * 
 * <p>
 * Uber bucket holds a reference to the static revision root bucket tree.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 * @author Marc Kramis, University of Konstanz
 */
public final class UberBucket implements IReferenceBucket {

    /** Number of revisions. */
    private final long mRevisionCount;

    /** Reference key for first indirect bucket. */
    private final long mReferenceKeys[];

    /** Referenced hashcodes for keys. */
    private final byte[][] mReferenceHashs;

    /** Key of this UberBucket. */
    private final long mBucketKey;

    /** Counter for new buckets. */
    private long mBucketCounter;

    /**
     * New uber bucket
     * 
     * @param pBucketKey
     *            key of this bucket
     * @param pRevisionCount
     *            count of all revisions in this storage
     * @param pBucketCounter
     *            Counter for all buckets
     */
    public UberBucket(final long pBucketKey, final long pRevisionCount, final long pBucketCounter) {
        mRevisionCount = pRevisionCount;
        mReferenceKeys = new long[1];
        mReferenceHashs = new byte[1][];
        Arrays.fill(mReferenceHashs, new byte[0]);
        mBucketKey = pBucketKey;
        mBucketCounter = pBucketCounter;
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
            pOutput.writeInt(IConstants.UBERBUCKET);
            pOutput.writeLong(mBucketKey);
            pOutput.writeLong(mRevisionCount);
            pOutput.writeLong(mBucketCounter);
            pOutput.writeLong(mReferenceKeys[0]);
            pOutput.writeInt(mReferenceHashs[0].length);
            pOutput.write(mReferenceHashs[0]);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getBucketKey() {
        return mBucketKey;
    }

    /**
     * Incrementing the counter.
     * 
     * @return the incremented counter
     */
    public long incrementBucketCounter() {
        mBucketCounter = mBucketCounter + 1;
        return mBucketCounter;
    }

    /**
     * Getter for mBucketCounter.
     * 
     * @return the mBucketCounter
     */
    public long getBucketCounter() {
        return mBucketCounter;
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
        return toStringHelper(this).add("mBucketKey", mBucketKey).add("mBucketCounter", mBucketCounter).add(
            "mRevisionCount", mRevisionCount).add("mReferenceKeys", Arrays.toString(mReferenceKeys)).add(
            "mReferenceHashs", Arrays.toString(mReferenceHashs)).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[][] getReferenceHashs() {
        return mReferenceHashs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReferenceHash(int pIndex, byte[] pHash) {
        mReferenceHashs[pIndex] = pHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 17207;
        int result = 1;
        result = prime * result + (int)(mBucketCounter ^ (mBucketCounter >>> 32));
        result = prime * result + (int)(mBucketKey ^ (mBucketKey >>> 32));
        for (byte[] hash : mReferenceHashs) {
            result = prime * result + Arrays.hashCode(hash);
        }
        result = prime * result + Arrays.hashCode(mReferenceKeys);
        result = prime * result + (int)(mRevisionCount ^ (mRevisionCount >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashCode secureHash() {
        final Hasher code =
            StandardSettings.HASHFUNC.newHasher().putLong(mBucketKey).putLong(mRevisionCount).putLong(
                mBucketCounter);
        for (int i = 0; i < mReferenceKeys.length; i++) {
            code.putLong(mReferenceKeys[i]);
            code.putBytes(mReferenceHashs[i]);
        }
        return code.hash();
    }
}

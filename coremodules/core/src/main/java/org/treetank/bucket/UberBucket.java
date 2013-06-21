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
import java.util.Objects;

import org.treetank.bucket.interfaces.IReferenceBucket;
import org.treetank.exception.TTIOException;

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
            "mRevisionCount", mRevisionCount).add("mReferenceKeys", Arrays.toString(mReferenceKeys))
            .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(mBucketKey, mBucketCounter, Arrays.hashCode(mReferenceKeys));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    /**
     * Copying a bucket into a new one.
     * 
     * @param pBucket
     *            to be copied
     * @return new copy
     */
    public static final UberBucket copy(final UberBucket pBucket) {
        final UberBucket copy =
            new UberBucket(pBucket.mBucketKey, pBucket.mRevisionCount, pBucket.mBucketCounter);
        copy.mReferenceKeys[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET] =
            pBucket.mReferenceKeys[IReferenceBucket.GUARANTEED_INDIRECT_OFFSET];
        return copy;
    }

}

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
import org.treetank.bucket.interfaces.IRevisionBucket;
import org.treetank.exception.TTIOException;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;

/**
 * <h1>RevisionRootBucket</h1>
 * 
 * <p>
 * Revision root bucket holds a reference to the name bucket as well as the static data-bucket tree.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 * @author Marc Kramis, University of Konstanz
 */
public final class RevisionRootBucket implements IRevisionBucket, IReferenceBucket {

    /** Offset of name bucket reference. */
    public static final int META_REFERENCE_OFFSET = 1;

    /** Last allocated data key. */
    private long mMaxDataKey;

    /** Revision of this bucket. */
    private final long mRevision;

    /** Reference keys. */
    private final long[] mReferenceKeys;

    /** Hashcode of keys. */
    private final byte[][] mReferenceHashs;

    /** Key of this bucket. */
    private final long mBucketKey;

    /**
     * Constructor of RevisionRootBuckets.
     * 
     * @param pBucketKey
     *            Key of this bucket
     * @param pRevision
     *            to be created
     * @param pMaxDataKey
     *            maximal data key given
     */
    public RevisionRootBucket(final long pBucketKey, final long pRevision, final long pMaxDataKey) {
        mRevision = pRevision;
        mReferenceKeys = new long[2];
        mReferenceHashs = new byte[2][];
        Arrays.fill(mReferenceHashs, new byte[0]);
        mMaxDataKey = pMaxDataKey;
        mBucketKey = pBucketKey;
    }

    /**
     * Get last allocated data key.
     * 
     * @return Last allocated data key.
     */
    public long getMaxDataKey() {
        return mMaxDataKey;
    }

    /**
     * Increment number of datas by one while allocating another key.
     */
    public long incrementMaxDataKey() {
        return mMaxDataKey++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRevision() {
        return mRevision;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeInt(IConstants.REVISIONROOTBUCKET);
            pOutput.writeLong(mBucketKey);
            pOutput.writeLong(mRevision);
            pOutput.writeLong(mMaxDataKey);
            for (long key : mReferenceKeys) {
                pOutput.writeLong(key);
            }
            for (byte[] hash : mReferenceHashs) {
                pOutput.writeInt(hash.length);
                pOutput.write(hash);
            }
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
        return toStringHelper(this).add("mBucketKey", mBucketKey).add("mRevision", mRevision).add(
            "mMaxDataKey", mMaxDataKey).add("mReferenceKeys", Arrays.toString(mReferenceKeys)).add(
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
        final int prime = 53623;
        int result = 1;
        result = prime * result + (int)(mBucketKey ^ (mBucketKey >>> 32));
        result = prime * result + (int)(mMaxDataKey ^ (mMaxDataKey >>> 32));
        result = prime * result + Arrays.hashCode(mReferenceKeys);
        for (byte[] hash : mReferenceHashs) {
            result = prime * result + Arrays.hashCode(hash);
        }
        result = prime * result + (int)(mRevision ^ (mRevision >>> 32));
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
            StandardSettings.HASHFUNC.newHasher().putLong(mBucketKey).putLong(mMaxDataKey).putLong(mRevision);
        for (int i = 0; i < mReferenceKeys.length; i++) {
            code.putLong(mReferenceKeys[i]);
            code.putBytes(mReferenceHashs[i]);
        }
        return code.hash();
    }

}

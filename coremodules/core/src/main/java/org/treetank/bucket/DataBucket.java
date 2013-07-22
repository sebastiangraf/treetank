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
import org.treetank.api.IData;
import org.treetank.bucket.interfaces.IBucket;
import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.PrimitiveSink;

/**
 * <h1>DataBucket</h1>
 * 
 * <p>
 * A data bucket stores a set of datas.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 * @author Marc Kramis, University of Konstanz
 */
public final class DataBucket implements IBucket {

    /** Key of data bucket. This is the base key of all contained datas. */
    private final long mBucketKey;

    /** Array of datas. This can have null datas that were removed. */
    private final IData[] mDatas;

    /** Pointer to last data bucket representing the same amount of data. */
    private final long mLastBucketKey;

    /**
     * Create data bucket.
     * 
     * @param pBucketKey
     *            Base key assigned to this data bucket.
     */
    public DataBucket(final long pBucketKey, final long pLastBucketKey) {
        mBucketKey = pBucketKey;
        mLastBucketKey = pLastBucketKey;
        mDatas = new IData[IConstants.CONTENT_COUNT];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getBucketKey() {
        return mBucketKey;
    }

    /**
     * Getting the pointer to the former representation of the same data-bucket.
     * 
     * @return the pointer to the last bucket.
     */
    public long getLastBucketPointer() {
        return mLastBucketKey;
    }

    /**
     * Get data at a given offset.
     * 
     * @param pOffset
     *            Offset of data within local data bucket.
     * @return data at given offset.
     */
    public IData getData(final int pOffset) {
        return getDatas()[pOffset];
    }

    /**
     * Overwrite a single data at a given offset.
     * 
     * @param pOffset
     *            Offset of data to overwrite in this data bucket.
     * @param pData
     *            Data to store at given dataofffset.
     */
    public void setData(final int pOffset, final IData pData) {
        getDatas()[pOffset] = pData;
    }

    /**
     * Getter for datas
     * 
     * @return the mDatas
     */
    public IData[] getDatas() {
        return mDatas;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeInt(IConstants.DATABUCKET);
            pOutput.writeLong(mBucketKey);
            pOutput.writeLong(mLastBucketKey);
            for (final IData data : getDatas()) {
                if (data == null) {
                    pOutput.writeInt(IConstants.NULLDATA);
                } else {
                    if (data instanceof DeletedData) {
                        pOutput.writeInt(IConstants.DELETEDDATA);
                    } else {
                        pOutput.writeInt(IConstants.INTERFACEDATA);
                    }
                    data.serialize(pOutput);
                }
            }
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mBucketKey", mBucketKey).add("mDatas", Arrays.toString(mDatas))
            .toString();
    }

    /**
     * Static class to mark deleted entries within the bucket.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static class DeletedData implements IData {
        /**
         * Enum for DeletedDataFunnel.
         * 
         * @author Sebastian Graf, University of Konstanz
         * 
         */
        enum DeletedDataFunnel implements Funnel<IData> {
            INSTANCE;
            public void funnel(IData from, PrimitiveSink into) {
                into.putLong(from.getDataKey());
            }
        }

        /**
         * Data key of the deleted data.
         */
        private long mDataKey;

        /**
         * Constructor.
         * 
         * @param pDataKey
         *            datakey to be replaced with a deleteddata
         */
        public DeletedData(final long pDataKey) {
            mDataKey = pDataKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getDataKey() {
            return mDataKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DataOutput pOutput) throws TTIOException {
            try {
                pOutput.writeLong(mDataKey);
            } catch (final IOException exc) {
                throw new TTIOException(exc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return toStringHelper(this).add("mDataKey", mDataKey).toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Funnel<IData> getFunnel() {
            return (Funnel<IData>)DeletedDataFunnel.INSTANCE;

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashCode secureHash() {
        final Hasher code = StandardSettings.HASHFUNC.newHasher().putLong(mBucketKey).putLong(mLastBucketKey);
        for (int i = 0; i < mDatas.length; i++) {
            if (mDatas[i] != null) {
                code.putObject(mDatas[i], mDatas[i].getFunnel());
            }
        }
        return code.hash();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 26267;
        int result = 1;
        result = prime * result + (int)(mLastBucketKey ^ (mLastBucketKey >>> 32));
        result = prime * result + Arrays.deepHashCode(mDatas);
        result = prime * result + (int)(mBucketKey ^ (mBucketKey >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

}

/**
 * 
 */
package org.treetank.bucket;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.treetank.api.IData;
import org.treetank.api.IDataFactory;
import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

/**
 * Simple Factory for generating {@link DumbData}s mainly for testing the bucket-layer.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class DumbDataFactory implements IDataFactory {

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public IData deserializeData(DataInput pSource) throws TTIOException {
        try {
            final long key = pSource.readLong();
            byte[] data = new byte[pSource.readInt()];
            pSource.readFully(data);
            return new DumbData(key, data);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Simple DumbData just for testing the {@link DataBucket}s.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static class DumbData implements IData {

        /**
         * Enum for DumbDataFunnel.
         * 
         * @author Sebastian Graf, University of Konstanz
         * 
         */
        enum DumbDataFunnel implements Funnel<IData> {
            INSTANCE;
            public void funnel(IData from, PrimitiveSink into) {
                DumbData data = (DumbData)from;
                into.putLong(data.getDataKey()).putBytes(data.mValue);
            }
        }

        long mDataKey;
        byte[] mValue;

        /**
         * Simple constructor.
         * 
         * @param pDataKey
         *            to be set
         * @param pValue
         *            to be set
         */
        public DumbData(long pDataKey, byte[] pValue) {
            mDataKey = pDataKey;
            mValue = pValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DataOutput pOutput) throws TTIOException {
            try {
                pOutput.writeLong(mDataKey);
                pOutput.writeInt(mValue.length);
                pOutput.write(mValue);
            } catch (final IOException exc) {
                throw new TTIOException(exc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getDataKey() {
            return mDataKey;
        }

        /**
         * Setting a data key to this dumb data.
         * 
         * @param pKey
         *            to be set
         */
        public void setDataKey(final long pKey) {
            mDataKey = pKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return toStringHelper(this).add("mDataKey", mDataKey).add("values", Objects.hash(mValue))
                .toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            final int prime = 94907;
            int result = 1;
            result = prime * result + (int)(mDataKey ^ (mDataKey >>> 32));
            result = prime * result + Arrays.hashCode(mValue);
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
        public Funnel<IData> getFunnel() {
            return DumbDataFunnel.INSTANCE;
        }
    }

}

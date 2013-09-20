package org.treetank.filelistener.file.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import org.treetank.api.IMetaEntry;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

/**
 * This factory is used to give the treetank
 * system knowledge of the MetaEntries this interface module
 * uses to store the data.
 * 
 * @author Andreas Rain
 * 
 */
public class FilelistenerMetaDataFactory implements IMetaEntryFactory {

    private final static int KEY = 1;
    private final static int VALUE = 2;

    @Override
    public IMetaEntry deserializeEntry(DataInput input) throws TTIOException {
        try {
            final int kind = input.readInt();

            switch (kind) {
            case KEY:
                final int valSize = input.readInt();
                final byte[] bytes = new byte[valSize];
                input.readFully(bytes);
                return new MetaKey(new String(bytes));
            case VALUE:
                return new MetaValue(input.readLong());
            default:
                throw new IllegalStateException("Kind not defined.");
            }
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Key representing a relative path to the mount point.
     * 
     * @author Andreas Rain
     * 
     */
    public static class MetaKey implements IMetaEntry {
        /**
         * Enum for MetaKeyFunnel.
         * 
         * @author Sebastian Graf, University of Konstanz
         * 
         */
        enum MetaKeyFunnel implements Funnel<IMetaEntry> {
            INSTANCE;
            public void funnel(IMetaEntry from, PrimitiveSink into) {
                MetaKey node = (MetaKey)from;
                into.putString(node.mKey);
            }
        }

        /** Key Variable. */
        private final String mKey;

        /**
         * Constructor
         * 
         * @param pData
         *            setting the key
         */
        public MetaKey(final String pData) {
            mKey = pData;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DataOutput output) throws TTIOException {
            try {
                output.writeInt(KEY);
                output.writeInt(mKey.getBytes().length);
                output.write(mKey.getBytes());
            } catch (final IOException exc) {
                throw new TTIOException(exc);
            }
        }

        public String getKey() {
            return this.mKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(mKey);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean equals(final Object pObj) {
            return this.hashCode() == pObj.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Funnel<IMetaEntry> getFunnel() {
            return MetaKeyFunnel.INSTANCE;
        }
    }

    /**
     * Value which is basically the byte representation
     * of the header FileData
     * 
     * @author Andreas Rain
     * 
     */
    public static class MetaValue implements IMetaEntry {
        /**
         * Enum for MetaValueFunnel.
         * 
         * @author Sebastian Graf, University of Konstanz
         * 
         */
        enum MetaValueFunnel implements Funnel<IMetaEntry> {
            INSTANCE;
            public void funnel(IMetaEntry from, PrimitiveSink into) {
                MetaValue node = (MetaValue)from;
                into.putLong(node.mData);
            }
        }

        /** Value Variable. */
        private final long mData;

        /**
         * Constructor.
         * 
         * @param pData
         *            setting the value
         */
        public MetaValue(final long pData) {
            mData = pData;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DataOutput output) throws TTIOException {
            try {
                output.writeInt(VALUE);
                output.writeLong(mData);
            } catch (final IOException exc) {
                throw new TTIOException(exc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(getData());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final boolean equals(final Object pObj) {
            return this.hashCode() == pObj.hashCode();
        }

        public long getData() {
            return mData;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Funnel<IMetaEntry> getFunnel() {
            return MetaValueFunnel.INSTANCE;
        }

    }

}

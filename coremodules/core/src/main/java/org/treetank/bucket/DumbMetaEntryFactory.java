package org.treetank.bucket;

import static com.google.common.base.Objects.toStringHelper;

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
 * 
 * Dumb MetaEntryFactory mainly for testing the core.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class DumbMetaEntryFactory implements IMetaEntryFactory {

    /** Constant for the KEY. */
    private final static int KEY = 1;
    /** Constant for the VALUE. */
    private final static int VALUE = 2;

    /**
     * {@inheritDoc}
     */
    @Override
    public IMetaEntry deserializeEntry(DataInput pData) throws TTIOException {
        try {
            final int kind = pData.readInt();
            switch (kind) {
            case KEY:
                return new DumbKey(pData.readLong());
            case VALUE:
                return new DumbValue(pData.readLong());
            default:
                throw new IllegalStateException("Kind not defined.");
            }
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Simple class for a simple key, just holding a long.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static class DumbKey implements IMetaEntry {

        /**
         * Enum for DumbKeyFunnel.
         * 
         * @author Sebastian Graf, University of Konstanz
         * 
         */
        enum DumbKeyFunnel implements Funnel<IMetaEntry> {
            INSTANCE;
            public void funnel(IMetaEntry from, PrimitiveSink into) {
                DumbKey data = (DumbKey)from;
                into.putLong(data.mData);
            }
        }

        /** The data itself. */
        private final long mData;

        /**
         * Constructor.
         * 
         * @param pData
         *            setting the data
         */
        public DumbKey(final long pData) {
            mData = pData;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DataOutput pOutput) throws TTIOException {
            try {
                pOutput.writeInt(KEY);
                pOutput.writeLong(mData);
            } catch (final IOException exc) {
                throw new TTIOException(exc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(mData);
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
        public String toString() {
            return toStringHelper(this).add("mData", mData).toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Funnel<IMetaEntry> getFunnel() {
            return DumbKeyFunnel.INSTANCE;
        }
    }

    /**
     * Simple class for a simple key, just holding a long.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static class DumbValue implements IMetaEntry {

        /**
         * Enum for DumbValueFunnel.
         * 
         * @author Sebastian Graf, University of Konstanz
         * 
         */
        enum DumbValueFunnel implements Funnel<IMetaEntry> {
            INSTANCE;
            public void funnel(IMetaEntry from, PrimitiveSink into) {
                DumbValue data = (DumbValue)from;
                into.putLong(data.mData);
            }
        }

        /** The data itself. */
        private final long mData;

        /**
         * Constructor.
         * 
         * @param pData
         *            setting the data
         */
        public DumbValue(final long pData) {
            mData = pData;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DataOutput pOutput) throws TTIOException {
            try {
                pOutput.writeInt(VALUE);
                pOutput.writeLong(mData);
            } catch (final IOException exc) {
                throw new TTIOException(exc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(mData);
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
        public String toString() {
            return toStringHelper(this).add("mData", mData).toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Funnel<IMetaEntry> getFunnel() {
            return DumbValueFunnel.INSTANCE;
        }
    }

}

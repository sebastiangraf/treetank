package org.treetank.node;

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
 * Meta page for node layer mainly representing a hashmap mapping hashes to tagnames.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NodeMetaPageFactory implements IMetaEntryFactory {

    private final static int KEY = 1;
    private final static int VALUE = 2;

    /**
     * Create a meta-entry out of a serialized byte-representation.
     * 
     * @param pData
     *            byte representation.
     * @return the created metaEntry.
     * @throws TTIOException
     *             if anything weird happens
     */
    public IMetaEntry deserializeEntry(final DataInput pData) throws TTIOException {
        try {
            final int kind = pData.readInt();
            switch (kind) {
            case KEY:
                return new MetaKey(pData.readInt());
            case VALUE:
                final int valSize = pData.readInt();
                final byte[] bytes = new byte[valSize];
                pData.readFully(bytes);
                return new MetaValue(new String(bytes));
            default:
                throw new IllegalStateException("Kind not defined.");
            }
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Key for the name map, representing only an integer.
     * 
     * @author Sebastian Graf, University of Konstanz
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
                into.putInt(node.mKey);
            }
        }

        /** Key Variable. */
        private final int mKey;

        /**
         * Constructor
         * 
         * @param pData
         *            setting the key
         */
        public MetaKey(final int pData) {
            mKey = pData;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DataOutput pOutput) throws TTIOException {
            try {
                pOutput.writeInt(KEY);
                pOutput.writeInt(mKey);
            } catch (final IOException exc) {
                throw new TTIOException(exc);
            }
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
     * Value for the name map, representing only an String.
     * 
     * @author Sebastian Graf, University of Konstanz
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
                into.putString(node.mData);
            }
        }

        /** Value Variable. */
        private final String mData;

        /**
         * Constructor.
         * 
         * @param pData
         *            setting the value
         */
        public MetaValue(final String pData) {
            mData = pData;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DataOutput pOutput) throws TTIOException {
            try {
                pOutput.writeInt(VALUE);
                final byte[] tmp = getData().getBytes();
                pOutput.writeInt(tmp.length);
                pOutput.write(tmp);
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

        public String getData() {
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

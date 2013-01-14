package org.treetank.node;

import java.util.Objects;

import org.treetank.api.IMetaEntry;
import org.treetank.api.IMetaEntryFactory;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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
     * {@inheritDoc}
     */
    @Override
    public IMetaEntry deserializeEntry(byte[] pData) {
        final ByteArrayDataInput input = ByteStreams.newDataInput(pData);
        final int kind = input.readInt();
        switch (kind) {
        case KEY:
            return new MetaKey(input.readInt());
        case VALUE:
            final int valSize = input.readInt();
            final byte[] bytes = new byte[valSize];
            input.readFully(bytes);
            return new MetaValue(new String(bytes));
        }
        return null;
    }

    /**
     * Key for the name map, representing only an integer.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static class MetaKey implements IMetaEntry {
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
        public byte[] getByteRepresentation() {
            final ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeInt(KEY);
            output.writeInt(mKey);
            return output.toByteArray();
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
    }

    /**
     * Value for the name map, representing only an String.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static class MetaValue implements IMetaEntry {
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
        public byte[] getByteRepresentation() {
            final ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeInt(VALUE);
            final byte[] tmp = getData().getBytes();
            output.writeInt(tmp.length);
            output.write(tmp);
            return output.toByteArray();
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

    }

}

package org.treetank.filelistener.file.node;

import java.util.Objects;

import org.treetank.api.IMetaEntry;
import org.treetank.api.IMetaEntryFactory;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * This factory is used to give the treetank
 * system knowledge of the MetaEntries this interface module
 * uses to store the data.
 * 
 * @author Andreas Rain
 *
 */
public class FilelistenerMetaPageFactory implements IMetaEntryFactory{
    
    private final static int KEY = 1;
    private final static int VALUE = 2;

    @Override
    public IMetaEntry deserializeEntry(byte[] pData) {
        final ByteArrayDataInput input = ByteStreams.newDataInput(pData);
        final int kind = input.readInt();

        final int valSize = input.readInt();
        final byte[] bytes = new byte[valSize];
        input.readFully(bytes);
        
        switch (kind) {
        case KEY:
            return new MetaKey(new String(bytes));
        case VALUE:
            return new MetaValue(bytes);
        }
        return null;
    }

    /**
     * Key representing a relative path to the mount point.
     * 
     * @author Andreas Rain
     * 
     */
    public static class MetaKey implements IMetaEntry {
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
        public byte[] getByteRepresentation() {
            final ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeInt(KEY);
            output.writeInt(mKey.getBytes().length);
            output.write(mKey.getBytes());
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
     * Value which is basically the byte representation
     * of the header FileNode
     * 
     * @author Andreas Rain
     * 
     */
    public static class MetaValue implements IMetaEntry {
        /** Value Variable. */
        private final byte[] mData;

        /**
         * Constructor.
         * 
         * @param pData
         *            setting the value
         */
        public MetaValue(final byte[] pData) {
            mData = pData;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public byte[] getByteRepresentation() {
            final ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeInt(VALUE);
            final byte[] tmp = getData();
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

        public byte[] getData() {
            return mData;
        }

    }

}

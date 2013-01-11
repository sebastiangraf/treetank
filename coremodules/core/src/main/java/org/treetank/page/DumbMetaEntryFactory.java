package org.treetank.page;

import java.util.Objects;

import org.treetank.api.IMetaEntry;
import org.treetank.api.IMetaEntryFactory;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class DumbMetaEntryFactory implements IMetaEntryFactory {

    private final static int KEY = 1;
    private final static int VALUE = 2;

    @Override
    public IMetaEntry deserializeEntry(byte[] pData) {
        final ByteArrayDataInput input = ByteStreams.newDataInput(pData);
        final int kind = input.readInt();
        switch (kind) {
        case KEY:
            return new MetaKey(input.readLong());
        case VALUE:
            return new MetaValue(input.readLong());
        }
        return null;
    }

    public static class MetaKey implements IMetaEntry {

        private final long mData;

        public MetaKey(final long pData) {
            mData = pData;
        }

        @Override
        public byte[] getByteRepresentation() {
            final ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeInt(KEY);
            output.writeLong(mData);
            return output.toByteArray();
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
    }

    public static class MetaValue implements IMetaEntry {

        private final long mData;

        public MetaValue(final long pData) {
            mData = pData;
        }

        @Override
        public byte[] getByteRepresentation() {
            final ByteArrayDataOutput output = ByteStreams.newDataOutput();
            output.writeInt(VALUE);
            output.writeLong(mData);
            return output.toByteArray();
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

    }

}

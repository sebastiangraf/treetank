package org.treetank.page;

import static com.google.common.base.Objects.toStringHelper;

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
            return new DumbKey(input.readLong());
        case VALUE:
            return new DumbValue(input.readLong());
        }
        return null;
    }

    public static class DumbKey implements IMetaEntry {

        private final long mData;

        public DumbKey(final long pData) {
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

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return toStringHelper(this).add("mData", mData).toString();
        }
    }

    public static class DumbValue implements IMetaEntry {

        private final long mData;

        public DumbValue(final long pData) {
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

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return toStringHelper(this).add("mData", mData).toString();
        }

    }

}

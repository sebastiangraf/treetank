package org.treetank.page;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import org.treetank.api.IMetaEntry;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.exception.TTIOException;

public class DumbMetaEntryFactory implements IMetaEntryFactory {

    private final static int KEY = 1;
    private final static int VALUE = 2;

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

    public static class DumbKey implements IMetaEntry {

        private final long mData;

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
    }

    public static class DumbValue implements IMetaEntry {

        private final long mData;

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

    }

}

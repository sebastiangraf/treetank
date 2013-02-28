/**
 * 
 */
package org.treetank.page;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import org.treetank.api.INode;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTIOException;

/**
 * Simple Factory for generating {@link DumbNode}s mainly for testing the page-layer.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class DumbNodeFactory implements INodeFactory {

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public INode deserializeNode(DataInput pSource) throws TTIOException {
        try {
            return new DumbNode(pSource.readLong(), pSource.readLong());
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Simple DumbNode just for testing the {@link NodePage}s.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static class DumbNode implements INode {

        long mNodeKey;
        long mHash;

        /**
         * Simple constructor.
         * 
         * @param pNodeKey
         *            to be set
         * @param pHash
         *            to be set
         */
        public DumbNode(long pNodeKey, long pHash) {
            mNodeKey = pNodeKey;
            mHash = pHash;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DataOutput pOutput) throws TTIOException {
            try {
                pOutput.writeLong(mNodeKey);
                pOutput.writeLong(mHash);
            } catch (final IOException exc) {
                throw new TTIOException(exc);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getNodeKey() {
            return mNodeKey;
        }

        /**
         * {@inheritDoc}
         */
        public void setNodeKey(final long pNodeKey) {
            mNodeKey = pNodeKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getHash() {
            return mHash;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(mNodeKey, mHash);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            return this.hashCode() == obj.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return toStringHelper(this).add("mNodeKey", mNodeKey).add("mHash", mHash).toString();
        }

    }

}

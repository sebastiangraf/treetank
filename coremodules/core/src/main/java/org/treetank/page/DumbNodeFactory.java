/**
 * 
 */
package org.treetank.page;

import static com.google.common.base.Objects.toStringHelper;

import java.util.Objects;

import org.treetank.api.INode;
import org.treetank.api.INodeFactory;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * Simple Factory for generating {@link DumbNode}s mainly for testing the page-layer.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class DumbNodeFactory implements INodeFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public INode deserializeNode(byte[] pSource) {
        final ByteArrayDataInput input = ByteStreams.newDataInput(pSource);
        return new DumbNode(input.readLong(), input.readLong());
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
        public byte[] getByteRepresentation() {
            final ByteArrayDataOutput pOutput = ByteStreams.newDataOutput();
            pOutput.writeLong(mNodeKey);
            pOutput.writeLong(mHash);
            return pOutput.toByteArray();
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
        public void setHash(long pHash) {
            mHash = pHash;
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

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}

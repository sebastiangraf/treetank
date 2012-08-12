/**
 * 
 */
package org.treetank.page;

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

        final long mNodeKey;
        final long mHash;

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
        public void setNodeKey(long pNodeKey) {
            throw new UnsupportedOperationException();
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
        @Override
        public void setHash(long pHash) {
            throw new UnsupportedOperationException();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getHash() {
            return mHash;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DumbNodeFactory []");
        return builder.toString();
    }

}

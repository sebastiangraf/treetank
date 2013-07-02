/**
 * 
 */
package org.treetank.bucket;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.treetank.api.INode;
import org.treetank.api.INodeFactory;
import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

/**
 * Simple Factory for generating {@link DumbNode}s mainly for testing the bucket-layer.
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
            final long key = pSource.readLong();
            byte[] data = new byte[pSource.readInt()];
            pSource.readFully(data);
            return new DumbNode(key, data);
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * Simple DumbNode just for testing the {@link NodeBucket}s.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static class DumbNode implements INode {

        /**
         * Enum for DumbNodeFunnel.
         * 
         * @author Sebastian Graf, University of Konstanz
         * 
         */
        enum DumbNodeFunnel implements Funnel<INode> {
            INSTANCE;
            public void funnel(INode from, PrimitiveSink into) {
                DumbNode node = (DumbNode)from;
                into.putLong(node.getNodeKey()).putBytes(node.mValue);
            }
        }

        long mNodeKey;
        byte[] mValue;

        /**
         * Simple constructor.
         * 
         * @param pNodeKey
         *            to be set
         * @param pHash
         *            to be set
         */
        public DumbNode(long pNodeKey, byte[] pValue) {
            mNodeKey = pNodeKey;
            mValue = pValue;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DataOutput pOutput) throws TTIOException {
            try {
                pOutput.writeLong(mNodeKey);
                pOutput.writeInt(mValue.length);
                pOutput.write(mValue);
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
         * Setting a node key to this dumb node.
         * 
         * @param pKey
         *            to be set
         */
        public void setNodeKey(final long pKey) {
            mNodeKey = pKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return toStringHelper(this).add("mNodeKey", mNodeKey).add("values", Objects.hash(mValue))
                .toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            final int prime = 94907;
            int result = 1;
            result = prime * result + (int)(mNodeKey ^ (mNodeKey >>> 32));
            result = prime * result + Arrays.hashCode(mValue);
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            return obj.hashCode() == this.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Funnel<INode> getFunnel() {
            return DumbNodeFunnel.INSTANCE;
        }
    }

}

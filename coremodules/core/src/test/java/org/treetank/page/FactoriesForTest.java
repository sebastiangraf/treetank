/**
 * 
 */
package org.treetank.page;

import java.util.Random;

import org.treetank.api.INode;
import org.treetank.api.INodeFactory;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class FactoriesForTest implements INodeFactory {

    public final static FactoriesForTest INSTANCE = new FactoriesForTest();

    private final static Random RANDOM = new Random();

    private FactoriesForTest() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public INode deserializeNode(byte[] pSource) {
        final ByteArrayDataInput input = ByteStreams.newDataInput(pSource);
        return new DumpNode(input.readLong(), input.readLong());
    }

    public static final INode generateOne() {
        return new DumpNode(RANDOM.nextLong(), RANDOM.nextLong());
    }

    static class DumpNode implements INode {

        final long mNodeKey;
        final long mHash;

        DumpNode(long pNodeKey, long pHash) {
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

}

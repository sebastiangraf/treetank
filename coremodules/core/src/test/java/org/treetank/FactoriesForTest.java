/**
 * 
 */
package org.treetank;

import org.treetank.TestHelper.DumpNode;
import org.treetank.api.INode;
import org.treetank.api.INodeFactory;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class FactoriesForTest implements INodeFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public INode deserializeNode(byte[] pSource) {
        final ByteArrayDataInput input = ByteStreams.newDataInput(pSource);
        return new DumpNode(input.readLong(), input.readLong());
    }

    public static final INode generateOne() {
        return new DumpNode(TestHelper.random.nextLong(), TestHelper.random.nextLong());
    }

}

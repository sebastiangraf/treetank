/**
 * 
 */
package org.treetank.io.bytepipe;

import java.util.ArrayList;
import java.util.List;

import org.treetank.exception.TTByteHandleException;

/**
 * 
 * Pipeline to handle Bytes before stored in the backends.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class ByteHandlePipeline implements IByteHandler {

    /** Pipeline hold over here. */
    private final List<IByteHandler> mParts;

    /**
     * 
     * Constructor.
     * 
     * @param pParts
     *            to be stored, Order is important!
     */
    public ByteHandlePipeline(final IByteHandler... pParts) {
        mParts = new ArrayList<IByteHandler>();
        for (IByteHandler part : pParts) {
            mParts.add(part);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] serialize(final byte[] pToSerialize) throws TTByteHandleException {
        byte[] pipeData = pToSerialize;
        for (IByteHandler part : mParts) {
            pipeData = part.serialize(pipeData);
        }
        return pipeData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] deserialize(final byte[] pToDeserialize) throws TTByteHandleException {
        byte[] pipeData = pToDeserialize;
        for (int i = mParts.size() - 1; i >= 0; i--) {
            pipeData = mParts.get(i).deserialize(pipeData);
        }
        return pipeData;
    }

}

/**
 * 
 */
package org.treetank.io.bytepipe;

import static com.google.common.base.Objects.toStringHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.treetank.exception.TTByteHandleException;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;

import com.google.inject.Inject;

/**
 * 
 * Pipeline to handle Bytes before stored in the backends.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class ByteHandlerPipeline implements IByteHandlerPipeline {

    /** Pipeline hold over here. */
    private final List<IByteHandler> mParts;

    /**
     * 
     * Constructor.
     * 
     * @param pParts
     *            to be stored, Order is important!
     */
    @Inject
    public ByteHandlerPipeline(final IByteHandler... pParts) {
        mParts = new ArrayList<IByteHandler>();
        for (IByteHandler part : pParts) {
            mParts.add(part);
        }
    }

    /**
     * {@inheritDoc}
     */
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
    public byte[] deserialize(final byte[] pToDeserialize) throws TTByteHandleException {
        byte[] pipeData = pToDeserialize;
        for (int i = mParts.size() - 1; i >= 0; i--) {
            pipeData = mParts.get(i).deserialize(pipeData);
        }
        return pipeData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<IByteHandler> iterator() {
        return mParts.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mParts", mParts).toString();
    }

}

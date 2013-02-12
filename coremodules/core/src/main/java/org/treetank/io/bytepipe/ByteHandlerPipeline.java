/**
 * 
 */
package org.treetank.io.bytepipe;

import static com.google.common.base.Objects.toStringHelper;

import java.io.InputStream;
import java.io.OutputStream;
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
    public OutputStream serialize(final OutputStream pToSerialize) throws TTByteHandleException {
        OutputStream lastOutput = pToSerialize;
        for (IByteHandler part : mParts) {
            lastOutput = part.serialize(lastOutput);
        }
        return lastOutput;
    }

    /**
     * {@inheritDoc}
     */
    public InputStream deserialize(final InputStream pToDeserialize) throws TTByteHandleException {
        InputStream lastInput = pToDeserialize;
        for (int i = mParts.size() - 1; i >= 0; i--) {
            lastInput = mParts.get(i).deserialize(lastInput);
        }
        return lastInput;
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

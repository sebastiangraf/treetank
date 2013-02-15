/**
 * 
 */
package org.treetank.io.bytepipe;

import static com.google.common.base.Objects.toStringHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.treetank.exception.TTByteHandleException;

/**
 * Decorator to zip any data.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Zipper implements IByteHandler {

    /**
     * {@inheritDoc}
     */
    public OutputStream serialize(final OutputStream pToSerialize) throws TTByteHandleException {
        return new DeflaterOutputStream(pToSerialize);
    }

    /**
     * {@inheritDoc}
     */
    public InputStream deserialize(final InputStream pToDeserialize) throws TTByteHandleException {
        return new InflaterInputStream(pToDeserialize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}

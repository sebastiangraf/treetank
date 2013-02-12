/**
 * 
 */
package org.treetank.io.bytepipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
//        try {
//            return new GZIPOutputStream(pToSerialize);
//        } catch (final IOException exc) {
//            throw new TTByteHandleException(exc);
//        }
        return pToSerialize;
    }

    /**
     * {@inheritDoc}
     */
    public InputStream deserialize(final InputStream pToDeserialize) throws TTByteHandleException {
//        try {
//            return new GZIPInputStream(pToDeserialize);
//        } catch (final IOException exc) {
//            throw new TTByteHandleException(exc);
//        }
        return pToDeserialize;
    }

}

/**
 * 
 */
package org.treetank.io.bytepipe;

import static com.google.common.base.Objects.toStringHelper;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.treetank.exception.TTByteHandleException;

/**
 * Decorator to zip any data.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Zipper implements IByteHandler {

    private static final Deflater COMPRESSOR = new Deflater();

    private static final Inflater DECOMPRESSOR = new Inflater();

    private final byte[] mTmp;

    private final ByteArrayOutputStream mOut;

    /**
     * Constructor.
     * 
     * @param pComponent
     */
    public Zipper() {
        mTmp = new byte[32767];
        mOut = new ByteArrayOutputStream();
    }

    /**
     * {@inheritDoc}
     */
    public byte[] serialize(final byte[] pToSerialize) throws TTByteHandleException {
        COMPRESSOR.reset();
        mOut.reset();
        COMPRESSOR.setInput(pToSerialize);
        COMPRESSOR.finish();
        int count;
        while (!COMPRESSOR.finished()) {
            count = COMPRESSOR.deflate(mTmp);
            mOut.write(mTmp, 0, count);
        }
        final byte[] result = mOut.toByteArray();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public byte[] deserialize(byte[] pToDeserialize) throws TTByteHandleException {
        DECOMPRESSOR.reset();
        mOut.reset();
        DECOMPRESSOR.setInput(pToDeserialize);
        int count;
        while (!DECOMPRESSOR.finished()) {
            try {
                count = DECOMPRESSOR.inflate(mTmp);
            } catch (final DataFormatException exc) {
                throw new TTByteHandleException(exc);
            }
            mOut.write(mTmp, 0, count);
        }
        final byte[] result = mOut.toByteArray();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mCompressor", COMPRESSOR).add("mDecompressor", DECOMPRESSOR)
            .toString();
    }

}

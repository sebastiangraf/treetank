/**
 * 
 */
package org.treetank.io.bytepipe;

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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((COMPRESSOR == null) ? 0 : COMPRESSOR.hashCode());
        result = prime * result + ((DECOMPRESSOR == null) ? 0 : DECOMPRESSOR.hashCode());
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Zipper [mCompressor=");
        builder.append(COMPRESSOR);
        builder.append(", mDecompressor=");
        builder.append(DECOMPRESSOR);
        builder.append("]");
        return builder.toString();
    }

}

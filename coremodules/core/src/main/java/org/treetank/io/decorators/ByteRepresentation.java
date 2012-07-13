/**
 * 
 */
package org.treetank.io.decorators;

/**
 * Simple ByteRepresentation, just pipeing through
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class ByteRepresentation implements IByteRepresentation {

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] serialize(byte[] pToSerialize) {
        return pToSerialize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] deserialize(byte[] pToDeserialize) {
        return pToDeserialize;
    }

}

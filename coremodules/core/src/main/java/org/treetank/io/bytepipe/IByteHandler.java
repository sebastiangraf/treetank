package org.treetank.io.bytepipe;

import org.treetank.exception.TTByteHandleException;

import com.google.inject.ImplementedBy;

/**
 * Interface for the decorator, representing any byte representation to be
 * serialized or to serialize.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@ImplementedBy(ByteHandlePipeline.class)
public interface IByteHandler {

    /**
     * Method to serialize any byte-chunk.
     * 
     * @param pToSerialize
     *            byte to be serialized
     * @return result of the serialization
     * @throws TTByteHandleException
     *             to be capsulated.
     */
    byte[] serialize(byte[] pToSerialize) throws TTByteHandleException;

    /**
     * Method to deserialize any byte-chunk.
     * 
     * @param pToDeserialize
     *            to deserialize
     * @return result of the deserialization
     * @throws TTByteHandleException
     *             to be capsulated.
     */
    byte[] deserialize(byte[] pToDeserialize) throws TTByteHandleException;

}

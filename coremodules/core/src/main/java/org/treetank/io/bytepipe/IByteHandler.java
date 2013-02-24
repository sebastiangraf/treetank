package org.treetank.io.bytepipe;

import java.io.InputStream;
import java.io.OutputStream;

import org.treetank.exception.TTByteHandleException;

import com.google.inject.ImplementedBy;

/**
 * Interface for the decorator, representing any byte representation to be
 * serialized or to serialize.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@ImplementedBy(ByteHandlerPipeline.class)
public interface IByteHandler extends Cloneable {

    /**
     * Method to serialize any byte-chunk.
     * 
     * @param pToSerialize
     *            byte to be serialized
     * @return outputstream from the pipeline
     * @throws TTByteHandleException
     *             to be capsulated.
     */
    OutputStream serialize(OutputStream pToSerialize) throws TTByteHandleException;

    /**
     * Method to deserialize any byte-chunk.
     * 
     * @param pToDeserialize
     *            to deserialize
     * @return the inputstream from the pipeline
     * @throws TTByteHandleException
     *             to be capsulated.
     */
    InputStream deserialize(InputStream pToDeserialize) throws TTByteHandleException;

    /**
     * Defining the cloning operation
     * 
     * @return a clone of the current {@link IByteHandler}-instance.
     */
    IByteHandler clone();

    /**
     * Concartenating interface for offering dedicated access to {@link ByteHandlerPipeline} for injections.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public interface IByteHandlerPipeline extends IByteHandler, Iterable<IByteHandler> {

        /**
         * Defining the cloning operation
         * 
         * @return a clone of the current {@link IByteHandler}-instance.
         */
        IByteHandlerPipeline clone();

    }

}

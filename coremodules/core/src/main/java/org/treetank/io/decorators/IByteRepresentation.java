package org.treetank.io.decorators;

/**
 * Interface for the decorator, representing any byte representation to be
 * serialized or to serialize.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IByteRepresentation {

	/**
	 * Method to serialize any byte-chunk.
	 * 
	 * @param pToSerialize
	 *            byte to be serialized
	 * @return result of the serialization
	 */
	byte[] serialize(byte[] pToSerialize);

	/**
	 * Method to deserialize any byte-chunk.
	 * 
	 * @param pToDeserialize
	 *            to deserialize
	 * @return result of the deserialization
	 */
	byte[] deserialize(byte[] pToDeserialize);

}

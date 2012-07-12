package org.treetank.io.decorators;

/**
 * Decorator for Byte-Representation of anything to be serialized
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class ByteRepresentationDecorator implements
		IByteRepresentation {

	/** Component for the Decorator. */
	private final IByteRepresentation mComponent;

	/**
	 * Simple Constructor, setting the component.
	 * 
	 * @param pComponent
	 *            to be set
	 */
	protected ByteRepresentationDecorator(final IByteRepresentation pComponent) {
		this.mComponent = pComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] serialize(byte[] pToSerialize) {
		return mComponent.serialize(pToSerialize);
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] deserialize(byte[] pToDeserialize) {
		return mComponent.deserialize(pToDeserialize);
	}
}

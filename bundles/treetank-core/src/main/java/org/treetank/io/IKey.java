package org.treetank.io;

/**
 * Providing a key corresponding to the storage. A Key is the link to the
 * persistent representation in the physical database e.g. the offset in a file
 * or the key in a relational mapping.
 * 
 * More than one keys are possible if necessary e.g. related to the
 * file-layer-implementation: the offset plus the length. Only one key must be
 * unique.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IKey {

	/**
	 * Getting the main identifier
	 * 
	 * @return a long identifying the page.
	 */
	long getIdentifier();

	/**
	 * Getting all keys.
	 * 
	 * @return a long-array containing all information to get the data.
	 */
	long[] getKeys();

}

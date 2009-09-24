package com.treetank.io;

import com.treetank.io.berkeley.BerkeleyKey;
import com.treetank.io.file.FileKey;

/**
 * Factory to build the key out of a fixed source.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class KeyFactory {

	/** Constant to define the file-keys */
	public static final int FILEKIND = 1;
	/** Constant to define the berkeley-keys */
	public static final int BERKELEYKIND = 2;

	/**
	 * Simple create-method.
	 * 
	 * @param source
	 *            the input from the storage
	 * @return the Key.
	 */
	public static final AbstractKey createKey(final ITTSource source) {
		final int kind = source.readInt();
		AbstractKey returnVal = null;
		switch (kind) {
		case FILEKIND:
			returnVal = new FileKey(source);
			break;
		case BERKELEYKIND:
			returnVal = new BerkeleyKey(source);
			break;
		}
		return returnVal;
	}
}

package com.treetank.io;

import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;

/**
 * Interface for Reading the stored pages in every backend.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IReader {

	/**
	 * Getting the {@link StorageProperties} for the given storage;
	 * 
	 * @return the {@link StorageProperties} for this storage
	 * @throws TreetankIOException
	 *             if somethind bad happens
	 */
	StorageProperties getProps() throws TreetankIOException;

	/**
	 * Getting the first reference of the {@link Uberpage}
	 * 
	 * @return a {@link PageReference} with link to the first reference
	 * @throws TreetankIOException
	 *             if something bad happens
	 */
	PageReference<?> readFirstReference() throws TreetankIOException;

	/**
	 * Getting a reference for the given pointer
	 * 
	 * @param pageReference
	 *            the reference for the page to be determined
	 * @return a {@link AbstractPage} as the base for a page
	 * @throws TreetankIOException
	 *             if something bad happens during read
	 */
	AbstractPage read(final PageReference<? extends AbstractPage> pageReference)
			throws TreetankIOException;

	/**
	 * Closing the storage.
	 * 
	 * @throws TreetankIOException
	 *             if something bad happens while access
	 */
	void close() throws TreetankIOException;

}

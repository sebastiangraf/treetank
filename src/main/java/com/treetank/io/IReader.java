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
	 */
	public StorageProperties getProps();

	/**
	 * Getting the first reference of the {@link Uberpage}
	 * 
	 * @return
	 */
	public PageReference<?> readFirstReference();

	/**
	 * Getting a reference for the given pointer
	 * 
	 * @param pageReference
	 *            the reference for the page to be determined
	 * @return a {@link AbstractPage} as the base for a page
	 */
	public AbstractPage read(
			final PageReference<? extends AbstractPage> pageReference);

	/**
	 * Closing the storage.
	 */
	public void close();

}

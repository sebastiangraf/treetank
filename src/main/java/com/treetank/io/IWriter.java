package com.treetank.io;

import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;

/**
 * Interface to provide the abstract layer related to write access of the
 * tt-backend.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IWriter extends IReader {

	/**
	 * Initializing the storage if the {@link UberPage} is bootstraped.
	 */
	public void initializingStorage(final StorageProperties props);

	/**
	 * Writing a page related to the reference
	 * 
	 * @param pageReference
	 *            that points to a page
	 */
	public void write(final PageReference<? extends AbstractPage> pageReference);

	/**
	 * Write Beacon for the first reference
	 * 
	 * @param pageReference
	 *            that points to the beacon
	 */
	public void writeBeacon(final PageReference<UberPage> pageReference);

	/**
	 * Closing the write access.
	 */
	public void close();

}

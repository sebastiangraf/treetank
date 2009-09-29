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
	 * 
	 * @throw TreetankIOException if something bad happens
	 */
	void initializingStorage(final StorageProperties props)
			throws TreetankIOException;

	/**
	 * Writing a page related to the reference
	 * 
	 * @param pageReference
	 *            that points to a page
	 * @throws TreetankIOException
	 *             execption to be thrown if something bad happens
	 */
	void write(final PageReference<? extends AbstractPage> pageReference)
			throws TreetankIOException;

	/**
	 * Write Beacon for the first reference
	 * 
	 * @param pageReference
	 *            that points to the beacon
	 * @throws TreetankIOException
	 *             exception if something bad happens
	 */
	void writeBeacon(final PageReference<UberPage> pageReference)
			throws TreetankIOException;

	/**
	 * Closing the write access.
	 * 
	 * @throws TreetankIOException
	 *             if closing fails
	 */
	void close() throws TreetankIOException;

}

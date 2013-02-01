/**
 * 
 */
package org.treetank.revisioning;

import org.treetank.cache.LogContainer;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.page.NodePage;

/**
 * This interface offers methods to revision data differently.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */

public interface IRevisioning {

    /**
     * Method to reconstruct a complete NodePage with the help of party filled
     * pages plus a revision-delta which determines the necessary steps back.
     * 
     * @param pages
     *            the base of the complete Nodepage
     * @param revToRestore
     *            the revision needed to build up the complete milestone.
     * @return the complete NodePage
     */
    NodePage combinePages(final NodePage[] pages);

    /**
     * Method to reconstruct a complete NodePage for reading as well as a
     * NodePage for serializing with the Nodes to write already on there.
     * 
     * @param pNewPageKey
     *            page key of the new page
     * @param pPages
     *            the base of the complete Nodepage
     * @param pFullDump
     *            boolean if entire page should be written. Must be triggered from extern seind it is based on
     *            the revisionToRestore-Param
     * @return a NodePageContainer holding a complete NodePage for reading a one
     *         for writing
     */
    LogContainer<NodePage> combinePagesForModification(final long pNewPageKey, final NodePage[] pPages,
        final boolean pFullDump);

    /**
     * Getting the keys for the relevant revision root-pages to restore underlaying nodepages.
     * 
     * @param pRevToRestore
     *            number of revisions necessary to restore one version
     * @param pStartKey
     *            starting key for getting the rest of the pages
     * @param pSeqKey
     *            the sequential key namely the version
     * @param pReader
     *            the reader for getting the intermediate pages and the link to the revision root pages
     * @return an array with the keys to the revision root pages from the newest to the oldest
     * @throws TTIOException
     */
    long[] getRevRootKeys(final int pRevToRestore, final long pStartKey, final long pSeqKey,
        final IBackendReader pReader) throws TTIOException;

}

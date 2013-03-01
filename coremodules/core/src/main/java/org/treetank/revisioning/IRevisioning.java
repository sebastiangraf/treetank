/**
 * 
 */
package org.treetank.revisioning;

import org.treetank.log.LogValue;
import org.treetank.page.NodePage;

/**
 * This interface offers methods to revision data differently.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */

public interface IRevisioning {

    /**
     * Method to reconstruct a complete {@link NodePage} with the help of party filled
     * pages plus a revision-delta which determines the necessary steps back.
     * 
     * @param pages
     *            the base of the complete {@link NodePage}
     * @return the complete {@link NodePage}
     */
    NodePage combinePages(final NodePage[] pages);

    /**
     * Method to reconstruct a complete {@link NodePage} for reading as well as a
     * NodePage for serializing with the Nodes to write already on there.
     * 
     * @param pRevisionsToRestore
     *            number of revisions to restore
     * @param pNewPageKey
     *            page key of the new page
     * @param pPages
     *            the base of the complete Nodepage
     * @param pFullDump
     *            boolean if entire page should be written. Must be triggered from extern seind it is based on
     *            the revisionToRestore-Param
     * @return a {@link LogValue} holding a complete {@link NodePage} for reading a one
     *         for writing
     */
    LogValue combinePagesForModification(final int pRevisionsToRestore, final long pNewPageKey,
        final NodePage[] pPages, final boolean pFullDump);

}

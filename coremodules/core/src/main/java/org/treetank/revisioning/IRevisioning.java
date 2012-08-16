/**
 * 
 */
package org.treetank.revisioning;

import org.treetank.cache.NodePageContainer;
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
     * @param pages
     *            the base of the complete Nodepage
     * @param mileStoneRevision
     *            the revision needed to build up the complete milestone.
     * @return a NodePageContainer holding a complete NodePage for reading a one
     *         for writing
     */
    NodePageContainer combinePagesForModification(final NodePage[] pages);

    /**
     * Getting the numbers of necessary revisions to restore.
     * 
     * @return the number of revisions to restore.
     */
    int getRevisionsToRestore();

    /**
     * 
     * Factory for generating an {@link IRevisioning}-instance. Needed mainly
     * because of Guice-Assisted utilization.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static interface IRevisioningFactory {

        /**
         * Generating a storage for a fixed file.
         * 
         * @param pRevisionsToRestore
         *            number of revisions to restore.
         * @return an {@link IRevisioning}-instance
         */
        IRevisioning create(int pRevisionsToRestore);
    }

}

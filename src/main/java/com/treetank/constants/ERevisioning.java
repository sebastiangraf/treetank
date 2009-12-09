package com.treetank.constants;

import java.util.Set;
import java.util.TreeSet;

import com.treetank.cache.NodePageContainer;
import com.treetank.node.NodePersistenter;
import com.treetank.page.NodePage;

/**
 * Enum for providing different revision algorithms. Each kind must implement
 * one method to reconstruct NodePages for Modification and for Reading.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum ERevisioning {

    /**
     * Sliding Snapshot. A fixed number of revisions is needed to reconstruct a
     * NodePage.
     */
    SLIDING_SNAPSHOT {

        /**
         * {@inheritDoc}
         */
        @Override
        public NodePage combinePages(final NodePage[] pages,
                final int mileStoneRevision) {
            final long nodePageKey = pages[0].getNodePageKey();
            final NodePage returnVal = new NodePage(nodePageKey, pages[0]
                    .getRevision());
            for (NodePage page : pages) {
                assert page.getNodePageKey() == nodePageKey;
                for (int i = 0; i < page.getNodes().length; i++) {
                    if (page.getNode(i) != null && returnVal.getNode(i) == null) {
                        returnVal.setNode(i, page.getNode(i));
                    }
                }
            }
            return returnVal;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NodePageContainer combinePagesForModification(
                final NodePage[] pages, final int mileStoneRevision) {

            final long nodePageKey = pages[0].getNodePageKey();
            final NodePage[] returnVal = {
                    new NodePage(nodePageKey, pages[0].getRevision() + 1),
                    new NodePage(nodePageKey, pages[0].getRevision() + 1) };

            final Set<Integer> nodesSet = new TreeSet<Integer>();

            for (int j = 0; j < pages.length; j++) {
                assert pages[j].getNodePageKey() == nodePageKey;
                for (int i = 0; i < pages[j].getNodes().length; i++) {
                    // Caching the complete page
                    if (pages[j].getNode(i) != null
                            && returnVal[0].getNode(i) == null) {
                        returnVal[0].setNode(i, pages[j].getNode(i));

                        if (pages.length == mileStoneRevision) {
                            if (j < pages.length - 1) {
                                nodesSet.add(i);
                            } else {
                                if (!nodesSet.contains(i)) {
                                    returnVal[1].setNode(i, NodePersistenter
                                            .createNode(pages[j].getNode(i)));
                                }
                            }
                        }
                    }
                }
            }
            final NodePageContainer cont = new NodePageContainer(returnVal[0],
                    returnVal[1]);
            return cont;
        }
    },

    /**
     * Incremental Revisioning. Each Revision can be reconstructed with the help
     * of a the last full-dump plus the incremental steps between.
     */
    INCREMENTAL {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public NodePage combinePages(final NodePage[] pages,
                final int mileStoneRevision) {
            final long nodePageKey = pages[0].getNodePageKey();
            final NodePage returnVal = new NodePage(nodePageKey, pages[0]
                    .getRevision());
            for (NodePage page : pages) {
                assert page.getNodePageKey() == nodePageKey;
                for (int i = 0; i < page.getNodes().length; i++) {
                    if (page.getNode(i) != null && returnVal.getNode(i) == null) {
                        returnVal.setNode(i, page.getNode(i));
                    }
                }
                if (page.getRevision() % mileStoneRevision == 0) {
                    break;
                }
            }

            return returnVal;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NodePageContainer combinePagesForModification(
                final NodePage[] pages, final int revisionToFullDump) {
            final long nodePageKey = pages[0].getNodePageKey();
            final NodePage[] returnVal = {
                    new NodePage(nodePageKey, pages[0].getRevision() + 1),
                    new NodePage(nodePageKey, pages[0].getRevision() + 1) };

            for (int j = 0; j < pages.length; j++) {
                assert pages[j].getNodePageKey() == nodePageKey;
                for (int i = 0; i < pages[j].getNodes().length; i++) {
                    // Caching the complete page
                    if (pages[j].getNode(i) != null
                            && returnVal[0].getNode(i) == null) {
                        returnVal[0].setNode(i, pages[j].getNode(i));

                        if (returnVal[0].getRevision() % revisionToFullDump == 0) {
                            returnVal[1].setNode(i, NodePersistenter
                                    .createNode(pages[j].getNode(i)));
                        }
                    }
                }
            }

            final NodePageContainer cont = new NodePageContainer(returnVal[0],
                    returnVal[1]);
            return cont;
        }
    };

    /**
     * Method to reconstruct a complete NodePage with the help of party filled
     * pages plus a revision-delta which determines the necessary steps back.
     * 
     * @param pages
     *            the base of the complete Nodepage
     * @param mileStoneRevision
     *            the revision needed to build up the complete milestone.
     * @return the complete NodePage
     */
    public abstract NodePage combinePages(final NodePage[] pages,
            final int mileStoneRevision);

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
    public abstract NodePageContainer combinePagesForModification(
            final NodePage[] pages, final int mileStoneRevision);

}

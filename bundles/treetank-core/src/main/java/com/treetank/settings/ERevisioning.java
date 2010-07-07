package com.treetank.settings;

import java.util.Set;
import java.util.TreeSet;

import com.treetank.cache.NodePageContainer;
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
     * FullDump, Just dumping the complete older revision.
     */
    FULLDUMP {

        /**
         * {@inheritDoc}
         */
        @Override
        public NodePage combinePages(final NodePage[] pages,
                final int revToRestore) {
            final long nodePageKey = pages[0].getNodePageKey();
            final NodePage returnVal = new NodePage(nodePageKey,
                    pages[0].getRevision());

            for (int i = 0; i < pages[0].getNodes().length; i++) {
                returnVal.setNode(i, pages[0].getNode(i));
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

            for (int i = 0; i < pages[0].getNodes().length; i++) {
                returnVal[0].setNode(i, pages[0].getNode(i));
                if (pages[0].getNode(i) != null) {
                    returnVal[1].setNode(i, pages[0].getNode(i).clone());
                }
            }

            final NodePageContainer cont = new NodePageContainer(returnVal[0],
                    returnVal[1]);
            return cont;
        }

    },

    /**
     * Differential. Only the diffs are stored related to the last milestone
     * revision
     */
    DIFFERENTIAL {

        /**
         * {@inheritDoc}
         */
        @Override
        public NodePage combinePages(final NodePage[] pages,
                final int revToRestore) {
            final long nodePageKey = pages[0].getNodePageKey();
            final NodePage returnVal = new NodePage(nodePageKey,
                    pages[0].getRevision());
            final NodePage latest = pages[0];

            NodePage referencePage = pages[0];

            for (int i = 1; i < pages.length; i++) {
                if (pages[i].getRevision() % revToRestore == 0) {
                    referencePage = pages[i];
                    break;
                }
            }
            assert latest.getNodePageKey() == nodePageKey;
            assert referencePage.getNodePageKey() == nodePageKey;
            for (int i = 0; i < referencePage.getNodes().length; i++) {
                if (latest.getNode(i) != null) {
                    returnVal.setNode(i, latest.getNode(i));
                } else {
                    returnVal.setNode(i, referencePage.getNode(i));
                }
            }
            return returnVal;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public NodePageContainer combinePagesForModification(
                final NodePage[] pages, final int revToRestore) {
            final long nodePageKey = pages[0].getNodePageKey();
            final NodePage[] returnVal = {
                    new NodePage(nodePageKey, pages[0].getRevision() + 1),
                    new NodePage(nodePageKey, pages[0].getRevision() + 1) };

            final NodePage latest = pages[0];
            NodePage fullDump = pages[0];
            for (int i = 1; i < pages.length; i++) {
                if (pages[i].getRevision() % revToRestore == 0) {
                    fullDump = pages[i];
                    break;
                }
            }

            // iterate through all nodes
            for (int j = 0; j < returnVal[0].getNodes().length; j++) {
                if (latest.getNode(j) != null) {
                    returnVal[0].setNode(j, latest.getNode(j));
                    returnVal[1].setNode(j, latest.getNode(j).clone());
                } else {
                    if (fullDump.getNode(j) != null) {
                        returnVal[0].setNode(j, fullDump.getNode(j));
                        if ((latest.getRevision() + 1) % revToRestore == 0) {
                            returnVal[1]
                                    .setNode(j, fullDump.getNode(j).clone());
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
     * Sliding Snapshot. A fixed number of revisions is needed to reconstruct a
     * NodePage.
     */
    SLIDING_SNAPSHOT {

        /**
         * {@inheritDoc}
         */
        @Override
        public NodePage combinePages(final NodePage[] pages,
                final int revToRestore) {
            final long nodePageKey = pages[0].getNodePageKey();
            final NodePage returnVal = new NodePage(nodePageKey,
                    pages[0].getRevision());
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
                final NodePage[] pages, final int revToRestore) {

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

                        if (pages.length == revToRestore) {
                            if (j < pages.length - 1) {
                                nodesSet.add(i);
                            } else {
                                if (!nodesSet.contains(i)) {
                                    returnVal[1].setNode(i, pages[j].getNode(i)
                                            .clone());
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
                final int revToRestore) {
            final long nodePageKey = pages[0].getNodePageKey();
            final NodePage returnVal = new NodePage(nodePageKey,
                    pages[0].getRevision());
            for (NodePage page : pages) {
                assert page.getNodePageKey() == nodePageKey;
                for (int i = 0; i < page.getNodes().length; i++) {
                    if (page.getNode(i) != null && returnVal.getNode(i) == null) {
                        returnVal.setNode(i, page.getNode(i));
                    }
                }
                if (page.getRevision() % revToRestore == 0) {
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
                final NodePage[] pages, final int revToRestore) {
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

                        if (returnVal[0].getRevision() % revToRestore == 0) {
                            returnVal[1]
                                    .setNode(i, pages[j].getNode(i).clone());
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
     * @param revToRestore
     *            the revision needed to build up the complete milestone.
     * @return the complete NodePage
     */
    public abstract NodePage combinePages(final NodePage[] pages,
            final int revToRestore);

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

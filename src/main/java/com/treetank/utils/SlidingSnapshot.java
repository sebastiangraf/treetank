package com.treetank.utils;

import java.util.Set;
import java.util.TreeSet;

import com.treetank.cache.NodePageContainer;
import com.treetank.node.NodePersistenter;
import com.treetank.page.NodePage;

/**
 * This class helps out everytime it comes to the functionality of the sliding
 * snapshot. The sliding snapshot offers the possibility to reconstruct one
 * revision with the help of a fixed number of several revision.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class SlidingSnapshot {

    // /**
    // * This method computes the real window size depending on the actual
    // * revision
    // *
    // * @param revision
    // * on which base the real window size should be computed
    // * @return the real window size
    // */
    // public static int getRealWindowSize(final long revision) {
    // // rev 0 = 1
    // // rev 1 = 2
    // // rev n = IConstants.SNAPSHOT_WINDOW
    // return revision + 1 < (Integer) SettableProperties.SNAPSHOT_WINDOW
    // .getStandardProperty() ? (int) revision + 1
    // : (Integer) SettableProperties.SNAPSHOT_WINDOW
    // .getStandardProperty();
    // }

    /**
     * Combining multiple snapshot pages to one page
     * 
     * @param pages
     *            to be combines
     * @return the NodePage as a result
     */
    public static NodePage combinePages(final NodePage[] pages) {
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
     * Combining multiple snapshot pages to one page just related to the nodes
     * which should be serialized
     * 
     * @param pages
     *            to be combines
     * @return the NodePage as a result
     */
    public static NodePageContainer combinePagesForModification(
            final NodePage[] pages, final long revision,
            final int snapshotWindow) {
        final long nodePageKey = pages[0].getNodePageKey();
        final NodePage[] returnVal = { new NodePage(nodePageKey, revision),
                new NodePage(nodePageKey, revision) };

        final Set<Integer> nodesSet = new TreeSet<Integer>();

        for (int j = 0; j < pages.length; j++) {
            assert pages[j].getNodePageKey() == nodePageKey;
            for (int i = 0; i < pages[j].getNodes().length; i++) {
                // Caching the complete page
                if (pages[j].getNode(i) != null
                        && returnVal[0].getNode(i) == null) {
                    returnVal[0].setNode(i, pages[j].getNode(i));

                    if (pages.length == snapshotWindow) {
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
}

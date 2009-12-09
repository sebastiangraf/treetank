package com.treetank.constants;

import java.util.Set;
import java.util.TreeSet;

import com.treetank.cache.NodePageContainer;
import com.treetank.node.NodePersistenter;
import com.treetank.page.NodePage;

public enum ERevisioning {

    SLIDING_SNAPSHOT {
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
    INCREMENTAL {
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

    private ERevisioning() {
    }

    public abstract NodePage combinePages(final NodePage[] pages,
            final int mileStoneRevision);

    public abstract NodePageContainer combinePagesForModification(
            final NodePage[] pages, final int mileStoneRevision);

}

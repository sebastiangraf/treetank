package com.treetank.utils;

import java.util.Set;
import java.util.TreeSet;

import com.treetank.cache.NodePageContainer;
import com.treetank.node.NodePersistenter;
import com.treetank.page.NodePage;

public enum ERevisioning {

    SLIDING_SNAPSHOT {
        @Override
        public NodePage combinePages(NodePage[] pages) {
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
        public NodePageContainer combinePagesForModification(NodePage[] pages,
                long revision, int snapshotWindow) {

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
    },
    INCREMENTEL {
        @Override
        public NodePage combinePages(NodePage[] pages) {
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
        public NodePageContainer combinePagesForModification(NodePage[] pages,
                long revision, int revisionToFullDump) {
            final long nodePageKey = pages[0].getNodePageKey();
            final NodePage[] returnVal = { new NodePage(nodePageKey, revision),
                    new NodePage(nodePageKey, revision) };

            for (int j = 0; j < pages.length; j++) {
                assert pages[j].getNodePageKey() == nodePageKey;
                for (int i = 0; i < pages[j].getNodes().length; i++) {
                    // Caching the complete page
                    if (pages[j].getNode(i) != null
                            && returnVal[0].getNode(i) == null) {
                        returnVal[0].setNode(i, pages[j].getNode(i));

                        if (revision % revisionToFullDump == 0) {
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

    public abstract NodePage combinePages(final NodePage[] pages);

    public abstract NodePageContainer combinePagesForModification(
            final NodePage[] pages, final long revision,
            final int snapshotWindow);

}

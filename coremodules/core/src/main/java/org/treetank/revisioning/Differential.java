/**
 * 
 */
package org.treetank.revisioning;

import org.treetank.cache.NodePageContainer;
import org.treetank.page.NodePage;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Differential implements IRevisioning {

    /**
     * Parameter to determine the gap between two full-dumps
     */
    private final int mRevToRestore;

    /**
     * 
     * Constructor.
     * 
     * @param pRevToRestore
     *            to be set.
     */
    @Inject
    public Differential(@Assisted int pRevToRestore) {
        mRevToRestore = pRevToRestore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(NodePage[] pages) {
        final long nodePageKey = pages[0].getNodePageKey();
        final NodePage returnVal = new NodePage(nodePageKey, pages[0].getRevision());
        final NodePage latest = pages[0];

        NodePage referencePage = pages[0];

        for (int i = 1; i < pages.length; i++) {
            if (pages[i].getRevision() % mRevToRestore == 0) {
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
    public NodePageContainer combinePagesForModification(NodePage[] pages) {
        final long nodePageKey = pages[0].getNodePageKey();
        final NodePage[] returnVal =
            {
                new NodePage(nodePageKey, pages[0].getRevision() + 1),
                new NodePage(nodePageKey, pages[0].getRevision() + 1)
            };

        final NodePage latest = pages[0];
        NodePage fullDump = pages[0];
        for (int i = 1; i < pages.length; i++) {
            if (pages[i].getRevision() % mRevToRestore == 0) {
                fullDump = pages[i];
                break;
            }
        }

        // iterate through all nodes
        for (int j = 0; j < returnVal[0].getNodes().length; j++) {
            if (latest.getNode(j) != null) {
                returnVal[0].setNode(j, latest.getNode(j));
                returnVal[1].setNode(j, latest.getNode(j));
            } else {
                if (fullDump.getNode(j) != null) {
                    returnVal[0].setNode(j, fullDump.getNode(j));
                    if ((latest.getRevision() + 1) % mRevToRestore == 0) {
                        returnVal[1].setNode(j, fullDump.getNode(j));
                    }
                }
            }
        }

        final NodePageContainer cont = new NodePageContainer(returnVal[0], returnVal[1]);
        return cont;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRevisionsToRestore() {
        return mRevToRestore;
    }

}

/**
 * 
 */
package org.treetank.revisioning;

import org.treetank.cache.NodePageContainer;
import org.treetank.page.NodePage;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Incremental versioning of {@link NodePage}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Incremental implements IRevisioning {

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
    public Incremental(@Assisted int pRevToRestore) {
        mRevToRestore = pRevToRestore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(NodePage[] pages) {
        final long nodePageKey = pages[0].getNodePageKey();
        final NodePage returnVal = new NodePage(nodePageKey, pages[0].getRevision());
        for (NodePage page : pages) {
            assert page.getNodePageKey() == nodePageKey;
            for (int i = 0; i < page.getNodes().length; i++) {
                if (page.getNode(i) != null && returnVal.getNode(i) == null) {
                    returnVal.setNode(i, page.getNode(i));
                }
            }
            if (page.getRevision() % mRevToRestore == 0) {
                break;
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

        for (int j = 0; j < pages.length; j++) {
            assert pages[j].getNodePageKey() == nodePageKey;
            for (int i = 0; i < pages[j].getNodes().length; i++) {
                // Caching the complete page
                if (pages[j].getNode(i) != null && returnVal[0].getNode(i) == null) {
                    returnVal[0].setNode(i, pages[j].getNode(i));

                    if (returnVal[0].getRevision() % mRevToRestore == 0) {
                        returnVal[1].setNode(i, pages[j].getNode(i));
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mRevToRestore;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Incremental [mRevToRestore=");
        builder.append(mRevToRestore);
        builder.append("]");
        return builder.toString();
    }

}

/**
 * 
 */
package org.treetank.revisioning;

import java.util.Properties;

import org.treetank.access.conf.ContructorProps;
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
     * @param pProperties
     *            to be set.
     */
    @Inject
    public Incremental(@Assisted Properties pProperties) {
        mRevToRestore = Integer.parseInt(pProperties.getProperty(ContructorProps.NUMBERTORESTORE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(NodePage[] pages) {
        final NodePage returnVal = new NodePage(pages[0].getPageKey(), pages[0].getSeqKey());
        for (int j = 0; j < pages.length; j++) {
            for (int i = 0; i < pages[j].getNodes().length; i++) {
                if (pages[j].getNode(i) != null && returnVal.getNode(i) == null) {
                    returnVal.setNode(i, pages[j].getNode(i));
                }
            }
            if (j % mRevToRestore == 0) {
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
        final NodePage[] returnVal =
            {
                new NodePage(pages[0].getPageKey(), pages[0].getSeqKey()),
                new NodePage(pages[0].getPageKey(), pages[0].getSeqKey())
            };

        for (int j = 0; j < pages.length; j++) {
            for (int i = 0; i < pages[j].getNodes().length; i++) {
                // Caching the complete page
                if (pages[j].getNode(i) != null && returnVal[0].getNode(i) == null) {
                    returnVal[0].setNode(i, pages[j].getNode(i));

                    // copy of all nodes from the last fulldump to this revision to ensure read-scalability
                    if (j % mRevToRestore == 0) {
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

/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;

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
        final NodePage returnVal = new NodePage(pages[0].getPageKey());
        for (int j = 0; j < pages.length; j++) {
            for (int i = 0; i < pages[j].getNodes().length; i++) {
                if (pages[j].getNode(i) != null && returnVal.getNode(i) == null) {
                    returnVal.setNode(i, pages[j].getNode(i));
                }
            }
            if ((j + 1) % mRevToRestore == 0) {
                break;
            }
        }

        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePageContainer combinePagesForModification(long pNewPageKey, NodePage[] pages) {
        final NodePage[] returnVal = {
            new NodePage(pages[0].getPageKey()), new NodePage(pNewPageKey)
        };

        for (int j = 0; j < pages.length; j++) {
            for (int i = 0; i < pages[j].getNodes().length; i++) {
                // Caching the complete page
                if (pages[j].getNode(i) != null && returnVal[0].getNode(i) == null) {
                    returnVal[0].setNode(i, pages[j].getNode(i));

                    // copy of all nodes from the last fulldump to this revision to ensure read-scalability
                    if ((j + 1) % mRevToRestore == 0) {
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
    public String toString() {
        return toStringHelper(this).add("mRevToRestore", mRevToRestore).toString();
    }

}

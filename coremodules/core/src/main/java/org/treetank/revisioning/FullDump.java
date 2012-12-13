/**
 * 
 */
package org.treetank.revisioning;

import java.util.Properties;

import org.treetank.cache.NodePageContainer;
import org.treetank.page.NodePage;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * FullDump versioning of {@link NodePage}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class FullDump implements IRevisioning {

    /**
     * 
     * Constructor.
     * 
     * @param pRevToRestore
     *            not really set since FullDump revisions are always restorable within one version.
     */
    @Inject
    public FullDump(@Assisted Properties pProperties) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(NodePage[] pages) {
        final long nodePageKey = pages[0].getPageKey();
        final NodePage returnVal = new NodePage(nodePageKey);

        for (int i = 0; i < pages[0].getNodes().length; i++) {
            returnVal.setNode(i, pages[0].getNode(i));
        }

        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePageContainer combinePagesForModification(NodePage[] pages) {
        final long nodePageKey = pages[0].getPageKey();
        final NodePage[] returnVal = {
            new NodePage(nodePageKey), new NodePage(nodePageKey)
        };

        for (int i = 0; i < pages[0].getNodes().length; i++) {
            returnVal[0].setNode(i, pages[0].getNode(i));
            if (pages[0].getNode(i) != null) {
                returnVal[1].setNode(i, pages[0].getNode(i));
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
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FullDump []");
        return builder.toString();
    }

}

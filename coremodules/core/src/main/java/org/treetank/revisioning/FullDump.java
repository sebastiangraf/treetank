/**
 * 
 */
package org.treetank.revisioning;

import org.treetank.cache.NodePageContainer;
import org.treetank.page.NodePage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class FullDump implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(NodePage[] pages) {
        final long nodePageKey = pages[0].getNodePageKey();
        final NodePage returnVal = new NodePage(nodePageKey, pages[0].getRevision());

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
        final long nodePageKey = pages[0].getNodePageKey();
        final NodePage[] returnVal =
            {
                new NodePage(nodePageKey, pages[0].getRevision() + 1),
                new NodePage(nodePageKey, pages[0].getRevision() + 1)
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

}

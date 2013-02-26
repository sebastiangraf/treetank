/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;

import org.treetank.log.LogValue;
import org.treetank.page.NodePage;

/**
 * FullDump versioning of {@link NodePage}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class FullDump implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(NodePage[] pages) {
        final NodePage returnVal = new NodePage(pages[0].getPageKey(), pages[0].getLastPagePointer());
        for (int i = 0; i < pages[0].getNodes().length; i++) {
            returnVal.setNode(i, pages[0].getNode(i));
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogValue combinePagesForModification(int pRevisionsToRestore, long pNewPageKey, NodePage[] pages,
        boolean fullDump) {
        final NodePage[] returnVal =
            {
                new NodePage(pages[0].getPageKey(), pages[0].getLastPagePointer()),
                new NodePage(pNewPageKey, pages[0].getPageKey())
            };

        for (int i = 0; i < pages[0].getNodes().length; i++) {
            returnVal[0].setNode(i, pages[0].getNode(i));
            returnVal[1].setNode(i, pages[0].getNode(i));
        }

        return new LogValue(returnVal[0], returnVal[1]);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}

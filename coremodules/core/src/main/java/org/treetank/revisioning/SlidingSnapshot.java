/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Preconditions.checkArgument;

import org.treetank.access.PageReadTrx;
import org.treetank.cache.NodePageContainer;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.page.NodePage;

/**
 * Sliding Snapshot versioning of {@link NodePage}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class SlidingSnapshot implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(final NodePage[] pages) {
        // create entire page..
        final NodePage returnVal = new NodePage(pages[0].getPageKey());
        // ...iterate through the nodes and check if it is stored..
        for (int i = 0; i < pages[0].getNodes().length; i++) {
            boolean pageSkip = false;
            // ... form the newest version to the oldest one..
            for (int j = 0; !pageSkip && j < pages.length; j++) {
                // if the node is not set yet but existing in the current version..
                if (pages[j].getNode(i) != null) {
                    // ...break out the loop the next time and..
                    pageSkip = true;
                    // ...set it
                    returnVal.setNode(i, pages[j].getNode(i));
                }

            }
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePageContainer combinePagesForModification(long pNewPageKey, NodePage[] pPages,
        boolean pFullDump) {
        checkArgument(!pFullDump, "Full Dump not possible within sliding snapshot");
        // create pages for container..
        final NodePage[] returnVal = {
            new NodePage(pPages[0].getPageKey()), new NodePage(pNewPageKey)
        };
        // ...iterate through the nodes and check if it is stored..
        for (int i = 0; i < pPages[0].getNodes().length; i++) {
            // ... form the newest version to the oldest one and..
            for (int j = 0; j < pPages.length; j++) {
                // if the node is set, just continue with the next node, otherwise..
                if (returnVal[0].getNode(i) != null) {
                    continue;
                }
                // ...set it to the read-cache and..
                returnVal[0].setNode(i, pPages[j].getNode(i));
                // ..if we receive the oldest version where the node was not set yet, then copy it by hand
                if (j == pPages.length - 1) {
                    returnVal[1].setNode(i, pPages[j].getNode(i));
                }

            }
        }
        // return the container
        return new NodePageContainer(returnVal[0], returnVal[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long[]
        getRevRootKeys(int pRevToRestore, long pLongStartKey, long pSeqKey, IBackendReader pReader)
            throws TTIOException {
        long[] returnVal = new long[pRevToRestore];
        long revCounter = pSeqKey;
        for (int i = 0; i < returnVal.length; i++) {
            returnVal[i] = PageReadTrx.dereferenceLeafOfTree(pReader, pLongStartKey, revCounter);
            revCounter--;
        }
        return returnVal;
    }
}

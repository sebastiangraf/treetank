/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import org.treetank.access.PageReadTrx;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.log.LogValue;
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
        checkArgument(pages.length > 0, "At least one Nodepage must be provided");
        // create entire page..
        final NodePage returnVal = new NodePage(pages[0].getPageKey(), pages[0].getLastPagePointer());
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
    public LogValue combinePagesForModification(int pRevisionsToRestore, long pNewPageKey, NodePage[] pPages,
        boolean pFullDump) {
        checkArgument(pPages.length > 0, "At least one Nodepage must be provided");
        // create pages for container..
        final NodePage[] returnVal =
            {
                new NodePage(pPages[0].getPageKey(), pPages[0].getLastPagePointer()),
                new NodePage(pNewPageKey, pPages[0].getPageKey())
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
                if (pPages.length >= pRevisionsToRestore && j == pPages.length - 1) {
                    returnVal[1].setNode(i, pPages[j].getNode(i));
                }

            }
        }
        // return the container
        return new LogValue(returnVal[0], returnVal[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long[] getRevRootKeys(int pRevToRestore, long pLongStartKey, long pSeqKey, IBackendReader pReader)
        throws TTIOException {
        // taking care about first versions where versionNumber < slidingWindow, taking the smaller one.
        final long[] returnVal = new long[pRevToRestore < pSeqKey + 1 ? pRevToRestore : (int)pSeqKey + 1];
        long revCounter = pSeqKey;
        for (int i = 0; i < returnVal.length; i++) {
            returnVal[i] = (PageReadTrx.dereferenceLeafOfTree(pReader, pLongStartKey, revCounter));
            revCounter--;
        }

        return returnVal;
    }
}

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
import org.treetank.page.interfaces.IPage;

/**
 * Incremental versioning of {@link NodePage}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Incremental implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(final NodePage[] pages) {
        checkArgument(pages.length > 0, "At least one Nodepage must be provided");
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
    public LogValue<IPage> combinePagesForModification(long pNewPageKey, NodePage[] pages,
        boolean pFullDump) {
        checkArgument(pages.length > 0, "At least one Nodepage must be provided");
        // create pages for container..
        final NodePage[] returnVal = {
            new NodePage(pages[0].getPageKey()), new NodePage(pNewPageKey)
        };
        // ...iterate through the nodes and check if it is stored..
        for (int i = 0; i < pages[0].getNodes().length; i++) {
            boolean pageSkip = false;
            // ... form the newest version to the oldest one..
            for (int j = 0; !pageSkip && j < pages.length; j++) {
                // if the node is not set yet but existing in the current version..
                if (pages[j].getNode(i) != null) {
                    // ...break out the loop the next time and..
                    pageSkip = true;
                    // ...set it to the read-cache and..
                    returnVal[0].setNode(i, pages[j].getNode(i));
                    // ...if a fulldump becomes necessary, set it to the write cache as well.
                    if (pFullDump) {
                        returnVal[1].setNode(i, pages[j].getNode(i));
                    }
                }
            }
        }
        // return the container
        return new LogValue<IPage>(returnVal[0], returnVal[1]);
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
        final long currentRevKey = PageReadTrx.dereferenceLeafOfTree(pReader, pLongStartKey, pSeqKey);
        // Revision to retrieve is a full-dump
        if (pSeqKey % pRevToRestore == 0) {
            return new long[] {
                currentRevKey
            };
        } else {
            final long lastFullDumpRev = (long)Math.floor(pSeqKey / pRevToRestore) * pRevToRestore;
            long[] returnVal = new long[(int)(pSeqKey - lastFullDumpRev + 1)];
            returnVal[0] = currentRevKey;
            int i = 1;
            for (long rev = pSeqKey - 1; rev >= lastFullDumpRev; rev--) {
                returnVal[i] = PageReadTrx.dereferenceLeafOfTree(pReader, pLongStartKey, rev);
                i++;
            }
            return returnVal;
        }
    }

}

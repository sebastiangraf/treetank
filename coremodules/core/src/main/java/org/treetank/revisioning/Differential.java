/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import org.treetank.access.PageReadTrx;
import org.treetank.cache.NodePageContainer;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.page.NodePage;

/**
 * Differential versioning of {@link NodePage}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Differential implements IRevisioning {

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(final NodePage[] pages) {
        // check to have only the newer version and the related fulldump to read on
        checkArgument(pages.length <= 2,
            "parameter should just consists of one or two single pages, depending if last page was fulldumped or not");
        // create entire page..
        final NodePage returnVal = new NodePage(pages[0].getPageKey());
        // ...and for all nodes...
        for (int i = 0; i < pages[0].getNodes().length; i++) {
            // ..check if node exists in newer version, and if not...
            if (pages[0].getNodes()[i] != null) {
                returnVal.setNode(i, pages[0].getNode(i));
            }// ...set the version from the last fulldump
            else if (pages.length > 1) {
                returnVal.setNode(i, pages[1].getNode(i));
            }
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePageContainer
        combinePagesForModification(long pNewPageKey, NodePage[] pages, boolean pFullDump) {
        // check to have only the newer version and the related fulldump to read on
        checkArgument(pages.length <= 2,
            "parameter should just consists of one or two single pages, depending if last page was fulldumped or not");
        // create pages for container..
        final NodePage[] returnVal = {
            new NodePage(pages[0].getPageKey()), new NodePage(pNewPageKey)
        };

        // ...iterate through the nodes and check if it is stored..
        for (int j = 0; j < returnVal[0].getNodes().length; j++) {
            // ...check if the node was written within the last version, if so...
            if (pages[0].getNode(j) != null) {
                // ...set it in the read and write-version to be rewritten again...
                returnVal[0].setNode(j, pages[0].getNode(j));
                returnVal[1].setNode(j, pages[0].getNode(j));
            }else if (pages.length > 1) {
                // otherwise, just store then node from the fulldump to complete read-page except...
                returnVal[0].setNode(j, pages[1].getNode(j));
                // ..a fulldump becomes necessary.
                if (pFullDump) {
                    returnVal[1].setNode(j, pages[1].getNode(j));
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
    public String toString() {
        return toStringHelper(this).toString();
    }

    /**
     * {@inheritDoc}
     * 
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
            long lastFullDumpKey = PageReadTrx.dereferenceLeafOfTree(pReader, pLongStartKey, lastFullDumpRev);
            return new long[] {
                currentRevKey, lastFullDumpKey
            };
        }
    }
}

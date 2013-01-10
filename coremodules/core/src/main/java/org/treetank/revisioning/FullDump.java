/**
 * 
 */
package org.treetank.revisioning;

import static com.google.common.base.Objects.toStringHelper;

import org.treetank.access.PageReadTrx;
import org.treetank.cache.NodePageContainer;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.page.NodePage;
import static com.google.common.base.Preconditions.checkArgument;

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
        checkArgument(pages.length == 1, "parameter should just consists of one single page");
        final NodePage returnVal = new NodePage(pages[0].getPageKey());
        for (int i = 0; i < pages[0].getNodes().length; i++) {
            returnVal.setNode(i, pages[0].getNode(i));
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePageContainer
        combinePagesForModification(long pNewPageKey, NodePage[] pages, boolean fullDump) {
        checkArgument(pages.length == 1, "parameter should just consists of one single page");
        checkArgument(fullDump, "Because of the nature, fulldump should occur always");
        final NodePage[] returnVal = {
            new NodePage(pages[0].getPageKey()), new NodePage(pNewPageKey)
        };

        for (int i = 0; i < pages[0].getNodes().length; i++) {
            returnVal[0].setNode(i, pages[0].getNode(i));
            returnVal[1].setNode(i, pages[0].getNode(i));
        }

        final NodePageContainer cont = new NodePageContainer(returnVal[0], returnVal[1]);
        return cont;
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
    public long[]
        getRevRootKeys(int pRevToRestore, long pLongStartKey, long pSeqKey, IBackendReader pReader)
            throws TTIOException {
        final long currentRevKey = PageReadTrx.dereferenceLeafOfTree(pReader, pLongStartKey, pSeqKey);
        return new long[] {
            currentRevKey
        };
    }

}

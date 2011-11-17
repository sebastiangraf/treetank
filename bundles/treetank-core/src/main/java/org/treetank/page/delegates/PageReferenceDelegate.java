/**
 * 
 */
package org.treetank.page.delegates;

import org.treetank.access.WriteTransactionState;
import org.treetank.exception.AbsTTException;
import org.treetank.io.ITTSink;
import org.treetank.page.PageReference;
import org.treetank.page.interfaces.IReferencePage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class PageReferenceDelegate implements IReferencePage {

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(ITTSink paramOut) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRevision() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageReference[] getReferences() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit(WriteTransactionState paramState) throws AbsTTException {
        // TODO Auto-generated method stub

    }

}

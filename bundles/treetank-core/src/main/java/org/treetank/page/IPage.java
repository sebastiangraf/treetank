package org.treetank.page;

import org.treetank.access.WriteTransactionState;
import org.treetank.exception.AbsTTException;
import org.treetank.io.ITTSink;

public interface IPage {

    void commit(final WriteTransactionState paramState) throws AbsTTException;

    void serialize(final ITTSink paramOut);

    PageReference[] getReferences();

    long getRevision();

}

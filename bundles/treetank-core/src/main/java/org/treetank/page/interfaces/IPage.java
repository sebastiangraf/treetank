package org.treetank.page.interfaces;

import org.treetank.access.WriteTransactionState;
import org.treetank.exception.AbsTTException;
import org.treetank.io.ITTSink;
import org.treetank.page.PageReference;

public interface IPage {

    void commit(final WriteTransactionState paramState) throws AbsTTException;

    void serialize(final ITTSink paramOut);

    PageReference[] getReferences();

    long getRevision();

}

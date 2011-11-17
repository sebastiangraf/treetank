package org.treetank.page.interfaces;

import org.treetank.access.WriteTransactionState;
import org.treetank.exception.AbsTTException;
import org.treetank.io.ITTSink;
import org.treetank.page.PageReference;

public interface IPage {

    void serialize(final ITTSink paramOut);

    long getRevision();

    PageReference[] getReferences();

    void commit(final WriteTransactionState paramState) throws AbsTTException;

}

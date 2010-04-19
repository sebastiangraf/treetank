package com.treetank.saxon.wrapper;

import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;

/**
 * Currently not used. For use with XQuery Update. Requires a "commercial" Saxon 
 * license.
 * 
 * @author johannes
 *
 */
public class MutableDocumentWrapper extends MutableNodeWrapper {

  protected MutableDocumentWrapper(ISession session, IWriteTransaction wtx)
      throws TreetankException {
    super(session, wtx);
  }

}

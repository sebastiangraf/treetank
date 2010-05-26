package com.treetank.saxon.wrapper;

import net.sf.saxon.om.MutableDocumentInfo;

import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;

/**
 * <h1>MutableDocumentWrapper</h1>
 * 
 * <p>Main entry point for creating a modifiable tree in Saxon in concunction
 * with the implementation of Saxon's MutableNodeInfo core interface. Represents
 * a document node.</p>
 * 
 * <p><strong>Currently not used.</strong> For use with XQuery Update and 
 * requires a "commercial" Saxon license. Furthermore as of now not stable and 
 * doesn't support third party applications.</p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public class MutableDocumentWrapper extends MutableNodeWrapper
    implements
    MutableDocumentInfo {

  protected MutableDocumentWrapper(ISession session, IWriteTransaction wtx)
      throws TreetankException {
    super(session, wtx);
  }

  @Override
  public void resetIndexes() {
    // TODO Auto-generated method stub

  }

}

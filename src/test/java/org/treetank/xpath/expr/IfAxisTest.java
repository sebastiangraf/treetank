package org.treetank.xpath.expr;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.IAxisTest;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.XPathAxis;


/**
 * JUnit-test class to test the functionality of the DubFilter.
 * 
 * @author Tina Scherer
 * 
 */
public class IfAxisTest {

  public static final String PATH =
      "generated" + File.separator + "IfAxisTest.tnk";
  
  IAxis ifExpr, thenExpr, elseExpr;

  @Before
  public void setUp() {
    
    Session.removeSession(PATH);
    
  }

  
  
  @Test
  public void testIf() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());
    
    
    rtx.moveTo(2L);
    
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "if (text()) then . else child::node()"), new long[] {2L});
    
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "if (node()) then . else child::node()"), new long[] {2L});
    
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "if (processing-instruction()) then . else child::node()"), 
        new long[] {3L, 4L, 7L, 8L, 11L});
    
   
    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}



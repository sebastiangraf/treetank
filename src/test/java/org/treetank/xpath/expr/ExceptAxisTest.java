package org.treetank.xpath.expr;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.IAxisTest;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.XPathAxis;


/**
 * JUnit-test class to test the functionality of the UnionAxis.
 * 
 * @author Tina Scherer
 * 
 */
public class ExceptAxisTest {

  public static final String PATH =
    "target" + File.separator + "tnk" + File.separator + "Test.tnk";

  
  
  @Test
  public void testExcept() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());

    
    rtx.moveTo(2L);

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "child::node() except b"), 
        new long[] {3L, 7L, 11L});

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "child::node() except child::node()/attribute::p:x"), 
        new long[] {3L, 4L, 7L, 11L});
   
    
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "child::node()/parent::node() except self::node()"), 
        new long[] {});
    
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "//node() except //text()"), 
        new long[] {2L, 4L, 8L, 6L, 9L});
    
    
    rtx.moveTo(2L);
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "b/preceding::node() except text()"), 
        new long[] {6L, 5L, 4L});
    
    
    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}



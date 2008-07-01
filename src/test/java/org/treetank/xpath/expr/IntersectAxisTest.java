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
public class IntersectAxisTest {

  public static final String PATH =
      "generated" + File.separator + "IntersectAxisTest.tnk";

  @Before
  public void setUp() {
    
    Session.removeSession(PATH);
  }

  
  
  @Test
  public void testIntersect() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());

    
    rtx.moveTo(2L);

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "child::node() intersect b"), 
        new long[] {4L, 8L});
    
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "child::node() intersect b intersect child::node()/@p:x"), 
        new long[] {8L});

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "child::node() intersect child::node()/attribute::p:x"), 
        new long[] {8L});
   
    
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "child::node()/parent::node() intersect self::node()"), 
        new long[] {2L});
    
   
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "//node() intersect //text()"), 
        new long[] {3L, 7L, 11L, 5L, 10L});
    
    rtx.moveTo(2L);
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx, "b/preceding::node() intersect text()"), 
        new long[] {3L, 7L});

    
    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}



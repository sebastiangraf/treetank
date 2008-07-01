package org.treetank.xpath.filter;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.IAxisTest;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.XPathAxis;


/**
 * JUnit-test class to test the functionality of the DubFilter.
 * 
 * @author Tina Scherer
 * 
 */
public class DubFilterTest {

  public static final String PATH =
      "generated" + File.separator + "DubFilterTest.tnk";

  @Before
  public void setUp() {
    
    Session.removeSession(PATH);
  }

  
  
  @Test
  public void testDupElemination() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    
    wtx.moveTo(2L);

    IAxisTest.testIAxisConventions(new XPathAxis(
        wtx, "child::node()/parent::node()"), new long[] {2L});

    
    IAxisTest.testIAxisConventions(new XPathAxis(
        wtx, "b/following-sibling::node()"), new long[] {7L, 8L, 11L});

    
    IAxisTest.testIAxisConventions(new XPathAxis(
        wtx, "b/preceding::node()"), new long[] {3L, 7L, 6L, 5L, 4L});
    
    
    
    IAxisTest.testIAxisConventions(new XPathAxis(
        wtx, "//c/ancestor::node()"), new long[] {4L, 2L, 8L});
    
    
    wtx.abort();
    wtx.close();
    session.close();

  }

}



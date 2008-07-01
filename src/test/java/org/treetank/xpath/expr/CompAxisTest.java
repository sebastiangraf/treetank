
package org.treetank.xpath.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.XPathAxis;

/**
 * JUnit-test class to test the functionality of the CompAxis.
 * 
 * @author Tina Scherer
 */
public class CompAxisTest {

  public static final String PATH = "generated" + File.separator
      + "CompAxisTest.tnk";

  @Before
  public void setUp() {

    Session.removeSession(PATH);
  }

  @Test
  public void testComp() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());

    // Find descendants starting from nodeKey 0L (root).
    rtx.moveToDocumentRoot();

    final IAxis axis1 = new XPathAxis(rtx, "1.0 = 1.0");
    assertEquals(true, axis1.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis1.hasNext());
    
    final IAxis axis2 = new XPathAxis(rtx, "(1, 2, 3) < (2, 3)");
    assertEquals(true, axis2.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis2.hasNext());
    
    final IAxis axis3 = new XPathAxis(rtx, "(1, 2, 3) > (3, 4)");
    assertEquals(true, axis3.hasNext());
    assertEquals(false, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis3.hasNext());
 
    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}
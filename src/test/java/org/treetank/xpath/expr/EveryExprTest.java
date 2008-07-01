
package org.treetank.xpath.expr;

import static org.junit.Assert.assertEquals;

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
import org.treetank.xpath.XPathAxis;

/**
 * JUnit-test class to test the functionality of the UnionAxis.
 * 
 * @author Tina Scherer
 */
public class EveryExprTest {

  public static final String PATH = "generated" + File.separator
      + "EveryExprTest.tnk";

  @Before
  public void setUp() {

    Session.removeSession(PATH);
  }

  @Test
  public void testEveryExpr() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());
    
    final IAxis axis1 = new XPathAxis(rtx, "every $child in child::node()"
        + "satisfies $child/@i");
    assertEquals(true, axis1.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis1.hasNext());

    final IAxis axis2 = new XPathAxis(rtx, "every $child in child::node()"
        + "satisfies $child/@abc");
    assertEquals(true, axis2.hasNext());
    assertEquals(false, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis2.hasNext());

    rtx.moveTo(2L);
    final IAxis axis3 = new XPathAxis(rtx, "every $child in child::element()"
        + " satisfies $child/attribute::attribute()");
    assertEquals(true, axis3.hasNext());
    assertEquals(false, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis3.hasNext());

    rtx.moveTo(2L);
    final IAxis axis4 = new XPathAxis(rtx,
        "every $child in child::element() satisfies $child/child::c");
    assertEquals(true, axis4.hasNext());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis4.hasNext());

    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}

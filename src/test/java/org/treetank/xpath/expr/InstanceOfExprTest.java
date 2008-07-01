
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
 * JUnit-test class to test the functionality of the InstanceOfExpr.
 * 
 * @author Tina Scherer
 */
public class InstanceOfExprTest {

  public static final String PATH = "generated" + File.separator
      + "InstanceOfExprTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testInstanceOfExpr() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());

    final IAxis axis1 = new XPathAxis(rtx, "1 instance of xs:integer");
    assertEquals(true, axis1.hasNext());
    assertEquals(rtx.keyForName("xs:boolean"), rtx.getTypeKey());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis1.hasNext());

    final IAxis axis2 = new XPathAxis(rtx, "\"hallo\" instance of xs:integer");
    assertEquals(true, axis2.hasNext());
    assertEquals(rtx.keyForName("xs:boolean"), rtx.getTypeKey());
    assertEquals(false, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis2.hasNext());

    final IAxis axis3 = new XPathAxis(rtx, "\"hallo\" instance of xs:string ?");
    assertEquals(true, axis3.hasNext());
    assertEquals(rtx.keyForName("xs:boolean"), rtx.getTypeKey());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis3.hasNext());

    final IAxis axis4 = new XPathAxis(rtx, "\"hallo\" instance of xs:string +");
    assertEquals(true, axis4.hasNext());
    assertEquals(rtx.keyForName("xs:boolean"), rtx.getTypeKey());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis4.hasNext());

    final IAxis axis5 = new XPathAxis(rtx, "\"hallo\" instance of xs:string *");
    assertEquals(true, axis5.hasNext());
    assertEquals(rtx.keyForName("xs:boolean"), rtx.getTypeKey());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis5.hasNext());

    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}

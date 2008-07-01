
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
import org.treetank.utils.TypedValue;
import org.treetank.xpath.XPathAxis;

/**
 * JUnit-test class to test the functionality of the RangeAxis.
 * 
 * @author Tina Scherer
 */
public class RangeAxisTest {

  public static final String PATH = "generated" + File.separator
      + "RangeAxisTest.tnk";

  @Before
  public void setUp() {

    Session.removeSession(PATH);
  }

  @Test
  public void testRangeExpr() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());

    // TODO: tests are false, because the integers are not converted correctly
    // from the byte array
//    final IAxis axis1 = new XPathAxis(rtx, "1 to 4");
//    assertEquals(true, axis1.hasNext());
//    assertEquals(1, TypedValue.parseInt(rtx.getRawValue()));
//    assertEquals(true, axis1.hasNext());
//     assertEquals(2, TypedValue.parseInt(rtx.getRawValue()));
//    assertEquals(true, axis1.hasNext());
//     assertEquals(3, TypedValue.parseInt(rtx.getRawValue()));
//    assertEquals(true, axis1.hasNext());
//     assertEquals(4, TypedValue.parseInt(rtx.getRawValue()));
//    assertEquals(false, axis1.hasNext());
//
//     final IAxis axis2 = new XPathAxis(rtx, "10 to 10");
//     assertEquals(true, axis2.hasNext());
//      assertEquals(10, TypedValue.parseInt(rtx.getRawValue()));
//     assertEquals(false, axis2.hasNext());

     rtx.moveTo(2L);
     final IAxis axis3 = new XPathAxis(rtx, "15 to 10");
     assertEquals(false, axis3.hasNext());

    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}

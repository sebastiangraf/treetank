
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
 * JUnit-test class to test the functionality of the VarRefExpr.
 * 
 * @author Tina Scherer
 */
public class VarRefExprTest {

  public static final String PATH = "generated" + File.separator
      + "VarRefExprTest.tnk";

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
    
     final IAxis axis = new XPathAxis(rtx, "for $a in b return $a");
    
     final VariableAxis variable = new VariableAxis(rtx, axis);
     
     final VarRefExpr axis1 = new VarRefExpr(rtx, variable);
//     assertEquals(false, axis1.hasNext());
   axis1.update(4L);
   assertEquals(true, axis1.hasNext());
   assertEquals(4L, rtx.getNodeKey());
   axis1.update(11L);
   assertEquals(true, axis1.hasNext());
   assertEquals(11L, rtx.getNodeKey());
   axis1.update(2L);
   assertEquals(true, axis1.hasNext());
   assertEquals(2L, rtx.getNodeKey());
   assertEquals(false, axis1.hasNext());
     
     final VarRefExpr axis2 = new VarRefExpr(rtx, variable);
//   assertEquals(false, axis2.hasNext());
   axis2.update(9L);
   assertEquals(true, axis2.hasNext());
   assertEquals(9L, rtx.getNodeKey());
   assertEquals(false, axis2.hasNext());
   axis2.update(10L);
   assertEquals(true, axis2.hasNext());
   assertEquals(10L, rtx.getNodeKey());
   assertEquals(false, axis2.hasNext());
   
    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}

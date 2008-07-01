
package org.treetank.xpath.expr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IItem;
import org.treetank.api.IItemList;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.XPathAxis;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.types.Type;

/**
 * JUnit-test class to test the functionality of the CastableExpr.
 * 
 * @author Tina Scherer
 */
public class CastableExprTest {

  public static final String PATH = "generated" + File.separator
      + "CastableExprTest.tnk";
  
  IItem item1;
  IItem item2;
  IItemList list;
  int key1;
  int key2;

  @Before
  public void setUp() {

    Session.removeSession(PATH);
    item1 = new AtomicValue(false);
    item2 = new AtomicValue(14, Type.INTEGER);
    
    
  }

  @Test
  public void testCastableExpr() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());
   
    key1 = rtx.getItemList().addItem(item1);
    key2 = rtx.getItemList().addItem(item2);
    
    final IAxis axis1 = new XPathAxis(rtx, "1 castable as xs:decimal");
    assertEquals(true, axis1.hasNext());
    assertEquals(rtx.keyForName("xs:boolean"), rtx.getTypeKey());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis1.hasNext());
    
    final IAxis axis2 = new XPathAxis(rtx, "10.0 castable as xs:anyAtomicType");
    try {
      assertEquals(true, axis2.hasNext());
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:XPST0080 "
          + "Target type of a cast or castable expression must not be "
        + "xs:NOTATION or xs:anyAtomicType."));
    }
    
    //Token is not implemented yet.
//    final IAxis axis3 = new XPathAxis(rtx, "\"hello\" castable as xs:token");
//    assertEquals(true, axis3.hasNext());
//    assertEquals(Type.BOOLEAN, rtx.getValueTypeAsType());
//    assertEquals(true, rtx.getValueAsBoolean());
//    assertEquals(false, axis3.hasNext());
    
    final IAxis axis4 = new XPathAxis(rtx, "\"hello\" castable as xs:string");
    assertEquals(true, axis4.hasNext());
    assertEquals(rtx.keyForName("xs:boolean"), rtx.getTypeKey());
    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis4.hasNext());
    
//    final IAxis axis5 = new XPathAxis(rtx, "\"hello\" castable as xs:decimal");
//    assertEquals(true, axis5.hasNext());
//    assertEquals(rtx.keyForName("xs:boolean"), rtx.getTypeKey());
//    assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
//    assertEquals(false, axis5.hasNext());


    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}

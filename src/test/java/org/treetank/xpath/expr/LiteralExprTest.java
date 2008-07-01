
package org.treetank.xpath.expr;

import static org.junit.Assert.assertEquals;

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
import org.treetank.xpath.types.Type;

/**
 * JUnit-test class to test the functionality of the LiteralExpr.
 * 
 * @author Tina Scherer
 */
public class LiteralExprTest {

  public static final String PATH = "generated" + File.separator
      + "LiteralExprTest.tnk";
  
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
  public void testLiteralExpr() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());
    
    key1 = rtx.getItemList().addItem(item1);
    key2 = rtx.getItemList().addItem(item2);

     
    final IAxis axis1 = new LiteralExpr(rtx, key1);
    assertEquals(true, axis1.hasNext());
    assertEquals(key1, rtx.getNodeKey());
    assertEquals(rtx.keyForName("xs:boolean"), rtx.getTypeKey());
    assertEquals(false, Boolean.parseBoolean(rtx.getValue()));
    assertEquals(false, axis1.hasNext());

    
    final IAxis axis2 = new LiteralExpr(rtx, key2);
    assertEquals(true, axis2.hasNext());
    assertEquals(key2, rtx.getNodeKey());
    assertEquals(rtx.keyForName("xs:integer"), rtx.getTypeKey());
    assertEquals(14, (int) Double.parseDouble(rtx.getValue()));
    assertEquals(false, axis2.hasNext());

    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}

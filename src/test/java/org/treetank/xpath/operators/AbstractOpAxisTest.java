
package org.treetank.xpath.operators;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.expr.LiteralExpr;
import org.treetank.xpath.types.Type;

public class AbstractOpAxisTest {

  public static final String PATH = "generated" + File.separator
      + "AbstractOpAxisTest.tnk";

  @Before
  public void setUp() throws Exception {

    Session.removeSession(PATH);

  }

  @Test
  public final void testHasNext() {

    final ISession session = Session.beginSession(PATH);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());
    IItem item1 = new AtomicValue(1.0, Type.DOUBLE);
    IItem item2 = new AtomicValue(2.0, Type.DOUBLE);

    IAxis op1 = new LiteralExpr(rtx, rtx.getItemList().addItem(item1));
    IAxis op2 = new LiteralExpr(rtx, rtx.getItemList().addItem(item2));
    AbstractOpAxis axis = new DivOpAxis(rtx, op1, op2);
    
    assertEquals(true, axis.hasNext());
    assertEquals(rtx.keyForName("xs:double"), rtx.getTypeKey());
    assertEquals(false, axis.hasNext());
    
    //here both operands are the empty sequence
    axis = new DivOpAxis(rtx, op1, op2);
    assertEquals(true, axis.hasNext());
    assertThat(Double.NaN, is(Double.parseDouble(rtx.getValue())));
    assertEquals(rtx.keyForName("xs:double"), rtx.getTypeKey());
    assertEquals(false, axis.hasNext());
   
    
    
  }

}

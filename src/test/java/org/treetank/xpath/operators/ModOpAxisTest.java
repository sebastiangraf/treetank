
package org.treetank.xpath.operators;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
import org.treetank.xpath.expr.SequenceAxis;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.types.Type;

public class ModOpAxisTest {

  public static final String PATH = "generated" + File.separator
      + "ModOpTest.tnk";

  @Before
  public void setUp() throws Exception {

    Session.removeSession(PATH);

  }

  @Test
  public final void testOperate() {

    final ISession session = Session.beginSession(PATH);
    IReadTransaction rtx = session.beginReadTransaction(new ItemList());
    IItem item1 = new AtomicValue(3.0, Type.DOUBLE);
    IItem item2 = new AtomicValue(2.0, Type.DOUBLE);

    IAxis op1 = new LiteralExpr(rtx, rtx.getItemList().addItem(item1));
    IAxis op2 = new LiteralExpr(rtx, rtx.getItemList().addItem(item2));
    AbstractOpAxis axis = new ModOpAxis(rtx, op1, op2);
    
    assertEquals(true, axis.hasNext());
    assertThat(1.0, is(Double.parseDouble(rtx.getValue())));
    assertEquals(rtx.keyForName("xs:double"), rtx.getTypeKey());
    assertEquals(false, axis.hasNext());
    
    rtx.close();
    session.close();
  }

  @Test
  public final void testGetReturnType() {

    final ISession session = Session.beginSession(PATH);
    IReadTransaction rtx = session.beginReadTransaction();

    IAxis op1 = new SequenceAxis(rtx);
    IAxis op2 = new SequenceAxis(rtx);
    AbstractOpAxis axis = new ModOpAxis(rtx, op1, op2);

    assertEquals(Type.DOUBLE, axis.getReturnType(rtx.keyForName("xs:double"),
        rtx.keyForName("xs:double")));
    assertEquals(Type.DOUBLE, axis.getReturnType(rtx.keyForName("xs:decimal"),
        rtx.keyForName("xs:double")));
    assertEquals(Type.FLOAT, axis.getReturnType(rtx.keyForName("xs:float"), rtx
        .keyForName("xs:decimal")));
    assertEquals(Type.DECIMAL, axis.getReturnType(rtx.keyForName("xs:decimal"),
        rtx.keyForName("xs:integer")));
    // assertEquals(Type.INTEGER,
    // axis.getReturnType(rtx.keyForName("xs:integer"),
    // rtx.keyForName("xs:integer")));
   

    Type type;
    try {
      type = axis.getReturnType(rtx.keyForName("xs:dateTime"), rtx
          .keyForName("xs:yearMonthDuration"));
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertThat(
          e.getMessage(),
          is("err:XPTY0004 The type is not appropriate the expression or the "
              + "typedoes not match a required type as specified by the matching rules."));
    }
    
    try {
      type = axis.getReturnType(rtx.keyForName("xs:dateTime"),
           rtx.keyForName("xs:double"));
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertThat(
          e.getMessage(),
          is("err:XPTY0004 The type is not appropriate the expression or the "
              + "typedoes not match a required type as specified by the matching rules."));
    }
    
    try {
      type = axis.getReturnType(rtx.keyForName("xs:string"), rtx
          .keyForName("xs:yearMonthDuration"));
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertThat(
          e.getMessage(),
          is("err:XPTY0004 The type is not appropriate the expression or the "
              + "typedoes not match a required type as specified by the matching rules."));
    }
    
    try {
      type = axis.getReturnType(rtx.keyForName("xs:dateTime"), rtx
          .keyForName("xs:IDREF"));
      fail("Expected an XPathError-Exception.");
    } catch (XPathError e) {
      assertThat(
          e.getMessage(),
          is("err:XPTY0004 The type is not appropriate the expression or the "
              + "typedoes not match a required type as specified by the matching rules."));
    }
    
    rtx.close();
    session.close();
  }

}

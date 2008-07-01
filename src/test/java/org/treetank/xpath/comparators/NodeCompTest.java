
package org.treetank.xpath.comparators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.comparators.AbstractComparator;
import org.treetank.xpath.comparators.CompKind;
import org.treetank.xpath.comparators.NodeComp;
import org.treetank.xpath.expr.LiteralExpr;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.types.Type;

public class NodeCompTest {

  AbstractComparator comparator;

  public static final String PATH = "generated" + File.separator
      + "NodeCompTest.tnk";

  ISession session;

  IWriteTransaction wtx;

  IReadTransaction rtx;

  @Before
  public void setUp() {

    Session.removeSession(PATH);

    // Build simple test tree.
    session = Session.beginSession(PATH);
    wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();
    rtx = session.beginReadTransaction(new ItemList());

    comparator = new NodeComp(rtx, new LiteralExpr(rtx, -2), new LiteralExpr(
        rtx, -1), CompKind.IS);
  }

  @After
  public void tearDown() {

    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();
  }

  @Test
  public void testCompare() {

    AtomicValue[] op1 = { new AtomicValue(2, Type.INTEGER) };
    AtomicValue[] op2 = { new AtomicValue(3, Type.INTEGER) };
    AtomicValue[] op3 = { new AtomicValue(3, Type.INTEGER) };

    assertEquals(false, comparator.compare(op1, op2));
    assertEquals(true, comparator.compare(op3, op2));

    try {
      comparator = new NodeComp(rtx, new LiteralExpr(rtx, -2), new LiteralExpr(
          rtx, -1), CompKind.PRE);
      comparator.compare(op1, op2);
      fail("Expexcted not yet implemented exception.");
    } catch (IllegalStateException e) {
      assertEquals("Evaluation of node comparisons not possible", e
          .getMessage());
    }

    try {
      comparator = new NodeComp(rtx, new LiteralExpr(rtx, -2), new LiteralExpr(
          rtx, -1), CompKind.FO);
      comparator.compare(op1, op2);
      fail("Expexcted not yet implemented exception.");
    } catch (IllegalStateException e) {
      assertEquals("Evaluation of node comparisons not possible", e
          .getMessage());
    }

  }

  @Test
  public void testAtomize() {
    
    IAxis axis = new LiteralExpr(rtx, -2);
    axis.hasNext(); //this is needed, because hasNext() has already been called
    AtomicValue[] value = comparator.atomize(axis);
    assertEquals(value.length, 1);
    assertEquals(rtx.getNodeKey(), value[0].getNodeKey());
    assertEquals("xs:integer", value[0].getType());

    try {
      axis = new DescendantAxis(rtx, false);
      axis.hasNext();
      comparator.atomize(axis);
    } catch (XPathError e) {
      assertEquals("err:XPTY0004 The type is not appropriate the expression or"
          + " the typedoes not match a required type as specified by the "
          + "matching rules.", e.getMessage());
    }
    
    
  }

  @Test
  public void testGetType() {

    assertEquals(Type.INTEGER, comparator.getType(123, 2435));
  }
}

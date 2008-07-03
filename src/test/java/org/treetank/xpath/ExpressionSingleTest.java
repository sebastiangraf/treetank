package org.treetank.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.ChildAxis;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.axislayer.FollowingSiblingAxis;
import org.treetank.axislayer.NestedAxis;
import org.treetank.axislayer.ParentAxis;
import org.treetank.axislayer.SelfAxis;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.ExpressionSingle;
import org.treetank.xpath.expr.UnionAxis;
import org.treetank.xpath.filter.DupFilterAxis;

public class ExpressionSingleTest {

  ExpressionSingle builder;

  public static final String XML =
      "src"
          + File.separator
          + "test"
          + File.separator
          + "resoruces"
          + File.separator
          + "factbook.xml";

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "ChainBuilderTest.tnk";

  @Before
  public void setUp() {

    builder = new ExpressionSingle();
    Session.removeSession(PATH);
  }

  @Test
  public void testAdd() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // test one axis
    IAxis self = new SelfAxis(wtx);
    builder.add(self);
    assertEquals(builder.getExpr(), self);

    // test 2 axis
    IAxis axis1 = new SelfAxis(wtx);
    IAxis axis2 = new SelfAxis(wtx);
    builder.add(axis1);
    builder.add(axis2);
    assertTrue(builder.getExpr() instanceof NestedAxis);

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testDup() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    builder = new ExpressionSingle();
    builder.add(new ChildAxis(wtx));
    builder.add(new DescendantAxis(wtx));
    assertTrue(builder.getExpr() instanceof NestedAxis);

    builder = new ExpressionSingle();
    builder.add(new ChildAxis(wtx));
    builder.add(new DescendantAxis(wtx));
    assertEquals(true, builder.isOrdered());
    assertTrue(builder.getExpr() instanceof NestedAxis);

    builder = new ExpressionSingle();
    builder.add(new ChildAxis(wtx));
    builder.add(new DescendantAxis(wtx));
    builder.add(new ChildAxis(wtx));
    assertEquals(false, builder.isOrdered());

    builder = new ExpressionSingle();
    builder = new ExpressionSingle();
    builder.add(new ChildAxis(wtx));
    builder.add(new DescendantAxis(wtx));
    builder.add(new ChildAxis(wtx));
    builder.add(new ParentAxis(wtx));
    assertEquals(true, builder.isOrdered());

    builder = new ExpressionSingle();
    builder.add(new ChildAxis(wtx));
    builder.add(new DescendantAxis(wtx));
    builder.add(new FollowingSiblingAxis(wtx));
    assertEquals(false, builder.isOrdered());

    builder = new ExpressionSingle();
    builder
        .add(new UnionAxis(wtx, new DescendantAxis(wtx), new ParentAxis(wtx)));
    assertEquals(false, builder.isOrdered());
    assertTrue(builder.getExpr() instanceof DupFilterAxis);

    wtx.abort();
    wtx.close();
    session.close();

  }
}
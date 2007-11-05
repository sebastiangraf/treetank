package org.treetank.axislayer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class PostOrderTest {

  public static final String PATH =
      "generated" + File.separator + "PostOrderAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testIterate() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    final IAxis axis = new PostOrderAxis(wtx, true);

    assertEquals(true, axis.hasNext());
    assertEquals(3L, wtx.getNodeKey());

    assertEquals(true, axis.hasNext());
    assertEquals(5L, wtx.getNodeKey());

    assertEquals(true, axis.hasNext());
    assertEquals(6L, wtx.getNodeKey());

    assertEquals(true, axis.hasNext());
    assertEquals(4L, wtx.getNodeKey());

    assertEquals(true, axis.hasNext());
    assertEquals(7L, wtx.getNodeKey());

    assertEquals(true, axis.hasNext());
    assertEquals(9L, wtx.getNodeKey());

    assertEquals(true, axis.hasNext());
    assertEquals(10L, wtx.getNodeKey());

    assertEquals(true, axis.hasNext());
    assertEquals(8L, wtx.getNodeKey());

    assertEquals(true, axis.hasNext());
    assertEquals(11L, wtx.getNodeKey());

    assertEquals(true, axis.hasNext());
    assertEquals(2L, wtx.getNodeKey());

    assertEquals(true, axis.hasNext());
    assertEquals(0L, wtx.getNodeKey());

    assertEquals(false, axis.hasNext());

    wtx.abort();
    wtx.close();
    session.close();

  }
}

package org.treetank.xmllayer;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxisIterator;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class PostOrderIteratorTest {

  public static final String PATH =
      "generated" + File.separator + "PostOrderAxisIteratorTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testIterate() throws Exception {

    // Build simple test tree.
    final ISession session = Session.getSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    final IAxisIterator postOrderIterator = new PostOrderIterator(wtx, true);

    assertEquals(2L, wtx.getNodeKey());
    assertEquals(true, postOrderIterator.next());

    assertEquals(4L, wtx.getNodeKey());
    assertEquals(true, postOrderIterator.next());

    assertEquals(5L, wtx.getNodeKey());
    assertEquals(true, postOrderIterator.next());

    assertEquals(3L, wtx.getNodeKey());
    assertEquals(true, postOrderIterator.next());

    assertEquals(6L, wtx.getNodeKey());
    assertEquals(true, postOrderIterator.next());

    assertEquals(8L, wtx.getNodeKey());
    assertEquals(true, postOrderIterator.next());

    assertEquals(9L, wtx.getNodeKey());
    assertEquals(true, postOrderIterator.next());

    assertEquals(7L, wtx.getNodeKey());
    assertEquals(true, postOrderIterator.next());

    assertEquals(10L, wtx.getNodeKey());
    assertEquals(true, postOrderIterator.next());

    assertEquals(1L, wtx.getNodeKey());
    assertEquals(true, postOrderIterator.next());

    assertEquals(0L, wtx.getNodeKey());
    assertEquals(false, postOrderIterator.next());

    wtx.abort();
    session.close();

  }
}

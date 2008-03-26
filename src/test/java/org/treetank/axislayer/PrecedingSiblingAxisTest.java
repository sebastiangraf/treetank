package org.treetank.axislayer;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class PrecedingSiblingAxisTest {

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "PrecedingSiblingAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testAxisConventions() {
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(10L);
    IAxisTest.testIAxisConventions(
        new PrecedingSiblingAxis(wtx),
        new long[] { 9L });

    wtx.moveTo(4L);
    IAxisTest.testIAxisConventions(
        new PrecedingSiblingAxis(wtx),
        new long[] { 3L });

    wtx.moveTo(11L);
    IAxisTest.testIAxisConventions(new PrecedingSiblingAxis(wtx), new long[] {
        8L,
        7L,
        4L,
        3L });

    wtx.moveTo(2L);
    IAxisTest
        .testIAxisConventions(new PrecedingSiblingAxis(wtx), new long[] {});

    wtx.moveTo(8L);
    wtx.moveToAttribute(0);
    IAxisTest
        .testIAxisConventions(new PrecedingSiblingAxis(wtx), new long[] {});

    wtx.abort();
    wtx.close();
    session.close();

  }

}

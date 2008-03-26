package org.treetank.axislayer;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class FollowingAxisTest {

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "FollowingAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testAxisConventions() {
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(9L);
    IAxisTest.testIAxisConventions(new FollowingAxis(wtx), new long[] {
        10L,
        11L });

    wtx.moveTo(4L);
    IAxisTest.testIAxisConventions(new FollowingAxis(wtx), new long[] {
        7L,
        8L,
        9L,
        10L,
        11L });

    wtx.moveTo(11L);
    IAxisTest.testIAxisConventions(new FollowingAxis(wtx), new long[] {});

    wtx.moveTo(2L);
    IAxisTest.testIAxisConventions(new FollowingAxis(wtx), new long[] {});

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FollowingAxis(wtx), new long[] {});

    wtx.moveTo(8L);
    wtx.moveToAttribute(0);
    IAxisTest.testIAxisConventions(new FollowingAxis(wtx), new long[] {});

    wtx.abort();
    wtx.close();
    session.close();

  }

}

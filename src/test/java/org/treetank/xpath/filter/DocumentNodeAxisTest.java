
package org.treetank.xpath.filter;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.IAxisTest;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.filter.DocumentNodeAxis;

public class DocumentNodeAxisTest {

  public static final String PATH = "target" + File.separator + "tnk"
      + File.separator + "DocumentNodeAxisTest.tnk";

  @Before
  public void setUp() {

    Session.removeSession(PATH);
  }

  @Test
  public void testIterate() {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(2L);
    IAxisTest.testIAxisConventions(new DocumentNodeAxis(wtx),
        new long[] { IReadTransaction.DOCUMENT_ROOT_KEY });

    wtx.moveTo(4L);
    IAxisTest.testIAxisConventions(new DocumentNodeAxis(wtx),
        new long[] { IReadTransaction.DOCUMENT_ROOT_KEY });

    wtx.moveTo(8L);
    IAxisTest.testIAxisConventions(new DocumentNodeAxis(wtx),
        new long[] { IReadTransaction.DOCUMENT_ROOT_KEY });

    wtx.moveTo(8L);
    wtx.moveToAttribute(0);
    IAxisTest.testIAxisConventions(new DocumentNodeAxis(wtx),
        new long[] { IReadTransaction.DOCUMENT_ROOT_KEY });

    wtx.moveTo(11L);
    IAxisTest.testIAxisConventions(new DocumentNodeAxis(wtx),
        new long[] { IReadTransaction.DOCUMENT_ROOT_KEY });

    wtx.abort();
    wtx.close();
    session.close();

  }

}

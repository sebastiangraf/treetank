package org.treetank.xpath;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.xmllayer.XMLShredder;

/**
 * Testcase for working with XPath and WriteTransactions
 * @author Sebastian Graf, University of Konstanz
 *
 */
public final class XPathWriteTransactionTest {

  //XMark 1 GB
  public static final String XML =
      "src"
          + File.separator
          + "test"
          + File.separator
          + "resources"
          + File.separator
          + "auction.xml";

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "XMark.tnk";

  ISession session;

  IWriteTransaction wtx;

  @Before
  public void setUp() {

    Session.removeSession(PATH);
    // Build simple test tree.
    XMLShredder.shred(XML, new SessionConfiguration(PATH));

    // Verify.
    session = Session.beginSession(PATH);
    wtx = session.beginWriteTransaction();
  }

  @Test
  public void test() throws Exception {
    wtx.moveToDocumentRoot();
    final XPathAxis xpa = new XPathAxis(wtx, "//*");
    if (!xpa.hasNext()) {
      fail();
    }

  }

  @After
  public void tearDown() {
    wtx.abort();
    wtx.close();
    session.close();
  }

}

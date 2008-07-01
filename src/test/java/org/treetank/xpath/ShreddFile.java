package org.treetank.xpath;

import java.io.File;

import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.xmllayer.XMLShredder;

public class ShreddFile {

  public static final String XML =
      "src"
          + File.separator
          + "test"
          + File.separator
          + "ressources"
          + File.separator
          + "test.xml";

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "test.tnk";

  public static void main(String[] args) {
    // Setup parsed session.
    XMLShredder.shred(XML, new SessionConfiguration(PATH));

    // Verify.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction(new ItemList());
    rtx.moveToDocumentRoot();

  }

}

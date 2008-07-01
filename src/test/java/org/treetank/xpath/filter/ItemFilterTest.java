
package org.treetank.xpath.filter;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.IFilterTest;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.xpath.filter.ItemFilter;

public class ItemFilterTest {

  public static final String PATH = "target" + File.separator + "tnk"
      + File.separator + "ItemFilterTest.tnk";

  @Before
  public void setUp() {

    Session.removeSession(PATH);
  }

  @Test
  public void testIFilterConvetions() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(8L);
    IFilterTest.testIFilterConventions(new ItemFilter(wtx), true);
    
    
    wtx.moveTo(3L);
    IFilterTest.testIFilterConventions(new ItemFilter(wtx), true);

    wtx.moveTo(2L);
    wtx.moveToAttribute(0);
    IFilterTest.testIFilterConventions(new ItemFilter(wtx), true);

    wtx.abort();
    wtx.close();
    session.close();

  }
}

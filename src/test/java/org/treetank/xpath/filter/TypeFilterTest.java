package org.treetank.xpath.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.axislayer.IFilterTest;
import org.treetank.sessionlayer.Session;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.xmllayer.XMLShredder;
import org.treetank.xpath.XPathAxis;
import org.treetank.xpath.functions.XPathError;

public class TypeFilterTest {

  public static final String XML =
      "src"
          + File.separator
          + "test"
          + File.separator
          + "resources"
          + File.separator
          + "test.xml";

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "TypeFilterTest.tnk";

  @Before
  public void setUp() {

    Session.removeSession(PATH);
  }

  @Test
  public void testIFilterConvetions() {

    // Build simple test tree.
    //    final ISession session = Session.beginSession(PATH);
    //    final IWriteTransaction wtx = session.beginWriteTransaction();
    //    TestDocument.create(wtx);
    //    

    XMLShredder.shred(XML, new SessionConfiguration(PATH));

    // Verify.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction();
    final IAxis axis = new XPathAxis(rtx, "a");
    final IReadTransaction xtx = axis.getTransaction();

    xtx.moveTo(8L);
    IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:untyped"), true);
    IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:long"), false);

    xtx.moveTo(3L);
    IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:untyped"), true);
    IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:double"), false);

    xtx.moveTo(2L);
    xtx.moveToAttribute(0);
    IFilterTest.testIFilterConventions(
        new TypeFilter(xtx, "xs:untypedAtomic"),
        true);

    IFilterTest.testIFilterConventions(
        new TypeFilter(xtx, "xs:anyAtomicType"),
        false);
    try {
      IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:bla"), false);
      fail("Expected a Type not found error.");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:XPST0051 "
          + "Type is not defined in the in-scope schema types as an "
          + "atomic type."));
    }

    xtx.close();
    rtx.close();
    session.close();

  }
}

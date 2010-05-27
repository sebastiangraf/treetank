package com.treetank.saxon.wrapper;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Value;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.service.xml.XMLShredder;
import com.treetank.utils.DocumentCreater;

public class TestNodeWrapper {

  /** Treetank session on Treetank test document. */
  private transient static ISession sessionTest;

  /** Document node. */
  private transient NodeWrapper node;

  @BeforeClass
  public static void setUp() throws TreetankException {
    Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
    final IDatabase database =
        Database.openDatabase(TestHelper.PATHS.PATH1.getFile());
    sessionTest = database.getSession();
    final IWriteTransaction wtx = sessionTest.beginWriteTransaction();
    DocumentCreater.create(wtx);
    wtx.commit();
    wtx.close();
  }

  @Before
  public void beforeMethod() throws TreetankException {
    final Processor proc = new Processor(false);
    final Configuration config = proc.getUnderlyingConfiguration();

    final IDatabase database =
        Database.openDatabase(TestHelper.PATHS.PATH1.getFile());
    sessionTest = database.getSession();

    node =
        (NodeWrapper) new DocumentWrapper(
            sessionTest,
            config,
            TestHelper.PATHS.PATH1.getFile().getAbsolutePath()).wrap();
  }

  @After
  public void afterMethod() throws TreetankException {
    Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
    Database.truncateDatabase(TestHelper.PATHS.PATH2.getFile());
    Database.forceCloseDatabase(TestHelper.PATHS.PATH1.getFile());
    Database.forceCloseDatabase(TestHelper.PATHS.PATH2.getFile());
  }

  @Test
  public void testAtomize() throws XPathException {
    final NodeWrapper wrapper = new NodeWrapper(sessionTest, 0);
    final Value value = wrapper.atomize();
    TestCase.assertEquals(true, value instanceof UntypedAtomicValue);
    TestCase.assertEquals("oops1foooops2baroops3", value.getStringValue());
  }

  @Test
  public void testCompareOrder() throws XPathException, TreetankException {
    final Processor proc = new Processor(false);
    final Configuration config = proc.getUnderlyingConfiguration();

    final IDatabase database =
        Database.openDatabase(TestHelper.PATHS.PATH2.getFile());
    final ISession session = database.getSession();

    // Not the same document.
    NodeWrapper node =
        (NodeWrapper) new DocumentWrapper(
            session,
            config,
            TestHelper.PATHS.PATH2.getFile().getAbsolutePath()).wrap();
    NodeWrapper other =
        (NodeWrapper) new DocumentWrapper(
            sessionTest,
            config,
            TestHelper.PATHS.PATH1.getFile().getAbsolutePath()).wrap(3);
    TestCase.assertEquals(-2, node.compareOrder(other));

    // Before.
    node =
        (NodeWrapper) new DocumentWrapper(
            sessionTest,
            config,
            TestHelper.PATHS.PATH1.getFile().getAbsolutePath()).wrap();
    other =
        (NodeWrapper) new DocumentWrapper(
            sessionTest,
            config,
            TestHelper.PATHS.PATH1.getFile().getAbsolutePath()).wrap(3);
    TestCase.assertEquals(-1, node.compareOrder(other));

    // After.
    node =
        (NodeWrapper) new DocumentWrapper(
            sessionTest,
            config,
            TestHelper.PATHS.PATH1.getFile().getAbsolutePath()).wrap(3);
    other =
        (NodeWrapper) new DocumentWrapper(
            sessionTest,
            config,
            TestHelper.PATHS.PATH1.getFile().getAbsolutePath()).wrap(0);
    TestCase.assertEquals(1, node.compareOrder(other));

    // Same.
    node =
        (NodeWrapper) new DocumentWrapper(
            sessionTest,
            config,
            TestHelper.PATHS.PATH1.getFile().getAbsolutePath()).wrap(3);
    other =
        (NodeWrapper) new DocumentWrapper(
            sessionTest,
            config,
            TestHelper.PATHS.PATH1.getFile().getAbsolutePath()).wrap(3);
    TestCase.assertEquals(0, node.compareOrder(other));
  }

  @Test
  public void testGetAttributeValue() {
    final Processor proc = new Processor(false);
    node =
        (NodeWrapper) new DocumentWrapper(sessionTest, proc
            .getUnderlyingConfiguration(), TestHelper.PATHS.PATH1
            .getFile()
            .getAbsolutePath()).wrap(1);

    final AxisIterator iterator = node.iterateAxis(Axis.ATTRIBUTE);
    final NodeInfo attribute = (NodeInfo) iterator.next();

    node.getNamePool().allocate(
        attribute.getPrefix(),
        attribute.getURI(),
        attribute.getLocalPart());

    node = (NodeWrapper) node.getParent();
    TestCase.assertEquals("j", node.getAttributeValue(attribute
        .getFingerprint()));
  }

  @Test
  @Ignore("Not ready to run!")
  public void testGetBaseURI() throws Exception {
    // Test without xml:base specified.
    TestCase.assertEquals(
        TestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
        node.getBaseURI());

    // Test with xml:base specified.
    final File source =
        new File("src"
            + File.separator
            + "test"
            + File.separator
            + "resources"
            + File.separator
            + "data"
            + File.separator
            + "testBaseURI.xml");

    Database.truncateDatabase(new File(
        TestHelper.PATHS.PATH2.getFile(),
        "baseURI"));
    final IDatabase database =
        Database.openDatabase(new File(
            TestHelper.PATHS.PATH2.getFile(),
            "baseURI"));
    final ISession mSession = database.getSession();
    final IWriteTransaction mWTX = mSession.beginWriteTransaction();
    final XMLEventReader reader = XMLShredder.createReader(source);
    final XMLShredder shredder = new XMLShredder(mWTX, reader, true);
    shredder.call();
    mWTX.close();

    final Processor proc = new Processor(false);
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(mSession, proc
            .getUnderlyingConfiguration(), new File(TestHelper.PATHS.PATH2
            .getFile(), "baseURI").getAbsolutePath()).wrap();
    doc.getNamePool().allocate(
        "xml",
        "http://www.w3.org/XML/1998/namespace",
        "base");
    doc.getNamePool().allocate("", "", "baz");

    final NameTest test =
        new NameTest(Type.ELEMENT, "", "baz", doc.getNamePool());
    final AxisIterator iterator = doc.iterateAxis(Axis.DESCENDANT, test);
    final NodeInfo baz = (NodeInfo) iterator.next();

    TestCase.assertEquals("http://example.org", baz.getBaseURI());
  }
}

package org.treetank.xmllayer;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxisIterator;
import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

public class SubtreeSAXHandlerTest {
  public static final String PATH =
      "generated" + File.separator + "SubtreeSAXHandlerTest.tnk";

  public static final String EXPECTED_PATH =
      "generated" + File.separator + "ExpectedSubtreeSAXHandlerTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
    new File(EXPECTED_PATH).delete();
  }

  @Test
  public void testNormal() throws Exception {

    // Setup expected session.
    final ISession expectedSession = new Session(EXPECTED_PATH);
    final IWriteTransaction expectedTrx =
        expectedSession.beginWriteTransaction();
    TestDocument.create(expectedTrx);

    // Setup parsed session.
    final ISession session = new Session(PATH);
    final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setValidating(false);
    saxParserFactory.setNamespaceAware(true);
    final SAXParser parser = saxParserFactory.newSAXParser();
    final InputSource inputSource = new InputSource("xml/test.xml");
    parser.parse(inputSource, new SubtreeSAXHandler(session));

    final IReadTransaction trx = session.beginReadTransaction();

    expectedTrx.moveToRoot();
    trx.moveToRoot();
    final IAxisIterator expectedDescendants =
        new DescendantAxisIterator(expectedTrx);
    final IAxisIterator descendants = new DescendantAxisIterator(trx);

    while (expectedDescendants.next() && descendants.next()) {
      assertEquals(expectedTrx.getNodeKey(), trx.getNodeKey());
      assertEquals(expectedTrx.getParentKey(), trx.getParentKey());
      assertEquals(expectedTrx.getFirstChildKey(), trx.getFirstChildKey());
      assertEquals(expectedTrx.getLeftSiblingKey(), trx.getLeftSiblingKey());
      assertEquals(expectedTrx.getRightSiblingKey(), trx.getRightSiblingKey());
      assertEquals(expectedTrx.getChildCount(), trx.getChildCount());
      assertEquals(expectedTrx.getKind(), trx.getKind());
      assertEquals(expectedTrx.nameForKey(expectedTrx.getLocalPartKey()), trx
          .nameForKey(trx.getLocalPartKey()));
      assertEquals(expectedTrx.nameForKey(expectedTrx.getURIKey()), trx
          .nameForKey(trx.getURIKey()));
      assertEquals(expectedTrx.nameForKey(expectedTrx.getPrefixKey()), trx
          .nameForKey(trx.getPrefixKey()));
      assertEquals(new String(
          expectedTrx.getValue(),
          IConstants.DEFAULT_ENCODING), new String(
          trx.getValue(),
          IConstants.DEFAULT_ENCODING));
    }

    expectedTrx.abort();
    expectedSession.close();

    session.close();

  }

  @Test
  public void test1Subtree() throws Exception {

    // Setup parsed session.
    final ISession session = new Session(PATH);
    final SubtreeSAXHandler handler = new SubtreeSAXHandler(session);
    handler.startDocument();
    handler.subtreeStarting(0);
    handler.startElement("", "fooEins", "", new AttributesImpl());
    handler.startElement("", "barEins", "", new AttributesImpl());
    handler.endElement("", "barEins", "");
    handler.endElement("", "fooEins", "");
    handler.subtreeEnding(0);
    handler.subtreeStarting(1);
    handler.startElement("", "fooZwei", "", new AttributesImpl());
    handler.startElement("", "barZwei", "", new AttributesImpl());
    handler.endElement("", "barZwei", "");
    handler.endElement("", "fooZwei", "");
    handler.subtreeEnding(1);
    handler.endDocument();

    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();

    rTrx.moveToFirstChild();
    assertEquals("fooZwei", rTrx.getLocalPart());
    rTrx.moveToFirstChild();
    assertEquals("barZwei", rTrx.getLocalPart());
    rTrx.moveToParent();
    rTrx.moveToRightSibling();
    assertEquals("fooEins", rTrx.getLocalPart());
    rTrx.moveToFirstChild();
    assertEquals("barEins", rTrx.getLocalPart());
    session.close();

  }

  @Test
  public void test2Subtree() throws Exception {

    // Setup parsed session.
    final ISession session = new Session(PATH);
    final SubtreeSAXHandler handler = new SubtreeSAXHandler(session);
    handler.startDocument();

    handler.subtreeStarting(1);
    handler.startElement("", "subtreestartnode-1", "", new AttributesImpl());
    handler.startElement("PLAY", "PLAY", "", new AttributesImpl());
    handler.startElement("TITLE", "TITLE", "", new AttributesImpl());
    final char[] elements =
        {
            'T',
            'h',
            'e',
            ' ',
            'T',
            'r',
            'a',
            'g',
            'e',
            'd',
            'y',
            ' ',
            'o',
            'f',
            ' ',
            'A',
            'n',
            't',
            'o',
            'n',
            'y',
            ' ',
            'a',
            'n',
            'd',
            ' ',
            'C',
            'l',
            'e',
            'o',
            'p',
            'a',
            't',
            'r',
            'a' };
    handler.characters(elements, 0, 35);
    handler.endElement("", "TITLE", "TITLE");
    handler.startElement("", "FM", "FM", new AttributesImpl());
    handler.startElement("", "splitnode-655360", "", new AttributesImpl());
    handler.subtreeStarting(10);
    handler.startElement("", "subtreestartnode-10", "", new AttributesImpl());
    handler.startElement("", "P", "P", new AttributesImpl());
    final char[] elements2 = { 'b', 'l', 'a', '2' };
    handler.characters(elements2, 0, 4);
    handler.endElement("", "P", "P");
    handler.endElement("", "subtreestartnode-10", "");
    handler.subtreeEnding(10);
    handler.endElement("", "splitnode-655360", "");
    handler.endElement("", "FM", "FM");
    handler.endElement("", "TITLE", "TITLE");
    handler.endElement("", "PLAY", "PLAY");
    handler.endElement("", "subtreestartnode-1", "");
    handler.subtreeEnding(1);
    handler.endDocument();

    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    //checking second subtree
    rTrx.moveToFirstChild();
    assertEquals("subtreestartnode-10", rTrx.getLocalPart());
    rTrx.moveToFirstChild();
    assertEquals("P", rTrx.getLocalPart());
    rTrx.moveToFirstChild();
    assertEquals(new String(elements2), new String(rTrx.getValue()));
    rTrx.moveToParent();

    //checking first subtree
    rTrx.moveToParent();
    rTrx.moveToRightSibling();
    assertEquals("subtreestartnode-1", rTrx.getLocalPart());
    rTrx.moveToFirstChild();
    assertEquals("PLAY", rTrx.getLocalPart());
    rTrx.moveToFirstChild();
    assertEquals("TITLE", rTrx.getLocalPart());
    rTrx.moveToFirstChild();
    assertEquals(new String(elements), new String(rTrx.getValue()));
    rTrx.moveToParent();
    rTrx.moveToRightSibling();
    assertEquals("FM", rTrx.getLocalPart());
    rTrx.moveToFirstChild();
    assertEquals("splitnode-655360", rTrx.getLocalPart());

    session.close();

  }
}

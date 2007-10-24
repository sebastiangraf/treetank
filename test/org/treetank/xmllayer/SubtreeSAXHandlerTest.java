package org.treetank.xmllayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Test;
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
    final ISession expectedSession = Session.beginSession(EXPECTED_PATH);
    final IWriteTransaction expectedWTX =
        expectedSession.beginWriteTransaction();
    TestDocument.create(expectedWTX);
    expectedWTX.commit();
    //    expectedWTX.close();

    // Setup parsed session.
    final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setValidating(false);
    saxParserFactory.setNamespaceAware(true);
    final SAXParser parser = saxParserFactory.newSAXParser();
    final InputSource inputSource = new InputSource("xml/test.xml");
    final ISession session = Session.beginSession(new File(PATH));
    final IWriteTransaction wrtx = session.beginWriteTransaction();
    parser.parse(inputSource, new SubtreeSAXHandler(wrtx));
    wrtx.commit();
    //    wrtx.close();

    final IReadTransaction expectedTrx = expectedSession.beginReadTransaction();
    final IReadTransaction rtrx2 = session.beginReadTransaction();

    final Axis expectedDescendants = new DescendantAxis(expectedTrx);
    final Axis descendants = new DescendantAxis(rtrx2);

    while (expectedDescendants.hasNext() && descendants.hasNext()) {
      if (!expectedDescendants.mCurrentNode.equals(descendants.mCurrentNode)) {
        fail(expectedDescendants.mCurrentNode.toString()
            + " and "
            + descendants.mCurrentNode
            + " are not the same!");
      }
    }

    expectedTrx.close();
    expectedSession.close();

    rtrx2.close();
    session.close();

  }

  @Test
  public void test1Subtree() throws Exception {

    final ISession session = Session.beginSession(new File(PATH));
    final IWriteTransaction wrtx = session.beginWriteTransaction();
    // Setup parsed session.
    final SubtreeSAXHandler handler = new SubtreeSAXHandler(wrtx);
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

    wrtx.commit();
    //    wrtx.close();

    final IReadTransaction rtx = session.beginReadTransaction();
    rtx.moveToRoot();

    rtx.moveToFirstChild();
    assertEquals("fooZwei", rtx.getLocalPart());
    rtx.moveToFirstChild();
    assertEquals("barZwei", rtx.getLocalPart());
    rtx.moveToParent();
    rtx.moveToRightSibling();
    assertEquals("fooEins", rtx.getLocalPart());
    rtx.moveToFirstChild();
    assertEquals("barEins", rtx.getLocalPart());

    rtx.close();
    session.close();

  }

  @Test
  public void test2Subtree() throws Exception {

    final ISession session = Session.beginSession(new File(PATH));
    final IWriteTransaction wrtx = session.beginWriteTransaction();

    // Setup parsed session.
    final SubtreeSAXHandler handler = new SubtreeSAXHandler(wrtx);
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

    wrtx.commit();
    //    wrtx.close();

    final IReadTransaction rtx = session.beginReadTransaction();
    rtx.moveToRoot();
    //checking second subtree
    rtx.moveToFirstChild();
    assertEquals("subtreestartnode-10", rtx.getLocalPart());
    rtx.moveToFirstChild();
    assertEquals("P", rtx.getLocalPart());
    rtx.moveToFirstChild();
    assertEquals(new String(elements2), new String(rtx.getValue()));
    rtx.moveToParent();

    //checking first subtree
    rtx.moveToParent();
    rtx.moveToRightSibling();
    assertEquals("subtreestartnode-1", rtx.getLocalPart());
    rtx.moveToFirstChild();
    assertEquals("PLAY", rtx.getLocalPart());
    rtx.moveToFirstChild();
    assertEquals("TITLE", rtx.getLocalPart());
    rtx.moveToFirstChild();
    assertEquals(new String(elements), new String(rtx.getValue()));
    rtx.moveToParent();
    rtx.moveToRightSibling();
    assertEquals("FM", rtx.getLocalPart());
    rtx.moveToFirstChild();
    assertEquals("splitnode-655360", rtx.getLocalPart());

    rtx.close();
    session.close();

  }
}

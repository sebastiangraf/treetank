/*
 * Copyright (c) 2007, Marc Kramis
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.xmllayer;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.xml.sax.InputSource;

public class SAXHandlerTest {

  public static final String PATH =
      "generated" + File.separator + "SAXHandlerTest.tnk";

  public static final String EXPECTED_PATH =
      "generated" + File.separator + "ExpectedSAXHandlerTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
    Session.removeSession(EXPECTED_PATH);
  }

  @Test
  public void testSAXHandler() throws Exception {

    // Setup expected session.
    final ISession expectedSession = Session.beginSession(EXPECTED_PATH);
    final IWriteTransaction expectedWTX =
        expectedSession.beginWriteTransaction();
    TestDocument.create(expectedWTX);
    expectedWTX.commit();
    expectedWTX.close();

    // Setup parsed session.
    final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setValidating(false);
    saxParserFactory.setNamespaceAware(true);
    final SAXParser parser = saxParserFactory.newSAXParser();
    final InputSource inputSource = new InputSource("xml/test.xml");
    final ISession session = Session.beginSession(new File(PATH));
    final IWriteTransaction wrtx = session.beginWriteTransaction();
    parser.parse(inputSource, new SAXHandler(wrtx));
    wrtx.close();

    final IReadTransaction expectedTrx = expectedSession.beginReadTransaction();
    final IReadTransaction rtrx2 = session.beginReadTransaction();

    final AbstractAxis expectedDescendants = new DescendantAxis(expectedTrx);
    final AbstractAxis descendants = new DescendantAxis(rtrx2);

    while (expectedDescendants.hasNext() && descendants.hasNext()) {
      if (!expectedDescendants.next().equals(descendants.next())) {
        fail();
      }
    }

    expectedTrx.close();
    expectedSession.close();

    rtrx2.close();
    session.close();

  }

  @Test
  public void testSAXHandlerWithSubtreeInsertion() throws Exception {

    // Setup expected session.
    final ISession session = Session.beginSession(EXPECTED_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();
    wtx.close();

    // Setup parsed session.
    final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    saxParserFactory.setValidating(false);
    saxParserFactory.setNamespaceAware(true);
    final SAXParser parser = saxParserFactory.newSAXParser();
    final InputSource inputSource = new InputSource("xml/test.xml");
    final IWriteTransaction wtx2 = session.beginWriteTransaction();
    wtx2.moveTo(11L);
    parser.parse(inputSource, new SAXHandler(wtx2));
    wtx2.commit();
    wtx2.close();

    final IReadTransaction rtx2 = session.beginReadTransaction();
    final Writer writer = new StringWriter();
    new SAXGenerator(rtx2, writer, false).run();
    Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\""
        + " standalone=\"yes\"?><p:a xmlns:p=\"ns\" i=\"j\">oops1<b>foo<c/>"
        + "</b>oops2<b p:x=\"y\"><c/>bar</b>oops3<p:a i=\"j\">oops1<b>foo<c/>"
        + "</b>oops2<b p:x=\"y\"><c/>bar</b>oops3</p:a></p:a>", writer
        .toString());
    rtx2.close();

    session.close();
  }

}

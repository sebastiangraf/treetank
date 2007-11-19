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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.sessionlayer.Session;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.utils.IConstants;
import org.treetank.utils.TestDocument;

public class XMLShredderTest {

  public static final String XML = "xml" + File.separator + "test.xml";

  public static final String PATH =
      "generated" + File.separator + "XMLShredderTest.tnk";

  public static final String EXPECTED_PATH =
      "generated" + File.separator + "ExpectedXMLShredderTest.tnk";

  @BeforeClass
  public static void setUp() {
    Session.removeSession(PATH);
    Session.removeSession(EXPECTED_PATH);
  }

  @Test
  public void testSTAXShredder() throws IOException, XMLStreamException {

    // Setup expected session.
    final ISession expectedSession = Session.beginSession(EXPECTED_PATH);
    final IWriteTransaction expectedTrx =
        expectedSession.beginWriteTransaction();
    TestDocument.create(expectedTrx);
    expectedTrx.commit();

    // Setup parsed session.
    XMLShredder.shred(XML, new SessionConfiguration(PATH));

    // Verify.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction();
    rtx.moveToDocumentRoot();
    final Iterator<Long> expectedDescendants = new DescendantAxis(expectedTrx);
    final Iterator<Long> descendants = new DescendantAxis(rtx);

    assertEquals(expectedTrx.getRevisionSize(), rtx.getRevisionSize());
    while (expectedDescendants.hasNext() && descendants.hasNext()) {
      assertEquals(expectedTrx.getNodeKey(), rtx.getNodeKey());
      assertEquals(expectedTrx.getParentKey(), rtx.getParentKey());
      assertEquals(expectedTrx.getFirstChildKey(), rtx.getFirstChildKey());
      assertEquals(expectedTrx.getLeftSiblingKey(), rtx.getLeftSiblingKey());
      assertEquals(expectedTrx.getRightSiblingKey(), rtx.getRightSiblingKey());
      assertEquals(expectedTrx.getChildCount(), rtx.getChildCount());
      assertEquals(expectedTrx.getAttributeCount(), rtx.getAttributeCount());
      assertEquals(expectedTrx.getNamespaceCount(), rtx.getNamespaceCount());
      assertEquals(expectedTrx.getKind(), rtx.getKind());
      assertEquals(expectedTrx.nameForKey(expectedTrx.getLocalPartKey()), rtx
          .nameForKey(rtx.getLocalPartKey()));
      assertEquals(expectedTrx.nameForKey(expectedTrx.getURIKey()), rtx
          .nameForKey(rtx.getURIKey()));
      assertEquals(expectedTrx.nameForKey(expectedTrx.getPrefixKey()), rtx
          .nameForKey(rtx.getPrefixKey()));
      if (expectedTrx.isText()) {
        assertEquals(new String(
            expectedTrx.getValue(),
            IConstants.DEFAULT_ENCODING), new String(
            rtx.getValue(),
            IConstants.DEFAULT_ENCODING));
      }
    }

    expectedTrx.close();
    expectedSession.close();
    rtx.close();
    session.close();

  }

  @Test
  public void testShredIntoExisting() throws IOException, XMLStreamException {
    try {
      XMLShredder.shred(XML, new SessionConfiguration(PATH));
      TestCase.fail();
    } catch (Exception e) {
      // Must fail.
    }
  }

}

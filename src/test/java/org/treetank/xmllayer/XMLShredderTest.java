/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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

  public static final String XML =
      "src"
          + File.separator
          + "test"
          + File.separator
          + "resources"
          + File.separator
          + "test.xml";

  public static final String XML2 =
      "src"
          + File.separator
          + "test"
          + File.separator
          + "resources"
          + File.separator
          + "test2.xml";

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "XMLShredderTest.tnk";

  public static final String PATH2 =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "XMLShredderNSTest.tnk";

  public static final String EXPECTED_PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "ExpectedXMLShredderTest.tnk";

  public static final String EXPECTED_PATH2 =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "ExpectedXMLShredderNSTest.tnk";

  @BeforeClass
  public static void setUp() {
    Session.removeSession(PATH);
    Session.removeSession(EXPECTED_PATH);
    Session.removeSession(PATH2);
    Session.removeSession(EXPECTED_PATH2);
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

    assertEquals(expectedTrx.getNodeCount(), rtx.getNodeCount());
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
      assertEquals(expectedTrx.nameForKey(expectedTrx.getNameKey()), rtx
          .nameForKey(rtx.getNameKey()));
      assertEquals(expectedTrx.nameForKey(expectedTrx.getURIKey()), rtx
          .nameForKey(rtx.getURIKey()));
      if (expectedTrx.isTextKind()) {
        assertEquals(new String(
            expectedTrx.getRawValue(),
            IConstants.DEFAULT_ENCODING), new String(
            rtx.getRawValue(),
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

  @Test
  public void testAttributesNSPrefix() throws IOException, XMLStreamException {

    // Setup expected session.
    final ISession expectedSession2 = Session.beginSession(EXPECTED_PATH2);
    final IWriteTransaction expectedTrx2 =
        expectedSession2.beginWriteTransaction();
    TestDocument.createWithoutNamespace(expectedTrx2);
    expectedTrx2.commit();

    // Setup parsed session.
    XMLShredder.shred(XML2, new SessionConfiguration(PATH2));

    // Verify.
    final ISession session = Session.beginSession(PATH2);
    final IReadTransaction rtx = session.beginReadTransaction();
    rtx.moveToDocumentRoot();
    final Iterator<Long> expectedAttributes = new DescendantAxis(expectedTrx2);
    final Iterator<Long> attributes = new DescendantAxis(rtx);

    assertEquals(expectedTrx2.getNodeCount(), rtx.getNodeCount());
    while (expectedAttributes.hasNext() && attributes.hasNext()) {
      assertEquals(expectedTrx2.getNamespaceCount(), rtx.getNamespaceCount());
      assertEquals(expectedTrx2.getAttributeCount(), rtx.getAttributeCount());
      for (int i = 0; i < expectedTrx2.getAttributeCount(); i++) {
        assertEquals(expectedTrx2.getAttributeName(i), rtx.getAttributeName(i));
        assertEquals(expectedTrx2.getAttributeNameKey(i), rtx
            .getAttributeNameKey(i));
        assertEquals(expectedTrx2.getAttributeURI(i), rtx.getAttributeURI(i));

      }
    }

    expectedTrx2.close();
    expectedSession2.close();
    rtx.close();
    session.close();

  }

}

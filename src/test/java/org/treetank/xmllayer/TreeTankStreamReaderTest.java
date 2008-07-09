/*
 * Copyright (c) 2008, Johannes Lichtenberger (HiWi), University of Konstanz
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

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class TreeTankStreamReaderTest {

  public static final String PATH =
    "target"
    + File.separator + "TreeTankStreamReaderTest.tnk";

  public ISession session;

  @Before
  public void setUp() {
    Session.removeSession(PATH);

    // Setup session.
    session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();
    wtx.close();
  }

  @Test
  public void testTreeTankStreamReader() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    final String out = StAXOutput.output(new TreeTankStreamReader(rtx));
    TestCase.assertEquals(TestDocument.XML, out);

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderSubtree1() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(8L));
    final String out = StAXOutput.output(new TreeTankStreamReader(rtx));
    TestCase.assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<b p:x=\"y\"><c/>bar</b>",
        out);

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderSubtree2() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(4L));
    final String out = StAXOutput.output(new TreeTankStreamReader(rtx));
    TestCase.assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<b>foo<c/></b>",
        out);

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderAttributeCount() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase
        .assertEquals(1L, new TreeTankStreamReader(rtx).getAttributeCount());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderIsAttributeSpecified() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase.assertEquals(true, new TreeTankStreamReader(rtx)
        .isAttributeSpecified(0));

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderGetAttributeNamespace() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase.assertEquals("ns", new TreeTankStreamReader(rtx)
        .getAttributeNamespace(0));

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderGetCharacterEncodingScheme() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    TestCase.assertEquals("UTF-8", new TreeTankStreamReader(rtx)
        .getCharacterEncodingScheme());
    TestCase.assertEquals("UTF-8", new TreeTankStreamReader(rtx).getEncoding());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderGetElementText() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase.assertEquals("oops1oops2oops3", new TreeTankStreamReader(rtx)
        .getElementText());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderGetLocalName() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase.assertEquals("a", new TreeTankStreamReader(rtx).getLocalName());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderNamespaceCount() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase.assertEquals(1, new TreeTankStreamReader(rtx).getNamespaceCount());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderNamespacePrefix() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase.assertEquals("p", new TreeTankStreamReader(rtx)
        .getNamespacePrefix(0));

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderNamespaceURI() throws XMLStreamException {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase
        .assertEquals("ns", new TreeTankStreamReader(rtx).getNamespaceURI());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderNamespaceURIPrefix() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase
        .assertEquals("ns", new TreeTankStreamReader(rtx).getNamespaceURI());
    TestCase.assertEquals(
        "http://www.w3.org/2000/xmlns/",
        new TreeTankStreamReader(rtx).getNamespaceURI("xmlns"));
    TestCase.assertEquals("ns", new TreeTankStreamReader(rtx)
        .getNamespaceURI(0));

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderGetPrefix() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase.assertEquals("p", new TreeTankStreamReader(rtx).getPrefix());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderGetTextCharacters() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(5L));

    char[] textChars = new TreeTankStreamReader(rtx).getTextCharacters();

    TestCase.assertEquals('f', textChars[0]);
    TestCase.assertEquals('o', textChars[1]);
    TestCase.assertEquals('o', textChars[2]);

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderGetTextCharactersLength() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(5L));

    final int LENGTH = 3;

    // target == null.
    try {
      new TreeTankStreamReader(rtx).getTextCharacters(4, null, 0, 3);
      fail("Should rise a NullPointerException");
    } catch (NullPointerException expected) {
    }

    // targetStart < 0.
    try {
      new TreeTankStreamReader(rtx).getTextCharacters(
          4,
          new char[LENGTH],
          -1,
          3);
      fail("Should rise a IllegalStateException");
    } catch (IllegalStateException expected) {
    }

    // targetStart > target.length.
    try {
      new TreeTankStreamReader(rtx)
          .getTextCharacters(4, new char[LENGTH], 4, 3);
      fail("Should rise a IllegalStateException");
    } catch (IllegalStateException expected) {
    }

    // length < 0.
    try {
      new TreeTankStreamReader(rtx).getTextCharacters(
          4,
          new char[LENGTH],
          3,
          -1);
      fail("Should rise a IllegalStateException");
    } catch (IllegalStateException expected) {
    }

    // targetStart + length > target.length.
    try {
      new TreeTankStreamReader(rtx)
          .getTextCharacters(4, new char[LENGTH], 2, 2);
      fail("Should rise a IllegalStateException");
    } catch (IllegalStateException expected) {
    }

    char[] textChars = new char[LENGTH];

    // available < 0.
    try {
      new TreeTankStreamReader(rtx).getTextCharacters(4, textChars, 0, 3);
      fail("Should rise an IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException expected) {
    }

    new TreeTankStreamReader(rtx).getTextCharacters(1, textChars, 0, 2);

    TestCase.assertEquals('o', textChars[0]);
    TestCase.assertEquals('o', textChars[1]);

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderGetVersion() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase.assertEquals("1.0", new TreeTankStreamReader(rtx).getVersion());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderHasName() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));
    TestCase.assertEquals(true, new TreeTankStreamReader(rtx).hasName());

    Assert.assertEquals(true, rtx.moveToAttribute(0));
    TestCase.assertEquals(true, new TreeTankStreamReader(rtx).hasName());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderHasText() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(3L));
    TestCase.assertEquals(true, new TreeTankStreamReader(rtx).hasText());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderNextTag() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(4L));

    TestCase.assertEquals(1, new TreeTankStreamReader(rtx).nextTag());

    rtx.close();
    session.close();
  }

  @Test
  public void testTreeTankStreamReaderRequire() {
    // Generate from this session.
    final IReadTransaction rtx = session.beginReadTransaction();

    Assert.assertEquals(true, rtx.moveTo(2L));

    // type = XMLEvent.END_ELEMENT but should be XMLEvent.START_ELEMENT.
    try {
      new TreeTankStreamReader(rtx).require(2, "ns", "a");
      fail("Should rise an IllegalStateException");
    } catch (IllegalStateException expected) {
    }

    // NamespaceURI = "testns" but should be "ns".
    try {
      new TreeTankStreamReader(rtx).require(1, "testns", "a");
      fail("Should rise an IllegalStateException");
    } catch (IllegalStateException expected) {
    }

    // localName = "b" but should be "a".
    try {
      new TreeTankStreamReader(rtx).require(1, "ns", "p");
      fail("Should rise an IllegalStateException");
    } catch (IllegalStateException expected) {
    }

    rtx.close();
    session.close();
  }
}
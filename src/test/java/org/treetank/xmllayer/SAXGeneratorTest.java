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

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class SAXGeneratorTest {

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "SAXGeneratorTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testSAXGenerator() {
    try {
      // Setup session.
      final ISession session = Session.beginSession(PATH);
      final IWriteTransaction wtx = session.beginWriteTransaction();
      TestDocument.create(wtx);
      wtx.commit();
      wtx.close();

      // Generate from this session.
      final Writer writer = new StringWriter();
      final IReadTransaction rtx = session.beginReadTransaction();
      final SAXGenerator generator = new SAXGenerator(rtx, writer, false);
      generator.run();
      TestCase.assertEquals(TestDocument.XML, writer.toString());
      rtx.close();
      session.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getLocalizedMessage());
    }
  }

  @Test
  public void testSAXSubtreeGenerator() {
    try {
      // Setup session.
      final ISession session = Session.beginSession(PATH);
      final IWriteTransaction wtx = session.beginWriteTransaction();
      TestDocument.create(wtx);
      wtx.commit();
      wtx.close();

      // Generate from this session.
      final Writer writer = new StringWriter();
      final IReadTransaction rtx = session.beginReadTransaction();
      Assert.assertEquals(true, rtx.moveTo(8L));
      final SAXGenerator generator = new SAXGenerator(rtx, writer, false);
      generator.run();
      Assert.assertEquals(
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
              + "<b p:x=\"y\"><c/>bar</b>",
          writer.toString());
      rtx.close();
      session.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getLocalizedMessage());
    }
  }

}

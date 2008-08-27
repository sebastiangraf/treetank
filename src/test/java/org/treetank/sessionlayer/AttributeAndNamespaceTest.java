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

package org.treetank.sessionlayer;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.DocumentTest;

public class AttributeAndNamespaceTest {

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "AttributeAndNamespaceTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testAttribute() throws IOException {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);

    wtx.moveTo(1L);
    TestCase.assertEquals(1, wtx.getAttributeCount());
    wtx.moveToAttribute(0);
    TestCase.assertEquals("i", wtx.getName());

    wtx.moveTo(9L);
    TestCase.assertEquals(1, wtx.getAttributeCount());
    wtx.moveToAttribute(0);
    TestCase.assertEquals("p:x", wtx.getName());
    TestCase.assertEquals("ns", wtx.getURI());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testNamespace() throws IOException {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);

    wtx.moveTo(1L);
    TestCase.assertEquals(1, wtx.getNamespaceCount());
    wtx.moveToNamespace(0);
    TestCase.assertEquals("p", wtx.getName());
    TestCase.assertEquals("ns", wtx.getURI());

    wtx.abort();
    wtx.close();
    session.close();

  }

}

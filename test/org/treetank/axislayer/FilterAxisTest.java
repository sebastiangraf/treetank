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

package org.treetank.axislayer;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class FilterAxisTest {

  public static final String PATH =
      "generated" + File.separator + "FilterAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testNameAxisTest() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FilterAxis(
        new DescendantAxis(wtx),
        new NameFilter(wtx, "b")), new long[] { 4L, 8L });

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testValueAxisTest() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FilterAxis(
        new DescendantAxis(wtx),
        new ValueFilter(wtx, "foo")), new long[] { 5L });

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testValueAndNameAxisTest() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(2L);
    IAxisTest.testIAxisConventions(new FilterAxis(
        new AttributeAxis(wtx),
        new NameFilter(wtx, "i"),
        new ValueFilter(wtx, "j")), new long[] { 2L });

    wtx.moveTo(8L);
    IAxisTest.testIAxisConventions(new FilterAxis(
        new AttributeAxis(wtx),
        new NameFilter(wtx, "y"),
        new ValueFilter(wtx, "y")), new long[] {});

    wtx.abort();
    wtx.close();
    session.close();

  }

}
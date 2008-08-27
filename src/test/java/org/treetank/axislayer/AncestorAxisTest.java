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

package org.treetank.axislayer;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.DocumentTest;

public class AncestorAxisTest {

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "AncestorAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testAxisConventions() {
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);

    wtx.moveTo(12L);
    IAxisTest
        .testIAxisConventions(new AncestorAxis(wtx), new long[] { 9L, 1L });

    wtx.moveTo(4L);
    IAxisTest.testIAxisConventions(new AncestorAxis(wtx), new long[] { 1L });

    wtx.moveTo(5L);
    IAxisTest.testIAxisConventions(new AncestorAxis(wtx), new long[] { 1L });

    wtx.moveTo(1L);
    IAxisTest.testIAxisConventions(new AncestorAxis(wtx), new long[] {});

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testAxisConventionsIncludingSelf() {
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);

    wtx.moveTo(11L);
    IAxisTest.testIAxisConventions(new AncestorAxis(wtx, true), new long[] {
        11L,
        9L,
        1L });

    wtx.moveTo(5L);
    IAxisTest.testIAxisConventions(new AncestorAxis(wtx, true), new long[] {
        5L,
        1L });

    wtx.moveTo(4L);
    IAxisTest.testIAxisConventions(new AncestorAxis(wtx, true), new long[] {
        4L,
        1L });

    wtx.moveTo(1L);
    IAxisTest.testIAxisConventions(
        new AncestorAxis(wtx, true),
        new long[] { 1L });

    wtx.abort();
    wtx.close();
    session.close();

  }

}

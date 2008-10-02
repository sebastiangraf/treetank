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
import org.perfidix.Bench;
import org.perfidix.BenchClass;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.DocumentTest;

@BenchClass(runs = 1)
public class ChildAxisTest {

  public static final String TEST_ITERATE_PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "ChildAxisTestIterate.tnk";

  public static final String TEST_PERSISTENT_PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "ChildAxisTestPersistent.tnk";

  @Before
  public void setUp() {
    Session.removeSession(TEST_ITERATE_PATH);
    Session.removeSession(TEST_PERSISTENT_PATH);
  }

  @Test
  @Bench(runs = 10)
  public void testIterate() {

    final ISession session = Session.beginSession(TEST_ITERATE_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);

    wtx.moveTo(1L);
    IAxisTest.testIAxisConventions(new ChildAxis(wtx), new long[] {
        4L,
        5L,
        8L,
        9L,
        13L });

    wtx.moveTo(5L);
    IAxisTest.testIAxisConventions(new ChildAxis(wtx), new long[] { 6L, 7L });

    wtx.moveTo(13L);
    IAxisTest.testIAxisConventions(new ChildAxis(wtx), new long[] {});

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  @Bench(runs = 10)
  public void testPersistent() {

    final ISession session = Session.beginSession(TEST_PERSISTENT_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);
    wtx.commit();
    wtx.close();
    session.close();

    final ISession session1 = Session.beginSession(TEST_PERSISTENT_PATH);
    final IReadTransaction rtx = session1.beginReadTransaction();

    rtx.moveTo(1L);
    IAxisTest.testIAxisConventions(new ChildAxis(rtx), new long[] {
        4L,
        5L,
        8L,
        9L,
        13L });

    rtx.moveTo(5L);
    IAxisTest.testIAxisConventions(new ChildAxis(rtx), new long[] { 6L, 7L });

    rtx.moveTo(13L);
    IAxisTest.testIAxisConventions(new ChildAxis(rtx), new long[] {});

    rtx.close();
    session1.close();

  }

}

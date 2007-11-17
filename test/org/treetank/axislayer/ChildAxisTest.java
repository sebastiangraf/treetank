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

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class ChildAxisTest {

  public static final String TEST_ITERATE_PATH =
      "generated" + File.separator + "ChildAxisTestIterate.tnk";

  public static final String TEST_PERSISTENT_PATH =
      "generated" + File.separator + "ChildAxisTestPersistent.tnk";

  @Before
  public void setUp() {
    Session.removeSession(TEST_ITERATE_PATH);
    Session.removeSession(TEST_PERSISTENT_PATH);
  }

  @Test
  public void testIterate() {

    final ISession session = Session.beginSession(TEST_ITERATE_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(2L);
    AbstractAxisTest.testAxisConventions(new ChildAxis(wtx), new long[] {
        3L,
        4L,
        7L,
        8L,
        11L });

    wtx.moveTo(4L);
    AbstractAxisTest.testAxisConventions(new ChildAxis(wtx), new long[] {
        5L,
        6L });

    wtx.moveTo(11L);
    AbstractAxisTest.testAxisConventions(new ChildAxis(wtx), new long[] {});

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testPersistent() {

    final ISession session = Session.beginSession(TEST_PERSISTENT_PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();
    wtx.close();
    session.close();

    final ISession session1 = Session.beginSession(TEST_PERSISTENT_PATH);
    final IReadTransaction rtx = session1.beginReadTransaction();

    rtx.moveTo(2L);
    AbstractAxisTest.testAxisConventions(new ChildAxis(rtx), new long[] {
        3L,
        4L,
        7L,
        8L,
        11L });

    rtx.moveTo(4L);
    AbstractAxisTest.testAxisConventions(new ChildAxis(rtx), new long[] {
        5L,
        6L });

    rtx.moveTo(11L);
    AbstractAxisTest.testAxisConventions(new ChildAxis(rtx), new long[] {});

    rtx.close();
    session1.close();

  }

}

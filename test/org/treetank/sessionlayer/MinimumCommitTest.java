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

package org.treetank.sessionlayer;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.TestDocument;

public class MinimumCommitTest {

  public static final String PATH =
      "generated" + File.separator + "MinimumCommitTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void test() throws IOException {

    ISession session = Session.beginSession(PATH);
    IWriteTransaction wtx = session.beginWriteTransaction();
    TestCase.assertEquals(0L, wtx.getRevisionNumber());
    wtx.commit();

    wtx.close();
    session.close();

    session = Session.beginSession(PATH);
    wtx = session.beginWriteTransaction();
    TestCase.assertEquals(1L, wtx.getRevisionNumber());
    TestDocument.create(wtx);
    wtx.commit();
    wtx.close();

    wtx = session.beginWriteTransaction();
    TestCase.assertEquals(2L, wtx.getRevisionNumber());
    wtx.commit();
    wtx.close();

    IReadTransaction rtx = session.beginReadTransaction();
    TestCase.assertEquals(2L, rtx.getRevisionNumber());
    rtx.close();
    session.close();

  }

  @Test
  public void testTimestamp() throws IOException {

    ISession session = Session.beginSession(PATH);
    IWriteTransaction wtx = session.beginWriteTransaction();
    TestCase.assertEquals(0L, wtx.getRevisionTimestamp());
    wtx.commit();
    wtx.close();

    IReadTransaction rtx = session.beginReadTransaction();
    if (rtx.getRevisionTimestamp() >= (System.currentTimeMillis() + 1)) {
      TestCase.fail("Committed revision timestamp must be smaller than now.");
    }
    rtx.close();

    session.close();

  }

}

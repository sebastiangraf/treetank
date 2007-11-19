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

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.TestDocument;

public class MultipleCommitTest {

  public static final String PATH =
      "generated" + File.separator + "MultipleCommitTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void test() {
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestCase.assertEquals(0L, wtx.getRevisionNumber());
    TestCase.assertEquals(2L, wtx.getRevisionSize());
    wtx.commit();

    wtx.insertElementAsFirstChild("foo", "", "");
    TestCase.assertEquals(1L, wtx.getRevisionNumber());
    TestCase.assertEquals(3L, wtx.getRevisionSize());
    wtx.abort();

    TestCase.assertEquals(1L, wtx.getRevisionNumber());
    TestCase.assertEquals(2L, wtx.getRevisionSize());
    wtx.close();

    session.close();
  }

  @Test
  public void testAutoCommit() {
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction(100, 1);
    TestDocument.create(wtx);
    wtx.close();

    final IReadTransaction rtx = session.beginReadTransaction();
    Assert.assertEquals(12, rtx.getRevisionSize());
    rtx.close();
    session.close();
  }

}

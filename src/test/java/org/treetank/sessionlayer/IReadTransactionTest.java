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

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.DocumentTest;

public class IReadTransactionTest {

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "IReadTransactionTest.tnk";

  public static ISession SESSION;

  @BeforeClass
  public static void setUp() {
    Session.removeSession(PATH);
    SESSION = Session.beginSession(PATH);
    final IWriteTransaction wtx = SESSION.beginWriteTransaction();
    DocumentTest.create(wtx);
    wtx.insertToken("foo", IReadTransaction.NULL_NODE_KEY);
    wtx.close();
  }

  @AfterClass
  public static void tearDown() {
    SESSION.close();
  }

  @Test
  public void testDocumentRoot() {
    final IReadTransaction rtx = SESSION.beginReadTransaction();

    assertEquals(true, rtx.moveToDocumentRoot());
    assertEquals(true, rtx.isDocumentRootKind());
    assertEquals(false, rtx.hasParent());
    assertEquals(false, rtx.hasLeftSibling());
    assertEquals(false, rtx.hasRightSibling());
    assertEquals(true, rtx.hasFirstChild());

    rtx.close();
  }

  @Test
  public void testFullTextRoot() {
    final IReadTransaction rtx = SESSION.beginReadTransaction();

    assertEquals(true, rtx.moveToFullTextRoot());
    assertEquals(true, rtx.isFullTextRootKind());
    assertEquals(false, rtx.hasParent());
    assertEquals(false, rtx.hasLeftSibling());
    assertEquals(false, rtx.hasRightSibling());
    assertEquals(true, rtx.hasFirstChild());

    assertEquals(true, rtx.moveToToken("foo"));
    assertEquals(true, rtx.moveToToken("f"));
    final long fKey = rtx.getNodeKey();
    assertEquals(false, rtx.moveToToken("bar"));
    assertEquals(fKey, rtx.getNodeKey());

    rtx.close();
  }

  @Test
  public void testConventions() {
    final IReadTransaction rtx = SESSION.beginReadTransaction();

    // IReadTransaction Convention 1.
    assertEquals(true, rtx.moveToDocumentRoot());
    long key = rtx.getNodeKey();

    // IReadTransaction Convention 2.
    assertEquals(rtx.hasParent(), rtx.moveToParent());
    assertEquals(key, rtx.getNodeKey());

    assertEquals(rtx.hasFirstChild(), rtx.moveToFirstChild());
    assertEquals(2L, rtx.getNodeKey());

    assertEquals(false, rtx.moveTo(Integer.MAX_VALUE));
    assertEquals(false, rtx.moveTo(Integer.MIN_VALUE));
    assertEquals(2L, rtx.getNodeKey());

    assertEquals(rtx.hasRightSibling(), rtx.moveToRightSibling());
    assertEquals(2L, rtx.getNodeKey());

    assertEquals(rtx.hasFirstChild(), rtx.moveToFirstChild());
    assertEquals(3L, rtx.getNodeKey());

    assertEquals(rtx.hasRightSibling(), rtx.moveToRightSibling());
    assertEquals(4L, rtx.getNodeKey());

    assertEquals(rtx.hasLeftSibling(), rtx.moveToLeftSibling());
    assertEquals(3L, rtx.getNodeKey());

    assertEquals(rtx.hasParent(), rtx.moveToParent());
    assertEquals(2L, rtx.getNodeKey());

    rtx.close();
  }

}

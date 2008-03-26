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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;

public class FullTextTest {

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "FullTextTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testTokenLifecycle() throws IOException {

    ISession session = Session.beginSession(PATH);

    final IWriteTransaction wtx = session.beginWriteTransaction();

    // Create token.
    final long elementKey1 = wtx.insertElementAsFirstChild("foo", "");
    final long tokenKey1 = wtx.insertToken("foo", elementKey1);

    // Move to token.
    wtx.moveToDocumentRoot();
    Assert.assertEquals(true, wtx.moveToToken("foo"));
    wtx.moveToDocumentRoot();
    Assert.assertEquals(false, wtx.moveToToken("bar"));
    wtx.moveToDocumentRoot();
    Assert.assertEquals(true, wtx.moveToToken("fo"));

    // Insert another key for token.
    wtx.moveToDocumentRoot();
    final long elementKey2 = wtx.insertElementAsFirstChild("foo", "");
    final long tokenKey2 = wtx.insertToken("foo", elementKey2);
    Assert.assertEquals(tokenKey1, tokenKey2);
    Assert.assertEquals(true, wtx.moveToToken("foo"));

    // Remove token.
    wtx.moveToDocumentRoot();
    wtx.removeToken("foo", elementKey1);
    wtx.moveToDocumentRoot();
    Assert.assertEquals(true, wtx.moveToToken("foo"));

    wtx.removeToken("foo", elementKey2);
    wtx.moveToDocumentRoot();
    Assert.assertEquals(false, wtx.moveToToken("foo"));

    wtx.abort();
    wtx.close();
    session.close();
  }

  @Test
  public void testInsertChild() throws IOException {

    ISession session = Session.beginSession(PATH);

    final IWriteTransaction wtx = session.beginWriteTransaction();
    wtx.moveToFullTextRoot();
    TestCase.assertEquals(2L, wtx.insertFullTextAsFirstChild(13));
    TestCase.assertEquals(3L, wtx.insertFullTextAsFirstChild(14));
    TestCase.assertEquals(4L, wtx.insertFullTextAsRightSibling(15));
    TestCase.assertEquals(5L, wtx.insertFullTextAsRightSibling(16));
    TestCase.assertEquals(6L, wtx.insertFullTextAsFirstChild(17));
    wtx.moveToParent();
    TestCase.assertEquals(7L, wtx.insertFullTextAsFirstChild(18));
    wtx.commit();
    wtx.close();
    session.close();

    session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction();

    rtx.moveToFullTextRoot();
    TestCase.assertEquals(IReadTransaction.FULLTEXT_ROOT_KEY, rtx.getNodeKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx.getParentKey());
    TestCase.assertEquals(2L, rtx.getFirstChildKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getLeftSiblingKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getRightSiblingKey());

    rtx.moveToFirstChild();
    TestCase.assertEquals(13, rtx.getNameKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(true, rtx.hasFirstChild());
    TestCase.assertEquals(false, rtx.hasLeftSibling());
    TestCase.assertEquals(false, rtx.hasRightSibling());
    TestCase.assertEquals(2L, rtx.getNodeKey());
    TestCase.assertEquals(IReadTransaction.FULLTEXT_ROOT_KEY, rtx
        .getParentKey());
    TestCase.assertEquals(3L, rtx.getFirstChildKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getLeftSiblingKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getRightSiblingKey());

    rtx.moveToFirstChild();
    TestCase.assertEquals(14, rtx.getNameKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(false, rtx.hasLeftSibling());
    TestCase.assertEquals(true, rtx.hasRightSibling());
    TestCase.assertEquals(3L, rtx.getNodeKey());
    TestCase.assertEquals(2L, rtx.getParentKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getFirstChildKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getLeftSiblingKey());
    TestCase.assertEquals(4L, rtx.getRightSiblingKey());

    rtx.moveToRightSibling();
    TestCase.assertEquals(15, rtx.getNameKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(true, rtx.hasLeftSibling());
    TestCase.assertEquals(true, rtx.hasRightSibling());
    TestCase.assertEquals(4L, rtx.getNodeKey());
    TestCase.assertEquals(2L, rtx.getParentKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getFirstChildKey());
    TestCase.assertEquals(3L, rtx.getLeftSiblingKey());
    TestCase.assertEquals(5L, rtx.getRightSiblingKey());

    rtx.moveToRightSibling();
    TestCase.assertEquals(16, rtx.getNameKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(true, rtx.hasFirstChild());
    TestCase.assertEquals(true, rtx.hasLeftSibling());
    TestCase.assertEquals(false, rtx.hasRightSibling());
    TestCase.assertEquals(5L, rtx.getNodeKey());
    TestCase.assertEquals(2L, rtx.getParentKey());
    TestCase.assertEquals(7L, rtx.getFirstChildKey());
    TestCase.assertEquals(4L, rtx.getLeftSiblingKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getRightSiblingKey());

    rtx.moveToFirstChild();
    TestCase.assertEquals(18, rtx.getNameKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(false, rtx.hasLeftSibling());
    TestCase.assertEquals(true, rtx.hasRightSibling());
    TestCase.assertEquals(7L, rtx.getNodeKey());
    TestCase.assertEquals(5L, rtx.getParentKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getFirstChildKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getLeftSiblingKey());
    TestCase.assertEquals(6L, rtx.getRightSiblingKey());

    rtx.moveToRightSibling();
    TestCase.assertEquals(17, rtx.getNameKey());
    TestCase.assertEquals(true, rtx.hasParent());
    TestCase.assertEquals(false, rtx.hasFirstChild());
    TestCase.assertEquals(true, rtx.hasLeftSibling());
    TestCase.assertEquals(false, rtx.hasRightSibling());
    TestCase.assertEquals(6L, rtx.getNodeKey());
    TestCase.assertEquals(5L, rtx.getParentKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getFirstChildKey());
    TestCase.assertEquals(7L, rtx.getLeftSiblingKey());
    TestCase.assertEquals(IReadTransaction.NULL_NODE_KEY, rtx
        .getRightSiblingKey());

    rtx.close();

    session.close();
  }

}

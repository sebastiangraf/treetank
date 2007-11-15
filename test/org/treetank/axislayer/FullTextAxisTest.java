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

import static org.junit.Assert.assertEquals;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.UTF;

public class FullTextAxisTest {

  public static final String PATH =
      "generated" + File.separator + "FullTextAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testIterate() {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final long nodeKey1 = wtx.insertTextAsFirstChild(UTF.getBytes("foo"));
    final long nodeKey2 = wtx.insertTextAsRightSibling(UTF.getBytes("foo"));
    final long tokenKey1 = wtx.index("foo", nodeKey1);
    wtx.commit();
    final long tokenKey2 = wtx.index("foo", nodeKey2);
    assertEquals(tokenKey1, tokenKey2);

    // Verify axis
    final long key1 = wtx.getNodeKey();
    final IAxis axis1 = new FullTextAxis(wtx, "foo");
    assertEquals(true, axis1.hasNext());
    assertEquals(nodeKey1, axis1.next());
    assertEquals(true, axis1.hasNext());
    assertEquals(nodeKey2, axis1.next());
    assertEquals(false, axis1.hasNext());
    assertEquals(key1, wtx.getNodeKey());

    final long key2 = wtx.getNodeKey();
    final IAxis axis2 = new FullTextAxis(wtx, "bar");
    assertEquals(false, axis2.hasNext());
    assertEquals(key2, wtx.getNodeKey());

    final long key3 = wtx.getNodeKey();
    final IAxis axis3 = new FullTextAxis(wtx, "ba");
    assertEquals(false, axis3.hasNext());
    assertEquals(key3, wtx.getNodeKey());

    final long key4 = wtx.getNodeKey();
    final IAxis axis4 = new FullTextAxis(wtx, "fo");
    assertEquals(false, axis4.hasNext());
    assertEquals(key4, wtx.getNodeKey());

    try {
      final long key5 = wtx.getNodeKey();
      final IAxis axis5 = new FullTextAxis(wtx, null);
      assertEquals(false, axis5.hasNext());
      assertEquals(key5, wtx.getNodeKey());
      TestCase.fail();
    } catch (Exception e) {
      // Must catch.
    }

    try {
      final long key6 = wtx.getNodeKey();
      final IAxis axis6 = new FullTextAxis(wtx, "");
      assertEquals(false, axis6.hasNext());
      assertEquals(key6, wtx.getNodeKey());
      TestCase.fail();
    } catch (Exception e) {
      // Must catch.
    }

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testWildcard() {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final long nodeKey1 = wtx.insertTextAsFirstChild(UTF.getBytes("foo"));
    final long nodeKey2 = wtx.insertTextAsRightSibling(UTF.getBytes("foo"));
    final long tokenKey1 = wtx.index("foo", nodeKey1);
    wtx.commit();
    final long tokenKey2 = wtx.index("foo", nodeKey2);
    assertEquals(tokenKey1, tokenKey2);
    final long tokenKey3 = wtx.index("bar", nodeKey2);

    // Verify axis
    final long key1 = wtx.getNodeKey();
    final IAxis axis1 = new FullTextAxis(wtx, "f*");
    assertEquals(true, axis1.hasNext());
    assertEquals(nodeKey1, axis1.next());
    assertEquals(true, axis1.hasNext());
    assertEquals(nodeKey2, axis1.next());
    assertEquals(false, axis1.hasNext());
    assertEquals(key1, wtx.getNodeKey());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testFullSuffixWildcard() {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final long nodeKey1 = wtx.insertTextAsFirstChild(UTF.getBytes("foo"));
    final long nodeKey2 = wtx.insertTextAsRightSibling(UTF.getBytes("foo"));
    final long tokenKey1 = wtx.index("foo", nodeKey1);
    wtx.commit();
    final long tokenKey2 = wtx.index("foo", nodeKey2);
    assertEquals(tokenKey1, tokenKey2);

    // Verify axis
    final long key1 = wtx.getNodeKey();
    final IAxis axis1 = new FullTextAxis(wtx, "foo*");
    assertEquals(true, axis1.hasNext());
    assertEquals(nodeKey1, axis1.next());
    assertEquals(true, axis1.hasNext());
    assertEquals(nodeKey2, axis1.next());
    assertEquals(false, axis1.hasNext());
    assertEquals(key1, wtx.getNodeKey());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testFullWildcard() {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final long nodeKey1 = wtx.insertTextAsFirstChild(UTF.getBytes("foo"));
    final long nodeKey2 = wtx.insertTextAsRightSibling(UTF.getBytes("foo"));
    final long tokenKey1 = wtx.index("foo", nodeKey1);
    wtx.commit();
    final long tokenKey2 = wtx.index("foo", nodeKey2);
    assertEquals(tokenKey1, tokenKey2);

    // Verify axis
    final long key1 = wtx.getNodeKey();
    final IAxis axis1 = new FullTextAxis(wtx, "*");
    assertEquals(true, axis1.hasNext());
    assertEquals(nodeKey1, axis1.next());
    assertEquals(true, axis1.hasNext());
    assertEquals(nodeKey2, axis1.next());
    assertEquals(false, axis1.hasNext());
    assertEquals(key1, wtx.getNodeKey());

    wtx.abort();
    wtx.close();
    session.close();

  }

}

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
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;

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
    final long nodeKey1 = wtx.insertTextAsFirstChild("foo");
    final long nodeKey2 = wtx.insertTextAsRightSibling("foo");
    final long tokenKey1 = wtx.insertToken("foo", nodeKey1);
    wtx.commit();
    final long tokenKey2 = wtx.insertToken("foo", nodeKey2);
    assertEquals(tokenKey1, tokenKey2);

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FullTextAxis(wtx, "foo"), new long[] {
        nodeKey1,
        nodeKey2 });

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FullTextAxis(wtx, "bar"), new long[] {});

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FullTextAxis(wtx, "ba"), new long[] {});

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FullTextAxis(wtx, "fo"), new long[] {});

    try {
      wtx.moveToDocumentRoot();
      IAxisTest.testIAxisConventions(new FullTextAxis(wtx, null), new long[] {
          nodeKey1,
          nodeKey2 });
      TestCase.fail("Token of FullTextAxis() must not be null.");
    } catch (Exception e) {
      // Must catch.
    }

    try {
      wtx.moveToDocumentRoot();
      IAxisTest.testIAxisConventions(new FullTextAxis(wtx, ""), new long[] {
          nodeKey1,
          nodeKey2 });
      TestCase.fail("Token of FullTextAxis() must not be empty.");
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
    final long nodeKey1 = wtx.insertTextAsFirstChild("foo");
    final long nodeKey2 = wtx.insertTextAsRightSibling("foo");
    final long tokenKey1 = wtx.insertToken("foo", nodeKey1);
    wtx.commit();
    final long tokenKey2 = wtx.insertToken("foo", nodeKey2);
    assertEquals(tokenKey1, tokenKey2);
    final long tokenKey3 = wtx.insertToken("bar", nodeKey2);

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FullTextAxis(wtx, "f*"), new long[] {
        nodeKey1,
        nodeKey2 });

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FullTextAxis(wtx, "x*"), new long[] {});

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testFullSuffixWildcard() {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final long nodeKey1 = wtx.insertTextAsFirstChild("foo");
    final long nodeKey2 = wtx.insertTextAsRightSibling("foo");
    final long tokenKey1 = wtx.insertToken("foo", nodeKey1);
    wtx.commit();
    final long tokenKey2 = wtx.insertToken("foo", nodeKey2);
    assertEquals(tokenKey1, tokenKey2);

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FullTextAxis(wtx, "foo*"), new long[] {
        nodeKey1,
        nodeKey2 });

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testFullWildcard() {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final long nodeKey1 = wtx.insertTextAsFirstChild("foo");
    final long nodeKey2 = wtx.insertTextAsRightSibling("foo");
    final long tokenKey1 = wtx.insertToken("foo", nodeKey1);
    wtx.commit();
    final long tokenKey2 = wtx.insertToken("foo", nodeKey2);
    assertEquals(tokenKey1, tokenKey2);

    wtx.moveToDocumentRoot();
    IAxisTest.testIAxisConventions(new FullTextAxis(wtx, "*"), new long[] {
        nodeKey1,
        nodeKey2 });

    wtx.abort();
    wtx.close();
    session.close();

  }

}

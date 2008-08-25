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
import org.treetank.api.IAxis;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.DocumentTest;

public class NestedAxisTest {

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "NestedAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testNestedAxisTest() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();

    // XPath expression /p:a/b/text()
    // Part: /p:a
    final IAxis childA =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));
    // Part: /b
    final IAxis childB =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "b"));
    // Part: /text()
    final IAxis text = new FilterAxis(new ChildAxis(wtx), new TextFilter(wtx));
    // Part: /p:a/b/text()
    final IAxis axis = new NestedAxis(new NestedAxis(childA, childB), text);

    IAxisTest.testIAxisConventions(axis, new long[] { 5L, 10L });

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testNestedAxisTest2() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();

    // XPath expression /[:a/b/@p:x]
    // Part: /p:a
    final IAxis childA =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));
    // Part: /b
    final IAxis childB =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "b"));
    // Part: /@x
    final IAxis attributeX =
        new FilterAxis(new AttributeAxis(wtx), new NameFilter(wtx, "p:x"));
    // Part: /p:a/b/@p:x
    final IAxis axis =
        new NestedAxis(new NestedAxis(childA, childB), attributeX);

    IAxisTest.testIAxisConventions(axis, new long[] { 8L });

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testNestedAxisTest3() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);

    // Find desceFndants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();

    // XPath expression p:a/node():
    // Part: /p:a
    final IAxis childA =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));

    // Part: /node()
    final IAxis childNode =
        new FilterAxis(new ChildAxis(wtx), new NodeFilter(wtx));

    // Part: /p:a/node():
    final IAxis axis = new NestedAxis(childA, childNode);

    IAxisTest.testIAxisConventions(axis, new long[] { 3L, 4L, 7L, 8L, 11L });

    wtx.abort();
    wtx.close();
    session.close();

  }

}

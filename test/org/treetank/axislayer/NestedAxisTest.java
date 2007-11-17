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
import org.treetank.api.IAxis;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class NestedAxisTest {

  public static final String PATH =
      "generated" + File.separator + "ChainedAxisTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testChainedAxisTest() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();

    // XPath expression /a/b/text():
    // Part: /a
    final IAxis childA =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx.keyForName("a")));
    // Part: /b
    final IAxis childB =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx.keyForName("b")));
    // Part: /text()
    final IAxis text = new FilterAxis(new ChildAxis(wtx), new TextFilter());
    // Part: /a/b/text():
    final IAxis axis = new NestedAxis(new NestedAxis(childA, childB), text);

    IAxisTest.testIAxisConventions(axis, new long[] { 5L, 10L });

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testChainedAxisTest2() {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();

    // XPath expression /a/b/@x:
    // Part: /a
    final IAxis childA =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx.keyForName("a")));
    // Part: /b
    final IAxis childB =
        new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx.keyForName("b")));
    // Part: /@x
    final IAxis attributeX =
        new FilterAxis(new AttributeAxis(wtx), new NameFilter(wtx
            .keyForName("x")));
    // Part: /a/b/@x:
    final IAxis axis =
        new NestedAxis(new NestedAxis(childA, childB), attributeX);

    IAxisTest.testIAxisConventions(axis, new long[] { 8L });

    wtx.abort();
    wtx.close();
    session.close();

  }

}

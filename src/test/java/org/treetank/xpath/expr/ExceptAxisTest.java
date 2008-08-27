/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
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

package org.treetank.xpath.expr;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.IAxisTest;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.DocumentTest;
import org.treetank.xpath.XPathAxis;

/**
 * JUnit-test class to test the functionality of the UnionAxis.
 * 
 * @author Tina Scherer
 * 
 */
public class ExceptAxisTest {

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "ExceptAxisTest.tnk";

  @Test
  public void testExcept() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction();

    rtx.moveTo(1L);

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "child::node() except b"),
        new long[] { 4L, 8L, 13L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "child::node() except child::node()[attribute::p:x]"), new long[] {
        4L,
        5L,
        8L,
        13L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "child::node()/parent::node() except self::node()"), new long[] {});

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//node() except //text()"), new long[] { 1L, 5L, 9L, 7L, 11L });

    rtx.moveTo(1L);
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "b/preceding::node() except text()"), new long[] { 7L, 6L, 5L });

    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}

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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.IAxisTest;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.DocumentTest;
import org.treetank.xpath.XPathAxis;

/**
 * JUnit-test class to test the functionality of the DubFilter.
 * 
 * @author Tina Scherer
 * 
 */
public class ForAxisTest {

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "ForAxisTest.tnk";

  IAxis ifExpr, thenExpr, elseExpr;

  @Before
  public void setUp() {

    Session.removeSession(PATH);

  }

  @Test
  public void testFor() throws IOException {

    // Build simple test tree.
    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);
    IReadTransaction rtx = session.beginReadTransaction();

    rtx.moveTo(1L);

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "for $a in child::text() return child::node()"), new long[] {
        4L,
        5L,
        8L,
        9L,
        13L,
        4L,
        5L,
        8L,
        9L,
        13L,
        4L,
        5L,
        8L,
        9L,
        13L});

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "for $a in child::node() return $a/node()"), new long[] {
        6L,
        7L,
        11L,
        12L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "for $a in child::node() return $a/text()"), new long[] { 6L, 12L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "for $a in child::node() return $a/c"), new long[] { 7L, 11L });

    //    IAxisTest.testIAxisConventions(new XPathAxis(
    //        rtx, "for $a in child::node(), $b in /node(), $c in ., $d in /c return $a/c"), 
    //        new long[] {7L, 11L});

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "for $a in child::node() return $a[@p:x]"), new long[] { 9L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "for $a in . return $a"),
        new long[] { 1L });

    IAxis axis =
        new XPathAxis(rtx, "for $i in (10, 20), $j in (1, 2) return ($i + $j)");
    assertEquals(true, axis.hasNext());

    assertEquals("11.0", rtx.getValue());
    assertEquals(true, axis.hasNext());
    assertEquals("12.0", rtx.getValue());
    assertEquals(true, axis.hasNext());
    assertEquals("21.0", rtx.getValue());
    assertEquals(true, axis.hasNext());
    assertEquals("22.0", rtx.getValue());
    assertEquals(false, axis.hasNext());

    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();

  }

}

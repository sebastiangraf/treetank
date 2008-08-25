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

package org.treetank.xpath;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.IAxisTest;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.DocumentTest;

/**
 * JUnit-test class to test the functionality of the XPathAxis.
 * 
 * @author Tina Scherer
 */
public class XPathAxisTest {

  public static final String PATH =
      "target" + File.separator + "tnk" + File.separator + "XPathAxisTest.tnk";

  ISession session;

  IWriteTransaction wtx;

  IReadTransaction rtx;

  @Before
  public void setUp() {

    Session.removeSession(PATH);

    // Build simple test tree.
    session = Session.beginSession(PATH);
    wtx = session.beginWriteTransaction();
    DocumentTest.create(wtx);

    // Find descendants starting from nodeKey 0L (root).
    wtx.moveToDocumentRoot();
    rtx = session.beginReadTransaction(new ItemList());
  }

  @After
  public void tearDown() {

    rtx.close();
    wtx.abort();
    wtx.close();
    session.close();
  }

  @Test
  public void testSteps() throws IOException {

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "/text:p/b"),
        new long[] {});

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b"), new long[] {
        4L,
        8L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/c"), new long[] {
        6L,
        9L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "/p:a"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "child::p:a/child::b"),
        new long[] { 4L, 8L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::p:"
        + "a/child::b/child::c"), new long[] { 6L, 9L });

  }

  @Test
  public void testAttributes() throws IOException {

    // Find descendants starting from nodeKey 0L (root).
    rtx.moveToDocumentRoot();

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "/p:a[@i]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "/p:a/@i"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a/b[@p:x]"),
        new long[] { 8L });

    XPathStringTest.testIAxisConventions(new XPathAxis(
        rtx,
        "descendant-or-self::node()/@p:x = 'y'"), new String[] { "true" });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[text()]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[element()]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[node()/text()]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[./node()]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "p:a[./node()/node()/node()]"), new long[] {});

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[//element()]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[/text()]"),
        new long[] {});

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[16<65]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[13>=4]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[13.0>=4]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[4 = 4]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[3=4]"),
        new long[] {});

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[3.2 = 3.22]"),
        new long[] {});

    // TODO:error with XPath 1.0 compatibility because one operand is parsed to
    // double
    // and with no compatibility error, because value can not be converted to
    // string
    // from the byte array
    // IAxisTest.testIAxisConventions(new XPathAxis(
    // rtx, "p:a[(3.2 + 0.02) = 3.22]"), new long[] {2L});

    // TODO: this is not working yet, because type is untyped -> ruls for cast
    // to double
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[@i = \"j\"]"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "descendant-or-self::node()[@p:x = \"y\"]"), new long[] { 8L });

    // IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[@i eq \"j\"]"),
    // new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a[@i=\"k\"]"),
        new long[] {});

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a/b[@p:x=\"y\"]"),
        new long[] { 8L });

  }

  @Test
  public void testNodeTests() throws IOException {

    // Find descendants starting from nodeKey 0L (root).
    rtx.moveToDocumentRoot();

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "/p:a/node()"),
        new long[] { 3L, 4L, 7L, 8L, 11L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a/text()"),
        new long[] { 3L, 7L, 11L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "/p:a/b/text()"),
        new long[] { 5L, 10L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a/b/node()"),
        new long[] { 5L, 6L, 9L, 10L });

  }

  @Test
  public void testDescendant() throws IOException {

    // Find descendants starting from nodeKey 0L (root).
    rtx.moveToDocumentRoot();

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a//b"), new long[] {
        4L,
        8L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//p:a"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "descendant-or-self::p:a"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "/p:a/descendant-or-self::b"), new long[] { 4L, 8L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a/descendant::b"),
        new long[] { 4L, 8L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "p:a/descendant::p:a"),
        new long[] {});

  }

  @Test
  public void testAncestor() throws IOException {

    // Find ancestor starting from nodeKey 8L.
    rtx.moveTo(8L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "ancestor::p:a"),
        new long[] { 2L });

    rtx.moveTo(10L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "ancestor::p:a"),
        new long[] { 2L });

    rtx.moveTo(10L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "ancestor::node()"),
        new long[] { 8L, 2L });

    rtx.moveTo(10L);
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "ancestor-or-self::node()"), new long[] { 10L, 8L, 2L });

  }

  @Test
  public void testParent() throws IOException {

    // Find ancestor starting from nodeKey 8L.
    rtx.moveTo(8L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "parent::p:a"),
        new long[] { 2L });

    rtx.moveTo(10L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "parent::b"),
        new long[] { 8L });

    rtx.moveTo(10L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "parent::node()"),
        new long[] { 8L });

    rtx.moveTo(11L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "parent::node()"),
        new long[] { 2L });
  }

  @Test
  public void testSelf() throws IOException {

    // Find ancestor starting from nodeKey 8L.
    rtx.moveTo(2L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "self::p:a"),
        new long[] { 2L });

    rtx.moveTo(8L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "self::b"),
        new long[] { 8L });

    rtx.moveTo(10L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "./node()"),
        new long[] {});

    rtx.moveTo(11L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "self::node()"),
        new long[] { 11L });

    rtx.moveTo(2L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "./b/node()"),
        new long[] { 5L, 6L, 9L, 10L });

  }

  @Test
  public void testPosition() throws IOException {

    // Find descendants starting from nodeKey 0L (root).
    rtx.moveTo(2L);

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "b"),
        new long[] { 4L, 8L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/c"), new long[] {
        6L,
        9L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/text()"), new long[] {
        5L,
        10L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/c"), new long[] {
        6L,
        9L });

  }

  //
  @Test
  public void testDupElemination() throws IOException {

    // Find descendants starting from nodeKey 0L (root).
    rtx.moveTo(2L);

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "child::node()/parent::node()"), new long[] { 2L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/c"), new long[] {
        6L,
        9L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/text()"), new long[] {
        5L,
        10L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/c"), new long[] {
        6L,
        9L });

  }

  @Test
  public void testUnabbreviate() throws IOException {

    // Find descendants starting from nodeKey 0L (root).
    rtx.moveTo(2L);

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::b"), new long[] {
        4L,
        8L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::*"), new long[] {
        4L,
        8L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "child::text()"),
        new long[] { 3L, 7L, 11L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "attribute::i"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "attribute::*"),
        new long[] { 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "parent::node()"),
        new long[] { 0L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "self::blau"),
        new long[] {});

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/"), new long[] { 0L });

    //     IAxisTest.testIAxisConventions(new XPathAxis(rtx,
    //     "child::b[fn:position() = 1]"), new long[] { 4L });
    //
    // // IAxisTest.testIAxisConventions(new XPathAxis(
    // rtx, "child::b[fn:position() = fn:last()]"), new long[] {8L});
    //  
    // IAxisTest.testIAxisConventions(new XPathAxis(
    // rtx, "child::b[fn:position() = fn:last()-1]"), new long[] {4L});
    //  
    //     IAxisTest.testIAxisConventions(new XPathAxis(rtx,
    //     "child::b[fn:position() > 1]"), new long[] { 8L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "child::b[attribute::p:x = \"y\"]"), new long[] { 8L });

    //    IAxisTest.testIAxisConventions(new XPathAxis(rtx,
    //        "child::b[attribute::p:x = \"y\"][fn:position() = 1]"),
    //        new long[] { 8L });

    //    IAxisTest.testIAxisConventions(new XPathAxis(rtx,
    //        "child::b[attribute::p:x = \"y\"][1]"), new long[] { 8L });

    //     IAxisTest.testIAxisConventions(new XPathAxis(rtx,
    //     "child::b[attribute::p:x = \"y\"][fn:position() = 3]"), new long[] {});

    //     IAxisTest.testIAxisConventions(new XPathAxis(rtx,
    //     "child::b[attribute::p:x = \"y\"][3]"), new long[] {});

    //     IAxisTest.testIAxisConventions(new XPathAxis(rtx,
    //     "child::b[fn:position() = 2][attribute::p:x = \"y\"]"),
    //     new long[] { 8L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "child::b[child::c]"),
        new long[] { 4L, 8L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "child::*[text() or c]"),
        new long[] { 4l, 8L });

    // IAxisTest.testIAxisConventions(new XPathAxis(
    // rtx, "child::*[text() or c][fn:position() = fn:last()]"), new long[]
    // {8L});

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "child::*[text() or c], /node(), //c"),
        new long[] { 4l, 8L, 2L, 6L, 9L });

  }

  @Test
  public void testMultiExpr() throws IOException {

    rtx.moveTo(2L);

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b, b, b"), new long[] {
        4L,
        8L,
        4L,
        8L,
        4L,
        8L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "b/c, ., //c"),
        new long[] { 6L, 9L, 2L, 6L, 9L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "b/text(), //text(), descendant-or-self::element()"), new long[] {
        5L,
        10L,
        3L,
        7L,
        11L,
        5L,
        10L,
        2L,
        4L,
        6L,
        8L,
        9L });

    rtx.moveTo(4L);
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "/p:a/b/c, ., .., .//text()"), new long[] { 6L, 9L, 4L, 2L, 5L });

  }

  @Test
  public void testCount() throws IOException {

    rtx.moveTo(2L);

    XPathStringTest.testIAxisConventions(new XPathAxis(
        rtx,
        "fn:count(//node())"), new String[] { "10" });

  }

}
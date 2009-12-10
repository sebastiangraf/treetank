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
 * $Id: XPathAxisTest.java 4433 2008-08-28 14:26:02Z scherer $
 */

package com.treetank.service.xml.xpath;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.access.Session;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.IAxisTest;
import com.treetank.exception.TreetankException;
import com.treetank.utils.DocumentCreater;

/**
 * JUnit-test class to test the functionality of the XPathAxis.
 * 
 * @author Tina Scherer
 */
public class XPathAxisTest {

    private ISession session;

    private IWriteTransaction wtx;

    private IReadTransaction rtx;

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();

        // Build simple test tree.
        session = Session.beginSession(ITestConstants.PATH1);
        wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();
        // Find descendants starting from nodeKey 0L (root).
        rtx = session.beginReadTransaction();
    }

    @After
    public void tearDown() throws TreetankException {

        rtx.close();
        session.close();
        TestHelper.closeEverything();
    }

    @Test
    public void testSteps() {

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/text:p/b"),
                new long[] {});

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b"),
                new long[] { 5L, 9L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/c"),
                new long[] { 7L, 11L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(
                new XPathAxis(rtx, "child::p:a/child::b"),
                new long[] { 5L, 9L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::p:"
                + "a/child::b/child::c"), new long[] { 7L, 11L });

    }

    @Test
    public void testAttributes() {

        // Find descendants starting from nodeKey 0L (root).
        rtx.moveToDocumentRoot();

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a[@i]"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/@i"),
                new long[] { 2L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/@i/@*"),
                new long[] {});

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/b[@p:x]"),
                new long[] { 9L });

        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
                "descendant-or-self::node()/@p:x = 'y'"),
                new String[] { "true" });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[text()]"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[element()]"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(
                new XPathAxis(rtx, "p:a[node()/text()]"), new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[./node()]"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "p:a[./node()/node()/node()]"), new long[] {});

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[//element()]"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[/text()]"),
                new long[] {});

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[16<65]"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[13>=4]"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[13.0>=4]"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[4 = 4]"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[3=4]"),
                new long[] {});

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[3.2 = 3.22]"),
                new long[] {});

        // TODO:error with XPath 1.0 compatibility because one operand is parsed
        // to
        // double
        // and with no compatibility error, because value can not be converted
        // to
        // string
        // from the byte array
        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "p:a[(3.2 + 0.02) = 3.22]"), new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[@i = \"j\"]"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "descendant-or-self::node()[@p:x = \"y\"]"), new long[] { 9L });

        // IAxisTest.testIAxisConventions(new XPathAxis(rtx,
        // "p:a[@i eq \"j\"]"),
        // new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[@i=\"k\"]"),
                new long[] {});

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/b[@p:x=\"y\"]"),
                new long[] { 9L });

    }

    @Test
    public void testNodeTests() {
        // Find descendants starting from nodeKey 0L (root).
        rtx.moveToDocumentRoot();

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/node()"),
                new long[] { 4L, 5L, 8L, 9L, 13L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/text()"),
                new long[] { 4L, 8L, 13L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/text()"),
                new long[] { 6L, 12L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/b/node()"),
                new long[] { 6L, 7L, 11L, 12L });

    }

    @Test
    public void testDescendant() {

        // Find descendants starting from nodeKey 0L (root).
        rtx.moveToDocumentRoot();

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a//b"),
                new long[] { 5L, 9L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//p:a"),
                new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "descendant-or-self::p:a"), new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "/p:a/descendant-or-self::b"), new long[] { 5L, 9L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/descendant::b"),
                new long[] { 5L, 9L });

        IAxisTest.testIAxisConventions(
                new XPathAxis(rtx, "p:a/descendant::p:a"), new long[] {});

    }

    @Test
    public void testAncestor() {

        // Find ancestor starting from nodeKey 8L.
        rtx.moveTo(11L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "ancestor::p:a"),
                new long[] { 1L });

        rtx.moveTo(13L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "ancestor::p:a"),
                new long[] { 1L });

        rtx.moveTo(11L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "ancestor::node()"),
                new long[] { 9L, 1L });

        rtx.moveTo(11L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "ancestor-or-self::node()"), new long[] { 11L, 9L, 1L });

    }

    @Test
    public void testParent() {

        // Find ancestor starting from nodeKey 8L.
        rtx.moveTo(9L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "parent::p:a"),
                new long[] { 1L });

        rtx.moveTo(11L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "parent::b"),
                new long[] { 9L });

        rtx.moveTo(11L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "parent::node()"),
                new long[] { 9L });

        rtx.moveTo(13L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "parent::node()"),
                new long[] { 1L });
    }

    @Test
    public void testSelf() {

        // Find ancestor starting from nodeKey 8L.
        rtx.moveTo(1L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "self::p:a"),
                new long[] { 1L });

        rtx.moveTo(9L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "self::b"),
                new long[] { 9L });

        rtx.moveTo(11L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "./node()"),
                new long[] {});

        rtx.moveTo(11L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "self::node()"),
                new long[] { 11L });

        rtx.moveTo(1L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "./b/node()"),
                new long[] { 6L, 7L, 11L, 12L });

    }

    @Test
    public void testPosition() {

        // Find descendants starting from nodeKey 0L (root).
        rtx.moveTo(1L);

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b"), new long[] {
                5L, 9L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/c"), new long[] {
                7L, 11L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/text()"),
                new long[] { 6L, 12L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/c"),
                new long[] { 7L, 11L });

    }

    //
    @Test
    public void testDupElemination() {

        // Find descendants starting from nodeKey 0L (root).
        rtx.moveTo(1L);

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "child::node()/parent::node()"), new long[] { 1L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/c"), new long[] {
                7L, 11L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/text()"),
                new long[] { 6L, 12L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/c"),
                new long[] { 7L, 11L });

    }

    @Test
    public void testUnabbreviate() {

        // Find descendants starting from nodeKey 0L (root).
        rtx.moveTo(1L);

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::b"),
                new long[] { 5L, 9L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::*"),
                new long[] { 5L, 9L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::text()"),
                new long[] { 4L, 8L, 13L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "attribute::i"),
                new long[] { 2L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "attribute::*"),
                new long[] { 2L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "parent::node()"),
                new long[] { 0L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "self::blau"),
                new long[] {});

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/"),
                new long[] { 0L });

        // IAxisTest.testIAxisConventions(new XPathAxis(rtx,
        // "child::b[fn:position() = 1]"), new long[] { 4L });
        //
        // // IAxisTest.testIAxisConventions(new XPathAxis(
        // rtx, "child::b[fn:position() = fn:last()]"), new long[] {8L});
        //  
        // IAxisTest.testIAxisConventions(new XPathAxis(
        // rtx, "child::b[fn:position() = fn:last()-1]"), new long[] {4L});
        //  
        // IAxisTest.testIAxisConventions(new XPathAxis(rtx,
        // "child::b[fn:position() > 1]"), new long[] { 8L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "child::b[attribute::p:x = \"y\"]"), new long[] { 9L });

        // IAxisTest.testIAxisConventions(new XPathAxis(rtx,
        // "child::b[attribute::p:x = \"y\"][fn:position() = 1]"),
        // new long[] { 8L });

        // IAxisTest.testIAxisConventions(new XPathAxis(rtx,
        // "child::b[attribute::p:x = \"y\"][1]"), new long[] { 8L });

        // IAxisTest.testIAxisConventions(new XPathAxis(rtx,
        // "child::b[attribute::p:x = \"y\"][fn:position() = 3]"), new long[]
        // {});

        // IAxisTest.testIAxisConventions(new XPathAxis(rtx,
        // "child::b[attribute::p:x = \"y\"][3]"), new long[] {});

        // IAxisTest.testIAxisConventions(new XPathAxis(rtx,
        // "child::b[fn:position() = 2][attribute::p:x = \"y\"]"),
        // new long[] { 8L });

        IAxisTest
                .testIAxisConventions(new XPathAxis(rtx, "child::b[child::c]"),
                        new long[] { 5L, 9L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "child::*[text() or c]"), new long[] { 5l, 9L });

        // IAxisTest.testIAxisConventions(new XPathAxis(
        // rtx, "child::*[text() or c][fn:position() = fn:last()]"), new long[]
        // {8L});

        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "child::*[text() or c], /node(), //c"), new long[] { 5l, 9L,
                1L, 7L, 11L });

    }

    @Test
    public void testMultiExpr() {

        rtx.moveTo(1L);

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b, b, b"),
                new long[] { 5L, 9L, 5L, 9L, 5L, 9L });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/c, ., //c"),
                new long[] { 7L, 11L, 1L, 7L, 11L });

        IAxisTest
                .testIAxisConventions(new XPathAxis(rtx,
                        "b/text(), //text(), descendant-or-self::element()"),
                        new long[] { 6L, 12L, 4L, 8L, 13L, 6L, 12L, 1L, 5L, 7L,
                                9L, 11L });

        rtx.moveTo(5L);
        IAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "/p:a/b/c, ., .., .//text()"),
                new long[] { 7L, 11L, 5L, 1L, 6L });

    }

    @Test
    public void testCount() throws IOException {

        rtx.moveTo(1L);

        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
                "fn:count(//node())"), new String[] { "10" });

    }

}
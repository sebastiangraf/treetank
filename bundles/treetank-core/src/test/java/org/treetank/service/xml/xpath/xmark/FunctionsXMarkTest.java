/*
 * Copyright (c) 2010, Patrick Lang (Master Project), University of Konstanz
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
 */
package org.treetank.service.xml.xpath.xmark;

import java.io.File;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTXPathException;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.service.xml.xpath.XPathStringChecker;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class performs tests for XQuery functions used for XMark bench test and
 * XPathMark bench test.
 * 
 * @author Patrick Lang, Konstanz University
 * 
 */
public class FunctionsXMarkTest {
    /** XML file name to test. */
    private static final String XMLFILE = "10mb.xml";
    /** Path to XML file. */
    private static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + XMLFILE;
    /** IDatabase instance. */
    private IDatabase mDatabase;
    /** ISession instance. */
    private ISession mSession;
    /** IReadTranscation instance. */
    private IReadTransaction mRtx;

    /**
     * Constructor, just to meet checkstyle requirements.
     */
    public FunctionsXMarkTest() {

    }

    /**
     * Method is called once before each test. It deletes all states, shreds XML file to database and
     * initializes the required variables.
     */
    @Ignore
    @Before
    public final void setUp() {
        try {
            TestHelper.deleteEverything();
            XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());
            mDatabase = TestHelper.getDatabase(PATHS.PATH1.getFile());
            mSession = mDatabase.getSession();
            mRtx = mSession.beginReadTransaction();
        } catch (final Exception mExe) {
            mExe.printStackTrace();
        }
    }

    /**
     * Test function string().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testString() throws TTXPathException {
        final String query = "fn:string(/site/people/person[@id=\"person3\"]/name)";
        final String result = "Limor Simone";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test comment.
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testComment() throws TTXPathException {
        final String query = "2 (: this is a comment :)";
        final String result = "2";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function node().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testNode() throws TTXPathException {
        final String query = "for $b in /site/people/person[@id=\"person1\"] return $b/name/node()";
        final String result = "Keung Yetim";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function text().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testText() throws TTXPathException {
        final String query = "for $b in /site/people/person[@id=\"person0\"] return $b/name/text()";
        final String result = "Krishna Merle";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function count().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testCount() throws TTXPathException {
        final String query =
            "fn:count(for $i in /site/closed_auctions/closed_auction[price/text() >= 40] return $i/price)";
        final String result = "670";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function position().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testPosition() throws TTXPathException {
        final String query = "/site/open_auctions/open_auction/bidder/increase[position()=1]";
        final String result = "<increase>10.50</increase>";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function not().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testNot() throws TTXPathException {
        final String query = "/site/people/person[not(homepage)][@id=\"person1\"]/name/text()";
        final String result = "<name>Keung Yetim</name>";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function id().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testId() throws TTXPathException {
        final String query = "fn:id(/site/people/person[@id=\"person1\"]/watches/watch/@open_auction)";
        final String result = "";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function data().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testData() throws TTXPathException {
        final String query = "for $b in /site/people/person[@id=\"person0\"] return fn:data($b/name)";
        final String result = "Krishna Merle";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function contains().
     * 
     * @throws TTXPathException
     */
    @Ignore
    @Test
    public final void testContains() throws TTXPathException {
        final String query =
            "/site/regions/*/item[contains(description,\"gold\")]/location[text()=\"El Salvador\"]";
        final String result = "<location>El Salvador</location>";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function exactly-one().
     * alternative query: exactly-one('a') -> result: a
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testExactlyOne() throws TTXPathException {
        final String query = "exactly-one(/site/people/person[@id=\"person0\"]/name)";
        final String result = "<name>Krishna Merle</name>";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function sum().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testSum() throws TTXPathException {
        final String query = "fn:sum(/site/open_auctions/open_auction/bidder/increase/text())";
        final String result = "96496.5";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function zero-or-one().
     * alternative query: zero-or-one('a') -> result: a
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testZeroOrOne() throws TTXPathException {
        final String query =
            " for $i in /site/open_auctions/open_auction return zero-or-one($i/reserve[text()=\"20.54\"]/text())";
        final String result = "20.54";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function max().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testMax() throws TTXPathException {
        final String query = "fn:max(for $i in /site/open_auctions/open_auction return $i/reserve/text())";
        final String result = "4701.79";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function min().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testMin() throws TTXPathException {
        final String query = "fn:min(for $i in /site/open_auctions/open_auction return $i/reserve/text())";
        final String result = "0.43";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function empty().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testEmpty() throws TTXPathException {
        final String query = "fn:empty(for $i in /site/open_auctions/open_auction return $i/reserve/text())";
        final String result = "false";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function one-or-more().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testOneOrMore() throws TTXPathException {
        final String query = "fn:one-or-more(\"a\")";
        final String result = "a";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function exists().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testExists() throws TTXPathException {
        final String query = "fn:exists( ('a', 'b', 'c') )";
        final String result = "true";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function substring-after().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testSubstringAfter() throws TTXPathException {
        final String query = "fn:substring-after(\"query\", \"u\")";
        final String result = "ery";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function substring-before().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testSubstringBefore() throws TTXPathException {
        final String query = "fn:substring-before(\"query\", \"r\")";
        final String result = "que";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function last().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testLast() throws TTXPathException {
        final String query = "/site/open_auctions/open_auction/reserve[last()]";
        final String result = "<reserve>539.66</reserve>";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function boolean().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testBoolean() throws TTXPathException {
        final String query = "fn:boolean(0)";
        final String result = "false";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function number().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testNumber() throws TTXPathException {
        final String query =
            "/site/open_auctions/open_auction/bidder[personref[@person=\"person2436\"]]/increase/number()";
        final String result = "12 12";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function distinct-values().
     * 
     * @throws TTXPathException
     */
    @Ignore
    @Test
    public final void testDistinctValues() throws TTXPathException {
        final String query =
            "fn:distinct-values(/site/open_auctions/open_auction/bidder[personref[@person=\"person2436\"]]/increase)";
        final String result = "12.00";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function root().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testRoot() throws TTXPathException {
        final String query = "fn:root()/site/people/person[@id=\"person0\"]/name/text()";
        final String result = "Krishna Merle";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function floor().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testFloor() throws TTXPathException {
        final String query = "fn:floor(5.7)";
        final String result = "5";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test <element attribute=""/> in return statement.
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testElementAttributeInReturn() throws TTXPathException {
        final String query =
            "for $b in /site/open_auctions/open_auction/bidder[personref[@person=\"person2436\"]]/increase return <element attribute=\"{$b/text()}\"/>";
        final String result = "<element attribute=\"12.00\"/><element attribute=\"12.00\"/>";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Close all connections.
     */
    @Ignore
    @After
    public final void tearDown() {
        try {
            mRtx.close();
            mSession.close();
            mDatabase.close();
            TestHelper.closeEverything();
        } catch (final AbsTTException mExe) {
            mExe.printStackTrace();
        }

    }

}

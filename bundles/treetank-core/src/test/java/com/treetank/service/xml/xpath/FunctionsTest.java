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

package com.treetank.service.xml.xpath;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TTException;
import com.treetank.exception.TTXPathException;
import com.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class contains test cases for not yet implemented xpath/xquery functions with test.xml file.
 * 
 * @author Patrick Lang, Konstanz University
 */
public class FunctionsTest {

    /** IReadTranscation instance. */
    private IReadTransaction mRtx;

    /**
     * Constructor, just to meet checkstyle requirements.
     */
    public FunctionsTest() {

    }

    /**
     * Method is called once before each test. It deletes all states, shreds XML file to database and
     * initializes the required variables.
     * 
     * @throws Exception
     *             of any kind
     */
    @Before
    public final void setUp() throws Exception {
        TestHelper.deleteEverything();

        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();
        mRtx = session.beginReadTransaction();
    }

    /**
     * Test function boolean().
     * @throws TTXPathException 
     */
    @Test
    public final void testBoolean() throws TTXPathException {
        final String query = "fn:boolean(0)";
        final String result = "false";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }
    
    /**
     * Test function boolean() for XPath 1.0.
     * @throws TTXPathException 
     */
    @Test
    public final void testBooleanXPath10() throws TTXPathException {
        final String query = "boolean(1)";
        final String result = "true";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }
    
    /**
     * Test function count().
     * @throws TTXPathException 
     */
    @Test
    public final void testCount() throws TTXPathException {
        final String query = "fn:count(//p:a/b)";
        final String result = "2";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function count() for XPath 1.0.
     * @throws TTXPathException 
     */
    @Test
    public final void testCountXPath10() throws TTXPathException {
        final String query = "count(//p:a/b)";
        final String result = "2";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function string().
     * @throws TTXPathException 
     */
    @Test
    public final void testString() throws TTXPathException {
        final String query = "fn:string(/p:a/b)";
        final String result = "foo bar";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function string() for XPath 1.0.
     * @throws TTXPathException 
     */
    @Test
    public final void testStringXPath10() throws TTXPathException {
        final String query = "string(/p:a/b)";
        final String result = "foo bar";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test comment.
     * @throws TTXPathException 
     */
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
    @Test
    public final void testNode() throws TTXPathException {
        final String query = "p:a[./node()/node()]";
        final String result = "{ns}a";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function text().
     * @throws TTXPathException 
     */
    @Test
    public final void testText() throws TTXPathException {
        final String query = "p:a[./text()]";
        final String result = "{ns}a";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function not().
     * @throws TTXPathException 
     */
    @Test
    public final void testNot() throws TTXPathException {
        final String query = "fn:not(//b)";
        final String result = "false";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function not() for XPath 1.0.
     * @throws TTXPathException 
     */
    @Test
    public final void testNotXPath10() throws TTXPathException {
        final String query = "not(//b)";
        final String result = "false";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function sum().
     * @throws TTXPathException 
     */
    @Test
    public final void testSum() throws TTXPathException {
        final String query = "fn:sum(5)";
        final String result = "1";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function sum() for XPath 1.0.
     * @throws TTXPathException 
     */
    @Test
    public final void testSumXPath10() throws TTXPathException {
        final String query = "sum(5)";
        final String result = "1";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function position().
     * @throws TTXPathException 
     */
    @Test
    @Ignore
    public final void testPosition() throws TTXPathException {
        final String query = "//b[position()=2]";
        final String result = "{ns}b";
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
        final String query = "//b/fn:id()";
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
        final String query = "fn:data(//b)";
        final String result = "foo bar";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function contains().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testContains() throws TTXPathException {
        final String query = "fn:contains(/p:a/b, \"\")";
        final String result = "true";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function exactly-one().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testExactlyOne() throws TTXPathException {
        final String query = "fn:exactly-one(\"a\")";
        final String result = "a";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function zero-or-one().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testZeroOrOne() throws TTXPathException {
        final String query = "fn:zero-or-one(\"a\")";
        final String result = "a";
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
        final String query = "fn:max((2, 1, 5, 4, 3))";
        final String result = "5";
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
        final String query = "fn:min((2, 1, 5, 4, 3))";
        final String result = "1";
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
        final String query = "fn:empty(/p:a)";
        final String result = "true";
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
        final String query = "fn:one-or-more(//b/c)";
        final String result = "<c xmlns:p=\"ns\"/><c xmlns:p=\"ns\"/>";
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
        final String query = "fn:exists(('a', 'b', 'c'))";
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
        final String query = "//b[last()]";
        final String result = "<b xmlns:p=\"ns\" p:x=\"y\"><c/>bar</b>";
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
        final String query = "fn:number('29.99')";
        final String result = "29.99";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Test function distinct-values().
     * @throws TTXPathException 
     */
    @Ignore
    @Test
    public final void testDistinctValues() throws TTXPathException {
        final String query = "fn:distinct-values(('a', 'a'))";
        final String result = "a";
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
        final String query = "fn:root()//c";
        final String result = "<c xmlns:p=\"ns\"/><c xmlns:p=\"ns\"/>";
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
     * 
     * @throws TTXPathException
     */
    @Ignore
    @Test
    public final void testElementAttributeInReturn() throws TTXPathException {
        final String query = "for $x in //b/text() return <element attr=\"{$x}\"/>";
        final String result = "<element attr=\"foo\"/><element attr=\"bar\"/>";
        XPathStringChecker.testIAxisConventions(new XPathAxis(mRtx, query), new String[] {
            result
        });
    }

    /**
     * Close all connections.
     */
    @After
    public final void tearDown() {
        try {
            mRtx.close();
            TestHelper.closeEverything();
        } catch (final TTException mExe) {
            mExe.printStackTrace();
        }

    }

}

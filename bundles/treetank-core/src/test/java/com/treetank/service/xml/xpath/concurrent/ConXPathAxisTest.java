package com.treetank.service.xml.xpath.concurrent;

import java.io.IOException;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.AbsAxisTest;
import com.treetank.exception.AbsTTException;
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.service.xml.xpath.XPathStringChecker;
import com.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit-test class to test the functionality of the XPathAxis.
 * 
 * @author Tina Scherer
 * @author Patrick Lang
 */
public class ConXPathAxisTest {

    private IDatabase database;

    private ISession session;

    private IWriteTransaction wtx;

    private IReadTransaction rtx;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();

        // Build simple test tree.
        database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        session = database.getSession();
        wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();
        // Find descendants starting from nodeKey 0L (root).
        rtx = session.beginReadTransaction();
    }

    @After
    public void tearDown() throws AbsTTException {

        rtx.close();
        session.close();
        database.close();
        TestHelper.closeEverything();
    }

    @Test
    public void testSteps() {

        try {

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/text:p/b"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/c"), new long[] {
                7L, 11L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::p:a/child::b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::p:" + "a/child::b/child::c"),
                new long[] {
                    7L, 11L
                });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    @Test
    public void testAttributes() {

        try {

            // Find descendants starting from nodeKey 0L (root).
            rtx.moveToDocumentRoot();

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a[@i]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/@i"), new long[] {
                2L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/@i/@*"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/b[@p:x]"), new long[] {
                9L
            });

            XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
                "descendant-or-self::node()/@p:x = 'y'"), new String[] {
                "true"
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[text()]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[element()]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[node()/text()]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[./node()]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[./node()/node()/node()]"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[//element()]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[/text()]"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[16<65]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[13>=4]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[13.0>=4]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[4 = 4]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[3=4]"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[3.2 = 3.22]"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[(3.2 + 0.02) = 3.22]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[@i = \"j\"]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "descendant-or-self::node()[@p:x = \"y\"]"),
                new long[] {
                    9L
                });

            // IAxisTest.testIAxisConventions(new XPathAxis(rtx,
            // "p:a[@i eq \"j\"]"),
            // new long[] { 1L });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a[@i=\"k\"]"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/b[@p:x=\"y\"]"), new long[] {
                9L
            });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    @Test
    public void testNodeTests() {

        try {
            // Find descendants starting from nodeKey 0L (root).
            rtx.moveToDocumentRoot();

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/node()"), new long[] {
                4L, 5L, 8L, 9L, 13L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/text()"), new long[] {
                4L, 8L, 13L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/text()"), new long[] {
                6L, 12L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/b/node()"), new long[] {
                6L, 7L, 11L, 12L
            });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    @Test
    public void testDescendant() {
        try {

            // Find descendants starting from nodeKey 0L (root).
            rtx.moveToDocumentRoot();

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a//b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "//p:a"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "descendant-or-self::p:a"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/descendant-or-self::b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/descendant::b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "p:a/descendant::p:a"), new long[] {});

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }
    }

    @Test
    public void testAncestor() {
        try {

            // Find ancestor starting from nodeKey 8L.
            rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "ancestor::p:a"), new long[] {
                1L
            });

            rtx.moveTo(13L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "ancestor::p:a"), new long[] {
                1L
            });

            rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "ancestor::node()"), new long[] {
                9L, 1L
            });

            rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "ancestor-or-self::node()"), new long[] {
                11L, 9L, 1L
            });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    @Test
    public void testParent() {
        try {
            // Find ancestor starting from nodeKey 8L.
            rtx.moveTo(9L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "parent::p:a"), new long[] {
                1L
            });

            rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "parent::b"), new long[] {
                9L
            });

            rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "parent::node()"), new long[] {
                9L
            });

            rtx.moveTo(13L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "parent::node()"), new long[] {
                1L
            });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }
    }

    @Test
    public void testSelf() {
        try {
            // Find ancestor starting from nodeKey 8L.
            rtx.moveTo(1L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "self::p:a"), new long[] {
                1L
            });

            rtx.moveTo(9L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "self::b"), new long[] {
                9L
            });

            rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "./node()"), new long[] {});

            rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "self::node()"), new long[] {
                11L
            });

            rtx.moveTo(1L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "./b/node()"), new long[] {
                6L, 7L, 11L, 12L
            });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    @Test
    public void testPosition() {
        try {
            // Find descendants starting from nodeKey 0L (root).
            rtx.moveTo(1L);

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/c"), new long[] {
                7L, 11L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/text()"), new long[] {
                6L, 12L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/c"), new long[] {
                7L, 11L
            });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    //
    @Test
    public void testDupElemination() {
        try {
            // Find descendants starting from nodeKey 0L (root).
            rtx.moveTo(1L);

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::node()/parent::node()"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/c"), new long[] {
                7L, 11L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/text()"), new long[] {
                6L, 12L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/c"), new long[] {
                7L, 11L
            });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    @Test
    public void testUnabbreviate() {
        try {
            // Find descendants starting from nodeKey 0L (root).
            rtx.moveTo(1L);

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::*"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::text()"), new long[] {
                4L, 8L, 13L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "attribute::i"), new long[] {
                2L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "attribute::*"), new long[] {
                2L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "parent::node()"), new long[] {
                0L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "self::blau"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/"), new long[] {
                0L
            });

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

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::b[attribute::p:x = \"y\"]"),
                new long[] {
                    9L
                });

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

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::b[child::c]"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::*[text() or c]"), new long[] {
                5l, 9L
            });

            // IAxisTest.testIAxisConventions(new XPathAxis(
            // rtx, "child::*[text() or c][fn:position() = fn:last()]"), new long[]
            // {8L});

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "child::*[text() or c], /node(), //c"),
                new long[] {
                    5l, 9L, 1L, 7L, 11L
                });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    @Test
    public void testMultiExpr() {
        try {
            rtx.moveTo(1L);

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "b, b, b"), new long[] {
                5L, 9L, 5L, 9L, 5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "b/c, ., //c"), new long[] {
                7L, 11L, 1L, 7L, 11L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx,
                "b/text(), //text(), descendant-or-self::element()"), new long[] {
                6L, 12L, 4L, 8L, 13L, 6L, 12L, 1L, 5L, 7L, 9L, 11L
            });

            rtx.moveTo(5L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(rtx, "/p:a/b/c, ., .., .//text()"), new long[] {
                7L, 11L, 5L, 1L, 6L
            });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    @Test
    public void testCount() throws IOException {
        try {
            rtx.moveTo(1L);

            XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, "fn:count(//node())"), new String[] {
                "10"
            });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

}

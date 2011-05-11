/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.service.xml.xpath.concurrent;

import java.io.IOException;

import org.treetank.TestHelper;
import org.treetank.axis.AbsAxisTest;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTXPathException;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.service.xml.xpath.XPathStringChecker;

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

    private AbsAxisTest.Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        TestHelper.createTestDocument();
        holder = AbsAxisTest.generateHolder();
    }

    @After
    public void tearDown() throws AbsTTException {

        holder.rtx.close();
        holder.session.close();
        TestHelper.closeEverything();
    }

    @Test
    public void testSteps() {

        try {

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/text:p/b"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a/b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a/b/c"), new long[] {
                7L, 11L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "child::p:a/child::b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "child::p:" + "a/child::b/child::c"),
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
            holder.rtx.moveToDocumentRoot();

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a[@i]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a/@i"), new long[] {
                2L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a/@i/@*"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a/b[@p:x]"), new long[] {
                9L
            });

            XPathStringChecker.testIAxisConventions(new XPathAxis(holder.rtx,
                "descendant-or-self::node()/@p:x = 'y'"), new String[] {
                "true"
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[text()]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[element()]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[node()/text()]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[./node()]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[./node()/node()/node()]"),
                new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[//element()]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[/text()]"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[16<65]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[13>=4]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[13.0>=4]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[4 = 4]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[3=4]"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[3.2 = 3.22]"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[(3.2 + 0.02) = 3.22]"),
                new long[] {
                    1L
                });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[@i = \"j\"]"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx,
                "descendant-or-self::node()[@p:x = \"y\"]"), new long[] {
                9L
            });

            // IAxisTest.testIAxisConventions(new XPathAxis(holder.rtx,
            // "p:a[@i eq \"j\"]"),
            // new long[] { 1L });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a[@i=\"k\"]"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a/b[@p:x=\"y\"]"), new long[] {
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
            holder.rtx.moveToDocumentRoot();

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a/node()"), new long[] {
                4L, 5L, 8L, 9L, 13L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a/text()"), new long[] {
                4L, 8L, 13L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a/b/text()"), new long[] {
                6L, 12L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a/b/node()"), new long[] {
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
            holder.rtx.moveToDocumentRoot();

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a//b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "//p:a"), new long[] {
                1L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "descendant-or-self::p:a"),
                new long[] {
                    1L
                });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a/descendant-or-self::b"),
                new long[] {
                    5L, 9L
                });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a/descendant::b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "p:a/descendant::p:a"), new long[] {});

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }
    }

    @Test
    public void testAncestor() {
        try {

            // Find ancestor starting from nodeKey 8L.
            holder.rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "ancestor::p:a"), new long[] {
                1L
            });

            holder.rtx.moveTo(13L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "ancestor::p:a"), new long[] {
                1L
            });

            holder.rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "ancestor::node()"), new long[] {
                9L, 1L
            });

            holder.rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "ancestor-or-self::node()"),
                new long[] {
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
            holder.rtx.moveTo(9L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "parent::p:a"), new long[] {
                1L
            });

            holder.rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "parent::b"), new long[] {
                9L
            });

            holder.rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "parent::node()"), new long[] {
                9L
            });

            holder.rtx.moveTo(13L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "parent::node()"), new long[] {
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
            holder.rtx.moveTo(1L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "self::p:a"), new long[] {
                1L
            });

            holder.rtx.moveTo(9L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "self::b"), new long[] {
                9L
            });

            holder.rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "./node()"), new long[] {});

            holder.rtx.moveTo(11L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "self::node()"), new long[] {
                11L
            });

            holder.rtx.moveTo(1L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "./b/node()"), new long[] {
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
            holder.rtx.moveTo(1L);

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "b/c"), new long[] {
                7L, 11L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "b/text()"), new long[] {
                6L, 12L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a/b/c"), new long[] {
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
            holder.rtx.moveTo(1L);

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "child::node()/parent::node()"),
                new long[] {
                    1L
                });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "b/c"), new long[] {
                7L, 11L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "b/text()"), new long[] {
                6L, 12L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a/b/c"), new long[] {
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
            holder.rtx.moveTo(1L);

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "child::b"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "child::*"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "child::text()"), new long[] {
                4L, 8L, 13L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "attribute::i"), new long[] {
                2L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "attribute::*"), new long[] {
                2L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "parent::node()"), new long[] {
                0L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "self::blau"), new long[] {});

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/"), new long[] {
                0L
            });

            // IAxisTest.testIAxisConventions(new XPathAxis(holder.rtx,
            // "child::b[fn:position() = 1]"), new long[] { 4L });
            //
            // // IAxisTest.testIAxisConventions(new XPathAxis(
            // holder.rtx, "child::b[fn:position() = fn:last()]"), new long[] {8L});
            //
            // IAxisTest.testIAxisConventions(new XPathAxis(
            // holder.rtx, "child::b[fn:position() = fn:last()-1]"), new long[] {4L});
            //
            // IAxisTest.testIAxisConventions(new XPathAxis(holder.rtx,
            // "child::b[fn:position() > 1]"), new long[] { 8L });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "child::b[attribute::p:x = \"y\"]"),
                new long[] {
                    9L
                });

            // IAxisTest.testIAxisConventions(new XPathAxis(holder.rtx,
            // "child::b[attribute::p:x = \"y\"][fn:position() = 1]"),
            // new long[] { 8L });

            // IAxisTest.testIAxisConventions(new XPathAxis(holder.rtx,
            // "child::b[attribute::p:x = \"y\"][1]"), new long[] { 8L });

            // IAxisTest.testIAxisConventions(new XPathAxis(holder.rtx,
            // "child::b[attribute::p:x = \"y\"][fn:position() = 3]"), new long[]
            // {});

            // IAxisTest.testIAxisConventions(new XPathAxis(holder.rtx,
            // "child::b[attribute::p:x = \"y\"][3]"), new long[] {});

            // IAxisTest.testIAxisConventions(new XPathAxis(holder.rtx,
            // "child::b[fn:position() = 2][attribute::p:x = \"y\"]"),
            // new long[] { 8L });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "child::b[child::c]"), new long[] {
                5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "child::*[text() or c]"), new long[] {
                5l, 9L
            });

            // IAxisTest.testIAxisConventions(new XPathAxis(
            // holder.rtx, "child::*[text() or c][fn:position() = fn:last()]"), new long[]
            // {8L});

            AbsAxisTest.testIAxisConventions(
                new XPathAxis(holder.rtx, "child::*[text() or c], /node(), //c"), new long[] {
                    5l, 9L, 1L, 7L, 11L
                });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    @Test
    public void testMultiExpr() {
        try {
            holder.rtx.moveTo(1L);

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "b, b, b"), new long[] {
                5L, 9L, 5L, 9L, 5L, 9L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "b/c, ., //c"), new long[] {
                7L, 11L, 1L, 7L, 11L
            });

            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx,
                "b/text(), //text(), descendant-or-self::element()"), new long[] {
                6L, 12L, 4L, 8L, 13L, 6L, 12L, 1L, 5L, 7L, 9L, 11L
            });

            holder.rtx.moveTo(5L);
            AbsAxisTest.testIAxisConventions(new XPathAxis(holder.rtx, "/p:a/b/c, ., .., .//text()"),
                new long[] {
                    7L, 11L, 5L, 1L, 6L
                });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

    @Test
    public void testCount() throws IOException {
        try {
            holder.rtx.moveTo(1L);

            XPathStringChecker.testIAxisConventions(new XPathAxis(holder.rtx, "fn:count(//node())"),
                new String[] {
                    "10"
                });

        } catch (final TTXPathException mExp) {
            mExp.getStackTrace();
        }

    }

}

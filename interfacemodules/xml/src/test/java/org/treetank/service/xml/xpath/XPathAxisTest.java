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

package org.treetank.service.xml.xpath;

import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.Holder;
import org.treetank.NodeHelper;
import org.treetank.NodeModuleFactory;
import org.treetank.TestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.axis.AxisTest;
import org.treetank.exception.TTException;
import org.treetank.io.IConstants;

import com.google.inject.Inject;

/**
 * JUnit-test class to test the functionality of the XPathAxis.
 * 
 * @author Tina Scherer
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public class XPathAxisTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
        Properties props = new Properties();
        props.put(IConstants.DBFILE, ResourceConfiguration.generateFileOutOfResource(
            TestHelper.PATHS.PATH1.getFile(), TestHelper.RESOURCENAME).getAbsolutePath());
        mResource = mResourceConfig.create(props, 10);
        NodeHelper.createTestDocument(mResource);
        holder =
            Holder.generateRtx(mResource);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testSteps() throws TTException {
        // Verify.

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/text:p/b"), new long[] {});

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a/b"), new long[] {
            5L, 9L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a/b/c"), new long[] {
            7L, 11L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "child::p:a/child::b"), new long[] {
            5L, 9L
        });

        AxisTest.testIAxisConventions(
            new XPathAxis(holder.getNRtx(), "child::p:" + "a/child::b/child::c"), new long[] {
                7L, 11L
            });

    }

    @Test
    public void testAttributes() throws TTException {

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a[@i]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a/@i"), new long[] {
            2L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a/@i/@*"), new long[] {});

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a/b[@p:x]"), new long[] {
            9L
        });

        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(),
            "descendant-or-self::node()/@p:x = 'y'"), new String[] {
            "true"
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[text()]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[element()]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[node()/text()]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[./node()]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[./node()/node()/node()]"),
            new long[] {});

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[//element()]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[/text()]"), new long[] {});

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[16<65]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[13>=4]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[13.0>=4]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[4 = 4]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[3=4]"), new long[] {});

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[3.2 = 3.22]"), new long[] {});

        // TODO:error with XPath 1.0 compatibility because one operand is parsed
        // to
        // double
        // and with no compatibility error, because value can not be converted
        // to
        // string
        // from the byte array
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[(3.2 + 0.02) = 3.22]"),
            new long[] {
                1L
            });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[@i = \"j\"]"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(),
            "descendant-or-self::node()[@p:x = \"y\"]"), new long[] {
            9L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a[@i=\"k\"]"), new long[] {});

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a/b[@p:x=\"y\"]"), new long[] {
            9L
        });

    }

    @Test
    public void testNodeTests() throws TTException {

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a/node()"), new long[] {
            4L, 5L, 8L, 9L, 13L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a/text()"), new long[] {
            4L, 8L, 13L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a/b/text()"), new long[] {
            6L, 12L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a/b/node()"), new long[] {
            6L, 7L, 11L, 12L
        });
    }

    @Test
    public void testDescendant() throws TTException {

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a//b"), new long[] {
            5L, 9L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "//p:a"), new long[] {
            1L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "descendant-or-self::p:a"),
            new long[] {
                1L
            });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a/descendant-or-self::b"),
            new long[] {
                5L, 9L
            });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a/descendant::b"), new long[] {
            5L, 9L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "p:a/descendant::p:a"),
            new long[] {});

    }

    @Test
    public void testAncestor() throws TTException {
        // Find ancestor starting from nodeKey 8L.
        holder.getNRtx().moveTo(11L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "ancestor::p:a"), new long[] {
            1L
        });

        holder.getNRtx().moveTo(13L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "ancestor::p:a"), new long[] {
            1L
        });

        holder.getNRtx().moveTo(11L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "ancestor::node()"), new long[] {
            9L, 1L
        });

        holder.getNRtx().moveTo(11L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "ancestor-or-self::node()"),
            new long[] {
                11L, 9L, 1L
            });
    }

    @Test
    public void testParent() throws TTException {
        // Find ancestor starting from nodeKey 8L.
        holder.getNRtx().moveTo(9L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "parent::p:a"), new long[] {
            1L
        });

        holder.getNRtx().moveTo(11L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "parent::b"), new long[] {
            9L
        });

        holder.getNRtx().moveTo(11L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "parent::node()"), new long[] {
            9L
        });

        holder.getNRtx().moveTo(13L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "parent::node()"), new long[] {
            1L
        });
    }

    @Test
    public void testSelf() throws TTException {
        holder.getNRtx().moveTo(1L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "self::p:a"), new long[] {
            1L
        });

        holder.getNRtx().moveTo(9L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "self::b"), new long[] {
            9L
        });

        holder.getNRtx().moveTo(11L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "./node()"), new long[] {});

        holder.getNRtx().moveTo(11L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "self::node()"), new long[] {
            11L
        });

        holder.getNRtx().moveTo(1L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "./b/node()"), new long[] {
            6L, 7L, 11L, 12L
        });

    }

    @Test
    public void testPosition() throws TTException {
        holder.getNRtx().moveTo(1L);

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "b"), new long[] {
            5L, 9L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "b/c"), new long[] {
            7L, 11L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "b/text()"), new long[] {
            6L, 12L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a/b/c"), new long[] {
            7L, 11L
        });

    }

    //
    @Test
    public void testDupElemination() throws TTException {
        holder.getNRtx().moveTo(1L);

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "child::node()/parent::node()"),
            new long[] {
                1L
            });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "b/c"), new long[] {
            7L, 11L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "b/text()"), new long[] {
            6L, 12L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a/b/c"), new long[] {
            7L, 11L
        });

    }

    @Test
    public void testUnabbreviate() throws TTException {
        holder.getNRtx().moveTo(1L);

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "child::b"), new long[] {
            5L, 9L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "child::*"), new long[] {
            5L, 9L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "child::text()"), new long[] {
            4L, 8L, 13L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "attribute::i"), new long[] {
            2L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "attribute::*"), new long[] {
            2L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "parent::node()"), new long[] {
            0L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "self::blau"), new long[] {});

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/"), new long[] {
            0L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "child::b[attribute::p:x = \"y\"]"),
            new long[] {
                9L
            });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "child::b[child::c]"), new long[] {
            5L, 9L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "child::*[text() or c]"),
            new long[] {
                5l, 9L
            });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(),
            "child::*[text() or c], /node(), //c"), new long[] {
            5l, 9L, 1L, 7L, 11L
        });
    }

    @Test
    public void testMultiExpr() throws TTException {
        holder.getNRtx().moveTo(1L);

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "b, b, b"), new long[] {
            5L, 9L, 5L, 9L, 5L, 9L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "b/c, ., //c"), new long[] {
            7L, 11L, 1L, 7L, 11L
        });

        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(),
            "b/text(), //text(), descendant-or-self::element()"), new long[] {
            6L, 12L, 4L, 8L, 13L, 6L, 12L, 1L, 5L, 7L, 9L, 11L
        });

        holder.getNRtx().moveTo(5L);
        AxisTest.testIAxisConventions(new XPathAxis(holder.getNRtx(), "/p:a/b/c, ., .., .//text()"),
            new long[] {
                7L, 11L, 5L, 1L, 6L
            });
    }

    @Test
    public void testCount() throws TTException {
        // Verify.
        holder.getNRtx().moveTo(1L);

        XPathStringChecker.testIAxisConventions(holder.getNRtx(), new XPathAxis(holder.getNRtx(),
            "fn:count(//node())"), new String[] {
            "10"
        });
    }

}

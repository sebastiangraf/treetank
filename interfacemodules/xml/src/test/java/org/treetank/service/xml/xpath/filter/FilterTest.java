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

package org.treetank.service.xml.xpath.filter;

import static org.testng.AssertJUnit.assertEquals;
import static org.treetank.node.IConstants.ROOT_NODE;

import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.Holder;
import org.treetank.ModuleFactory;
import org.treetank.NodeElementTestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.INodeReadTrx;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.AxisTest;
import org.treetank.axis.filter.AttributeFilter;
import org.treetank.axis.filter.ElementFilter;
import org.treetank.axis.filter.ItemFilter;
import org.treetank.axis.filter.NameFilter;
import org.treetank.axis.filter.NestedFilter;
import org.treetank.axis.filter.NodeFilter;
import org.treetank.axis.filter.TextFilter;
import org.treetank.axis.filter.TypeFilter;
import org.treetank.axis.filter.WildcardFilter;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTXPathException;
import org.treetank.service.xml.xpath.XPathAxis;

import com.google.inject.Inject;

/**
 * Test Cases for all AbsAxis-implementations
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class FilterTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeClass
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        CoreTestHelper.Holder holder = CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        NodeElementTestHelper.createTestDocument(mResource);
        this.holder = Holder.generateRtx(holder, mResource);
    }

    @AfterClass
    public void tearDown() throws TTException {
        holder.close();
        CoreTestHelper.deleteEverything();
    }

    @Test(dataProvider = "instantiateFilter")
    public void testFilter(Class<IFilterChecker> pFilterCheckerClass, IFilterChecker[] pFilterChecker)
        throws Exception {

        for (int i = 0; i < pFilterChecker.length; i++) {
            pFilterChecker[i].checkFilter(holder.getNRtx());

        }

    }

    @DataProvider(name = "instantiateFilter")
    public Object[][] instantiateFilter() throws TTByteHandleException {

        Object[][] returnVal = {
            {
                IFilterChecker.class, new IFilterChecker[] {
                    // DocumentNode Filter
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTXPathException, TTIOException {
                            pRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new DocumentNodeAxis(pRtx), new long[] {
                                ROOT_NODE
                            });

                            pRtx.moveTo(5L);
                            AxisTest.testIAxisConventions(new DocumentNodeAxis(pRtx), new long[] {
                                ROOT_NODE
                            });

                            pRtx.moveTo(9L);
                            AxisTest.testIAxisConventions(new DocumentNodeAxis(pRtx), new long[] {
                                ROOT_NODE
                            });

                            pRtx.moveTo(9L);
                            pRtx.moveToAttribute(0);
                            AxisTest.testIAxisConventions(new DocumentNodeAxis(pRtx), new long[] {
                                ROOT_NODE
                            });

                            pRtx.moveTo(13L);
                            AxisTest.testIAxisConventions(new DocumentNodeAxis(pRtx), new long[] {
                                ROOT_NODE
                            });

                        }

                    }, // DubFilter Test
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTXPathException, TTIOException {
                            pRtx.moveTo(1L);

                            AxisTest.testIAxisConventions(
                                new XPathAxis(pRtx, "child::node()/parent::node()"), new long[] {
                                    1L
                                });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "b/following-sibling::node()"),
                                new long[] {
                                    8L, 9L, 13L
                                });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "b/preceding::node()"),
                                new long[] {
                                    4L, 8L, 7L, 6L, 5L
                                });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "//c/ancestor::node()"),
                                new long[] {
                                    5L, 1L, 9L
                                });

                        }
                    }, // ItemFilter Test
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTXPathException, TTIOException {

                            pRtx.moveTo(9L);
                            org.treetank.axis.FilterTest.testFilterConventions(new ItemFilter(pRtx), true);

                            pRtx.moveTo(3L);
                            org.treetank.axis.FilterTest.testFilterConventions(new ItemFilter(pRtx), true);

                            pRtx.moveTo(2L);
                            pRtx.moveToAttribute(0);
                            org.treetank.axis.FilterTest.testFilterConventions(new ItemFilter(pRtx), true);

                        }
                    }, // Nested Filter Test
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTXPathException, TTIOException {

                            pRtx.moveTo(9L);
                            org.treetank.axis.FilterTest.testFilterConventions(new NestedFilter(pRtx,
                                new ItemFilter(holder.getNRtx()), new ElementFilter(pRtx), new NameFilter(
                                    pRtx, "b")), true);
                            org.treetank.axis.FilterTest.testFilterConventions(new NestedFilter(pRtx,
                                new ItemFilter(pRtx), new AttributeFilter(pRtx), new NameFilter(pRtx, "b")),
                                false);

                            pRtx.moveTo(4L);
                            org.treetank.axis.FilterTest.testFilterConventions(new NestedFilter(pRtx,
                                new NodeFilter(holder.getNRtx()), new ElementFilter(pRtx)), false);
                            org.treetank.axis.FilterTest.testFilterConventions(new NestedFilter(pRtx,
                                new NodeFilter(holder.getNRtx()), new TextFilter(pRtx)), true);

                            pRtx.moveTo(1L);
                            pRtx.moveToAttribute(0);
                            org.treetank.axis.FilterTest.testFilterConventions(new NestedFilter(pRtx,
                                new AttributeFilter(holder.getNRtx()), new NameFilter(pRtx, "i")), true);

                        }
                    }, // Predicate Filter Test
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTXPathException, TTIOException {
                            // Find descendants starting from nodeKey 0L (root).
                            pRtx.moveTo(ROOT_NODE);

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "/p:a[@i]"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a/b[@p:x]"), new long[] {
                                9L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[text()]"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[element()]"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[node()/text()]"),
                                new long[] {
                                    1L
                                });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[./node()]"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[./node()/node()/node()]"),
                                new long[] {});

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[//element()]"),
                                new long[] {
                                    1L
                                });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[/text()]"), new long[] {});

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[3<4]"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[13>=4]"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[13.0>=4]"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[4 = 4]"), new long[] {
                                1L
                            });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[3=4]"), new long[] {});

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "p:a[3.2 = 3.22]"),
                                new long[] {});

                            pRtx.moveTo(1L);

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "child::b[child::c]"),
                                new long[] {
                                    5L, 9L
                                });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx, "child::*[text() or c]"),
                                new long[] {
                                    5l, 9L
                                });

                            AxisTest.testIAxisConventions(new XPathAxis(pRtx,
                                "child::*[text() or c], /node(), //c"), new long[] {
                                5l, 9L, 1L, 7L, 11L
                            });
                        }
                    }, // Type Filter Test
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException, TTXPathException {

                            final AbsAxis axis = new XPathAxis(pRtx, "a");

                            axis.moveTo(9L);
                            org.treetank.axis.FilterTest.testFilterConventions(new TypeFilter(pRtx,
                                "xs:untyped"), true);
                            org.treetank.axis.FilterTest.testFilterConventions(
                                new TypeFilter(pRtx, "xs:long"), false);

                            pRtx.moveTo(4L);
                            org.treetank.axis.FilterTest.testFilterConventions(new TypeFilter(pRtx,
                                "xs:untyped"), true);
                            org.treetank.axis.FilterTest.testFilterConventions(new TypeFilter(pRtx,
                                "xs:double"), false);

                            pRtx.moveTo(1L);
                            pRtx.moveToAttribute(0);
                            org.treetank.axis.FilterTest.testFilterConventions(new TypeFilter(pRtx,
                                "xs:untyped"), true);

                            org.treetank.axis.FilterTest.testFilterConventions(new TypeFilter(pRtx,
                                "xs:anyType"), false);
                        }
                    }, // Wildcard Filter
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(9L);
                            org.treetank.axis.FilterTest.testFilterConventions(new WildcardFilter(pRtx, "b",
                                true), true);
                            pRtx.moveToAttribute(0);
                            try {
                                org.treetank.axis.FilterTest.testFilterConventions(new WildcardFilter(pRtx,
                                    "p", false), true);
                                Assert.fail("Expected an Exception, because attributes are not supported.");
                            } catch (IllegalStateException e) {
                                assertEquals(e.getMessage(),
                                    "Wildcards are not supported in attribute names yet.");

                            }
                            // IFilterTest.testIFilterConventions(new
                            // WildcardFilter(holder.getRtx(), "b",
                            // true), true);

                            // holder.getRtx().moveTo(3L);
                            // IFilterTest.testIFilterConventions(new ItemFilter(holder.getRtx()),
                            // true);

                            pRtx.moveTo(1L);
                            org.treetank.axis.FilterTest.testFilterConventions(new WildcardFilter(pRtx, "p",
                                false), true);
                            org.treetank.axis.FilterTest.testFilterConventions(new WildcardFilter(pRtx, "a",
                                true), true);
                            org.treetank.axis.FilterTest.testFilterConventions(new WildcardFilter(pRtx, "c",
                                true), false);
                            org.treetank.axis.FilterTest.testFilterConventions(new WildcardFilter(pRtx, "b",
                                false), false);
                        }
                    }
                }
            }
        };

        return returnVal;
    }

    /**
     * Interface to check axis.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    interface IFilterChecker {
        void checkFilter(INodeReadTrx pRtx) throws TTXPathException, TTIOException;

    }

}

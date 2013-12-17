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

package org.treetank.axis;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;
import static org.treetank.data.IConstants.ROOT_NODE;

import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.INodeReadTrx;
import org.treetank.axis.filter.NameFilter;
import org.treetank.axis.filter.NodeFilter;
import org.treetank.axis.filter.TextFilter;
import org.treetank.axis.filter.ValueFilter;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.testutil.CoreTestHelper;
import org.treetank.testutil.Holder;
import org.treetank.testutil.ModuleFactory;
import org.treetank.testutil.NodeElementTestHelper;

import com.google.inject.Inject;

/**
 * Test Cases for all AbsAxis-implementations
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class AxisTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeClass
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        final CoreTestHelper.Holder holder = CoreTestHelper.Holder.generateStorage();
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

    @Test(dataProvider = "instantiateAxis")
    public void testAxis(Class<IAxisChecker> pAxisCheckerClass, IAxisChecker[] pAxisChecker) throws Exception {

        for (int i = 0; i < pAxisChecker.length; i++) {
            pAxisChecker[i].checkAxis(holder.getNRtx());
        }

    }

    @DataProvider(name = "instantiateAxis")
    public Object[][] instantiateAxis() throws TTByteHandleException {

        Object[][] returnVal = {
            {
                IAxisChecker.class, new IAxisChecker[] {
                    // Child Axis Test
                    new IAxisChecker() {
                        @Override
                        public void checkAxis(INodeReadTrx ppRtx) throws TTIOException {

                            ppRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new ChildAxis(ppRtx), new long[] {
                                4L, 5L, 8L, 9L, 13L
                            });

                            ppRtx.moveTo(5L);
                            AxisTest.testIAxisConventions(new ChildAxis(ppRtx), new long[] {
                                6L, 7L
                            });

                            ppRtx.moveTo(13L);
                            AxisTest.testIAxisConventions(new ChildAxis(ppRtx), new long[] {});
                        }
                    }, // Descendant Axis Test, without self
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx ppRtx) throws TTIOException {
                            ppRtx.moveTo(ROOT_NODE);

                            AxisTest.testIAxisConventions(new DescendantAxis(ppRtx), new long[] {
                                1L, 4L, 5L, 6L, 7L, 8L, 9L, 11L, 12L, 13L
                            });

                            ppRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new DescendantAxis(ppRtx), new long[] {
                                4L, 5L, 6L, 7L, 8L, 9L, 11L, 12L, 13L
                            });

                            ppRtx.moveTo(9L);
                            AxisTest.testIAxisConventions(new DescendantAxis(ppRtx), new long[] {
                                11L, 12L
                            });

                            ppRtx.moveTo(13L);
                            AxisTest.testIAxisConventions(new DescendantAxis(ppRtx), new long[] {});

                        }
                    }, // Descendant Axis Test, with self
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx ppRtx) throws TTIOException {
                            ppRtx.moveTo(ROOT_NODE);
                            AxisTest.testIAxisConventions(new DescendantAxis(ppRtx, true), new long[] {
                                ROOT_NODE, 1L, 4L, 5L, 6L, 7L, 8L, 9L, 11L, 12L, 13L
                            });

                            ppRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new DescendantAxis(ppRtx, true), new long[] {
                                1L, 4L, 5L, 6L, 7L, 8L, 9L, 11L, 12L, 13L
                            });

                            ppRtx.moveTo(9L);
                            AxisTest.testIAxisConventions(new DescendantAxis(ppRtx, true), new long[] {
                                9L, 11L, 12L
                            });

                            ppRtx.moveTo(13L);
                            AxisTest.testIAxisConventions(new DescendantAxis(ppRtx, true), new long[] {
                                13L
                            });

                        }
                    }, // Following Axis Test
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx ppRtx) throws TTIOException {

                            ppRtx.moveTo(11L);
                            AxisTest.testIAxisConventions(new FollowingAxis(ppRtx), new long[] {
                                12L, 13L
                            });

                            ppRtx.moveTo(5L);
                            AxisTest.testIAxisConventions(new FollowingAxis(ppRtx), new long[] {
                                8L, 9L, 11L, 12L, 13L
                            });

                            ppRtx.moveTo(13L);
                            AxisTest.testIAxisConventions(new FollowingAxis(ppRtx), new long[] {});

                            ppRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new FollowingAxis(ppRtx), new long[] {});

                            ppRtx.moveTo(ROOT_NODE);
                            AxisTest.testIAxisConventions(new FollowingAxis(ppRtx), new long[] {});

                            ppRtx.moveTo(9L);
                            ppRtx.moveToAttribute(0);
                            AxisTest.testIAxisConventions(new FollowingAxis(ppRtx), new long[] {});

                        }
                    }, // Following Sibling Axis Test
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx ppRtx) throws TTIOException {
                            ppRtx.moveTo(9L);
                            AxisTest.testIAxisConventions(new FollowingSiblingAxis(ppRtx), new long[] {
                                13L
                            });

                            ppRtx.moveTo(5L);
                            AxisTest.testIAxisConventions(new FollowingSiblingAxis(ppRtx), new long[] {
                                8L, 9L, 13L
                            });

                            ppRtx.moveTo(4L);
                            AxisTest.testIAxisConventions(new FollowingSiblingAxis(ppRtx), new long[] {
                                5L, 8L, 9L, 13L
                            });

                            ppRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new FollowingSiblingAxis(ppRtx), new long[] {});

                            ppRtx.moveTo(9L);
                            ppRtx.moveToAttribute(0);
                            AxisTest.testIAxisConventions(new FollowingSiblingAxis(ppRtx), new long[] {});

                        }
                    }, // Level Order Axis Test
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(11L);
                            AxisTest.testIAxisConventions(new LevelOrderAxis(pRtx), new long[] {
                                12L
                            });
                            pRtx.moveTo(11L);
                            AxisTest.testIAxisConventions(new LevelOrderAxis(pRtx, true), new long[] {
                                11L, 12L
                            });
                            pRtx.moveTo(0L);
                            AxisTest.testIAxisConventions(new LevelOrderAxis(pRtx, true), new long[] {
                                0L, 1L, 4L, 5L, 8L, 9L, 13L, 6L, 7L, 11L, 12L
                            });

                            pRtx.moveTo(4L);
                            AxisTest.testIAxisConventions(new LevelOrderAxis(pRtx, true), new long[] {
                                4L, 5L, 8L, 9L, 13L, 6L, 7L, 11L, 12L
                            });

                            pRtx.moveTo(4L);
                            AxisTest.testIAxisConventions(new LevelOrderAxis(pRtx), new long[] {
                                5L, 8L, 9L, 13L, 6L, 7L, 11L, 12L
                            });

                            pRtx.moveTo(6L);
                            AxisTest.testIAxisConventions(new LevelOrderAxis(pRtx), new long[] {
                                7L
                            });

                            pRtx.moveTo(6L);
                            AxisTest.testIAxisConventions(new LevelOrderAxis(pRtx, true), new long[] {
                                6L, 7L
                            });

                        }
                    }, // Nested Axis Test 1
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {
                            // Find descendants starting from nodeKey 0L (root).
                            pRtx.moveTo(ROOT_NODE);

                            // XPath expression /p:a/b/text()
                            // Part: /p:a
                            final AbsAxis childA =
                                new FilterAxis(new ChildAxis(pRtx), pRtx, new NameFilter(pRtx, "p:a"));
                            // Part: /b
                            final AbsAxis childB =
                                new FilterAxis(new ChildAxis(pRtx), pRtx, new NameFilter(pRtx, "b"));
                            // Part: /text()
                            final AbsAxis text =
                                new FilterAxis(new ChildAxis(pRtx), pRtx, new TextFilter(pRtx));
                            // Part: /p:a/b/text()
                            final AbsAxis axis =
                                new NestedAxis(new NestedAxis(childA, childB, pRtx), text, pRtx);

                            AxisTest.testIAxisConventions(axis, new long[] {
                                6L, 12L
                            });

                        }
                    }, // Nested Axis Test 2
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {

                            // Find descendants starting from nodeKey 0L (root).
                            pRtx.moveTo(ROOT_NODE);

                            // XPath expression /[:a/b/@p:x]
                            // Part: /p:a
                            final AbsAxis childA =
                                new FilterAxis(new ChildAxis(pRtx), pRtx, new NameFilter(pRtx, "p:a"));
                            // Part: /b
                            final AbsAxis childB =
                                new FilterAxis(new ChildAxis(pRtx), pRtx, new NameFilter(pRtx, "b"));
                            // Part: /@x
                            final AbsAxis attributeX =
                                new FilterAxis(new AttributeAxis(pRtx), pRtx, new NameFilter(pRtx, "p:x"));
                            // Part: /p:a/b/@p:x
                            final AbsAxis axis =
                                new NestedAxis(new NestedAxis(childA, childB, pRtx), attributeX, pRtx);

                            AxisTest.testIAxisConventions(axis, new long[] {
                                10L
                            });

                        }
                    }, // Nestes Axis Test 3
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {

                            // Find desceFndants starting from nodeKey 0L (root).
                            pRtx.moveTo(ROOT_NODE);

                            // XPath expression p:a/node():
                            // Part: /p:a
                            final AbsAxis childA =
                                new FilterAxis(new ChildAxis(pRtx), pRtx, new NameFilter(pRtx, "p:a"));

                            // Part: /node()
                            final AbsAxis childNode =
                                new FilterAxis(new ChildAxis(pRtx), pRtx, new NodeFilter(pRtx));

                            // Part: /p:a/node():
                            final AbsAxis axis = new NestedAxis(childA, childNode, pRtx);

                            AxisTest.testIAxisConventions(axis, new long[] {
                                4L, 5L, 8L, 9L, 13L
                            });

                        }
                    }, // Ancestor Axis Test 1
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(12L);
                            AxisTest.testIAxisConventions(new AncestorAxis(pRtx), new long[] {
                                9L, 1L
                            });

                            pRtx.moveTo(4L);
                            AxisTest.testIAxisConventions(new AncestorAxis(pRtx), new long[] {
                                1L
                            });

                            pRtx.moveTo(5L);
                            AxisTest.testIAxisConventions(new AncestorAxis(pRtx), new long[] {
                                1L
                            });

                            pRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new AncestorAxis(pRtx), new long[] {});

                        }
                    }, // Ancestor Axis Test 2
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(11L);
                            AxisTest.testIAxisConventions(new AncestorAxis(pRtx, true), new long[] {
                                11L, 9L, 1L
                            });

                            pRtx.moveTo(5L);
                            AxisTest.testIAxisConventions(new AncestorAxis(pRtx, true), new long[] {
                                5L, 1L
                            });

                            pRtx.moveTo(4L);
                            AxisTest.testIAxisConventions(new AncestorAxis(pRtx, true), new long[] {
                                4L, 1L
                            });

                            pRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new AncestorAxis(pRtx, true), new long[] {
                                1L
                            });
                        }
                    }, // Attribute Axis Test
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(ROOT_NODE);
                            AxisTest.testIAxisConventions(new AttributeAxis(pRtx), new long[] {});

                            pRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new AttributeAxis(pRtx), new long[] {
                                2L
                            });

                            pRtx.moveTo(9L);
                            AxisTest.testIAxisConventions(new AttributeAxis(pRtx), new long[] {
                                10L
                            });

                            pRtx.moveTo(12L);
                            AxisTest.testIAxisConventions(new AttributeAxis(pRtx), new long[] {});

                            pRtx.moveTo(2L);
                            AxisTest.testIAxisConventions(new AttributeAxis(pRtx), new long[] {});
                        }
                    }, // Parent Axis Test
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {

                            pRtx.moveTo(5L);
                            AxisTest.testIAxisConventions(new ParentAxis(pRtx), new long[] {
                                1L
                            });

                            pRtx.moveTo(8L);
                            AxisTest.testIAxisConventions(new ParentAxis(pRtx), new long[] {
                                1L
                            });

                            pRtx.moveTo(10L);
                            AxisTest.testIAxisConventions(new ParentAxis(pRtx), new long[] {
                                9L
                            });
                        }
                    }, // Postorder Axis
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(ROOT_NODE);
                            AxisTest.testIAxisConventions(new PostOrderAxis(pRtx), new long[] {
                                4L, 6L, 7L, 5L, 8L, 11L, 12L, 9L, 13L, 1L, 0L
                            });

                        }
                    }, // Preceding Axis
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {

                            pRtx.moveTo(12L);
                            AxisTest.testIAxisConventions(new PrecedingAxis(pRtx), new long[] {
                                11L, 8L, 7L, 6L, 5L, 4L
                            });

                            pRtx.moveTo(5L);
                            AxisTest.testIAxisConventions(new PrecedingAxis(pRtx), new long[] {
                                4L
                            });

                            pRtx.moveTo(13L);
                            AxisTest.testIAxisConventions(new PrecedingAxis(pRtx), new long[] {
                                12L, 11L, 9L, 8L, 7L, 6L, 5L, 4L
                            });

                            pRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new PrecedingAxis(pRtx), new long[] {});

                            pRtx.moveTo(9L);
                            pRtx.moveToAttribute(0);
                            AxisTest.testIAxisConventions(new PrecedingAxis(pRtx), new long[] {});

                        }
                    }, // Preceding Sibling Axis
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {

                            pRtx.moveTo(12L);
                            AxisTest.testIAxisConventions(new PrecedingSiblingAxis(pRtx), new long[] {
                                11L
                            });

                            pRtx.moveTo(5L);
                            AxisTest.testIAxisConventions(new PrecedingSiblingAxis(pRtx), new long[] {
                                4L
                            });

                            pRtx.moveTo(13L);
                            AxisTest.testIAxisConventions(new PrecedingSiblingAxis(pRtx), new long[] {
                                9L, 8L, 5L, 4L
                            });

                            pRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new PrecedingSiblingAxis(pRtx), new long[] {});

                            pRtx.moveTo(9L);
                            pRtx.moveToAttribute(0);
                            AxisTest.testIAxisConventions(new PrecedingSiblingAxis(pRtx), new long[] {});

                        }
                    }, // new Self Axis
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(4L);
                            AxisTest.testIAxisConventions(new SelfAxis(pRtx), new long[] {
                                4L
                            });

                            pRtx.moveTo(8L);
                            AxisTest.testIAxisConventions(new SelfAxis(pRtx), new long[] {
                                8L
                            });

                        }
                    }, // Filter Axis Test 1
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(ROOT_NODE);
                            AxisTest.testIAxisConventions(new FilterAxis(new DescendantAxis(pRtx), pRtx,
                                new NameFilter(pRtx, "b")), new long[] {
                                5L, 9L
                            });

                        }
                    }, // Filter Axis Test 2
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(ROOT_NODE);
                            AxisTest.testIAxisConventions(new FilterAxis(new DescendantAxis(pRtx), pRtx,
                                new ValueFilter(pRtx, "foo")), new long[] {
                                6L
                            });

                        }
                    }, // Filter Axis Test 3
                    new IAxisChecker() {

                        @Override
                        public void checkAxis(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(1L);
                            AxisTest.testIAxisConventions(new FilterAxis(new AttributeAxis(pRtx), pRtx,
                                new NameFilter(pRtx, "i"), new ValueFilter(pRtx, "j")), new long[] {
                                2L
                            });

                            pRtx.moveTo(9L);
                            AxisTest.testIAxisConventions(new FilterAxis(new AttributeAxis(pRtx), pRtx,
                                new NameFilter(pRtx, "y"), new ValueFilter(pRtx, "y")), new long[] {});

                        }
                    }
                }
            }
        };

        return returnVal;
    }

    public static void testIAxisConventions(final AbsAxis axis, final long[] expectedKeys) {

        // IAxis Convention 1.
        final long startKey = axis.getNode().getDataKey();

        final long[] keys = new long[expectedKeys.length];
        int offset = 0;
        while (axis.hasNext()) {
            axis.next();
            // IAxis results.
            assertTrue(new StringBuilder("Test for: ").append(axis.getClass().getName()).toString(),
                offset < expectedKeys.length);
            keys[offset++] = axis.getNode().getDataKey();

            // IAxis Convention 2.
            try {
                axis.next();
                Assert.fail(new StringBuilder("Test for: ").append(axis.getClass().getName()).append(
                    "Should only allow to call next() once.").toString());
            } catch (final IllegalStateException exc) {
                // Must throw exception.
            }

            // IAxis Convention 3.
            axis.moveTo(ROOT_NODE);

        }

        // IAxis Convention 5.
        assertEquals(new StringBuilder("Test for: ").append(axis.getClass().getName()).toString(), startKey,
            axis.getNode().getDataKey());

        // IAxis results.
        assertArrayEquals(new StringBuilder("Test for: ").append(axis.getClass().getName()).toString(),
            expectedKeys, keys);

    }

    /**
     * Interface to check axis.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    interface IAxisChecker {
        void checkAxis(INodeReadTrx pRtx) throws TTIOException;

    }

}

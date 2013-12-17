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
import static org.treetank.data.IConstants.ROOT_NODE;

import java.util.Properties;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.INodeReadTrx;
import org.treetank.axis.filter.AbsFilter;
import org.treetank.axis.filter.AttributeFilter;
import org.treetank.axis.filter.CommentFilter;
import org.treetank.axis.filter.DocumentRootNodeFilter;
import org.treetank.axis.filter.ElementFilter;
import org.treetank.axis.filter.NameFilter;
import org.treetank.axis.filter.NodeFilter;
import org.treetank.axis.filter.PIFilter;
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
public class FilterTest {

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
                    // Attribute Filter
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(9L);
                            testFilterConventions(new AttributeFilter(pRtx), false);

                            pRtx.moveTo(4L);
                            testFilterConventions(new AttributeFilter(pRtx), false);

                            pRtx.moveTo(1L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new AttributeFilter(pRtx), true);

                            pRtx.moveTo(9L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new AttributeFilter(pRtx), true);

                        }
                    },// Comment Filter
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException {

                            pRtx.moveTo(9L);
                            testFilterConventions(new CommentFilter(pRtx), false);

                            pRtx.moveTo(4L);
                            testFilterConventions(new CommentFilter(pRtx), false);

                            pRtx.moveTo(1L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new CommentFilter(pRtx), false);

                            pRtx.moveTo(9L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new CommentFilter(pRtx), false);

                        }
                    }, // DocRoot Filter
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException {

                            pRtx.moveTo(0L);
                            testFilterConventions(new DocumentRootNodeFilter(pRtx), true);

                            pRtx.moveTo(1L);
                            testFilterConventions(new DocumentRootNodeFilter(pRtx), false);

                            pRtx.moveTo(1L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new DocumentRootNodeFilter(pRtx), false);

                            pRtx.moveTo(3L);
                            testFilterConventions(new DocumentRootNodeFilter(pRtx), false);

                            pRtx.moveTo(4L);
                            testFilterConventions(new DocumentRootNodeFilter(pRtx), false);

                            pRtx.moveTo(5L);
                            testFilterConventions(new DocumentRootNodeFilter(pRtx), false);

                            pRtx.moveTo(9L);
                            testFilterConventions(new DocumentRootNodeFilter(pRtx), false);

                            pRtx.moveTo(9L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new DocumentRootNodeFilter(pRtx), false);

                            pRtx.moveTo(12L);
                            testFilterConventions(new DocumentRootNodeFilter(pRtx), false);

                            pRtx.moveTo(13L);
                            pRtx.moveTo(ROOT_NODE);
                            testFilterConventions(new DocumentRootNodeFilter(pRtx), true);

                        }
                    }, // Element Filter
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException {

                            pRtx.moveTo(0L);
                            testFilterConventions(new ElementFilter(pRtx), false);

                            pRtx.moveTo(1L);
                            testFilterConventions(new ElementFilter(pRtx), true);

                            pRtx.moveTo(1L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new ElementFilter(pRtx), false);

                            pRtx.moveTo(4L);
                            testFilterConventions(new ElementFilter(pRtx), false);

                            pRtx.moveTo(5L);
                            testFilterConventions(new ElementFilter(pRtx), true);

                            pRtx.moveTo(6L);
                            testFilterConventions(new ElementFilter(pRtx), false);

                            pRtx.moveTo(9L);
                            testFilterConventions(new ElementFilter(pRtx), true);

                            pRtx.moveTo(9L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new ElementFilter(pRtx), false);

                            pRtx.moveTo(12L);
                            testFilterConventions(new ElementFilter(pRtx), false);

                        }
                    }, // Name Filter Test
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(9L);
                            testFilterConventions(new NameFilter(pRtx, "b"), true);

                            pRtx.moveTo(4L);
                            testFilterConventions(new NameFilter(pRtx, "b"), false);

                            pRtx.moveTo(7L);
                            testFilterConventions(new NameFilter(pRtx, "b"), false);

                        }
                    }, // Node Filter Test
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(9L);
                            testFilterConventions(new NodeFilter(pRtx), true);

                            pRtx.moveTo(4L);
                            testFilterConventions(new NodeFilter(pRtx), true);

                            pRtx.moveTo(1L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new NodeFilter(pRtx), false);

                        }
                    }, // PIFilter Test
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(8L);
                            testFilterConventions(new PIFilter(pRtx), false);

                            pRtx.moveTo(3L);
                            testFilterConventions(new PIFilter(pRtx), false);

                            pRtx.moveTo(1L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new PIFilter(pRtx), false);

                            pRtx.moveTo(9L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new PIFilter(pRtx), false);

                        }
                    }, // Text Filter Test
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(8L);
                            testFilterConventions(new TextFilter(pRtx), true);

                            pRtx.moveTo(3L);
                            testFilterConventions(new TextFilter(pRtx), false);

                            pRtx.moveTo(5L);
                            testFilterConventions(new TextFilter(pRtx), false);

                            pRtx.moveTo(1L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new TextFilter(pRtx), false);

                        }
                    }, // Value Filter Test
                    new IFilterChecker() {

                        @Override
                        public void checkFilter(INodeReadTrx pRtx) throws TTIOException {
                            pRtx.moveTo(4L);
                            testFilterConventions(new ValueFilter(pRtx, "oops1"), true);
                            testFilterConventions(new ValueFilter(pRtx, "foo"), false);

                            pRtx.moveTo(1L);
                            pRtx.moveToAttribute(0);
                            testFilterConventions(new ValueFilter(pRtx, "j"), true);

                            pRtx.moveTo(2L);
                            testFilterConventions(new ValueFilter(pRtx, "j"), true);

                        }
                    }

                }
            }
        };

        return returnVal;
    }

    private static void testFilterConventions(final AbsFilter filter, final boolean expected) {

        // IFilter Convention 1.
        final long startKey = filter.getNode().getDataKey();

        assertEquals(expected, filter.filter());

        // IAxis Convention 2.
        assertEquals(startKey, filter.getNode().getDataKey());

    }

    /**
     * Interface to check axis.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    interface IFilterChecker {
        void checkFilter(INodeReadTrx pRtx) throws TTIOException;

    }

}

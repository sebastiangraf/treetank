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

import static org.treetank.node.IConstants.ROOT_NODE;

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
import org.treetank.api.INodeReadTrx;
import org.treetank.axis.filter.NameFilter;
import org.treetank.axis.filter.NodeFilter;
import org.treetank.axis.filter.TextFilter;
import org.treetank.exception.TTException;

import com.google.inject.Inject;

@Guice(moduleFactory = NodeModuleFactory.class)
public class NestedAxisTest {

    private Holder holder;

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    @BeforeMethod
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
        mResource = mResourceConfig.create(TestHelper.PATHS.PATH1.getFile(), TestHelper.RESOURCENAME, 10);
        NodeHelper.createTestDocument(mResource);
        holder =
            Holder.generateRtx(mResource);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        holder.close();
        TestHelper.deleteEverything();
    }
    @Test
    public void testNestedAxisTest() throws TTException {
        final INodeReadTrx rtx = holder.getNRtx();

        // Find descendants starting from nodeKey 0L (root).
        rtx.moveTo(ROOT_NODE);

        // XPath expression /p:a/b/text()
        // Part: /p:a
        final AbsAxis childA = new FilterAxis(new ChildAxis(rtx), rtx, new NameFilter(rtx, "p:a"));
        // Part: /b
        final AbsAxis childB = new FilterAxis(new ChildAxis(rtx), rtx, new NameFilter(rtx, "b"));
        // Part: /text()
        final AbsAxis text = new FilterAxis(new ChildAxis(rtx), rtx, new TextFilter(rtx));
        // Part: /p:a/b/text()
        final AbsAxis axis = new NestedAxis(new NestedAxis(childA, childB, rtx), text, rtx);

        AxisTest.testIAxisConventions(axis, new long[] {
            6L, 12L
        });
    }

    @Test
    public void testNestedAxisTest2() throws TTException {
        final INodeReadTrx rtx = holder.getNRtx();

        // Find descendants starting from nodeKey 0L (root).
        rtx.moveTo(ROOT_NODE);

        // XPath expression /[:a/b/@p:x]
        // Part: /p:a
        final AbsAxis childA = new FilterAxis(new ChildAxis(rtx), rtx, new NameFilter(rtx, "p:a"));
        // Part: /b
        final AbsAxis childB = new FilterAxis(new ChildAxis(rtx), rtx, new NameFilter(rtx, "b"));
        // Part: /@x
        final AbsAxis attributeX = new FilterAxis(new AttributeAxis(rtx), rtx, new NameFilter(rtx, "p:x"));
        // Part: /p:a/b/@p:x
        final AbsAxis axis = new NestedAxis(new NestedAxis(childA, childB, rtx), attributeX, rtx);

        AxisTest.testIAxisConventions(axis, new long[] {
            10L
        });

    }

    @Test
    public void testNestedAxisTest3() throws TTException {
        final INodeReadTrx rtx = holder.getNRtx();

        // Find desceFndants starting from nodeKey 0L (root).
        rtx.moveTo(ROOT_NODE);

        // XPath expression p:a/node():
        // Part: /p:a
        final AbsAxis childA = new FilterAxis(new ChildAxis(rtx), rtx, new NameFilter(rtx, "p:a"));

        // Part: /node()
        final AbsAxis childNode = new FilterAxis(new ChildAxis(rtx), rtx, new NodeFilter(rtx));

        // Part: /p:a/node():
        final AbsAxis axis = new NestedAxis(childA, childNode, rtx);

        AxisTest.testIAxisConventions(axis, new long[] {
            4L, 5L, 8L, 9L, 13L
        });

    }
}

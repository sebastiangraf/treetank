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

import javax.xml.namespace.QName;

import org.testng.AssertJUnit;
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
import org.treetank.api.INodeWriteTrx;
import org.treetank.exception.TTException;

import com.google.inject.Inject;

@Guice(moduleFactory = NodeModuleFactory.class)
public class AttributeAxisTest {

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
            Holder.generateWtx(mResource);
    }

    @AfterMethod
    public void tearDown() throws TTException {
        holder.close();
        TestHelper.deleteEverything();
    }

    @Test
    public void testIterate() throws TTException {
        final INodeReadTrx wtx = holder.getNRtx();

        wtx.moveTo(ROOT_NODE);
        AxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] {});

        wtx.moveTo(1L);
        AxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] {
            2L
        });

        wtx.moveTo(9L);
        AxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] {
            10L
        });

        wtx.moveTo(12L);
        AxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] {});

        wtx.moveTo(2L);
        AxisTest.testIAxisConventions(new AttributeAxis(wtx), new long[] {});
    }

    @Test
    public void testMultipleAttributes() throws TTException {
        final INodeWriteTrx wtx = holder.getNWtx();
        final long nodeKey = wtx.insertElementAsFirstChild(new QName("foo"));
        wtx.insertAttribute(new QName("foo0"), "0");
        wtx.moveTo(nodeKey);
        wtx.insertAttribute(new QName("foo1"), "1");
        wtx.moveTo(nodeKey);
        wtx.insertAttribute(new QName("foo2"), "2");

        AssertJUnit.assertEquals(true, wtx.moveTo(nodeKey));

        AssertJUnit.assertEquals(true, wtx.moveToAttribute(0));
        AssertJUnit.assertEquals("0", wtx.getValueOfCurrentNode());
        AssertJUnit.assertEquals(new QName("foo0"), wtx.getQNameOfCurrentNode());

        AssertJUnit.assertEquals(true, wtx.moveTo(wtx.getNode().getParentKey()));
        AssertJUnit.assertEquals(true, wtx.moveToAttribute(1));
        AssertJUnit.assertEquals("1", wtx.getValueOfCurrentNode());
        AssertJUnit.assertEquals(new QName("foo1"), wtx.getQNameOfCurrentNode());

        AssertJUnit.assertEquals(true, wtx.moveTo(wtx.getNode().getParentKey()));
        AssertJUnit.assertEquals(true, wtx.moveToAttribute(2));
        AssertJUnit.assertEquals("2", wtx.getValueOfCurrentNode());
        AssertJUnit.assertEquals(new QName("foo2"), wtx.getQNameOfCurrentNode());

        AssertJUnit.assertEquals(true, wtx.moveTo(nodeKey));
        final AbsAxis axis = new AttributeAxis(wtx);

        AssertJUnit.assertEquals(true, axis.hasNext());
        axis.next();
        AssertJUnit.assertEquals(nodeKey + 1, wtx.getNode().getNodeKey());
        AssertJUnit.assertEquals(new QName("foo0"), wtx.getQNameOfCurrentNode());
        AssertJUnit.assertEquals("0", wtx.getValueOfCurrentNode());

        AssertJUnit.assertEquals(true, axis.hasNext());
        axis.next();
        AssertJUnit.assertEquals(nodeKey + 2, wtx.getNode().getNodeKey());
        AssertJUnit.assertEquals(new QName("foo1"), wtx.getQNameOfCurrentNode());
        AssertJUnit.assertEquals("1", wtx.getValueOfCurrentNode());

        AssertJUnit.assertEquals(true, axis.hasNext());
        axis.next();
        AssertJUnit.assertEquals(nodeKey + 3, wtx.getNode().getNodeKey());
        AssertJUnit.assertEquals(new QName("foo2"), wtx.getQNameOfCurrentNode());
        AssertJUnit.assertEquals("2", wtx.getValueOfCurrentNode());

        wtx.abort();
        wtx.close();
    }
}

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

package org.treetank.service.xml.xpath.expr;

import static org.testng.AssertJUnit.assertEquals;

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
import org.treetank.axis.AbsAxis;
import org.treetank.exception.TTException;
import org.treetank.node.AtomicValue;
import org.treetank.node.Type;
import org.treetank.node.interfaces.IValNode;
import org.treetank.utils.NamePageHash;

import com.google.inject.Inject;

/**
 * JUnit-test class to test the functionality of the LiteralExpr.
 * 
 * @author Tina Scherer
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public class LiteralExprTest {

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
        TestHelper.deleteEverything();
    }
    @Test
    public void testLiteralExpr() throws TTException {
        // Build simple test tree.

        final AtomicValue item1 = new AtomicValue(false);
        final AtomicValue item2 = new AtomicValue(14, Type.INTEGER);

        final int key1 = AbsAxis.addAtomicToItemList(holder.getNRtx(), item1);
        final int key2 = AbsAxis.addAtomicToItemList(holder.getNRtx(), item2);

        final AbsAxis axis1 = new LiteralExpr(holder.getNRtx(), key1);

        assertEquals(true, axis1.hasNext());
        assertEquals(key1, axis1.getNode().getNodeKey());
        assertEquals(NamePageHash.generateHashForString("xs:boolean"), axis1.getNode().getTypeKey());
        assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis1.getNode()).getRawValue())));
        assertEquals(false, axis1.hasNext());

        final AbsAxis axis2 = new LiteralExpr(holder.getNRtx(), key2);
        assertEquals(true, axis2.hasNext());
        assertEquals(key2, axis2.getNode().getNodeKey());
        assertEquals(NamePageHash.generateHashForString("xs:integer"), axis2.getNode().getTypeKey());
        assertEquals(14, Integer.parseInt(new String(((IValNode)axis2.getNode()).getRawValue())));
        assertEquals(false, axis2.hasNext());

    }

}
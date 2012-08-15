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

import org.testng.Assert;
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
import org.treetank.node.interfaces.IValNode;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.service.xml.xpath.XPathError;

import com.google.inject.Inject;

/**
 * JUnit-test class to test the functionality of the AndExpr.
 * 
 * @author Tina Scherer
 * 
 */
@Guice(moduleFactory = NodeModuleFactory.class)
public class AndExprTest {

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
    public void testAnd() throws TTException {

        long iTrue = AbsAxis.addAtomicToItemList(holder.getNRtx(), new AtomicValue(true));
        long iFalse = AbsAxis.addAtomicToItemList(holder.getNRtx(), new AtomicValue(false));

        AbsAxis trueLit1 = new LiteralExpr(holder.getNRtx(), iTrue);
        AbsAxis trueLit2 = new LiteralExpr(holder.getNRtx(), iTrue);
        AbsAxis falseLit1 = new LiteralExpr(holder.getNRtx(), iFalse);
        AbsAxis falseLit2 = new LiteralExpr(holder.getNRtx(), iFalse);

        AbsAxis axis1 = new AndExpr(holder.getNRtx(), trueLit1, trueLit2);
        assertEquals(true, axis1.hasNext());
        assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode()).getRawValue())));
        assertEquals(false, axis1.hasNext());

        AbsAxis axis2 = new AndExpr(holder.getNRtx(), trueLit1, falseLit1);
        assertEquals(true, axis2.hasNext());
        assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis2.getNode()).getRawValue())));
        assertEquals(false, axis2.hasNext());

        AbsAxis axis3 = new AndExpr(holder.getNRtx(), falseLit1, trueLit1);
        assertEquals(true, axis3.hasNext());
        assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis3.getNode()).getRawValue())));
        assertEquals(false, axis3.hasNext());

        AbsAxis axis4 = new AndExpr(holder.getNRtx(), falseLit1, falseLit2);
        assertEquals(true, axis4.hasNext());
        assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis4.getNode()).getRawValue())));
        assertEquals(false, axis4.hasNext());

    }

    @Test
    public void testAndQuery() throws TTException {

        holder.getNRtx().moveTo(1L);

        final AbsAxis axis1 = new XPathAxis(holder.getNRtx(), "text() and node()");
        assertEquals(true, axis1.hasNext());
        assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis1.getNode()).getRawValue())));
        assertEquals(false, axis1.hasNext());

        final AbsAxis axis2 = new XPathAxis(holder.getNRtx(), "comment() and node()");
        assertEquals(true, axis2.hasNext());
        assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis2.getNode()).getRawValue())));
        assertEquals(false, axis2.hasNext());

        final AbsAxis axis3 = new XPathAxis(holder.getNRtx(), "1 eq 1 and 2 eq 2");
        assertEquals(true, axis3.hasNext());
        assertEquals(true, Boolean.parseBoolean(new String(((IValNode)axis3.getNode()).getRawValue())));
        assertEquals(false, axis3.hasNext());

        final AbsAxis axis4 = new XPathAxis(holder.getNRtx(), "1 eq 1 and 2 eq 3");
        assertEquals(true, axis4.hasNext());
        assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis4.getNode()).getRawValue())));
        assertEquals(false, axis4.hasNext());

        // is never evaluated.
        final AbsAxis axis5 = new XPathAxis(holder.getNRtx(), "1 eq 2 and (3 idiv 0 = 1)");
        assertEquals(true, axis5.hasNext());
        assertEquals(false, Boolean.parseBoolean(new String(((IValNode)axis5.getNode()).getRawValue())));
        assertEquals(false, axis5.hasNext());

        final AbsAxis axis6 = new XPathAxis(holder.getNRtx(), "1 eq 1 and 3 idiv 0 = 1");
        try {
            assertEquals(true, axis6.hasNext());
            Assert.fail("Expected XPath exception, because of division by zero");
        } catch (XPathError e) {
            assertEquals("err:FOAR0001: Division by zero.", e.getMessage());
        }

    }

}

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

package org.treetank.service.xml.xpath.operators;

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
import org.treetank.node.Type;
import org.treetank.node.interfaces.IValNode;
import org.treetank.service.xml.xpath.XPathError;
import org.treetank.service.xml.xpath.axis.SequenceAxis;
import org.treetank.service.xml.xpath.expr.LiteralExpr;
import org.treetank.utils.NamePageHash;

import com.google.inject.Inject;
@Guice(moduleFactory = NodeModuleFactory.class)
public class MulOpAxisTest {

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
        TestHelper.deleteEverything();
    }

    @Test
    public final void testOperate() throws TTException {

        AtomicValue item1 = new AtomicValue(3.0, Type.DOUBLE);
        AtomicValue item2 = new AtomicValue(2.0, Type.DOUBLE);

        final int key1 = AbsAxis.addAtomicToItemList(holder.getNWtx(), item1);
        final int key2 = AbsAxis.addAtomicToItemList(holder.getNWtx(), item2);

        AbsAxis op1 = new LiteralExpr(holder.getNWtx(), key1);
        AbsAxis op2 = new LiteralExpr(holder.getNWtx(), key2);
        AbsObAxis axis = new MulOpAxis(holder.getNWtx(), op1, op2);

        assertEquals(true, axis.hasNext());
        assertEquals(6.0, Double.parseDouble(new String(((IValNode)axis.getNode()).getRawValue())));
        assertEquals(NamePageHash.generateHashForString("xs:double"), axis.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());

    }

    @Test
    public final void testGetReturnType() throws TTException {

        AbsAxis op1 = new SequenceAxis(holder.getNWtx());
        AbsAxis op2 = new SequenceAxis(holder.getNWtx());
        AbsObAxis axis = new MulOpAxis(holder.getNWtx(), op1, op2);

        assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash.generateHashForString("xs:double"),
            NamePageHash.generateHashForString("xs:double")));
        assertEquals(Type.DOUBLE, axis.getReturnType(NamePageHash.generateHashForString("xs:decimal"),
            NamePageHash.generateHashForString("xs:double")));
        assertEquals(Type.FLOAT, axis.getReturnType(NamePageHash.generateHashForString("xs:float"),
            NamePageHash.generateHashForString("xs:decimal")));
        assertEquals(Type.DECIMAL, axis.getReturnType(NamePageHash.generateHashForString("xs:decimal"),
            NamePageHash.generateHashForString("xs:integer")));
        // assertEquals(Type.INTEGER,
        // axis.getReturnType(NamePageHash.generateHashForString("xs:integer"),
        // NamePageHash.generateHashForString("xs:integer")));
        assertEquals(Type.YEAR_MONTH_DURATION, axis.getReturnType(NamePageHash
            .generateHashForString("xs:yearMonthDuration"), NamePageHash.generateHashForString("xs:double")));
        assertEquals(Type.YEAR_MONTH_DURATION, axis.getReturnType(NamePageHash
            .generateHashForString("xs:integer"), NamePageHash.generateHashForString("xs:yearMonthDuration")));
        assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(NamePageHash
            .generateHashForString("xs:dayTimeDuration"), NamePageHash.generateHashForString("xs:double")));
        assertEquals(Type.DAY_TIME_DURATION, axis.getReturnType(NamePageHash
            .generateHashForString("xs:integer"), NamePageHash.generateHashForString("xs:dayTimeDuration")));

        try {

            axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"), NamePageHash
                .generateHashForString("xs:yearMonthDuration"));
            Assert.fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertEquals(e.getMessage(), "err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules.");
        }

        try {

            axis.getReturnType(NamePageHash.generateHashForString("xs:dateTime"), NamePageHash
                .generateHashForString("xs:double"));
            Assert.fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertEquals(e.getMessage(), "err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules.");
        }

        try {

            axis.getReturnType(NamePageHash.generateHashForString("xs:string"), NamePageHash
                .generateHashForString("xs:yearMonthDuration"));
            Assert.fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertEquals(e.getMessage(), "err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules.");
        }

        try {

            axis.getReturnType(NamePageHash.generateHashForString("xs:yearMonthDuration"), NamePageHash
                .generateHashForString("xs:yearMonthDuration"));
            Assert.fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertEquals(e.getMessage(), "err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules.");
        }

    }

}

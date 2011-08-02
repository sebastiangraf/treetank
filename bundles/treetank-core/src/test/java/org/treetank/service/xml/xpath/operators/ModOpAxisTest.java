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

import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.api.IItem;
import org.treetank.axis.AbsAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.xpath.AtomicValue;
import org.treetank.service.xml.xpath.XPathError;
import org.treetank.service.xml.xpath.expr.LiteralExpr;
import org.treetank.service.xml.xpath.expr.SequenceAxis;
import org.treetank.service.xml.xpath.types.Type;
import org.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ModOpAxisTest {

    private Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        TestHelper.createTestDocument();
        holder = Holder.generate();
    }

    @After
    public void tearDown() throws AbsTTException {
        holder.close();
        TestHelper.deleteEverything();
    }

    @Test
    public final void testOperate() throws AbsTTException {

        IItem item1 = new AtomicValue(3.0, Type.DOUBLE);
        IItem item2 = new AtomicValue(2.0, Type.DOUBLE);

        AbsAxis op1 = new LiteralExpr(holder.rtx, holder.rtx.getItemList().addItem(item1));
        AbsAxis op2 = new LiteralExpr(holder.rtx, holder.rtx.getItemList().addItem(item2));
        AbsObAxis axis = new ModOpAxis(holder.rtx, op1, op2);

        assertEquals(true, axis.hasNext());
        assertThat(1.0, is(TypedValue.parseDouble(holder.rtx.getNode().getRawValue())));
        assertEquals(holder.rtx.keyForName("xs:double"), holder.rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());
    }

    @Test
    public final void testGetReturnType() throws AbsTTException {

        AbsAxis op1 = new SequenceAxis(holder.rtx);
        AbsAxis op2 = new SequenceAxis(holder.rtx);
        AbsObAxis axis = new ModOpAxis(holder.rtx, op1, op2);

        assertEquals(Type.DOUBLE, axis.getReturnType(holder.rtx.keyForName("xs:double"), holder.rtx
            .keyForName("xs:double")));
        assertEquals(Type.DOUBLE, axis.getReturnType(holder.rtx.keyForName("xs:decimal"), holder.rtx
            .keyForName("xs:double")));
        assertEquals(Type.FLOAT, axis.getReturnType(holder.rtx.keyForName("xs:float"), holder.rtx
            .keyForName("xs:decimal")));
        assertEquals(Type.DECIMAL, axis.getReturnType(holder.rtx.keyForName("xs:decimal"), holder.rtx
            .keyForName("xs:integer")));
        // assertEquals(Type.INTEGER,
        // axis.getReturnType(holder.rtx.keyForName("xs:integer"),
        // holder.rtx.keyForName("xs:integer")));

        try {

            axis.getReturnType(holder.rtx.keyForName("xs:dateTime"), holder.rtx
                .keyForName("xs:yearMonthDuration"));
            fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules."));
        }

        try {

            axis.getReturnType(holder.rtx.keyForName("xs:dateTime"), holder.rtx.keyForName("xs:double"));
            fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules."));
        }

        try {

            axis.getReturnType(holder.rtx.keyForName("xs:string"), holder.rtx
                .keyForName("xs:yearMonthDuration"));
            fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules."));
        }

        try {

            axis.getReturnType(holder.rtx.keyForName("xs:dateTime"), holder.rtx.keyForName("xs:IDREF"));
            fail("Expected an XPathError-Exception.");
        } catch (XPathError e) {
            assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate the expression or the "
                + "typedoes not match a required type as specified by the matching rules."));
        }

    }

}

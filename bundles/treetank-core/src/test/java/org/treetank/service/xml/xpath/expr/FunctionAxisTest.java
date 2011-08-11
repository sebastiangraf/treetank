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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.axis.AbsAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.utils.TypedValue;

/**
 * JUnit-test class to test the functionality of the AndExpr.
 * 
 * @author Tina Scherer
 * 
 */
public class FunctionAxisTest {

    private Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        TestHelper.createTestDocument();
        holder = Holder.generateRtx();
    }

    @After
    public void tearDown() throws AbsTTException {
        holder.close();
        TestHelper.closeEverything();
    }

    @Test
    public void testFunctions() throws AbsTTException {

        holder.getRtx().moveTo(1L);

        final AbsAxis axis1 = new XPathAxis(holder.getRtx(), "fn:count(text())");
        assertEquals(true, axis1.hasNext());
        assertEquals(3, (int)TypedValue.parseDouble(holder.getRtx().getNode().getRawValue()));
        assertEquals(false, axis1.hasNext());

        final AbsAxis axis2 = new XPathAxis(holder.getRtx(), "fn:count(//node())");
        assertEquals(true, axis2.hasNext());
        assertEquals(10, (int)TypedValue.parseDouble(holder.getRtx().getNode().getRawValue()));
        assertEquals(false, axis2.hasNext());

        final AbsAxis axis3 = new XPathAxis(holder.getRtx(), "fn:string(//node())");
        assertEquals(true, axis3.hasNext());
        assertEquals("oops1 foo oops2 bar oops3 oops1 foo oops2 bar oops3 foo bar", TypedValue
            .parseString(holder.getRtx().getNode().getRawValue()));
        assertEquals(false, axis3.hasNext());

        final AbsAxis axis4 = new XPathAxis(holder.getRtx(), "fn:string()");
        assertEquals(true, axis4.hasNext());
        assertEquals("oops1 foo oops2 bar oops3", TypedValue.parseString(holder.getRtx().getNode()
            .getRawValue()));
        assertEquals(false, axis4.hasNext());

        final AbsAxis axis5 = new XPathAxis(holder.getRtx(), "fn:string(./attribute::attribute())");
        assertEquals(true, axis5.hasNext());
        assertEquals("j", TypedValue.parseString(holder.getRtx().getNode().getRawValue()));
        assertEquals(false, axis5.hasNext());

        holder.getRtx().moveToAttribute(0);
        final AbsAxis axis6 = new XPathAxis(holder.getRtx(), "fn:string()");
        assertEquals(true, axis6.hasNext());
        assertEquals("j", TypedValue.parseString(holder.getRtx().getNode().getRawValue()));
        assertEquals(false, axis6.hasNext());
    }

}

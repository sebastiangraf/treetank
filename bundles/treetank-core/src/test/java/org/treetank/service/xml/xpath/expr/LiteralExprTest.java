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

import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.AbsAxisTest;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.xpath.AtomicValue;
import org.treetank.service.xml.xpath.expr.LiteralExpr;
import org.treetank.service.xml.xpath.types.Type;
import org.treetank.utils.DocumentCreater;
import org.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * JUnit-test class to test the functionality of the LiteralExpr.
 * 
 * @author Tina Scherer
 */
public class LiteralExprTest {

    private AbsAxisTest.Holder holder;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
        TestHelper.createTestDocument();
        holder = AbsAxisTest.generateHolder();
    }

    @After
    public void tearDown() throws AbsTTException {
        holder.rtx.close();
        holder.session.close();
        TestHelper.deleteEverything();
    }

    @Test
    public void testLiteralExpr() throws AbsTTException {
        // Build simple test tree.

        final IItem item1 = new AtomicValue(false);
        final IItem item2 = new AtomicValue(14, Type.INTEGER);

        final int key1 = holder.rtx.getItemList().addItem(item1);
        final int key2 = holder.rtx.getItemList().addItem(item2);

        final AbsAxis axis1 = new LiteralExpr(holder.rtx, key1);
        assertEquals(true, axis1.hasNext());
        assertEquals(key1, holder.rtx.getNode().getNodeKey());
        assertEquals(holder.rtx.keyForName("xs:boolean"), holder.rtx.getNode().getTypeKey());
        assertEquals(false, TypedValue.parseBoolean((holder.rtx.getNode().getRawValue())));
        assertEquals(false, axis1.hasNext());

        final AbsAxis axis2 = new LiteralExpr(holder.rtx, key2);
        assertEquals(true, axis2.hasNext());
        assertEquals(key2, holder.rtx.getNode().getNodeKey());
        assertEquals(holder.rtx.keyForName("xs:integer"), holder.rtx.getNode().getTypeKey());
        assertEquals(14, (int)TypedValue.parseDouble(holder.rtx.getNode().getRawValue()));
        assertEquals(false, axis2.hasNext());

    }

}

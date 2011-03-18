/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: AbstractOpAxisTest.java 4410 2008-08-27 13:42:43Z kramis $
 */

package org.treetank.service.xml.xpath.operators;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.axis.AbsAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.xpath.AtomicValue;
import org.treetank.service.xml.xpath.expr.LiteralExpr;
import org.treetank.service.xml.xpath.operators.AbsObAxis;
import org.treetank.service.xml.xpath.operators.DivOpAxis;
import org.treetank.service.xml.xpath.types.Type;
import org.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AbsOpAxisTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public final void testHasNext() throws AbsTTException {

        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        IReadTransaction rtx = session.beginReadTransaction();
        IItem item1 = new AtomicValue(1.0, Type.DOUBLE);
        IItem item2 = new AtomicValue(2.0, Type.DOUBLE);

        AbsAxis op1 = new LiteralExpr(rtx, rtx.getItemList().addItem(item1));
        AbsAxis op2 = new LiteralExpr(rtx, rtx.getItemList().addItem(item2));
        AbsObAxis axis = new DivOpAxis(rtx, op1, op2);

        assertEquals(true, axis.hasNext());
        assertEquals(rtx.keyForName("xs:double"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());

        // here both operands are the empty sequence
        axis = new DivOpAxis(rtx, op1, op2);
        assertEquals(true, axis.hasNext());
        assertThat(Double.NaN, is(TypedValue.parseDouble(rtx.getNode().getRawValue())));
        assertEquals(rtx.keyForName("xs:double"), rtx.getNode().getTypeKey());
        assertEquals(false, axis.hasNext());
        rtx.close();
        session.close();
        database.close();

    }

}

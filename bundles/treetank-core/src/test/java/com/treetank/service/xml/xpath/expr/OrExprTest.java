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
 * $Id: OrExprTest.java 4487 2008-10-02 09:12:29Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.exception.AbsTTException;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.service.xml.xpath.XPathError;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * JUnit-test class to test the functionality of the AndExpr.
 * 
 * @author Tina Scherer
 */
public class OrExprTest {

    @Before
    public void setUp() throws AbsTTException {

        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testOr() throws AbsTTException {

        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        IReadTransaction rtx = session.beginReadTransaction();

        long iTrue = rtx.getItemList().addItem(new AtomicValue(true));
        long iFalse = rtx.getItemList().addItem(new AtomicValue(false));

        AbsAxis trueLit1 = new LiteralExpr(rtx, iTrue);
        AbsAxis trueLit2 = new LiteralExpr(rtx, iTrue);
        AbsAxis falseLit1 = new LiteralExpr(rtx, iFalse);
        AbsAxis falseLit2 = new LiteralExpr(rtx, iFalse);

        AbsAxis axis1 = new OrExpr(rtx, trueLit1, trueLit2);
        assertEquals(true, axis1.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis1.hasNext());

        AbsAxis axis2 = new OrExpr(rtx, trueLit1, falseLit1);
        assertEquals(true, axis2.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis2.hasNext());

        AbsAxis axis3 = new OrExpr(rtx, falseLit1, trueLit1);
        assertEquals(true, axis3.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis3.hasNext());

        AbsAxis axis4 = new OrExpr(rtx, falseLit1, falseLit2);
        assertEquals(true, axis4.hasNext());
        assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis4.hasNext());

        rtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testOrQuery() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        IReadTransaction rtx = session.beginReadTransaction();

        rtx.moveTo(1L);

        final AbsAxis axis1 = new XPathAxis(rtx, "text() or node()");
        assertEquals(true, axis1.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis1.hasNext());

        final AbsAxis axis2 = new XPathAxis(rtx, "comment() or node()");
        assertEquals(true, axis2.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis2.hasNext());

        final AbsAxis axis3 = new XPathAxis(rtx, "1 eq 1 or 2 eq 2");
        assertEquals(true, axis3.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis3.hasNext());

        final AbsAxis axis4 = new XPathAxis(rtx, "1 eq 1 or 2 eq 3");
        assertEquals(true, axis4.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis4.hasNext());

        final AbsAxis axis5 = new XPathAxis(rtx, "1 eq 2 or (3 idiv 0 = 1)");
        try {
            assertEquals(true, axis5.hasNext());
            assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
            assertEquals(false, axis5.hasNext());
            fail("Exprected XPathError");
        } catch (XPathError e) {
            assertEquals("err:FOAR0001: Division by zero.", e.getMessage());
        }

        final AbsAxis axis6 = new XPathAxis(rtx, "1 eq 1 or (3 idiv 0 = 1)");
        assertEquals(true, axis6.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));

        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();
        database.close();

    }

}

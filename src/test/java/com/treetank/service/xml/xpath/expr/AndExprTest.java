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
 * $Id: AndExprTest.java 4487 2008-10-02 09:12:29Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.access.Session;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.service.xml.xpath.functions.XPathError;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

/**
 * JUnit-test class to test the functionality of the AndExpr.
 * 
 * @author Tina Scherer
 * 
 */
public class AndExprTest {

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testAnd() throws TreetankException {
        final ISession session = Session.beginSession(ITestConstants.PATH1);
        IReadTransaction rtx = session.beginReadTransaction();

        long iTrue = rtx.getItemList().addItem(new AtomicValue(true));
        long iFalse = rtx.getItemList().addItem(new AtomicValue(false));

        IAxis trueLit1 = new LiteralExpr(rtx, iTrue);
        IAxis trueLit2 = new LiteralExpr(rtx, iTrue);
        IAxis falseLit1 = new LiteralExpr(rtx, iFalse);
        IAxis falseLit2 = new LiteralExpr(rtx, iFalse);

        IAxis axis1 = new AndExpr(rtx, trueLit1, trueLit2);
        assertEquals(true, axis1.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx
                .getNode().getRawValue()))));
        assertEquals(false, axis1.hasNext());

        IAxis axis2 = new AndExpr(rtx, trueLit1, falseLit1);
        assertEquals(true, axis2.hasNext());
        assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx
                .getNode().getRawValue()))));
        assertEquals(false, axis2.hasNext());

        IAxis axis3 = new AndExpr(rtx, falseLit1, trueLit1);
        assertEquals(true, axis3.hasNext());
        assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx
                .getNode().getRawValue()))));
        assertEquals(false, axis3.hasNext());

        IAxis axis4 = new AndExpr(rtx, falseLit1, falseLit2);
        assertEquals(true, axis4.hasNext());
        assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx
                .getNode().getRawValue()))));
        assertEquals(false, axis4.hasNext());

        rtx.close();
        session.close();
    }

    @Test
    public void testAndQuery() throws TreetankException {
        // Build simple test tree.
        final ISession session = Session.beginSession(ITestConstants.PATH1);
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        IReadTransaction rtx = session.beginReadTransaction();

        rtx.moveTo(1L);

        final IAxis axis1 = new XPathAxis(rtx, "text() and node()");
        assertEquals(true, axis1.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx
                .getNode().getRawValue()))));
        assertEquals(false, axis1.hasNext());

        final IAxis axis2 = new XPathAxis(rtx, "comment() and node()");
        assertEquals(true, axis2.hasNext());
        assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx
                .getNode().getRawValue()))));
        assertEquals(false, axis2.hasNext());

        final IAxis axis3 = new XPathAxis(rtx, "1 eq 1 and 2 eq 2");
        assertEquals(true, axis3.hasNext());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx
                .getNode().getRawValue()))));
        assertEquals(false, axis3.hasNext());

        final IAxis axis4 = new XPathAxis(rtx, "1 eq 1 and 2 eq 3");
        assertEquals(true, axis4.hasNext());
        assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx
                .getNode().getRawValue()))));
        assertEquals(false, axis4.hasNext());

        // is never evaluated.
        final IAxis axis5 = new XPathAxis(rtx, "1 eq 2 and (3 idiv 0 = 1)");
        assertEquals(true, axis5.hasNext());
        assertEquals(false, Boolean.parseBoolean(TypedValue.parseString((rtx
                .getNode().getRawValue()))));
        assertEquals(false, axis5.hasNext());

        final IAxis axis6 = new XPathAxis(rtx, "1 eq 1 and 3 idiv 0 = 1");
        try {
            assertEquals(true, axis6.hasNext());
            fail("Expected XPath exception, because of division by zero");
        } catch (XPathError e) {
            assertEquals("err:FOAR0001: Division by zero.", e.getMessage());
        }

        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();

    }

}

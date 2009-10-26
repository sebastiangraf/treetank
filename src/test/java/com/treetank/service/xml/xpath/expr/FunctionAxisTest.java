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
 * $Id: FunctionAxisTest.java 4487 2008-10-02 09:12:29Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankIOException;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

/**
 * JUnit-test class to test the functionality of the AndExpr.
 * 
 * @author Tina Scherer
 * 
 */
public class FunctionAxisTest {

    @Before
    public void setUp() {

        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }

    @Test
    public void testFunctions() {
        try {
            // Build simple test tree.
            final ISession session = Session.beginSession(ITestConstants.PATH1);
            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);
            wtx.commit();
            IReadTransaction rtx = session.beginReadTransaction();

            rtx.moveTo(1L);

            final IAxis axis1 = new XPathAxis(rtx, "fn:count(text())");
            assertEquals(true, axis1.hasNext());
            assertEquals(3, (int) TypedValue.parseDouble(rtx.getNode()
                    .getRawValue()));
            assertEquals(false, axis1.hasNext());

            final IAxis axis2 = new XPathAxis(rtx, "fn:count(//node())");
            assertEquals(true, axis2.hasNext());
            assertEquals(10, (int) TypedValue.parseDouble(rtx.getNode()
                    .getRawValue()));
            assertEquals(false, axis2.hasNext());

            final IAxis axis3 = new XPathAxis(rtx, "fn:string(//node())");
            assertEquals(true, axis3.hasNext());
            assertEquals(
                    "oops1 foo oops2 bar oops3 oops1 foo oops2 bar oops3 foo bar",
                    TypedValue.parseString(rtx.getNode().getRawValue()));
            assertEquals(false, axis3.hasNext());

            final IAxis axis4 = new XPathAxis(rtx, "fn:string()");
            assertEquals(true, axis4.hasNext());
            assertEquals("oops1 foo oops2 bar oops3", TypedValue
                    .parseString(rtx.getNode().getRawValue()));
            assertEquals(false, axis4.hasNext());

            final IAxis axis5 = new XPathAxis(rtx,
                    "fn:string(./attribute::attribute())");
            assertEquals(true, axis5.hasNext());
            assertEquals("j", TypedValue.parseString(rtx.getNode()
                    .getRawValue()));
            assertEquals(false, axis5.hasNext());

            rtx.moveToAttribute(0);
            final IAxis axis6 = new XPathAxis(rtx, "fn:string()");
            assertEquals(true, axis6.hasNext());
            assertEquals("j", TypedValue.parseString(rtx.getNode()
                    .getRawValue()));
            assertEquals(false, axis6.hasNext());

            rtx.close();
            wtx.close();
            session.close();
        } catch (final TreetankIOException exc) {
            fail(exc.toString());
        }

    }

}

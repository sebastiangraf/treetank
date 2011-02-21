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
 * $Id: RangeAxisTest.java 4417 2008-08-27 21:19:26Z scherer $
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
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * JUnit-test class to test the functionality of the RangeAxis.
 * 
 * @author Tina Scherer
 */
public class RangeAxisTest {

    @Before
    public void setUp() throws AbsTTException {

        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testRangeExpr() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        IReadTransaction rtx = session.beginReadTransaction();

        // TODO: tests are false, because the integers are not converted
        // correctly
        // from the byte array
        // final IAxis axis1 = new XPathAxis(rtx, "1 to 4");
        // assertEquals(true, axis1.hasNext());
        // assertEquals(1, TypedValue.parseInt(rtx.getRawValue()));
        // assertEquals(true, axis1.hasNext());
        // assertEquals(2, TypedValue.parseInt(rtx.getRawValue()));
        // assertEquals(true, axis1.hasNext());
        // assertEquals(3, TypedValue.parseInt(rtx.getRawValue()));
        // assertEquals(true, axis1.hasNext());
        // assertEquals(4, TypedValue.parseInt(rtx.getRawValue()));
        // assertEquals(false, axis1.hasNext());
        //
        // final IAxis axis2 = new XPathAxis(rtx, "10 to 10");
        // assertEquals(true, axis2.hasNext());
        // assertEquals(10, TypedValue.parseInt(rtx.getRawValue()));
        // assertEquals(false, axis2.hasNext());

        rtx.moveTo(1L);
        final AbsAxis axis3 = new XPathAxis(rtx, "15 to 10");
        assertEquals(false, axis3.hasNext());

        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

}

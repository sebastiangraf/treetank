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
 * $Id: CastableExprTest.java 4487 2008-10-02 09:12:29Z scherer $
 */

package org.treetank.service.xml.xpath.expr;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.service.xml.xpath.XPathError;
import org.treetank.utils.DocumentCreater;
import org.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * JUnit-test class to test the functionality of the CastableExpr.
 * 
 * @author Tina Scherer
 */
public class CastableExprTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testCastableExpr() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        IReadTransaction rtx = session.beginReadTransaction();

        final AbsAxis axis1 = new XPathAxis(rtx, "1 castable as xs:decimal");
        assertEquals(true, axis1.hasNext());
        assertEquals(rtx.keyForName("xs:boolean"), rtx.getNode().getTypeKey());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis1.hasNext());

        final AbsAxis axis2 = new XPathAxis(rtx, "10.0 castable as xs:anyAtomicType");
        try {
            assertEquals(true, axis2.hasNext());
        } catch (XPathError e) {
            assertThat(e.getMessage(), is("err:XPST0080 "
                + "Target type of a cast or castable expression must not be "
                + "xs:NOTATION or xs:anyAtomicType."));
        }

        // Token is not implemented yet.
        // final IAxis axis3 = new XPathAxis(rtx,
        // "\"hello\" castable as xs:token");
        // assertEquals(true, axis3.hasNext());
        // assertEquals(Type.BOOLEAN, rtx.getValueTypeAsType());
        // assertEquals(true, rtx.getValueAsBoolean());
        // assertEquals(false, axis3.hasNext());

        final AbsAxis axis4 = new XPathAxis(rtx, "\"hello\" castable as xs:string");
        assertEquals(true, axis4.hasNext());
        assertEquals(rtx.keyForName("xs:boolean"), rtx.getNode().getTypeKey());
        assertEquals(true, Boolean.parseBoolean(TypedValue.parseString((rtx.getNode().getRawValue()))));
        assertEquals(false, axis4.hasNext());

        // final IAxis axis5 = new XPathAxis(rtx,
        // "\"hello\" castable as xs:decimal");
        // assertEquals(true, axis5.hasNext());
        // assertEquals(rtx.keyForName("xs:boolean"), rtx.getTypeKey());
        // assertEquals(true, Boolean.parseBoolean(rtx.getValue()));
        // assertEquals(false, axis5.hasNext());

        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

}

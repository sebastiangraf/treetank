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
 * $Id: VarRefExprTest.java 4417 2008-08-27 21:19:26Z scherer $
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
import org.treetank.service.xml.xpath.expr.VarRefExpr;
import org.treetank.service.xml.xpath.expr.VariableAxis;
import org.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * JUnit-test class to test the functionality of the VarRefExpr.
 * 
 * @author Tina Scherer
 */
public class VarRefExprTest {

    @Before
    public void setUp() throws AbsTTException {

        TestHelper.deleteEverything();
    }

    @Test
    public void testEveryExpr() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        IReadTransaction rtx = session.beginReadTransaction();

        final AbsAxis axis = new XPathAxis(rtx, "for $a in b return $a");

        final VariableAxis variable = new VariableAxis(rtx, axis);

        final VarRefExpr axis1 = new VarRefExpr(rtx, variable);
        // assertEquals(false, axis1.hasNext());
        axis1.update(5L);
        assertEquals(true, axis1.hasNext());
        assertEquals(5L, rtx.getNode().getNodeKey());
        axis1.update(13L);
        assertEquals(true, axis1.hasNext());
        assertEquals(13L, rtx.getNode().getNodeKey());
        axis1.update(1L);
        assertEquals(true, axis1.hasNext());
        assertEquals(1L, rtx.getNode().getNodeKey());
        assertEquals(false, axis1.hasNext());

        final VarRefExpr axis2 = new VarRefExpr(rtx, variable);
        // assertEquals(false, axis2.hasNext());
        axis2.update(13L);
        assertEquals(true, axis2.hasNext());
        assertEquals(13L, rtx.getNode().getNodeKey());
        assertEquals(false, axis2.hasNext());
        axis2.update(12L);
        assertEquals(true, axis2.hasNext());
        assertEquals(12L, rtx.getNode().getNodeKey());
        assertEquals(false, axis2.hasNext());

        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

}

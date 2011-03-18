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
 * $Id: NodeCompTest.java 4410 2008-08-27 13:42:43Z kramis $
 */

package org.treetank.service.xml.xpath.comparators;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTXPathException;
import org.treetank.service.xml.xpath.AtomicValue;
import org.treetank.service.xml.xpath.comparators.AbsComparator;
import org.treetank.service.xml.xpath.comparators.CompKind;
import org.treetank.service.xml.xpath.comparators.NodeComp;
import org.treetank.service.xml.xpath.expr.LiteralExpr;
import org.treetank.service.xml.xpath.types.Type;
import org.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NodeCompTest {

    private AbsComparator comparator;

    private IDatabase database;

    private ISession session;

    private IWriteTransaction wtx;

    private IReadTransaction rtx;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();

        // Build simple test tree.
        database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        session = database.getSession();
        wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        // Find descendants starting from nodeKey 0L (root).
        wtx.commit();
        wtx.moveToDocumentRoot();
        rtx = session.beginReadTransaction();

        comparator = new NodeComp(rtx, new LiteralExpr(rtx, -2), new LiteralExpr(rtx, -1), CompKind.IS);
    }

    @After
    public void tearDown() throws AbsTTException {
        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();
        database.close();
        TestHelper.closeEverything();
    }

    @Test
    public void testCompare() throws TTXPathException {

        AtomicValue[] op1 = {
            new AtomicValue(2, Type.INTEGER)
        };
        AtomicValue[] op2 = {
            new AtomicValue(3, Type.INTEGER)
        };
        AtomicValue[] op3 = {
            new AtomicValue(3, Type.INTEGER)
        };

        assertEquals(false, comparator.compare(op1, op2));
        assertEquals(true, comparator.compare(op3, op2));

        try {
            comparator = new NodeComp(rtx, new LiteralExpr(rtx, -2), new LiteralExpr(rtx, -1), CompKind.PRE);
            comparator.compare(op1, op2);
            fail("Expexcted not yet implemented exception.");
        } catch (IllegalStateException e) {
            assertEquals("Evaluation of node comparisons not possible", e.getMessage());
        }

        try {
            comparator = new NodeComp(rtx, new LiteralExpr(rtx, -2), new LiteralExpr(rtx, -1), CompKind.FO);
            comparator.compare(op1, op2);
            fail("Expexcted not yet implemented exception.");
        } catch (IllegalStateException e) {
            assertEquals("Evaluation of node comparisons not possible", e.getMessage());
        }

    }

    @Test
    public void testAtomize() throws TTXPathException {

        AbsAxis axis = new LiteralExpr(rtx, -2);
        axis.hasNext(); // this is needed, because hasNext() has already been
        // called
        AtomicValue[] value = comparator.atomize(axis);
        assertEquals(value.length, 1);
        assertEquals(rtx.getNode().getNodeKey(), value[0].getNodeKey());
        assertEquals("xs:integer", value[0].getType());

        try {
            axis = new DescendantAxis(rtx, false);
            axis.hasNext();
            comparator.atomize(axis);
        } catch (TTXPathException e) {
            assertEquals("err:XPTY0004 The type is not appropriate the expression or"
                + " the typedoes not match a required type as specified by the " + "matching rules. ", e
                .getMessage());
        }

    }

    @Test
    public void testGetType() throws TTXPathException {

        assertEquals(Type.INTEGER, comparator.getType(123, 2435));
    }
}

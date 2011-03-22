/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

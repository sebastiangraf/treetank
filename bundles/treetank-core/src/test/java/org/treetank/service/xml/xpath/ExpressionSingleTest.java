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

package org.treetank.service.xml.xpath;

import java.io.File;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.ChildAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.axis.FollowingSiblingAxis;
import org.treetank.axis.NestedAxis;
import org.treetank.axis.ParentAxis;
import org.treetank.axis.SelfAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.xpath.ExpressionSingle;
import org.treetank.service.xml.xpath.expr.UnionAxis;
import org.treetank.service.xml.xpath.filter.DupFilterAxis;
import org.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExpressionSingleTest {

    ExpressionSingle builder;

    public static final String XML =
        "src" + File.separator + "test" + File.separator + "resoruces" + File.separator + "factbook.xml";

    @Before
    public void setUp() throws AbsTTException {

        builder = new ExpressionSingle();
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testAdd() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        // test one axis
        AbsAxis self = new SelfAxis(wtx);
        builder.add(self);
        assertEquals(builder.getExpr(), self);

        // test 2 axis
        AbsAxis axis1 = new SelfAxis(wtx);
        AbsAxis axis2 = new SelfAxis(wtx);
        builder.add(axis1);
        builder.add(axis2);
        assertTrue(builder.getExpr() instanceof NestedAxis);

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testDup() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        builder = new ExpressionSingle();
        builder.add(new ChildAxis(wtx));
        builder.add(new DescendantAxis(wtx));
        assertTrue(builder.getExpr() instanceof NestedAxis);

        builder = new ExpressionSingle();
        builder.add(new ChildAxis(wtx));
        builder.add(new DescendantAxis(wtx));
        assertEquals(true, builder.isOrdered());
        assertTrue(builder.getExpr() instanceof NestedAxis);

        builder = new ExpressionSingle();
        builder.add(new ChildAxis(wtx));
        builder.add(new DescendantAxis(wtx));
        builder.add(new ChildAxis(wtx));
        assertEquals(false, builder.isOrdered());

        builder = new ExpressionSingle();
        builder = new ExpressionSingle();
        builder.add(new ChildAxis(wtx));
        builder.add(new DescendantAxis(wtx));
        builder.add(new ChildAxis(wtx));
        builder.add(new ParentAxis(wtx));
        assertEquals(true, builder.isOrdered());

        builder = new ExpressionSingle();
        builder.add(new ChildAxis(wtx));
        builder.add(new DescendantAxis(wtx));
        builder.add(new FollowingSiblingAxis(wtx));
        assertEquals(false, builder.isOrdered());

        builder = new ExpressionSingle();
        builder.add(new UnionAxis(wtx, new DescendantAxis(wtx), new ParentAxis(wtx)));
        assertEquals(false, builder.isOrdered());
        assertTrue(builder.getExpr() instanceof DupFilterAxis);

        wtx.abort();
        wtx.close();
        session.close();
        database.close();

    }
}

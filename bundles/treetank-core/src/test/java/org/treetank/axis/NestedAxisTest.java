/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: NestedAxisTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package org.treetank.axis;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.AttributeAxis;
import org.treetank.axis.ChildAxis;
import org.treetank.axis.FilterAxis;
import org.treetank.axis.NestedAxis;
import org.treetank.axis.filter.NameFilter;
import org.treetank.axis.filter.NodeFilter;
import org.treetank.axis.filter.TextFilter;
import org.treetank.exception.AbsTTException;
import org.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NestedAxisTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testNestedAxisTest() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        // Find descendants starting from nodeKey 0L (root).
        wtx.moveToDocumentRoot();

        // XPath expression /p:a/b/text()
        // Part: /p:a
        final AbsAxis childA = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));
        // Part: /b
        final AbsAxis childB = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "b"));
        // Part: /text()
        final AbsAxis text = new FilterAxis(new ChildAxis(wtx), new TextFilter(wtx));
        // Part: /p:a/b/text()
        final AbsAxis axis = new NestedAxis(new NestedAxis(childA, childB), text);

        AbsAxisTest.testIAxisConventions(axis, new long[] {
            6L, 12L
        });

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testNestedAxisTest2() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        // Find descendants starting from nodeKey 0L (root).
        wtx.moveToDocumentRoot();

        // XPath expression /[:a/b/@p:x]
        // Part: /p:a
        final AbsAxis childA = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));
        // Part: /b
        final AbsAxis childB = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "b"));
        // Part: /@x
        final AbsAxis attributeX = new FilterAxis(new AttributeAxis(wtx), new NameFilter(wtx, "p:x"));
        // Part: /p:a/b/@p:x
        final AbsAxis axis = new NestedAxis(new NestedAxis(childA, childB), attributeX);

        AbsAxisTest.testIAxisConventions(axis, new long[] {
            10L
        });

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testNestedAxisTest3() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        // Find desceFndants starting from nodeKey 0L (root).
        wtx.moveToDocumentRoot();

        // XPath expression p:a/node():
        // Part: /p:a
        final AbsAxis childA = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));

        // Part: /node()
        final AbsAxis childNode = new FilterAxis(new ChildAxis(wtx), new NodeFilter(wtx));

        // Part: /p:a/node():
        final AbsAxis axis = new NestedAxis(childA, childNode);

        AbsAxisTest.testIAxisConventions(axis, new long[] {
            4L, 5L, 8L, 9L, 13L
        });

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

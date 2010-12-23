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

package com.treetank.axis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.filter.NameFilter;
import com.treetank.axis.filter.NodeFilter;
import com.treetank.axis.filter.TextFilter;
import com.treetank.exception.TTException;
import com.treetank.utils.DocumentCreater;

public class NestedAxisTest {

    @Before
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testNestedAxisTest() throws TTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        // Find descendants starting from nodeKey 0L (root).
        wtx.moveToDocumentRoot();

        // XPath expression /p:a/b/text()
        // Part: /p:a
        final IAxis childA = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));
        // Part: /b
        final IAxis childB = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "b"));
        // Part: /text()
        final IAxis text = new FilterAxis(new ChildAxis(wtx), new TextFilter(wtx));
        // Part: /p:a/b/text()
        final IAxis axis = new NestedAxis(new NestedAxis(childA, childB), text);

        IAxisTest.testIAxisConventions(axis, new long[] {
            6L, 12L
        });

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testNestedAxisTest2() throws TTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        // Find descendants starting from nodeKey 0L (root).
        wtx.moveToDocumentRoot();

        // XPath expression /[:a/b/@p:x]
        // Part: /p:a
        final IAxis childA = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));
        // Part: /b
        final IAxis childB = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "b"));
        // Part: /@x
        final IAxis attributeX = new FilterAxis(new AttributeAxis(wtx), new NameFilter(wtx, "p:x"));
        // Part: /p:a/b/@p:x
        final IAxis axis = new NestedAxis(new NestedAxis(childA, childB), attributeX);

        IAxisTest.testIAxisConventions(axis, new long[] {
            10L
        });

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testNestedAxisTest3() throws TTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        // Find desceFndants starting from nodeKey 0L (root).
        wtx.moveToDocumentRoot();

        // XPath expression p:a/node():
        // Part: /p:a
        final IAxis childA = new FilterAxis(new ChildAxis(wtx), new NameFilter(wtx, "p:a"));

        // Part: /node()
        final IAxis childNode = new FilterAxis(new ChildAxis(wtx), new NodeFilter(wtx));

        // Part: /p:a/node():
        final IAxis axis = new NestedAxis(childA, childNode);

        IAxisTest.testIAxisConventions(axis, new long[] {
            4L, 5L, 8L, 9L, 13L
        });

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @After
    public void tearDown() throws TTException {
        TestHelper.closeEverything();
    }
}

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
 * $Id: FilterAxisTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package org.treetank.axis.filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxisTest;
import org.treetank.axis.AttributeAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.axis.FilterAxis;
import org.treetank.axis.filter.NameFilter;
import org.treetank.axis.filter.ValueFilter;
import org.treetank.exception.AbsTTException;
import org.treetank.utils.DocumentCreater;

public class FilterAxisTest {
    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testNameAxisTest() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveToDocumentRoot();
        AbsAxisTest.testIAxisConventions(new FilterAxis(new DescendantAxis(wtx), new NameFilter(wtx, "b")),
            new long[] {
                5L, 9L
            });

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testValueAxisTest() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveToDocumentRoot();
        AbsAxisTest.testIAxisConventions(new FilterAxis(new DescendantAxis(wtx), new ValueFilter(wtx, "foo")),
            new long[] {
                6L
            });

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @Test
    public void testValueAndNameAxisTest() throws AbsTTException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveTo(1L);
        AbsAxisTest.testIAxisConventions(new FilterAxis(new AttributeAxis(wtx), new NameFilter(wtx, "i"),
            new ValueFilter(wtx, "j")), new long[] {
            2L
        });

        wtx.moveTo(9L);
        AbsAxisTest.testIAxisConventions(new FilterAxis(new AttributeAxis(wtx), new NameFilter(wtx, "y"),
            new ValueFilter(wtx, "y")), new long[] {});

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

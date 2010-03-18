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
 * $Id: AncestorAxisTest.java 3507 2007-11-15 08:47:27Z kramis $
 */

package com.treetank.axis;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.utils.DocumentCreater;

public class IAxisTest {

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    public static void testIAxisConventions(final IAxis axis,
            final long[] expectedKeys) {

        final IReadTransaction rtx = axis.getTransaction();

        // IAxis Convention 1.
        final long startKey = rtx.getNode().getNodeKey();

        final long[] keys = new long[expectedKeys.length];
        int offset = 0;
        while (axis.hasNext()) {
            axis.next();
            // IAxis results.
            assertTrue(offset < expectedKeys.length);
            keys[offset++] = rtx.getNode().getNodeKey();

            // IAxis Convention 2.
            try {
                axis.next();
                fail("Should only allow to call next() once.");
            } catch (final IllegalStateException exc) {
                // Must throw exception.
            }

            // IAxis Convention 3.
            rtx.moveToDocumentRoot();

        }

        // IAxis Convention 5.
        assertEquals(startKey, rtx.getNode().getNodeKey());

        // IAxis results.
        assertArrayEquals(expectedKeys, keys);

    }

    @Test
    public void testIAxisUserExample() throws TreetankException {

        final IDatabase database = Database.openDatabase(ITestConstants.PATH1);
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveToDocumentRoot();
        final IAxis axis = new DescendantAxis(wtx);
        long count = 0L;
        while (axis.hasNext()) {
            count += 1;
        }
        Assert.assertEquals(10L, count);

        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }
}

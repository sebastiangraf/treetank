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
 * $Id: ForAxisTest.java 4487 2008-10-02 09:12:29Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.IAxisTest;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

/**
 * JUnit-test class to test the functionality of the DubFilter.
 * 
 * @author Tina Scherer
 * 
 */
public class ForAxisTest {

    @Before
    public void setUp() throws TreetankException {

        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testFor() throws TreetankException {
        // Build simple test tree.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());

        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        IReadTransaction rtx = session.beginReadTransaction();

        rtx.moveTo(1L);

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in child::text() return child::node()"),
            new long[] {
                4L, 5L, 8L, 9L, 13L, 4L, 5L, 8L, 9L, 13L, 4L, 5L, 8L, 9L, 13L
            });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in child::node() return $a/node()"),
            new long[] {
                6L, 7L, 11L, 12L
            });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in child::node() return $a/text()"),
            new long[] {
                6L, 12L
            });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in child::node() return $a/c"), new long[] {
            7L, 11L
        });

        // IAxisTest.testIAxisConventions(new XPathAxis(
        // rtx,
        // "for $a in child::node(), $b in /node(), $c in ., $d in /c return $a/c"),
        // new long[] {7L, 11L});

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in child::node() return $a[@p:x]"),
            new long[] {
                9L
            });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "for $a in . return $a"), new long[] {
            1L
        });

        IAxis axis = new XPathAxis(rtx, "for $i in (10, 20), $j in (1, 2) return ($i + $j)");
        assertEquals(true, axis.hasNext());

        assertEquals("11.0", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(true, axis.hasNext());
        assertEquals("12.0", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(true, axis.hasNext());
        assertEquals("21.0", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(true, axis.hasNext());
        assertEquals("22.0", TypedValue.parseString(rtx.getNode().getRawValue()));
        assertEquals(false, axis.hasNext());

        rtx.close();
        wtx.abort();
        wtx.close();
        session.close();
        database.close();
    }

}

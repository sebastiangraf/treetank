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
 * $Id: PrecedingAxisTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package org.treetank.axis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.PrecedingAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.utils.DocumentCreater;

public class PrecedingAxisTest {

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @Test
    public void testAxisConventions() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);

        wtx.moveTo(12L);
        AbsAxisTest.testIAxisConventions(new PrecedingAxis(wtx), new long[] {
            11L, 8L, 7L, 6L, 5L, 4L
        });

        wtx.moveTo(5L);
        AbsAxisTest.testIAxisConventions(new PrecedingAxis(wtx), new long[] {
            4L
        });

        wtx.moveTo(13L);
        AbsAxisTest.testIAxisConventions(new PrecedingAxis(wtx), new long[] {
            12L, 11L, 9L, 8L, 7L, 6L, 5L, 4L
        });

        wtx.moveTo(1L);
        AbsAxisTest.testIAxisConventions(new PrecedingAxis(wtx), new long[] {});

        wtx.moveTo(9L);
        wtx.moveToAttribute(0);
        AbsAxisTest.testIAxisConventions(new PrecedingAxis(wtx), new long[] {});

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

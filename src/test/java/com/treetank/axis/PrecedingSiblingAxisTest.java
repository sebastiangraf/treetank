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
 * $Id: PrecedingSiblingAxisTest.java 4417 2008-08-27 21:19:26Z scherer $
 */

package com.treetank.axis;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankIOException;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

public class PrecedingSiblingAxisTest {

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @Test
    public void testAxisConventions() {
        try {
            final ISession session = Session.beginSession(ITestConstants.PATH1);
            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);

            wtx.moveTo(12L);
            IAxisTest.testIAxisConventions(new PrecedingSiblingAxis(wtx),
                    new long[] { 11L });

            wtx.moveTo(5L);
            IAxisTest.testIAxisConventions(new PrecedingSiblingAxis(wtx),
                    new long[] { 4L });

            wtx.moveTo(13L);
            IAxisTest.testIAxisConventions(new PrecedingSiblingAxis(wtx),
                    new long[] { 9L, 8L, 5L, 4L });

            wtx.moveTo(1L);
            IAxisTest.testIAxisConventions(new PrecedingSiblingAxis(wtx),
                    new long[] {});

            wtx.moveTo(9L);
            wtx.moveToAttribute(0);
            IAxisTest.testIAxisConventions(new PrecedingSiblingAxis(wtx),
                    new long[] {});

            wtx.abort();
            wtx.close();
            session.close();
        } catch (final TreetankIOException exc) {
            fail(exc.toString());
        }
    }

    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }
}

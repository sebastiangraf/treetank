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
 * $Id: ChildAxisTest.java 4487 2008-10-02 09:12:29Z scherer $
 */

package com.treetank.axis;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.perfidix.annotation.Bench;
import org.perfidix.annotation.BenchClass;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.io.TreetankIOException;
import com.treetank.session.Session;
import com.treetank.utils.DocumentCreater;

@BenchClass(runs = 1)
public class ChildAxisTest {
    @Before
    public void setUp() {
       TestHelper.removeAllFiles();
    }

    @Test
    @Bench(runs = 10)
    public void testIterate() {
        try {
            final ISession session = Session.beginSession(ITestConstants.PATH1);
            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);

            wtx.moveTo(1L);
            IAxisTest.testIAxisConventions(new ChildAxis(wtx), new long[] { 4L,
                    5L, 8L, 9L, 13L });

            wtx.moveTo(5L);
            IAxisTest.testIAxisConventions(new ChildAxis(wtx), new long[] { 6L,
                    7L });

            wtx.moveTo(13L);
            IAxisTest.testIAxisConventions(new ChildAxis(wtx), new long[] {});

            wtx.abort();
            wtx.close();
            session.close();
        } catch (final TreetankIOException exc) {
            fail(exc.toString());
        }
    }

    @Test
    @Bench(runs = 10)
    public void testPersistent() {
        try {
            final ISession session = Session.beginSession(ITestConstants.PATH1);
            final IWriteTransaction wtx = session.beginWriteTransaction();
            DocumentCreater.create(wtx);
            wtx.commit();
            wtx.close();
            session.close();
        } catch (final TreetankIOException exc) {
            fail(exc.toString());
        }

        final ISession session1 = Session.beginSession(ITestConstants.PATH1);
        final IReadTransaction rtx = session1.beginReadTransaction();

        rtx.moveTo(1L);
        IAxisTest.testIAxisConventions(new ChildAxis(rtx), new long[] { 4L, 5L,
                8L, 9L, 13L });

        rtx.moveTo(5L);
        IAxisTest.testIAxisConventions(new ChildAxis(rtx),
                new long[] { 6L, 7L });

        rtx.moveTo(13L);
        IAxisTest.testIAxisConventions(new ChildAxis(rtx), new long[] {});

        rtx.close();
        session1.close();

    }

}

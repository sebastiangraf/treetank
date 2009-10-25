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
 * $Id: ThreadTest.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.session;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.utils.DocumentCreater;
import com.treetank.utils.TypedValue;

public class ThreadTest {

    public static final int WORKER_COUNT = 50;

    @Before
    public void setUp() {
        Session.removeSession(ITestConstants.PATH1);
    }

    @Test
    public void testThreads() throws Exception {

        ISession session = Session.beginSession(ITestConstants.PATH1);

        IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        ExecutorService taskExecutor = Executors
                .newFixedThreadPool(WORKER_COUNT);
        for (int i = 0; i < WORKER_COUNT; i++) {
            taskExecutor.execute(new Task(session.beginReadTransaction(0L)));
            wtx = session.beginWriteTransaction();
            wtx.moveTo(10L);
            wtx.setValue("value" + i);
            wtx.commit();
            wtx.close();
        }
        taskExecutor.shutdown();
        taskExecutor.awaitTermination(1000000, TimeUnit.SECONDS);

        session.close();
    }

    private class Task implements Runnable {

        private IReadTransaction mRTX;

        public Task(final IReadTransaction rtx) {
            mRTX = rtx;
        }

        public void run() {
            try {
                for (final long key : new DescendantAxis(mRTX)) {
                    // Nothing to do.
                }
                mRTX.moveTo(12L);
                TestCase.assertEquals("bar", TypedValue.parseString(mRTX
                        .getNode().getRawValue()));
                mRTX.close();
            } catch (Exception e) {
                TestCase.fail(e.getLocalizedMessage());
            }
        }
    }

}

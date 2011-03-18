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

package org.treetank.access;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.utils.DocumentCreater;
import org.treetank.utils.TypedValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThreadTest {

    public static final int WORKER_COUNT = 50;

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testThreads() throws Exception {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        IWriteTransaction wtx = session.beginWriteTransaction();

        DocumentCreater.create(wtx);
        wtx.commit();
        wtx.close();

        ExecutorService taskExecutor = Executors.newFixedThreadPool(WORKER_COUNT);
        long newKey = 10L;
        for (int i = 0; i < WORKER_COUNT; i++) {
            taskExecutor.submit(new Task(session.beginReadTransaction(i)));
            wtx = session.beginWriteTransaction();
            wtx.moveTo(newKey);
            wtx.setValue("value" + i);
            newKey = wtx.getNode().getNodeKey();
            wtx.commit();
            wtx.close();
        }
        taskExecutor.shutdown();
        taskExecutor.awaitTermination(1000000, TimeUnit.SECONDS);

        session.close();
        database.close();
    }

    private class Task implements Callable<Void> {

        private IReadTransaction mRTX;

        public Task(final IReadTransaction rtx) {
            mRTX = rtx;
        }

        public Void call() throws Exception {
            final AbsAxis axis = new DescendantAxis(mRTX);
            while (axis.hasNext()) {
                axis.next();
            }

            mRTX.moveTo(12L);
            assertEquals("bar", TypedValue.parseString(mRTX.getNode().getRawValue()));
            mRTX.close();
            return null;
        }
    }

}

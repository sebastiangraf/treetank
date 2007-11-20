/*
 * Copyright (c) 2007, Marc Kramis
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
 * $Id$
 */

package org.treetank.sessionlayer;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.utils.IConstants;
import org.treetank.utils.TestDocument;
import org.treetank.utils.UTF;

public class ThreadTest {

  public static final int WORKER_COUNT = 100;

  public static final String PATH =
      "generated" + File.separator + "ThreadTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testThreads() throws Exception {

    ISession session = Session.beginSession(PATH);

    IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();
    wtx.close();

    ExecutorService taskExecutor = Executors.newFixedThreadPool(WORKER_COUNT);
    for (int i = 0; i < WORKER_COUNT; i++) {
      taskExecutor.execute(new Task(session.beginReadTransaction(0L)));
      wtx = session.beginWriteTransaction();
      wtx.moveTo(10L);
      wtx.setValue(IConstants.STRING_TYPE, UTF.getBytes("value" + i));
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
        mRTX.moveTo(10L);
        TestCase.assertEquals("bar", UTF.parseString(mRTX.getValue()));
        mRTX.close();
      } catch (Exception e) {
        TestCase.fail(e.getLocalizedMessage());
      }
    }
  }

}

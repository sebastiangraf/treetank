/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id:ThreadTest.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IAxisIterator;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;
import org.treetank.utils.UTF;
import org.treetank.xmllayer.DescendantAxisIterator;

public class ThreadTest {

  public static final int WORKER_COUNT = 100;

  public static final String THREAD_TEST_PATH = "generated/ThreadTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(THREAD_TEST_PATH).delete();
  }

  @Test
  public void testThreads() throws Exception {

    ISession session = new Session(THREAD_TEST_PATH);

    final IWriteTransaction trx = session.beginWriteTransaction();
    TestDocument.create(trx);
    session.commit();

    ExecutorService taskExecutor = Executors.newFixedThreadPool(WORKER_COUNT);
    for (int i = 0; i < WORKER_COUNT; i++) {
      taskExecutor.execute(new Task(session.beginReadTransaction(0L)));
      final IWriteTransaction wTrx = session.beginWriteTransaction();
      wTrx.moveTo(10L);
      wTrx.setValue(UTF.convert("value" + i));
      session.commit();
    }
    taskExecutor.shutdown();
    taskExecutor.awaitTermination(1000000, TimeUnit.SECONDS);

    session.close();
  }

  private class Task implements Runnable {

    private IReadTransaction mRTX;

    public Task(final IReadTransaction rtx) throws Exception {
      mRTX = rtx;
    }

    public void run() {
      try {

        final IAxisIterator axis = new DescendantAxisIterator(mRTX);
        while (axis.next()) {
          // Move on.
        }
        mRTX.moveTo(16L);
      } catch (Exception e) {
        TestCase.fail(e.getLocalizedMessage());
      }
    }
  }

}

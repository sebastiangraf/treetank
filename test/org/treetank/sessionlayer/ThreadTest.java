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
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.utils.TestDocument;
import org.treetank.utils.UTF;

public class ThreadTest {

  public static final int WORKER_COUNT = 100;

  public static final String THREAD_TEST_PATH =
      "generated" + File.separator + "ThreadTest.tnk";

  @Before
  public void setUp() throws IOException {
    new File(THREAD_TEST_PATH).delete();
  }

  @Test
  public void testThreads() throws Exception {

    ISession session = Session.beginSession(THREAD_TEST_PATH);

    IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);
    wtx.commit();

    ExecutorService taskExecutor = Executors.newFixedThreadPool(WORKER_COUNT);
    for (int i = 0; i < WORKER_COUNT; i++) {
      taskExecutor.execute(new Task(session.beginReadTransaction(0L)));
      wtx = session.beginWriteTransaction();
      wtx.moveTo(10L);
      wtx.setValue(UTF.convert("value" + i));
      wtx.commit();
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

        final Iterable<INode> axis = new DescendantAxis(mRTX);
        for (final INode node : axis) {
          // Nothing to do.
        }
        mRTX.moveTo(16L);
        mRTX.close();
      } catch (Exception e) {
        TestCase.fail(e.getLocalizedMessage());
      }
    }
  }

}

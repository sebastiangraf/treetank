/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright (C) 2007 Marc Kramis
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
 * $Id$
 */

package org.treetank.nodelayer;

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
import org.treetank.nodelayer.Session;
import org.treetank.utils.IConstants;
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
    Thread[] worker = new Thread[WORKER_COUNT];
    for (int i = 0; i < WORKER_COUNT; i++) {
      taskExecutor.execute(new Task(session));
      final IWriteTransaction wTrx = session.beginWriteTransaction();
      wTrx.moveTo(10L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
      wTrx.setValue(UTF.convert("value" + i));
      session.commit();
    }
    taskExecutor.shutdown();
    taskExecutor.awaitTermination(1000000, TimeUnit.SECONDS);

    session.close();

    //    session = new Session(THREAD_TEST_PATH);
    //    final IReadTransaction rTrx = session.beginReadTransaction();
    //    TestCase.assertEquals((WORKER_COUNT + 1), rTrx.revisionKey());
    //    TestCase.assertTrue(rTrx.moveTo(16L));
    //    TestCase.assertEquals("value" + (WORKER_COUNT - 1), rTrx.getValue());
  }

  private class Task implements Runnable {

    private IReadTransaction mTrx;

    public Task(final ISession session) throws Exception {
      mTrx = session.beginReadTransaction(1L);
      mTrx.moveToRoot();
    }

    public void run() {
      try {
        
        final IAxisIterator axis = new DescendantAxisIterator(mTrx);
        while (axis.next()) {
          // Move on.
        }
        mTrx.moveTo(16L);
      } catch (Exception e) {
        TestCase.fail(e.getLocalizedMessage());
      }
    }
  }

}

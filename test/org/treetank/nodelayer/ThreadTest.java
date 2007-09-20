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

    Thread[] worker = new Thread[WORKER_COUNT];
    for (int i = 0; i < WORKER_COUNT; i++) {
      worker[i] = new Worker(session.beginReadTransaction(1L));
      worker[i].start();
      final IWriteTransaction wTrx = session.beginWriteTransaction();
      wTrx.moveTo(10L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT);
      wTrx.setValue(UTF.convert("value" + i));
      session.commit();
    }
    for (int i = 0; i < WORKER_COUNT; i++) {
      worker[i].join();
    }

    session.close();

    //    session = new Session(THREAD_TEST_PATH);
    //    final IReadTransaction rTrx = session.beginReadTransaction();
    //    TestCase.assertEquals((WORKER_COUNT + 1), rTrx.revisionKey());
    //    TestCase.assertTrue(rTrx.moveTo(16L));
    //    TestCase.assertEquals("value" + (WORKER_COUNT - 1), rTrx.getValue());
  }

  private class Worker extends Thread {

    private IReadTransaction trx;

    public Worker(final IReadTransaction initTrx) {
      trx = initTrx;
    }

    public void run() {
      try {
        trx.moveToRoot();
        final IAxisIterator axis = new DescendantAxisIterator(trx);
        while (axis.next()) {
          // Move on.
        }
        trx.moveTo(16L);
      } catch (Exception e) {
        TestCase.fail(e.getLocalizedMessage());
      }
    }
  }

}

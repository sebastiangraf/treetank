/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.nodelayer;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.utils.IConstants;
import org.treetank.utils.TestDocument;
import org.treetank.utils.UTF;
import org.treetank.xmllayer.DescendantAxisIterator;
import org.treetank.xmllayer.IAxisIterator;


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

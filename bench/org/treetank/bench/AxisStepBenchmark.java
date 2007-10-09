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
 * $Id$
 */

package org.treetank.bench;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.perfidix.BeforeFirstBenchRun;
import org.perfidix.Bench;
import org.perfidix.BenchClass;
import org.perfidix.Benchmark;
import org.perfidix.visitor.AsciiTable;
import org.treetank.api.IAxisIterator;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.nodelayer.Session;
import org.treetank.nodelayer.SessionConfiguration;
import org.treetank.xmllayer.ChildAxisIterator;
import org.treetank.xmllayer.DescendantAxisIterator;

@BenchClass(runs = 1)
public class AxisStepBenchmark {

  public final static int TASKS = 10;

  public final static String XML_PATH = "xml/shakespeare.xml";

  public final static String TNK_PATH = "tnk/shakespeare.tnk";

  public final static byte[] TNK_KEY = "1234567812345678".getBytes();

  private ISession mSession;

  private ExecutorService mTaskExecutor;

  @BeforeFirstBenchRun
  public void benchBeforeBenchClass() throws Exception {

    final SessionConfiguration config =
        new SessionConfiguration(TNK_PATH, TNK_KEY);

    //    new File(TNK_PATH).delete();
    //    XMLShredder.shred(XML_PATH, config);

    mSession = new Session(config);
    mTaskExecutor = Executors.newFixedThreadPool(TASKS);

  }

  @Bench
  public void benchTreeTankDescendant() throws Exception {

    mTaskExecutor.execute(new DescendantTask(mSession));

    mTaskExecutor.shutdown();
    mTaskExecutor.awaitTermination(1000000, TimeUnit.SECONDS);

  }

  @Bench
  public void benchConcurrentTreeTankDescendant() throws Exception {

    for (int i = 0; i < TASKS; i++) {
      mTaskExecutor.execute(new DescendantTask(mSession));
    }

    mTaskExecutor.shutdown();
    mTaskExecutor.awaitTermination(1000000, TimeUnit.SECONDS);

  }

  @Bench
  public void benchTreeTankChild() throws Exception {
    final IReadTransaction rTrx = mSession.beginReadTransaction();
    rTrx.moveToRoot();

    final IAxisIterator iter = new ChildAxisIterator(rTrx);
    while (iter.next()) {
    }

  }

  public static void main(final String[] args) {
    System.out
        .println("$Id:AxisStepBenchmark.java 1617 2006-10-12 17:32:13Z kramis $");
    try {
      Benchmark a = new Benchmark();
      AxisStepBenchmark bench = new AxisStepBenchmark();
      a.add(bench);
      org.perfidix.Result r = a.run();
      AsciiTable v = new AsciiTable();
      v.visit(r);
      System.out.println(v.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private class DescendantTask implements Runnable {

    private final IReadTransaction mTrx;

    public DescendantTask(final ISession session) throws Exception {
      mTrx = session.beginReadTransaction();
    }

    public void run() {
      try {
        mTrx.moveToRoot();
        final IAxisIterator iter = new DescendantAxisIterator(mTrx);
        while (iter.next()) {
          // Nothing to do here.
        }
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
  }

}

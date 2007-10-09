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

import java.io.File;

import org.perfidix.BeforeBenchClass;
import org.perfidix.Bench;
import org.perfidix.BenchClass;
import org.perfidix.Benchmark;
import org.perfidix.visitor.AsciiTable;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.nodelayer.SessionConfiguration;
import org.treetank.utils.IConstants;
import org.treetank.utils.UTF;

@BenchClass(runs = 1000)
public class UpdateBenchmark {

  private static final String FILE = "generated/UpdateBenchmark.tnk";

  private ISession session;

  private IWriteTransaction wtx;

  @BeforeBenchClass
  public void benchBeforeBenchClass() throws Exception {

    final SessionConfiguration config =
        new SessionConfiguration(FILE, "1234567812345678".getBytes());

    new File(FILE).delete();
    session = Session.getSession(config);

    wtx = session.beginWriteTransaction();
    wtx.insertRoot("benchmark");
    session.commit();

  }

  @Bench
  public void benchDeserializeAsObject() throws Exception {

    wtx = session.beginWriteTransaction();
    wtx.moveToRoot();
    for (int i = 0; i < 10; i++) {
      wtx.insertFirstChild(IConstants.TEXT, null, null, null, UTF
          .convert("hello, world"));
    }
    session.commit();

  }

  public static void main(final String[] args) {
    System.out
        .println("$Id:AxisStepBenchmark.java 1617 2006-10-12 17:32:13Z kramis $");
    try {
      Benchmark a = new Benchmark();
      UpdateBenchmark bench = new UpdateBenchmark();
      a.add(bench);
      org.perfidix.Result r = a.run();
      AsciiTable v = new AsciiTable();
      v.visit(r);
      System.out.println(v.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

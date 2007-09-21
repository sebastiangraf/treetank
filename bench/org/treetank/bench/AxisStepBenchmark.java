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

package org.treetank.bench;

import java.io.File;

import org.perfidix.BeforeFirstBenchRun;
import org.perfidix.Bench;
import org.perfidix.BenchClass;
import org.perfidix.Benchmark;
import org.perfidix.visitor.AsciiTable;
import org.treetank.api.IAxisIterator;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.nodelayer.Session;
import org.treetank.xmllayer.ChildAxisIterator;
import org.treetank.xmllayer.DescendantAxisIterator;
import org.treetank.xmllayer.XMLShredder;

@BenchClass(runs = 1)
public class AxisStepBenchmark {

  public final static String XML_PATH = "xml/shakespeare.xml";

  public final static String TNK_PATH = "tnk/shakespeare.tnk";

  private ISession session;

  @BeforeFirstBenchRun
  public void benchBeforeBenchClass() throws Exception {

    new File(TNK_PATH).delete();
    XMLShredder.shred(XML_PATH, TNK_PATH);

    session = new Session(TNK_PATH);

  }

  @Bench
  public void benchTreeTankDescendant() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();

    final IAxisIterator iter = new DescendantAxisIterator(rTrx);
    while (iter.next()) {
    }

  }

  @Bench
  public void benchTreeTankChild() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
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

}

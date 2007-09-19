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

package org.treetank.xmllayer;

import java.io.FileInputStream;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Sink;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;

import org.perfidix.BeforeBenchClass;
import org.perfidix.BeforeEachBenchRun;
import org.perfidix.Bench;
import org.perfidix.BenchClass;
import org.perfidix.Benchmark;
import org.perfidix.visitor.AsciiTable;
import org.treetank.nodelayer.IReadTransaction;
import org.treetank.nodelayer.ISession;
import org.treetank.nodelayer.Session;
import org.treetank.thirdparty.SaxonDocumentInfo;
import org.treetank.thirdparty.SaxonXQueryEvaluator;


@BenchClass(runs = 1)
public class XMarkTreeTankBenchmark {

  public final static String XML_PATH = "xml/1MB.xml";

  public final static String TNK_PATH = "tnk/1MB.tnk";

  private Configuration config;

  private StaticQueryContext context;

  private XQueryExpression[] query;

  private ISession session;

  @BeforeBenchClass
  public void benchBeforeBenchClass() throws Exception {

    // Remove existing TreeTank file.
//    new File(TNK_PATH).delete();

    // Make IC3 file ready.
//    session = new Session(TNK_PATH);
//    final IWriteTransaction wTrx = session.beginWriteTransaction();
//    XMLShredder.shred(XML_PATH, wTrx);
//    session.commit();

    // Compile xquery
    config = new Configuration();
    context = new StaticQueryContext(config);
    query = new XQueryExpression[20];
    for (int i = 0; i < 20; i++) {
      // Ignore XMark Query 18 which contains a function.
      if (i != 17) {
        query[i] =
            context.compileQuery(new FileInputStream("xquery/xmark"
                + (i + 1)
                + ".xquery"), null);
      }
    }
  }

  @BeforeEachBenchRun
  public void beforeBenchRun() throws Exception {
    session = new Session(TNK_PATH);
  }

  @Bench
  public void benchQuery01() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final SaxonXQueryEvaluator evaluator =
        new SaxonXQueryEvaluator(
            query[0],
            new SaxonDocumentInfo(config, rTrx),
            new Sink());
    evaluator.start();
    evaluator.join();
  }

  //  @Bench
  //  public void benchQuery02() throws Exception {
  //    final IReadTransaction rTrx = session.beginReadTransaction();
  //    rTrx.moveToRoot();
  //    final SaxonXQueryEvaluator evaluator =
  //        new SaxonXQueryEvaluator(
  //            query[1],
  //            new SaxonDocumentInfo(config, rTrx),
  //            new Sink());
  //    evaluator.start();
  //    evaluator.join();
  //  }

  //  @Bench
  //  public void benchQuery03() throws Exception {
  //    final IReadTransaction rTrx = session.beginReadTransaction();
  //    rTrx.moveToRoot();
  //    final SaxonXQueryEvaluator evaluator =
  //        new SaxonXQueryEvaluator(
  //            query[2],
  //            new SaxonDocumentInfo(config, rTrx),
  //            new Sink());
  //    evaluator.start();
  //    evaluator.join();
  //  }

  @Bench
  public void benchQuery04() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final SaxonXQueryEvaluator evaluator =
        new SaxonXQueryEvaluator(
            query[3],
            new SaxonDocumentInfo(config, rTrx),
            new Sink());
    evaluator.start();
    evaluator.join();
  }

  //  @Bench
  //  public void benchQuery05() throws Exception {
  //    final IReadTransaction rTrx = session.beginReadTransaction();
  //    rTrx.moveToRoot();
  //    final SaxonXQueryEvaluator evaluator =
  //        new SaxonXQueryEvaluator(
  //            query[4],
  //            new SaxonDocumentInfo(config, rTrx),
  //            new Sink());
  //    evaluator.start();
  //    evaluator.join();
  //  }

  @Bench
  public void benchQuery06() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final SaxonXQueryEvaluator evaluator =
        new SaxonXQueryEvaluator(
            query[5],
            new SaxonDocumentInfo(config, rTrx),
            new Sink());
    evaluator.start();
    evaluator.join();
  }

  @Bench
  public void benchQuery07() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final SaxonXQueryEvaluator evaluator =
        new SaxonXQueryEvaluator(
            query[6],
            new SaxonDocumentInfo(config, rTrx),
            new Sink());
    evaluator.start();
    evaluator.join();
  }

  @Bench
  public void benchQuery08() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final SaxonXQueryEvaluator evaluator =
        new SaxonXQueryEvaluator(
            query[7],
            new SaxonDocumentInfo(config, rTrx),
            new Sink());
    evaluator.start();
    evaluator.join();
  }

  @Bench
  public void benchQuery09() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final SaxonXQueryEvaluator evaluator =
        new SaxonXQueryEvaluator(
            query[8],
            new SaxonDocumentInfo(config, rTrx),
            new Sink());
    evaluator.start();
    evaluator.join();
  }

  @Bench
  public void benchQuery10() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final SaxonXQueryEvaluator evaluator =
        new SaxonXQueryEvaluator(
            query[9],
            new SaxonDocumentInfo(config, rTrx),
            new Sink());
    evaluator.start();
    evaluator.join();
  }

  //    @Bench
  //    public void benchQuery11() throws Exception {
  //      final IReadTransaction rTrx = session.beginReadTransaction();
  //      rTrx.moveToRoot();
  //      final SaxonXQueryEvaluator evaluator =
  //          new SaxonXQueryEvaluator(
  //              query[10],
  //              new SaxonDocumentInfo(config, rTrx),
  //              new Sink());
  //      evaluator.start();
  //      evaluator.join();
  //    }

  //    @Bench
  //    public void benchQuery12() throws Exception {
  //      final IReadTransaction rTrx = session.beginReadTransaction();
  //      rTrx.moveToRoot();
  //      final SaxonXQueryEvaluator evaluator =
  //          new SaxonXQueryEvaluator(
  //              query[11],
  //              new SaxonDocumentInfo(config, rTrx),
  //              new Sink());
  //      evaluator.start();
  //      evaluator.join();
  //    }

  //    @Bench
  //    public void benchQuery13() throws Exception {
  //      final IReadTransaction rTrx = session.beginReadTransaction();
  //      rTrx.moveToRoot();
  //      final SaxonXQueryEvaluator evaluator =
  //          new SaxonXQueryEvaluator(
  //              query[12],
  //              new SaxonDocumentInfo(config, rTrx),
  //              new Sink());
  //      evaluator.start();
  //      evaluator.join();
  //    }

  @Bench
  public void benchQuery14() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final SaxonXQueryEvaluator evaluator =
        new SaxonXQueryEvaluator(query[13], new SaxonDocumentInfo(
            config,
            rTrx), new Sink());
    evaluator.start();
    evaluator.join();
  }

  //    @Bench
  //    public void benchQuery15() throws Exception {
  //      final IReadTransaction rTrx = session.beginReadTransaction();
  //      rTrx.moveToRoot();
  //      final SaxonXQueryEvaluator evaluator =
  //          new SaxonXQueryEvaluator(
  //              query[14],
  //              new SaxonDocumentInfo(config, rTrx),
  //              new Sink());
  //      evaluator.start();
  //      evaluator.join();
  //    }

  @Bench
  public void benchQuery16() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final SaxonXQueryEvaluator evaluator =
        new SaxonXQueryEvaluator(query[15], new SaxonDocumentInfo(
            config,
            rTrx), new Sink());
    evaluator.start();
    evaluator.join();
  }

  @Bench
  public void benchQuery17() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final SaxonXQueryEvaluator evaluator =
        new SaxonXQueryEvaluator(query[16], new SaxonDocumentInfo(
            config,
            rTrx), new Sink());
    evaluator.start();
    evaluator.join();
  }

  //    @Bench
  //    public void benchQuery18() throws Exception {
  //      final IReadTransaction rTrx = session.beginReadTransaction();
  //      rTrx.moveToRoot();
  //      final SaxonXQueryEvaluator evaluator =
  //          new SaxonXQueryEvaluator(
  //              query[17],
  //              new SaxonDocumentInfo(config, rTrx),
  //              new Sink());
  //      evaluator.start();
  //      evaluator.join();
  //    }

  //    @Bench
  //    public void benchQuery19() throws Exception {
  //      final IReadTransaction rTrx = session.beginReadTransaction();
  //      rTrx.moveToRoot();
  //      final SaxonXQueryEvaluator evaluator =
  //          new SaxonXQueryEvaluator(
  //              query[18],
  //              new SaxonDocumentInfo(config, rTrx),
  //              new Sink());
  //      evaluator.start();
  //      evaluator.join();
  //    }

  //    @Bench
  //    public void benchQuery20() throws Exception {
  //      final IReadTransaction rTrx = session.beginReadTransaction();
  //      rTrx.moveToRoot();
  //      final SaxonXQueryEvaluator evaluator =
  //          new SaxonXQueryEvaluator(
  //              query[19],
  //              new SaxonDocumentInfo(config, rTrx),
  //              new Sink());
  //      evaluator.start();
  //      evaluator.join();
  //    }

  public static void main(final String[] args) {
    System.out
        .println("$Id:XMarkTreeTankBenchmark.java 1617 2006-10-12 17:32:13Z kramis $");
    try {
      Benchmark a = new Benchmark();
      XMarkTreeTankBenchmark bench = new XMarkTreeTankBenchmark();
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

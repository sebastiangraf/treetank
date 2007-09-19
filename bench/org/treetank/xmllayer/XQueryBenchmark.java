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

import java.io.File;

import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;

import org.perfidix.BeforeBenchClass;
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
public class XQueryBenchmark {

  public final static String XML_PATH = "xml/111MB.xml";

  public final static String TNK_PATH = "generated/111MB.tnk";

  public final static String RESULT_PATH = "generated/result.xml";

  private Configuration config;

  private StaticQueryContext context;

  private XQueryExpression query;

  private ISession session;

  @BeforeBenchClass
  public void benchBeforeBenchClass() throws Exception {

    // Remove existing TreeTank file.
    //        new File(TNK_PATH).delete();
    //        new File(RESULT_PATH).delete();

    // Compile xquery
    config = new Configuration();
    context = new StaticQueryContext(config);
    query = context.compileQuery("count(/site/regions/namerica/item)");
  }

  //  @Bench
  //  public void benchSAXQuery() throws Exception {
  //    final DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
  //    dynamicContext.setContextItem(config.buildDocument(new StreamSource(XML_PATH)));
  //    final Properties props = new Properties();
  //    props.setProperty(OutputKeys.METHOD, "xml");
  //    props.setProperty(OutputKeys.INDENT, "yes");
  //    query.run(dynamicContext, new StreamResult(new File(RESULT_PATH)), props);
  //  }

  @Bench
  public void benchIC3Shred() throws Exception {
    session = new Session(TNK_PATH);
    //    final IWriteTransaction wTrx = session.beginWriteTransaction();
    //    XMLShredder.shred(XML_PATH, wTrx);
    //    session.commit();
  }

  @Bench
  public void benchIC3Query() throws Exception {
    try {
      final IReadTransaction rTrx = session.beginReadTransaction();
      rTrx.moveToRoot();
      final SaxonXQueryEvaluator evaluator =
          new SaxonXQueryEvaluator(
              query,
              new SaxonDocumentInfo(config, rTrx),
              new StreamResult(new File(RESULT_PATH)));
      evaluator.start();
      evaluator.join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(final String[] args) {
    System.out
        .println("$Id:XQueryBenchmark.java 1617 2006-10-12 17:32:13Z kramis $");
    try {
      Benchmark a = new Benchmark();
      XQueryBenchmark bench = new XQueryBenchmark();
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

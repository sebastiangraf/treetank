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
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Sink;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;

import org.perfidix.BeforeBenchClass;
import org.perfidix.Bench;
import org.perfidix.BenchClass;
import org.perfidix.Benchmark;
import org.perfidix.visitor.AsciiTable;

@BenchClass(runs = 1)
public class XMarkSaxonBenchmark {

  public final static String XML_PATH = "xml/1MB.xml";

  private Configuration config;

  private StaticQueryContext context;

  private XQueryExpression[] query;

  private DynamicQueryContext dynamicContext;

  private Properties props;

  @BeforeBenchClass
  public void benchBeforeBenchClass() throws Exception {

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

    // Make saxon ready.
    dynamicContext = new DynamicQueryContext(config);
    dynamicContext.setContextItem(config.buildDocument(new StreamSource(
        XML_PATH)));
    props = new Properties();
    props.setProperty(OutputKeys.METHOD, "xml");
    props.setProperty(OutputKeys.INDENT, "yes");
  }

  @Bench
  public void benchQuery01() throws Exception {
    query[0].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery02() throws Exception {
    query[1].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery03() throws Exception {
    query[2].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery04() throws Exception {
    query[3].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery05() throws Exception {
    query[4].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery06() throws Exception {
    query[5].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery07() throws Exception {
    query[6].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery08() throws Exception {
    query[7].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery09() throws Exception {
    query[8].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery10() throws Exception {
    query[9].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery11() throws Exception {
    query[10].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery12() throws Exception {
    query[11].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery13() throws Exception {
    query[12].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery14() throws Exception {
    query[13].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery15() throws Exception {
    query[14].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery16() throws Exception {
    query[15].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery17() throws Exception {
    query[16].run(dynamicContext, new Sink(), props);
  }

  //  @Bench
  //  public void benchQuery18() throws Exception {
  //    query[17].run(dynamicContext, new Sink(), props);
  //  }

  @Bench
  public void benchQuery19() throws Exception {
    query[18].run(dynamicContext, new Sink(), props);
  }

  @Bench
  public void benchQuery20() throws Exception {
    query[19].run(dynamicContext, new Sink(), props);
  }

  public static void main(final String[] args) {
    System.out
        .println("$Id:XMarkSaxonBenchmark.java 1617 2006-10-12 17:32:13Z kramis $");
    try {
      Benchmark a = new Benchmark();
      XMarkSaxonBenchmark bench = new XMarkSaxonBenchmark();
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

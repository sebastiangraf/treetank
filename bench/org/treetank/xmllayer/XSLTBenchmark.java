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

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.event.Sink;

import org.perfidix.BeforeBenchClass;
import org.perfidix.Bench;
import org.perfidix.BenchClass;
import org.perfidix.Benchmark;
import org.perfidix.visitor.AsciiTable;
import org.treetank.nodelayer.IReadTransaction;
import org.treetank.nodelayer.ISession;
import org.treetank.nodelayer.Session;
import org.treetank.thirdparty.SaxonNodeInfo;
import org.treetank.thirdparty.SaxonXSLTTransformer;

@BenchClass(runs = 1)
public class XSLTBenchmark {

  public final static String XSL_PATH = "xml/shakespeare.xsl";

  public final static String XML_PATH = "xml/shakespeare.xml";

  public final static String TNK_PATH = "tnk/shakespeare.tnk";

  private Configuration config;

  private ISession session;

  private Result result;

  private Templates templates;

  @BeforeBenchClass
  public void benchBeforeBenchClass() throws Exception {

    // Load XML
    try {
      session = Session.getSession(TNK_PATH);
    } catch (Exception e) {
      e.printStackTrace();
    }

    //    new File(TNK_PATH).delete();
    //    XMLShredder.shred(XML_PATH, TNK_PATH);

    // Create Transformator
    TransformerFactory factory = new TransformerFactoryImpl();
    config = ((TransformerFactoryImpl) factory).getConfiguration();
    result = new Sink(); //new StreamResult("/Users/marc/Desktop/test.html"); //
    templates = factory.newTemplates(new StreamSource(XSL_PATH));

  }

  @Bench
  public void benchThreadedIC3XSLT() throws Exception {
    try {
      int count = 1;
      Thread[] threads = new Thread[count];
      for (int i = 0; i < count; i++) {
        final IReadTransaction trx = session.beginReadTransaction();
        trx.moveToRoot();
        trx.moveToFirstChild();
        threads[i] =
            new SaxonXSLTTransformer(
                templates.newTransformer(),
                new SaxonNodeInfo(config, trx),
                result);
        threads[i].start();
      }
      for (int i = 0; i < count; i++) {
        threads[i].join();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(final String[] args) {
    System.out
        .println("$Id:XSLTBenchmark.java 1617 2006-10-12 17:32:13Z kramis $");
    try {
      Benchmark a = new Benchmark();
      XSLTBenchmark bench = new XSLTBenchmark();
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

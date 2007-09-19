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
import java.util.List;

import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceConstant;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.XPathEvaluator;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.perfidix.BeforeFirstBenchRun;
import org.perfidix.Bench;
import org.perfidix.BenchClass;
import org.perfidix.Benchmark;
import org.perfidix.visitor.AsciiTable;
import org.treetank.nodelayer.IReadTransaction;
import org.treetank.nodelayer.ISession;
import org.treetank.nodelayer.Session;
import org.treetank.thirdparty.SaxonDocumentInfo;
import org.treetank.thirdparty.SaxonXPathEvaluator;
import org.xml.sax.InputSource;


@BenchClass(runs = 1)
public class XPathBenchmark {

  public final static String XML_PATH = "xml/shakespeare.xml";

  public final static String TNK_PATH = "tnk/shakespeare.tnk";

  private XPathExpression childQuery;

  private XPathExpression descendantQuery;

  private Configuration config;

  private XPath xpe;

  private ISession session;

  @BeforeFirstBenchRun
  public void benchBeforeBenchClass() throws Exception {

    // Following is specific to Saxon: should be in a properties file
    System.setProperty(
        "javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON,
        "net.sf.saxon.xpath.XPathFactoryImpl");

    final XPathFactory xpf =
        XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
    xpe = xpf.newXPath();
    config = ((XPathFactoryImpl) xpf).getConfiguration();

    //xpe.setNamespaceContext(this);
    descendantQuery = xpe.compile("count(//LINE)");
    childQuery = xpe.compile("count(/PLAYS/PLAY/TITLE)");

    session = new Session(TNK_PATH);
  }

  @Bench
  public void benchSAXDescendant() throws Exception {
    InputSource is = new InputSource(new File(XML_PATH).toURL().toString());
    SAXSource ss = new SAXSource(is);
    NodeInfo doc = ((XPathEvaluator) xpe).setSource(ss);
    List matchedLines =
        (List) descendantQuery.evaluate(doc, XPathConstants.NODESET);
    System.out.println(matchedLines.get(0));
  }

  @Bench
  public void benchSAXChild() throws Exception {
    InputSource is = new InputSource(new File(XML_PATH).toURL().toString());
    SAXSource ss = new SAXSource(is);
    NodeInfo doc = ((XPathEvaluator) xpe).setSource(ss);
    List matchedLines = (List) childQuery.evaluate(doc, XPathConstants.NODESET);
    System.out.println(matchedLines.get(0));
  }

  @Bench
  public void benchIC3Descendant() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final NodeInfo doc = new SaxonDocumentInfo(config, rTrx);
    final SaxonXPathEvaluator path =
        new SaxonXPathEvaluator(descendantQuery, doc);
    path.run();
    path.join();
    final List matchedLines = path.getResult();
    System.out.println(matchedLines.get(0));
  }

  @Bench
  public void benchIC3Child() throws Exception {
    final IReadTransaction rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    final NodeInfo doc = new SaxonDocumentInfo(config, rTrx);
    final SaxonXPathEvaluator path = new SaxonXPathEvaluator(childQuery, doc);
    path.run();
    path.join();
    final List matchedLines = path.getResult();
    System.out.println(matchedLines.get(0));
  }

  public static void main(final String[] args) {
    System.out
        .println("$Id:XPathBenchmark.java 1617 2006-10-12 17:32:13Z kramis $");
    try {
      Benchmark a = new Benchmark();
      XPathBenchmark bench = new XPathBenchmark();
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

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

package org.treetank.thirdparty;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceConstant;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.AbstractTransformer;
import org.treetank.nodelayer.IReadTransaction;
import org.treetank.nodelayer.ISession;
import org.treetank.nodelayer.Session;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * <h1>TreeTankCocoonXPathTransformer</h1>
 * 
 * <p>Cocoon transformer applying an XPath expression to an idefix storage.
 * The transformer only transforms <code>&lt;xpath&gt;</code> elements.
 * It therefore executes the XPath expression found in the first attribute.</p>
 * 
 * <p>
 * Example Input:
 * 
 * <pre>
 *   &lt;xpath expression="count(//text())"/&gt;
 *   &lt;anotherelement/&gt;
 * </pre>
 * 
 * Corresponding example output:
 * 
 * <pre>
 *   &lt;result&gt;
 *     &lt;item&gt; 10 &lt;item/&gt;
 *   &lt;result/&gt;
 *   &lt;anotherelement/&gt;
 * </pre>
 * </p>
 */
public class TreeTankCocoonXPathTransformer extends AbstractTransformer {

  /** Node to evaluate expression for. */
  private static final String XPATH = "xpath";

  /** Node length (optimization for string comparison). */
  private static final int XPATH_LENGTH = XPATH.length();

  /** Result node. */
  private static final String RESULT = "result";

  /** Result item node. */
  private static final String ITEM = "item";

  /** Attribute to fetch XPath expression from. */
  private static final int ATTRIBUTE = 0;

  /** XPathFactory singelton. */
  private static final XPathFactory XPATH_FACTORY;

  /** Configuration of XPATH_FACTORY. */
  private static final Configuration CONFIGURATION;

  /** Source attribute of sitemap transformer component. */
  private String mSource;

  /**
   * Static initialization of singletons.
   */
  static {
    try {
      System.setProperty(
          "javax.xml.xpath.XPathFactory:"
              + NamespaceConstant.OBJECT_MODEL_SAXON,
          "net.sf.saxon.xpath.XPathFactoryImpl");

      XPATH_FACTORY =
          XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
      CONFIGURATION = ((XPathFactoryImpl) XPATH_FACTORY).getConfiguration();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setup(
      final SourceResolver res,
      final Map objectMap,
      final String src,
      final Parameters params)
      throws ProcessingException,
      SAXException,
      IOException {
    mSource = src;
  }

  /**
   * {@inheritDoc}
   */
  public void startElement(
      final String ns,
      final String localName,
      final String qName,
      final Attributes attrs) throws SAXException {
    if (localName.length() == XPATH_LENGTH && localName.equalsIgnoreCase(XPATH)) {
      super.startElement("", RESULT, RESULT, new AttributesImpl());
      Iterator iterator = execute(attrs.getValue(ATTRIBUTE));
      Object item;
      String text;
      while (iterator.hasNext()) {
        super.startElement("", ITEM, ITEM, new AttributesImpl());
        item = iterator.next();
        if (item instanceof NodeInfo) {
          text = ((NodeInfo) item).getStringValue();
        } else {
          text = item.toString();
        }
        super.characters(text.toCharArray(), 0, text.length());
        super.endElement("", ITEM, ITEM);
      }
    } else {
      super.startElement(ns, localName, qName, attrs);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void endElement(
      final String ns,
      final String localName,
      final String qName) throws SAXException {
    if (localName.length() == XPATH_LENGTH && localName.equalsIgnoreCase(XPATH)) {
      super.endElement("", RESULT, RESULT);
    } else {
      super.endElement(ns, localName, qName);
    }
  }

  /**
   * Execute XPath expression and return iterator over result set.
   * 
   * @param query XPath 2.0 expression.
   * @return Iterator over result set.
   */
  private final Iterator execute(final String query) {

    Iterator iterator = null;

    try {
      final XPath xpe = XPATH_FACTORY.newXPath();
      final XPathExpression expression = xpe.compile(query);

      final ISession session = Session.getSession(mSource);
      final IReadTransaction rTrx = session.beginReadTransaction();
      rTrx.moveToRoot();
      final NodeInfo doc = new SaxonDocumentInfo(CONFIGURATION, rTrx);

      final SaxonXPathEvaluator path = new SaxonXPathEvaluator(expression, doc);
      path.run();
      path.join();

      iterator = path.getResult().iterator();

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }

    return iterator;
  }

}

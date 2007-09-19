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

import java.io.File;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceConstant;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.treetank.nodelayer.ISession;
import org.treetank.nodelayer.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.thirdparty.SaxonDocumentInfo;
import org.treetank.utils.TestDocument;


public class SaxonXPathTest {

  public static final String PATH = "generated/SaxonXPathTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testXPath() throws Exception {

    // Saxon setup.
    System.setProperty(
        "javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON,
        "net.sf.saxon.xpath.XPathFactoryImpl");
    final XPathFactory xpf =
        XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
    final XPath xpe = xpf.newXPath();
    final Configuration config = ((XPathFactoryImpl) xpf).getConfiguration();
    XPathExpression findLine = xpe.compile("count(//b)");

    // TreeTank setup.
    final ISession session = new Session(PATH);
    final IWriteTransaction trx = session.beginWriteTransaction();
    TestDocument.create(trx);

    trx.moveToRoot();
    NodeInfo doc = new SaxonDocumentInfo(config, trx);

    // Execute XPath.
    List result = (List) findLine.evaluate(doc, XPathConstants.NODESET);
    TestCase.assertNotNull(result);
    TestCase.assertEquals(2L, result.iterator().next());

    session.abort();
    session.close();
  }

}

package com.treetank.saxon.wrapper;

import java.io.File;
import java.util.ArrayList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import junit.framework.TestCase;
import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamespaceConstant;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.saxon.utils.DocNamespaceContext;
import com.treetank.utils.DocumentCreater;

public class NodeWrapperTest {

  /** TreeTank session. */
  private static ISession session;

  /** XPath expression. */
  private static XPath xpe;

  /** Saxon configuration. */
  private static Configuration config;

  /** Logger. */
  private static Log logger = LogFactory.getLog(NodeWrapperTest.class);

  /** Path to test file. */
  private File test =
      new File(new StringBuilder(File.separator)
          .append("tmp")
          .append(File.separator)
          .append("tnk")
          .append(File.separator)
          .append("path1")
          .toString());

  @Before
  public void setUp() throws TreetankException {
    Database.truncateDatabase(test);
    final IDatabase database = Database.openDatabase(test);
    session = database.getSession();
    final IWriteTransaction wtx = session.beginWriteTransaction();
    DocumentCreater.create(wtx);
    wtx.commit();
    wtx.close();

    // Saxon setup.
    System.setProperty(
        "javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON,
        "net.sf.saxon.xpath.XPathFactoryImpl");

    try {
      XPathFactory xpf =
          XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
      xpe = xpf.newXPath();
      config = ((XPathFactoryImpl) xpf).getConfiguration();
    } catch (XPathFactoryConfigurationException e) {
      logger.warn("XPathConfigurationException: " + e.getMessage(), e);
    }
  }

  @AfterClass
  public static void tearDown() throws Exception {
    Database.forceCloseDatabase(new File(new StringBuilder(File.separator)
        .append("tmp")
        .append(File.separator)
        .append("tnk")
        .append(File.separator)
        .append("path1")
        .toString()));
  }

  /**
   * XPath tests.
   * 
   * @param expressions
   *            Expressions, which are used.
   * @param doc
   *            The test document.
   * @param expectedResults
   *            Expected result for each expression.
   * @param result
   *            Array with the result types for each expression.
   * @param xpathConstants
   *            XPathConstants for each expression.
   * @param namespaces
   *            Array of boolean values for each expression.
   * @param namespace
   *            The namespace Context, which is used.
   * @throws Exception
   *             Any Exception which maybe occurs.
   */
  @SuppressWarnings("unchecked")
  public void test(
      final String[] expressions,
      final Object doc,
      final Object[] expectedResults,
      Object[] result,
      final QName[] xpathConstants,
      final boolean[] namespaces,
      final Object namespace) throws Exception {

    // For every expected result.
    for (int i = 0; i < expectedResults.length; i++) {

      // If namespace is required.
      if (namespaces[i]) {
        xpe.setNamespaceContext((NamespaceContext) namespace);
      }

      final XPathExpression findLine = xpe.compile(expressions[i]);

      // Cast the evaluated value.
      if (xpathConstants[i].equals(XPathConstants.NODESET)) {
        result[i] = findLine.evaluate(doc, xpathConstants[i]);
      } else if (xpathConstants[i].equals(XPathConstants.STRING)) {
        result[i] = (String) findLine.evaluate(doc, xpathConstants[i]);
      } else if (xpathConstants[i].equals(XPathConstants.NUMBER)) {
        result[i] =
            Double.parseDouble(findLine
                .evaluate(doc, xpathConstants[i])
                .toString());
      } else {
        throw new IllegalStateException("Unknown XPathConstant!");
      }

      TestCase.assertNotNull(result);

      if (xpathConstants[i].equals(XPathConstants.NODESET)) {
        ArrayList<IReadTransaction> test =
            (ArrayList<IReadTransaction>) result[i];

        final String res = (String) expectedResults[i];
        final String[] expRes = res.split(" ");

        // Iterate over expected result and the actual result and compare it.
        for (int j = 0; j < test.size(); j++) {
          final IReadTransaction rtx = test.get(j);
          final QName qName = test.get(j).getQNameOfCurrentNode();
          
          if (rtx.getNode().isElement()) {
            TestCase
                .assertEquals(expRes[j], qName.getPrefix()+":"+qName.getLocalPart());
          } else if (rtx.getNode().isText()) {
            TestCase.assertEquals(expRes[j], test
                .get(j)
                .getValueOfCurrentNode());
          }
        }
      } else {
        TestCase.assertEquals(expectedResults[i], result[i]);
      }
    }
  }

  @Test
  public void testExample() throws Exception {

    final String[] expressions =
        {
            "count(//b)",
            "count(//p:a)",
            "//p:a/@i",
            "//p:a/@p:i",
            "//b[1]/text()",
            "//b[2]",
            "//b[1]",
            "//b[2]/text()",
            "//p:a/text()" };

    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    final Object[] expectedResults = { 2D, 1D, "j", "", "foo",
    // "<b p:x=\"y\"><c/>bar</b>",
        "bar",
        "foo",
        // "<b>foo<c/></b>",
        "bar",
        "oops1 oops2 oops3" };

    Object[] result = { "", "", "", 0D, "", "", "", "", "" };

    final QName[] xpathConstants =
        {
            XPathConstants.NUMBER,
            XPathConstants.NUMBER,
            XPathConstants.STRING,
            XPathConstants.STRING,
            XPathConstants.STRING,
            XPathConstants.STRING,
            XPathConstants.STRING,
            XPathConstants.STRING,
            XPathConstants.NODESET, };

    final boolean[] namespaces =
        { false, true, true, false, false, false, false, false, true };

    test(
        expressions,
        doc,
        expectedResults,
        result,
        xpathConstants,
        namespaces,
        new DocNamespaceContext());
  }

  @Test
  public void testElementBCount() throws Exception {

    final XPathExpression findLine = xpe.compile("count(//b)");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final double result =
        Double.parseDouble(findLine
            .evaluate(doc, XPathConstants.NUMBER)
            .toString());
    TestCase.assertNotNull(result);
    TestCase.assertEquals(2.0, result);
  }

  @Test
  public void testElementACount() throws Exception {

    final XPathExpression findLine = xpe.compile("count(//a)");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final double result =
        Double.parseDouble(findLine
            .evaluate(doc, XPathConstants.NUMBER)
            .toString());
    TestCase.assertNotNull(result);
    TestCase.assertEquals(0.0, result);
  }

  @Test
  public void testNamespaceElementCount() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("count(//p:a)");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final double result =
        Double.parseDouble(findLine
            .evaluate(doc, XPathConstants.NUMBER)
            .toString());
    TestCase.assertNotNull(result);
    TestCase.assertEquals(1.0, result);
  }

  @Test
  public void testAttributeCount() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("count(//p:a/@i)");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final double result =
        Double.parseDouble(findLine
            .evaluate(doc, XPathConstants.NUMBER)
            .toString());
    TestCase.assertNotNull(result);
    TestCase.assertEquals(1.0, result);
  }

  @Test
  public void testNamespaceAttributeCount() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("count(//p:a/@p:i)");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final double result =
        Double.parseDouble(findLine
            .evaluate(doc, XPathConstants.NUMBER)
            .toString());

    TestCase.assertNotNull(result);
    TestCase.assertEquals(0D, result);
  }

  @Test
  public void testAttributeValue() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("//p:a/@i");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final String result =
        findLine.evaluate(doc, XPathConstants.STRING).toString();

    TestCase.assertNotNull(result);
    TestCase.assertEquals("j", result);
  }

  @Test
  public void testNamespaceAttributeValue() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("//p:a/@p:i");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final String result =
        findLine.evaluate(doc, XPathConstants.STRING).toString();

    TestCase.assertNotNull(result);
    TestCase.assertEquals("", result);
  }

  @Test
  public void testText() throws Exception {
    final XPathExpression findLine = xpe.compile("//b[1]/text()");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final String result =
        (String) findLine.evaluate(doc, XPathConstants.STRING);

    TestCase.assertNotNull(result);
    TestCase.assertEquals("foo", result);
  }

  @Test
  public void testText1() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("//p:a[1]/text()[1]");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final String result =
        (String) findLine.evaluate(doc, XPathConstants.STRING);

    TestCase.assertNotNull(result);
    TestCase.assertEquals("oops1", result);
  }

  @Test
  public void testDefaultNamespaceText1() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("//p:a/text()[1]");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final String result =
        (String) findLine.evaluate(doc, XPathConstants.STRING);

    TestCase.assertNotNull(result);
    TestCase.assertEquals("oops1", result);
  }

  @Test
  public void testDefaultNamespaceText2() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("//p:a/text()[2]");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final String result =
        (String) findLine.evaluate(doc, XPathConstants.STRING);

    TestCase.assertNotNull(result);
    TestCase.assertEquals("oops2", result);
  }
  
  @Test
  public void testDefaultNamespaceText3() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("//p:a/text()[3]");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final String result =
        (String) findLine.evaluate(doc, XPathConstants.STRING);

    TestCase.assertNotNull(result);
    TestCase.assertEquals("oops3", result);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testDefaultNamespaceTextAll() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("//p:a/text()");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final ArrayList<IReadTransaction> result =
        (ArrayList<IReadTransaction>) findLine.evaluate(
            doc,
            XPathConstants.NODESET);
    TestCase.assertNotNull(result);
    TestCase.assertEquals("oops1", result.get(0).getValueOfCurrentNode());
    TestCase.assertEquals("oops2", result.get(1).getValueOfCurrentNode());
    TestCase.assertEquals("oops3", result.get(2).getValueOfCurrentNode());
  }
  
  @Test
  public void testB1() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("//b[1]");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final String result =
        (String) findLine.evaluate(doc, XPathConstants.STRING);

    TestCase.assertNotNull(result);
    TestCase.assertEquals("foo", result);
  }
  
  @Test
  public void testB2() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("//b[2]");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final String result =
        (String) findLine.evaluate(doc, XPathConstants.STRING);

    TestCase.assertNotNull(result);
    TestCase.assertEquals("bar", result);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void testBAll() throws Exception {
    xpe.setNamespaceContext(new DocNamespaceContext());
    final XPathExpression findLine = xpe.compile("//b");
    final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(session, config, test
            .getAbsolutePath()).wrap();

    // Execute XPath.
    final ArrayList<IReadTransaction> result =
        (ArrayList<IReadTransaction>) findLine.evaluate(doc, XPathConstants.NODESET);

    TestCase.assertNotNull(result);
    TestCase.assertEquals("b", result.get(0).getQNameOfCurrentNode().getLocalPart());
    TestCase.assertEquals("b", result.get(1).getQNameOfCurrentNode().getLocalPart());
  }
}

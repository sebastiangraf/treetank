package com.treetank.saxon.wrapper;

import junit.framework.TestCase;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.saxon.evaluator.XPathEvaluator;
import com.treetank.utils.DocumentCreater;

/**
 * Test XPath S9Api.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class TestNodeWrapperS9ApiXPath extends XMLTestCase {

  /** Treetank database on Treetank test document. */
  private transient static IDatabase databaseTest;

  @BeforeClass
  public void setUp() throws TreetankException {
    Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
    databaseTest = Database.openDatabase(TestHelper.PATHS.PATH1.getFile());
    final IWriteTransaction wtx = databaseTest.getSession().beginWriteTransaction();
    DocumentCreater.create(wtx);
    wtx.commit();
    wtx.close();
    
    XMLUnit.setIgnoreWhitespace(true);
  }

  @AfterClass
  public void tearDown() throws TreetankException {
    Database.forceCloseDatabase(TestHelper.PATHS.PATH1.getFile());
  }

  @Test
  public void testB1() throws Exception {
    final XPathSelector selector =
        new XPathEvaluator("//b[1]", databaseTest).call();

    final StringBuilder strBuilder = new StringBuilder();

    for (final XdmItem item : selector) {
      strBuilder.append(item.toString());
    }
    
    assertXMLEqual(
        "expected pieces to be similar",
        "<b xmlns:p=\"ns\">foo<c xmlns:p=\"ns\"/></b>",
        strBuilder.toString());
  }

  @Test
  public void testB1String() throws Exception {
    final XPathSelector selector =
        new XPathEvaluator("//b[1]/text()", databaseTest).call();

    final StringBuilder strBuilder = new StringBuilder();

    for (final XdmItem item : selector) {
      strBuilder.append(item.toString());
    }

    TestCase.assertEquals("foo", strBuilder.toString());
  }

  @Test
  public void testB2() throws Exception {
    final XPathSelector selector =
        new XPathEvaluator("//b[2]", databaseTest).call();

    final StringBuilder strBuilder = new StringBuilder();

    for (final XdmItem item : selector) {
      strBuilder.append(item.toString());
    }

    assertXMLEqual(
        "expected pieces to be similar",
        "<b xmlns:p=\"ns\" p:x=\"y\"><c xmlns:p=\"ns\"/>bar</b>",
        strBuilder.toString());
  }

  @Test
  public void testB2Text() throws Exception {
    final XPathSelector selector =
        new XPathEvaluator("//b[2]/text()", databaseTest).call();

    final StringBuilder strBuilder = new StringBuilder();

    for (final XdmItem item : selector) {
      strBuilder.append(item.toString());
    }

    TestCase.assertEquals("bar", strBuilder.toString());
  }

  @Test
  public void testB() throws Exception {
    final XPathSelector selector =
        new XPathEvaluator("//b", databaseTest)
            .call();

    final StringBuilder strBuilder = new StringBuilder();

    strBuilder.append("<result>");
    for (final XdmItem item : selector) {
      strBuilder.append(item.toString());
    }
    strBuilder.append("</result>");

    assertXMLEqual(
        "expected pieces to be similar",
        "<result><b xmlns:p=\"ns\">foo<c xmlns:p=\"ns\"/></b><b xmlns:p=\"ns\" p:x=\"y\">"
                + "<c xmlns:p=\"ns\"/>bar</b></result>",
        strBuilder.toString());
  }

  @Test
  public void testCountB() throws Exception {
    final XPathSelector selector =
        new XPathEvaluator("count(//b)", databaseTest).call();

    final StringBuilder sb = new StringBuilder();

    for (final XdmItem item : selector) {
      sb.append(item.getStringValue());
    }

    TestCase.assertEquals("2", sb.toString());
  }

}

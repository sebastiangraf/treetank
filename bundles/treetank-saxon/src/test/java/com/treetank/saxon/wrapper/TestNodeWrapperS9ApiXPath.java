package com.treetank.saxon.wrapper;

import java.io.File;

import junit.framework.TestCase;

import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.saxon.evaluator.XPathEvaluator;
import com.treetank.utils.DocumentCreater;

/**
 * Test XPath S9Api.
 * 
 * @author johannes
 *
 */
public final class TestNodeWrapperS9ApiXPath {

  /** Treetank session on Treetank test document. */
  private transient static ISession sessionTest;

  /** Logger. */
  private static final Log LOGGER =
      LogFactory.getLog(TestNodeWrapperS9ApiXPath.class);

  /** Path to test file. */
  private static final File TEST =
      new File(new StringBuilder(File.separator)
          .append("tmp")
          .append(File.separator)
          .append("tnk")
          .append(File.separator)
          .append("path1")
          .toString());

  @BeforeClass
  public static void setUp() throws TreetankException {
    Database.truncateDatabase(TEST);
    final IDatabase database = Database.openDatabase(TEST);
    sessionTest = database.getSession();
    final IWriteTransaction wtx = sessionTest.beginWriteTransaction();
    DocumentCreater.create(wtx);
    wtx.commit();
    wtx.close();
  }

  @AfterClass
  public static void tearDown() throws TreetankException {
    Database.forceCloseDatabase(TEST);
  }

  @Ignore("Not Ready to Run")
  @Test
  public void testB1() throws Exception {
    final XPathSelector selector =
        new XPathEvaluator("//b[1]", sessionTest, TEST).call();

    final StringBuilder strBuilder = new StringBuilder();

    for (final XdmItem item : selector) {
      strBuilder.append(item.getStringValue());
    }

    TestCase.assertEquals("", strBuilder.toString());
  }
  
  @Ignore("Not Ready to Run")
  @Test
  public void testB2() throws Exception {
    final XPathSelector selector =
        new XPathEvaluator("//b[2]", sessionTest, TEST).call();

    final StringBuilder strBuilder = new StringBuilder();

    for (final XdmItem item : selector) {
      strBuilder.append(item.getStringValue());
    }

    TestCase.assertEquals("", strBuilder.toString());
  }

  @Ignore("Not Ready to Run")
  @Test
  public void testB() throws Exception {
    final XPathSelector selector =
        new XPathEvaluator("//b", sessionTest, TEST).call();

    final StringBuilder strBuilder = new StringBuilder();

    for (final XdmItem item : selector) {
      strBuilder.append(item.getStringValue());
    }

    TestCase.assertEquals("", strBuilder.toString());
  }
  
  @Ignore("Not Ready to Run")
  @Test
  public void testCountB() throws Exception {
    final XPathSelector selector =
        new XPathEvaluator("count(//b)", sessionTest, TEST).call();

    final StringBuilder sb = new StringBuilder();

    for (final XdmItem item : selector) {
      sb.append(item.getStringValue());
    }

    TestCase.assertEquals("2", sb.toString());
  }

}

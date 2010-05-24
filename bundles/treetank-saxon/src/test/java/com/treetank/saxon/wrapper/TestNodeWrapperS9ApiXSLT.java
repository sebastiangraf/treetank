package com.treetank.saxon.wrapper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;

import net.sf.saxon.s9api.Serializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.saxon.evaluator.XSLTEvaluator;
import com.treetank.utils.DocumentCreater;

/**
 * Test XSLT S9Api.
 * 
 * @author johannes
 *
 */
public final class TestNodeWrapperS9ApiXSLT {

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

  @Before
  public void setUp() throws TreetankException {
    Database.truncateDatabase(TEST);
    final IDatabase database = Database.openDatabase(TEST);
    sessionTest = database.getSession();
    final IWriteTransaction wtx = sessionTest.beginWriteTransaction();
    DocumentCreater.create(wtx);
    wtx.commit();
    wtx.close();
  }

  @After
  public void tearDown() throws TreetankException {
    Database.forceCloseDatabase(TEST);
  }

  @Ignore("Not Ready to Run")
  @Test
  public void testA() throws IOException {
    final File stylesheet =
        new File("src"
            + File.separator
            + "test"
            + File.separator
            + "resources"
            + File.separator
            + "styles"
            + File.separator
            + "books.xsl");
    
    final File books =
      new File("src"
          + File.separator
          + "test"
          + File.separator
          + "resources"
          + File.separator
          + "data"
          + File.separator
          + "books.xml");
    
    final Serializer serializer =
        new XSLTEvaluator(
            sessionTest,
            books,
            stylesheet,
            new ByteArrayOutputStream()).call();
 
    // Check against books html file.
    final BufferedReader in = new BufferedReader(new FileReader("src"
        + File.separator
        + "test"
        + File.separator
        + "resources"
        + File.separator
        + "output"
        + File.separator
        + "books1.html"));  
    final StringBuilder sBuilder = new StringBuilder();
    for (String line = in.readLine(); line != null; line = in.readLine()) {
      sBuilder.append(line.trim()); 
    }
    in.close();

    TestCase.assertEquals(sBuilder.toString(), serializer.toString());    
  }
}

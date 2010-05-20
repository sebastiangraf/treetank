package com.treetank.saxon.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;

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
public class TestNodeWrapperS9ApiXSLT {

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
  public void test() {
    final File stylesheet =
        new File("src"
            + File.separator
            + "test"
            + File.separator
            + "resources"
            + File.separator
            + "style"
            + File.separator
            + "style1");
    final Serializer serializer =
        new XSLTEvaluator(
            sessionTest,
            TEST,
            stylesheet,
            new ByteArrayOutputStream()).call();
    serializer.toString();
    
  }
}

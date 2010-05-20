package com.treetank.saxon.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.saxon.evaluator.XQueryEvaluator;
import com.treetank.service.xml.XMLShredder;

/**
 * Test XQuery S9Api.
 * 
 * @author johannes
 *
 */
public class TestNodeWrapperS9ApiXQuery {

  /** Treetank session on Treetank test document. */
  private transient static ISession sessionTest;

  /** Treetank session on books document. */
  private transient static ISession sessionBooks;

  /** Logger. */
  private static final Log LOGGER =
      LogFactory.getLog(TestNodeWrapperS9ApiXQuery.class);

  /** Path to books file. */
  private static final File BOOKSXML =
      new File(new StringBuilder("src")
          .append(File.separator)
          .append("test")
          .append(File.separator)
          .append("resources")
          .append(File.separator)
          .append("books.xml")
          .toString());

  /** Path to books TNK. */
  private transient static File booksTNK;

  @Before
  public void setUp() throws TreetankException {
    final String booksPath = BOOKSXML.getAbsolutePath();
    booksTNK = new File(booksPath.substring(0, booksPath.length() - 4));

    Database.truncateDatabase(booksTNK);
    try {
      Database.createDatabase(new DatabaseConfiguration(booksTNK));

      final IDatabase database = Database.openDatabase(booksTNK);
      sessionBooks = database.getSession();
      final IWriteTransaction mWTX = sessionBooks.beginWriteTransaction();
      final XMLEventReader reader = XMLShredder.createReader(BOOKSXML);
      final XMLShredder shredder = new XMLShredder(mWTX, reader, true);
      shredder.call();
      mWTX.close();
    } catch (TreetankIOException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (TreetankException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (XMLStreamException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  @AfterClass
  public static void tearDown() throws TreetankException {
    //      Database.forceCloseDatabase(TEST);
//    sessionBooks.close();
//    Database.forceCloseDatabase(booksTNK);
  }

  @Ignore("Not Ready to Run")
  @Test
  public void testWhereBooks() {
    final XQueryEvaluator xqe =
        new XQueryEvaluator(
            "for $x in /bookstore/book where $x/price>30 return $x/title",
            sessionBooks,
            booksTNK.getAbsoluteFile(),
            new ByteArrayOutputStream());
    final String result = xqe.call().toString();
    TestCase.assertNotNull(result);
    TestCase
        .assertEquals(
            "<title lang=\"en\">XQuery Kick Start</title><title lang=\"en\">Learning XML</title>",
            result);
  }

  @Ignore("Not Ready to Run")
  @Test
  public void testOrderByBooks() {
    final XQueryEvaluator xqe =
        new XQueryEvaluator(
            "for $x in /bookstore/book where $x/price>30 order by $x/title return $x/title",
            sessionBooks,
            booksTNK.getAbsoluteFile(),
            new ByteArrayOutputStream());
    final String result = xqe.call().toString();
    TestCase.assertNotNull(result);
    TestCase
        .assertEquals(
            "<title lang=\"en\">Learning XML</title><title lang=\"en\">XQuery Kick Start</title>",
            result);
  }
  
//  @Test
//  public void testWhereBooks() throws Exception {
//    final XQueryEvaluator xqe =
//        new XQueryEvaluator(
//            "for $x in /bookstore/book where $x/price>30 return $x/title",
//            sessionBooks,
//            booksTNK.getAbsoluteFile(),
//            new ByteArrayOutputStream());
//    final String result = xqe.call().toString();
//    TestCase.assertNotNull(result);
//    TestCase
//        .assertEquals(
//            "<title lang=\"en\">XQuery Kick Start</title><title lang=\"en\">Learning XML</title>",
//            result);
//  }

}

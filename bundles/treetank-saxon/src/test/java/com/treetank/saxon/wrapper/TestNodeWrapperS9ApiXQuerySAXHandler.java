package com.treetank.saxon.wrapper;

import java.io.File;

import javax.xml.stream.XMLEventReader;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.saxon.evaluator.XQueryEvaluatorSAXHandler;
import com.treetank.service.xml.XMLShredder;

/**
 * <h1>TestNodeWrapperS9ApiXQueryHandler</h1>
 * 
 * <p>Test the NodeWrapper with Saxon's S9Api for XQuery.</p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public class TestNodeWrapperS9ApiXQuerySAXHandler {

  /** Treetank session on books document. */
  private transient static ISession sessionBooks;

  /** Path to books file. */
  private static final File BOOKSXML =
      new File(new StringBuilder("src")
          .append(File.separator)
          .append("test")
          .append(File.separator)
          .append("resources")
          .append(File.separator)
          .append("data")
          .append(File.separator)
          .append("my-books.xml")
          .toString());

  @Before
  public void setUp() throws Exception {
    Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
    Database.createDatabase(new DatabaseConfiguration(TestHelper.PATHS.PATH1
        .getFile()));

    final IDatabase database =
        Database.openDatabase(TestHelper.PATHS.PATH1.getFile());
    sessionBooks = database.getSession();
    final IWriteTransaction mWTX = sessionBooks.beginWriteTransaction();
    final XMLEventReader reader = XMLShredder.createReader(BOOKSXML);
    final XMLShredder shredder = new XMLShredder(mWTX, reader, true);
    shredder.call();
    mWTX.close();
  }

  @AfterClass
  public static void tearDown() throws TreetankException {
    sessionBooks.close();
    Database.forceCloseDatabase(TestHelper.PATHS.PATH1.getFile());
  }

  @Test
  public void testWhereBooks() {
    final StringBuilder strBuilder = new StringBuilder();
    final ContentHandler contHandler = new XMLFilterImpl() {

      @Override
      public void startElement(
          final String uri,
          final String localName,
          final String qName,
          final Attributes atts) throws SAXException {
        strBuilder.append("<" + localName);

        for (int i = 0; i < atts.getLength(); i++) {
          strBuilder.append(" " + atts.getQName(i));
          strBuilder.append("=\"" + atts.getValue(i) + "\"");
        }

        strBuilder.append(">");
      }

      @Override
      public void endElement(String uri, String localName, String qName)
          throws SAXException {
        strBuilder.append("</" + localName + ">");
      }

      @Override
      public void characters(final char[] ch, final int start, final int length)
          throws SAXException {
        for (int i = start; i < start + length; i++) {
          strBuilder.append(ch[i]);
        }
      }
    };

    new XQueryEvaluatorSAXHandler(
        "for $x in /bookstore/book where $x/price>30 return $x/title",
        sessionBooks,
        TestHelper.PATHS.PATH1.getFile().getAbsoluteFile(),
        contHandler).run();

    TestCase
        .assertEquals(
            strBuilder.toString(),
            "<title lang=\"en\">XQuery Kick Start</title><title lang=\"en\">Learning XML</title>");
  }
}

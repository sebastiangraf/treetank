package com.treetank.saxon.wrapper;

import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.xml.stream.XMLEventReader;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.saxon.evaluator.XQueryEvaluator;
import com.treetank.service.xml.XMLShredder;

/**
 * Test XQuery S9Api.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class TestNodeWrapperS9ApiXQuery {

    /** Treetank database on books document. */
    private transient static IDatabase databaseBooks;

    /** Path to books file. */
    private static final File BOOKSXML = new File(new StringBuilder("src").append(File.separator).append(
        "test").append(File.separator).append("resources").append(File.separator).append("data").append(
        File.separator).append("my-books.xml").toString());

    @Before
    public void setUp() throws Exception {
        Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
        Database.createDatabase(new DatabaseConfiguration(TestHelper.PATHS.PATH1.getFile()));

        databaseBooks = Database.openDatabase(TestHelper.PATHS.PATH1.getFile());
        final IWriteTransaction mWTX = databaseBooks.getSession().beginWriteTransaction();
        final XMLEventReader reader = XMLShredder.createReader(BOOKSXML);
        final XMLShredder shredder = new XMLShredder(mWTX, reader, true);
        shredder.call();
        mWTX.close();
    }

    @AfterClass
    public static void tearDown() throws TreetankException {
        databaseBooks.close();
        Database.forceCloseDatabase(TestHelper.PATHS.PATH1.getFile());
    }

    @Test
    public void testWhereBooks() throws Exception {
        final XdmValue value =
            new XQueryEvaluator("for $x in /bookstore/book where $x/price>30 return $x/title", databaseBooks)
                .call();

        final StringBuilder strBuilder = new StringBuilder();

        for (final XdmItem item : value) {
            strBuilder.append(item.toString());
        }

        assertEquals("<title lang=\"en\">XQuery Kick Start</title><title lang=\"en\">Learning XML</title>",
            strBuilder.toString());
    }

    @Test
    public void testOrderByBooks() throws Exception {
        final XdmValue value =
            new XQueryEvaluator(
                "for $x in /bookstore/book where $x/price>30 order by $x/title return $x/title",
                databaseBooks).call();

        final StringBuilder strBuilder = new StringBuilder();

        for (final XdmItem item : value) {
            strBuilder.append(item.toString());
        }

        assertEquals("<title lang=\"en\">Learning XML</title><title lang=\"en\">XQuery Kick Start</title>",
            strBuilder.toString());
    }

    @Test
    public void testFLOWR() throws Exception {
        final XdmValue value =
            new XQueryEvaluator(
                "for $x in /bookstore/book let $y := $x/price where $y>30 order by $x/title return $x/title",
                databaseBooks).call();
        final StringBuilder strBuilder = new StringBuilder();

        for (final XdmItem item : value) {
            strBuilder.append(item.toString());
        }

        assertEquals("<title lang=\"en\">Learning XML</title><title lang=\"en\">XQuery Kick Start</title>",
            strBuilder.toString());
    }

}

package com.treetank.saxon.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;

import junit.framework.TestCase;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.saxon.evaluator.XQueryEvaluatorOutputStream;
import com.treetank.service.xml.XMLShredder;

/**
 * Test XQuery S9Api.
 * 
 * @author johannes
 * 
 */
public final class TestNodeWrapperS9ApiXQueryOutputStream {

	/** Treetank database on books document. */
	private transient static IDatabase databaseBooks;

	/** Path to books file. */
	private static final File BOOKSXML = new File(new StringBuilder("src")
			.append(File.separator).append("test").append(File.separator)
			.append("resources").append(File.separator).append("data").append(
					File.separator).append("my-books.xml").toString());

	@Before
	public void setUp() throws Exception {
		Database.truncateDatabase(TestHelper.PATHS.PATH1.getFile());
		Database.createDatabase(new DatabaseConfiguration(
				TestHelper.PATHS.PATH1.getFile()));

		databaseBooks = Database.openDatabase(TestHelper.PATHS.PATH1.getFile());
		final IWriteTransaction mWTX = databaseBooks.getSession()
				.beginWriteTransaction();
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
	public void testWhereBooks() {
		final OutputStream out = new ByteArrayOutputStream();
		new XQueryEvaluatorOutputStream(
				"for $x in /bookstore/book where $x/price>30 return $x/title",
				databaseBooks, out).run();
		final String result = out.toString();
		TestCase
				.assertEquals(
						"<title lang=\"en\">XQuery Kick Start</title><title lang=\"en\">Learning XML</title>",
						result);
	}

	@Test
	public void testOrderByBooks() {
		final OutputStream out = new ByteArrayOutputStream();
		new XQueryEvaluatorOutputStream(
				"for $x in /bookstore/book where $x/price>30 order by $x/title return $x/title",
				databaseBooks, out).run();
		final String result = out.toString();
		TestCase
				.assertEquals(
						"<title lang=\"en\">Learning XML</title><title lang=\"en\">XQuery Kick Start</title>",
						result);
	}

	@Test
	public void testFLOWR() {
		final OutputStream out = new ByteArrayOutputStream();
		new XQueryEvaluatorOutputStream(
				"for $x in /bookstore/book let $y := $x/price where $y>30 order by $x/title return $x/title",
				databaseBooks, out).run();
		final String result = out.toString();
		TestCase
				.assertEquals(
						"<title lang=\"en\">Learning XML</title><title lang=\"en\">XQuery Kick Start</title>",
						result);
	}

}

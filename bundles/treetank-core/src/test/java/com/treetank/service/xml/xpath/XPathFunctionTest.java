/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: XPathFunctionTest.java 4433 2008-08-28 14:26:02Z scherer $
 */

package com.treetank.service.xml.xpath;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.IAxisTest;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.XMLShredder;

/**
 * Performes the XPathFunctionalityTest provided on <a
 * href="http://sole.dimi.uniud.it/~massimo.franceschet/xpathmark/FT.html">
 * XPathMark</a>
 * 
 * @author Tina Scherer
 */
public class XPathFunctionTest {

	public static final String XML = "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator + "alphabet.xml";

	@Before
	public void setUp() throws Exception {
		TestHelper.deleteEverything();
		// Build simple test tree.
		XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());
	}

	@After
	public void tearDown() throws TreetankException {
		TestHelper.closeEverything();
	}

	@Test
	public void testA_Axes() throws TreetankException {

		// Verify.
		final IDatabase database = TestHelper
				.getDatabase(PATHS.PATH1.getFile());
		final ISession session = database.getSession();
		final IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToDocumentRoot();

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/*"), new long[] {
				58L, 63L, 77L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/parent::*"),
				new long[] { 20L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/descendant::*"),
				new long[] { 58L, 63, 67L, 72L, 77L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//L/descendant-or-self::*"), new long[] { 53L, 58L, 63, 67L,
				72L, 77L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/ancestor::*"),
				new long[] { 20L, 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//L/ancestor-or-self::*"), new long[] { 53L, 20L, 1L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//L/following-sibling::*"), new long[] { 83L, 97L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//L/preceding-sibling::*"), new long[] { 39L, 24L });

		IAxisTest
				.testIAxisConventions(new XPathAxis(rtx, "//L/following::*"),
						new long[] { 83L, 87L, 92L, 97L, 101L, 106L, 111L,
								115L, 120L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/preceding::*"),
				new long[] { 48L, 43L, 39L, 33L, 28L, 24L, 15L, 10L, 6L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/self::*"),
				new long[] { 53L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/@id/parent::*"),
				new long[] { 53L });

		rtx.close();
		session.close();
		database.close();

	}

	@Test
	public void testP_Filters() throws TreetankException {

		// Verify.
		final IDatabase database = TestHelper
				.getDatabase(PATHS.PATH1.getFile());
		final ISession session = database.getSession();
		final IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToDocumentRoot();

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//*[L]"),
				new long[] { 20L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//*[parent::L]"),
				new long[] { 58L, 63L, 77L });

		IAxisTest.testIAxisConventions(
				new XPathAxis(rtx, "//*[descendant::L]"),
				new long[] { 1L, 20L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//*[descendant-or-self::L]"), new long[] { 1L, 20L, 53L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//*[ancestor::L]"),
				new long[] { 58L, 63L, 77L, 67L, 72L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//*[ancestor-or-self::L]"), new long[] { 53L, 58L, 63L, 77L,
				67L, 72L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//*[following-sibling::L]"), new long[] { 24L, 39L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//*[preceding-sibling::L]"), new long[] { 83L, 97L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//*[following::L]"),
				new long[] { 6L, 10L, 15L, 24L, 39L, 28L, 33L, 43L, 48L });

		IAxisTest
				.testIAxisConventions(new XPathAxis(rtx, "//*[preceding::L]"),
						new long[] { 111L, 83L, 97L, 87L, 92L, 101L, 106L,
								115L, 120L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//*[self::L]"),
				new long[] { 53L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//*[@id]"),
				new long[] { 1L, 6L, 20L, 111L, 10L, 15L, 24L, 39L, 53L, 83L,
						97L, 28L, 33L, 43L, 48L, 58L, 63L, 77L, 67L, 72L, 87L,
						92L, 101L, 106L, 115L, 120L });

		rtx.moveTo(111L);
		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "preceding::node()"),
				new long[] { 110L, 106L, 105L, 101L, 97L, 96L, 92L, 91L, 87L,
						83L, 82L, 77L, 76L, 72L, 71L, 67L, 63L, 62L, 58L, 57L,
						53L, 52L, 48L, 47L, 43L, 39L, 38L, 33L, 32L, 28L, 24L,
						20L, 19L, 15L, 14L, 10L, 6L });

		rtx.moveTo(6L);
		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "following::node()"),
				new long[] { 20L, 24L, 28L, 32L, 33L, 38L, 39L, 43L, 47L, 48L,
						52L, 53L, 57L, 58L, 62L, 63L, 67L, 71L, 72L, 76L, 77L,
						82L, 83L, 87L, 91L, 92L, 96L, 97L, 101L, 105L, 106L,
						110L, 111L, 115L, 119L, 120L, 126L });

		rtx.close();
		session.close();
		database.close();
	}

	@Test
	public void testT_NodeTests() throws TreetankException {

		// Verify.
		final IDatabase database = TestHelper
				.getDatabase(PATHS.PATH1.getFile());
		final ISession session = database.getSession();
		final IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToDocumentRoot();

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/text()"),
				new long[] { 57L, 62L });

		// comments are not supported yet
		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/comment()"),
				new long[] {});

		// porcessing instructions are not supported yet
		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//L/processing-instruction()"), new long[] {});

		// porcessing instructions are not supported yet
		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//L/processing-instruction(\"myPI\")"), new long[] {});

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/node()"),
				new long[] { 57L, 58L, 62L, 63L, 77L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, " //L/N"),
				new long[] { 63L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/*"), new long[] {
				58L, 63L, 77L });

		rtx.close();
		session.close();
		database.close();
	}

	@Test
	public void testQ_Operators() throws TreetankException {

		// Verify.
		final IDatabase database = TestHelper
				.getDatabase(PATHS.PATH1.getFile());
		final ISession session = database.getSession();
		final IReadTransaction rtx = session.beginReadTransaction();
		rtx.moveToDocumentRoot();

		IAxisTest
				.testIAxisConventions(new XPathAxis(rtx, "//*[preceding::Q]"),
						new long[] { 111L, 83L, 97L, 87L, 92L, 101L, 106L,
								115L, 120L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//*[child::* and preceding::Q]"),
				new long[] { 111L, 83L, 97L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//*[fn:not(child::*) and preceding::Q]"), new long[] { 87L,
				92L, 101L, 106L, 115L, 120L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//*[preceding::L or following::L]"), new long[] { 6L, 111L,
				10L, 15L, 24L, 39L, 83L, 97L, 28L, 33L, 43L, 48L, 87L, 92L,
				101L, 106L, 115L, 120L });

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//L/ancestor::* | //L/descendant::*"), new long[] { 20L, 1L,
				58L, 63L, 67L, 72L, 77L });

		// IAxisTest.testIAxisConventions(new XPathAxis(rtx,
		// "//*[.=\"happy-go-lucky man\"]"), new long[] { 38L});

		IAxisTest.testIAxisConventions(new XPathAxis(rtx,
				"//*[@pre > 12 and @post < 15]"), new long[] { 58L, 63L, 77L,
				67L, 72L });

		IAxisTest.testIAxisConventions(
				new XPathAxis(rtx, "//*[@pre != @post]"), new long[] { 1L, 6L,
						20L, 111L, 10L, 15L, 53L, 28L, 33L, 43L, 48L, 58L, 63L,
						77L, 67L, 72L, 87L, 92L, 101L, 106L, 115L, 120L });

		IAxisTest.testIAxisConventions(
				new XPathAxis(rtx, "//*[@pre mod 2 = 0]"), new long[] { 6L,
						111L, 15L, 24L, 53L, 83L, 33L, 43L, 63L, 72L, 92L,
						101L, 120L });

		IAxisTest
				.testIAxisConventions(
						new XPathAxis(
								rtx,
								"//*[((@post * @post + @pre * @pre) div (@post + @pre)) > ((@post - @pre) * (@post - @pre))] "),
						new long[] { 6L, 111L, 24L, 39L, 53L, 83L, 97L, 48L,
								58L, 63L, 77L, 87L, 92L, 101L, 106L, 115L, 120L });

		rtx.close();
		session.close();
		database.close();

	}
	//
	// //TODO: functions!
	//
}
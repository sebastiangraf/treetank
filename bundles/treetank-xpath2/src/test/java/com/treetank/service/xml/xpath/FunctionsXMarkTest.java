package com.treetank.service.xml.xpath;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.service.xml.xpath.XPathStringChecker;

/**
 * This class performs tests for XQuery functions used for XMark bench test and
 * XPathMark bench test.
 * 
 * @author Patrick Lang, Konstanz University
 * 
 */
public final class FunctionsXMarkTest {

	/** XML File. */
	private static final String XMLFILE = "10mb.xml";

	/** Location of XML File. */
	private static final String XML = "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator + XMLFILE;

	/** IDatabase instance. */
	private IDatabase database = null;
	/** ISession instance. */
	private ISession session = null;
	/** IReadTransaction instance. */
	private IReadTransaction rtx = null;

	/**
	 * Empty constructor. Just to meet the formatter requirements.
	 */
	public FunctionsXMarkTest() {
	}

	@Before
	public void setUp() {
		try {
			TestHelper.deleteEverything();
			XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());
			database = TestHelper.getDatabase(PATHS.PATH1.getFile());
			session = database.getSession();
			rtx = session.beginReadTransaction();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test function string().
	 */
	@Test
	public void testString() {
		final String query = "fn:string(/site/people/person[@id=\"person3\"]/name)";
		final String result = "Limor Simone";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test comment.
	 */
	@Test
	public void testComment() {
		final String query = "2 (: this is a comment :)";
		final String result = "2";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function node().
	 */
	@Test
	public void testNode() {
		final String query = "for $b in /site/people/person[@id=\"person1\"] return $b/name/node()";
		final String result = "Keung Yetim";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function text().
	 */
	@Test
	public void testText() {
		final String query = "for $b in /site/people/person[@id=\"person0\"] return $b/name/text()";
		final String result = "Krishna Merle";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function count().
	 */
	@Test
	public void testCount() {
		final String query = "fn:count(for $i in /site/closed_auctions/closed_auction[price/text() >= 40] return $i/price)";
		final String result = "670";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function position().
	 */
	@Ignore
	@Test
	public void testPosition() {
		final String query = "/site/open_auctions/open_auction/bidder/increase[position()=1]";
		final String result = "<increase>10.50</increase>";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function not().
	 */
	@Ignore
	@Test
	public void testNot() {
		final String query = "/site/people/person[not(homepage)][@id=\"person1\"]/name/text()";
		final String result = "<name>Keung Yetim</name>";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function id().
	 */
	@Ignore
	@Test
	public void testId() {
		final String query = "fn:id(/site/people/person[@id=\"person1\"]/watches/watch/@open_auction)";
		final String result = "";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function data().
	 */
	@Ignore
	@Test
	public void testData() {
		final String query = "for $b in /site/people/person[@id=\"person0\"] return fn:data($b/name)";
		final String result = "Krishna Merle";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function contains().
	 */
	@Ignore
	@Test
	public void testContains() {
		final String query = "/site/regions/*/item[contains(description,\"gold\")]/location[text()=\"El Salvador\"]";
		final String result = "<location>El Salvador</location>";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function exactly-one(). alternative query: exactly-one('a') ->
	 * result: a
	 */
	@Ignore
	@Test
	public void testExactlyOne() {
		final String query = "exactly-one(/site/people/person[@id=\"person0\"]/name)";
		final String result = "<name>Krishna Merle</name>";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function sum().
	 */
	@Ignore
	@Test
	public void testSum() {
		final String query = "fn:sum(/site/open_auctions/open_auction/bidder/increase/text())";
		final String result = "96496.5";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function zero-or-one(). alternative query: zero-or-one('a') ->
	 * result: a
	 */
	@Ignore
	@Test
	public void testZeroOrOne() {
		final String query = " for $i in /site/open_auctions/open_auction return zero-or-one($i/reserve[text()=\"20.54\"]/text())";
		final String result = "20.54";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function max().
	 */
	@Ignore
	@Test
	public void testMax() {
		final String query = "fn:max(for $i in /site/open_auctions/open_auction return $i/reserve/text())";
		final String result = "4701.79";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function min().
	 */
	@Ignore
	@Test
	public void testMin() {
		final String query = "fn:min(for $i in /site/open_auctions/open_auction return $i/reserve/text())";
		final String result = "0.43";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function empty().
	 */
	@Ignore
	@Test
	public void testEmpty() {
		final String query = "fn:empty(for $i in /site/open_auctions/open_auction return $i/reserve/text())";
		final String result = "false";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function one-or-more().
	 */
	@Ignore
	@Test
	public void testOneOrMore() {
		final String query = "fn:one-or-more(\"a\")";
		final String result = "a";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function exists().
	 */
	@Ignore
	@Test
	public void testExists() {
		final String query = "fn:exists( ('a', 'b', 'c') )";
		final String result = "true";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function substring-after().
	 */
	@Ignore
	@Test
	public void testSubstringAfter() {
		final String query = "fn:substring-after(\"query\", \"u\")";
		final String result = "ery";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function substring-before().
	 */
	@Ignore
	@Test
	public void testSubstringBefore() {
		final String query = "fn:substring-before(\"query\", \"r\")";
		final String result = "que";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function last().
	 */
	@Ignore
	@Test
	public void testLast() {
		final String query = "/site/open_auctions/open_auction/reserve[last()]";
		final String result = "<reserve>539.66</reserve>";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function boolean().
	 */
	@Ignore
	@Test
	public void testBoolean() {
		final String query = "fn:boolean(0)";
		final String result = "false";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function number().
	 */
	@Ignore
	@Test
	public void testNumber() {
		final String query = "/site/open_auctions/open_auction/bidder[personref[@person=\"person2436\"]]/increase/number()";
		final String result = "12 12";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function distinct-values().
	 */
	@Ignore
	@Test
	public void testDistinctValues() {
		final String query = "fn:distinct-values(/site/open_auctions/open_auction/bidder[personref[@person=\"person2436\"]]/increase)";
		final String result = "12.00";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function root().
	 */
	@Ignore
	@Test
	public void testRoot() {
		final String query = "fn:root()/site/people/person[@id=\"person0\"]/name/text()";
		final String result = "Krishna Merle";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test function floor().
	 */
	@Ignore
	@Test
	public void testFloor() {
		final String query = "fn:floor(5.7)";
		final String result = "5";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	/**
	 * Test <element attribute=""/> in return statement.
	 */

	@Ignore
	@Test
	public void testElementAttributeInReturn() {
		final String query = "for $b in /site/open_auctions/open_auction/bidder[personref[@person=\"person2436\"]]/increase return <element attribute=\"{$b/text()}\"/>";
		final String result = "<element attribute=\"12.00\"/><element attribute=\"12.00\"/>";
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx, query),
				new String[] { result });
	}

	@After
	public void tearDown() {
		try {
			rtx.close();
			session.close();
			database.close();
			TestHelper.closeEverything();
		} catch (TreetankException e) {
			e.printStackTrace();
		}

	}

}

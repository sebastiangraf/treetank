package com.treetank.service.xml.xpath;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.perfidix.annotation.BenchClass;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.shredder.XMLShredder;

/**
 * Performes the XMark benchmark.
 * 
 * @author Patrick Lang
 */
// @BenchClass(runs = 2)
public class XMarkBenchTest {

	private static final String XML = "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator + "auction.xml";

	private IDatabase database = null;
	private ISession session = null;
	private IReadTransaction rtx = null;

	@Before
	public void setUp() throws Exception {
		TestHelper.deleteEverything();
		XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());

		database = TestHelper.getDatabase(PATHS.PATH1.getFile());
		session = database.getSession();
		rtx = session.beginReadTransaction();
	}

	@Test
	public void xMarkTest_Q1() {
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
				"for $b in /site/people/person[@id=\"person0\"] "
						+ "return $b/name/text()"),
				new String[] { "Sinisa Farrel" });

	}

	@Test
	public void xMarkTest_Q5() {
		XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
				"fn:count(for $i in /site/closed_auctions/closed_auction[price/text() >= 40] "
						+ "return $i/price)"), new String[] { "75" });

	}

	@After
	public void tearDown() throws TreetankException {
		TestHelper.closeEverything();
	}

}

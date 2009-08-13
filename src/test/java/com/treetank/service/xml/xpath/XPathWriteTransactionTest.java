package com.treetank.service.xml.xpath;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.service.xml.XMLShredder;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;

/**
 * Testcase for working with XPath and WriteTransactions
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class XPathWriteTransactionTest {

	public static final String XML = "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator
			+ "enwiki-revisions-test.xml";

	private ISession session;

	private IWriteTransaction wtx;

	@Before
	public void setUp() {

		Session.removeSession(ITestConstants.PATH1);
		// Build simple test tree.
		XMLShredder.shred(XML, new SessionConfiguration(ITestConstants.PATH1));

		// Verify.
		session = Session.beginSession(ITestConstants.PATH1);
		wtx = session.beginWriteTransaction();
	}

	@Test
	public void test() throws Exception {
		wtx.moveToDocumentRoot();
		// final XPathAxis xpa =
		// new XPathAxis(wtx, "//revision[./parent::page/title/text() = '"
		// + "AmericanSamoa"
		// + "']");
		final XPathAxis xpa = new XPathAxis(wtx, "//revision");
		if (!xpa.hasNext()) {
			fail();
		}

	}

	@After
	public void tearDown() {
		// wtx.abort();
		wtx.close();
		session.close();
	}

}

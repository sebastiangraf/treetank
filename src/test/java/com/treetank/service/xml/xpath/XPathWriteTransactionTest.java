package com.treetank.service.xml.xpath;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.XMLShredder;

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

    private IDatabase database;

    @Before
    public void setUp() throws Exception {
        TestHelper.deleteEverything();
        // Build simple test tree.
        XMLShredder.main(XML, ITestConstants.PATH1.getAbsolutePath());

        // Verify.
        database = Database.openDatabase(ITestConstants.PATH1);
        session = database.getSession();
        wtx = session.beginWriteTransaction();
    }

    @Test
    public void test() {
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
    public void tearDown() throws TreetankException {
        // wtx.abort();
        wtx.close();
        session.close();
        database.close();
        TestHelper.closeEverything();
    }

}

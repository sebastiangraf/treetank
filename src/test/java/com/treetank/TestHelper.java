package com.treetank;

import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import com.treetank.exception.TreetankException;
import com.treetank.session.Session;

/**
 * 
 * Helper class for offering convenient usage of <code>Sessions</code> for test
 * cases.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TestHelper {

    public TestHelper() {
        // Not used over here
    }

    @Test
    public void testDummy() {
        // Just empty to ensure maven running
    }

    @Ignore
    public static final void deleteEverything() {
        try {
            Session.removeSession(ITestConstants.PATH1);
            Session.removeSession(ITestConstants.PATH2);
            Session.removeSession(ITestConstants.NON_EXISTING_PATH);
            Session.removeSession(ITestConstants.TEST_INSERT_CHILD_PATH);
            Session.removeSession(ITestConstants.TEST_REVISION_PATH);
            Session.removeSession(ITestConstants.TEST_SHREDDED_REVISION_PATH);
            Session.removeSession(ITestConstants.TEST_EXISTING_PATH);
        } catch (final TreetankException exc) {
            fail(exc.toString());
        }
    }

    @Ignore
    public static final void closeEverything() {
//        Session.closeSession(ITestConstants.PATH1);
//        Session.closeSession(ITestConstants.PATH2);
//        Session.closeSession(ITestConstants.NON_EXISTING_PATH);
//        Session.closeSession(ITestConstants.TEST_INSERT_CHILD_PATH);
//        Session.closeSession(ITestConstants.TEST_REVISION_PATH);
//        Session.closeSession(ITestConstants.TEST_SHREDDED_REVISION_PATH);
//        Session.closeSession(ITestConstants.TEST_EXISTING_PATH);
    }
}

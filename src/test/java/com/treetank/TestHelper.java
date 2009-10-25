package com.treetank;

import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

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
    public static final void removeAllFiles() {
        try {
            Session.removeSession(ITestConstants.PATH1);
        } catch (final Exception exc) {
            fail(exc.toString());
        }
    }

}

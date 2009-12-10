package com.treetank;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.treetank.access.Session;
import com.treetank.exception.TreetankException;
import com.treetank.node.ElementNode;
import com.treetank.page.NodePage;

/**
 * 
 * Helper class for offering convenient usage of {@link Session}s for test
 * cases.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class TestHelper {

    public final static Random random = new Random();

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
        } catch (TreetankException e) {
            e.printStackTrace();
        }

    }

    @Ignore
    public static final void closeEverything() {
        try {
            Session.closeSession(ITestConstants.PATH1);
            Session.closeSession(ITestConstants.PATH2);
            Session.closeSession(ITestConstants.NON_EXISTING_PATH);
            Session.closeSession(ITestConstants.TEST_INSERT_CHILD_PATH);
            Session.closeSession(ITestConstants.TEST_REVISION_PATH);
            Session.closeSession(ITestConstants.TEST_SHREDDED_REVISION_PATH);
            Session.closeSession(ITestConstants.TEST_EXISTING_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Ignore
    public static NodePage getNodePage(final long revision, final int offset,
            final int length) {
        final NodePage page = new NodePage(0, revision);
        for (int i = offset; i < length; i++) {
            page.setNode(i, new ElementNode(random.nextLong(), random
                    .nextLong(), random.nextLong(), random.nextLong(), random
                    .nextLong(), random.nextInt(), random.nextInt(), random
                    .nextInt()));
        }
        return page;
    }

}

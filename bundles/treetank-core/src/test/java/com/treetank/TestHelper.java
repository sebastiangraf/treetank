package com.treetank;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

import com.treetank.access.Database;
import com.treetank.access.Session;
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
        Database.truncateDatabase(ITestConstants.PATH1);
        Database.truncateDatabase(ITestConstants.PATH2);
        Database.truncateDatabase(ITestConstants.NON_EXISTING_PATH);
        Database.truncateDatabase(ITestConstants.TEST_INSERT_CHILD_PATH);
        Database.truncateDatabase(ITestConstants.TEST_REVISION_PATH);
        Database.truncateDatabase(ITestConstants.TEST_SHREDDED_REVISION_PATH);
        Database.truncateDatabase(ITestConstants.TEST_EXISTING_PATH);

    }

    @Ignore
    public static final void closeEverything() {
        try {
            Database.forceCloseDatabase(ITestConstants.PATH1);
            Database.forceCloseDatabase(ITestConstants.PATH2);
            Database.forceCloseDatabase(ITestConstants.NON_EXISTING_PATH);
            Database.forceCloseDatabase(ITestConstants.TEST_INSERT_CHILD_PATH);
            Database.forceCloseDatabase(ITestConstants.TEST_REVISION_PATH);
            Database
                    .forceCloseDatabase(ITestConstants.TEST_SHREDDED_REVISION_PATH);
            Database.forceCloseDatabase(ITestConstants.TEST_EXISTING_PATH);
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

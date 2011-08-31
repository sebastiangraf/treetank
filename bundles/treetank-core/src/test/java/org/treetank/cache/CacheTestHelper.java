package org.treetank.cache;

import org.junit.Test;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.exception.AbsTTException;
import org.treetank.page.NodePage;

/**
 * Helper class for testing the cache.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class CacheTestHelper {

    protected static NodePage[][] PAGES;

    public static void setUp(final ICache cache) throws AbsTTException {
        PAGES = new NodePage[LRUCache.CACHE_CAPACITY + 1][ResourceConfiguration.VERSIONSTORESTORE + 1];
        for (int i = 0; i < PAGES.length; i++) {
            final NodePage page = new NodePage(i, 0);
            final NodePage[] revs = new NodePage[ResourceConfiguration.VERSIONSTORESTORE];

            for (int j = 0; j < ResourceConfiguration.VERSIONSTORESTORE; j++) {
                PAGES[i][j + 1] = new NodePage(i, 0);
                revs[j] = PAGES[i][j + 1];
            }
            PAGES[i][0] = page;
            cache.put(i, new NodePageContainer(page));
        }
    }

    @Test
    public void dummy() {
        // Only for dummy purposes.
    }

}

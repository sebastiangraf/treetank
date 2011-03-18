package org.treetank.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import org.treetank.cache.ICache;
import org.treetank.cache.LRUCache;
import org.treetank.cache.NodePageContainer;
import org.treetank.page.NodePage;
import org.treetank.settings.EDatabaseSetting;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class LRUCacheTest {

    private final NodePage[][] pages = new NodePage[LRUCache.CACHE_CAPACITY + 1][Integer
        .parseInt(EDatabaseSetting.REVISION_TO_RESTORE.getStandardProperty()) + 1];

    private ICache cache;

    @Before
    public void setUp() {
        cache = new LRUCache();
        for (int i = 0; i < pages.length; i++) {
            final NodePage page = new NodePage(i, 0);
            final NodePage[] revs =
                new NodePage[Integer.parseInt(EDatabaseSetting.REVISION_TO_RESTORE.getStandardProperty())];

            for (int j = 0; j < Integer.parseInt(EDatabaseSetting.REVISION_TO_RESTORE.getStandardProperty()); j++) {
                pages[i][j + 1] = new NodePage(i, 0);
                revs[j] = pages[i][j + 1];
            }
            pages[i][0] = page;
            cache.put(i, new NodePageContainer(page));
        }
    }

    @Test
    public void test() {
        for (int i = 1; i < pages.length; i++) {
            final NodePageContainer cont = cache.get(i);
            final NodePage current = cont.getComplete();
            assertEquals(pages[i][0], current);
        }

        final NodePageContainer page = cache.get(0);
        assertNull(page);

    }

}

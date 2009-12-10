package com.treetank.cache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.treetank.page.NodePage;
import com.treetank.settings.EDatabaseSetting;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class RAMCacheTest {

    private final NodePage[][] pages = new NodePage[LRUCache.CACHE_CAPACITY + 1][(Integer) EDatabaseSetting.MILESTONE_REVISION.getStandardProperty() + 1];

    private ICache cache;

    @Before
    public void setUp() {
        cache = new RAMCache();
        for (int i = 0; i < pages.length; i++) {
            final NodePage page = new NodePage(i, 0);
            final NodePage[] revs = new NodePage[(Integer) EDatabaseSetting.MILESTONE_REVISION.getStandardProperty()];

            for (int j = 0; j < (Integer) EDatabaseSetting.MILESTONE_REVISION.getStandardProperty(); j++) {
                pages[i][j + 1] = new NodePage(i, 0);
                revs[j] = pages[i][j + 1];
            }
            pages[i][0] = page;
            cache.put(i, new NodePageContainer(page));
        }
    }

    @Test
    public void test() {
        boolean foundAtLeastOne = false;
        for (int i = 0; i < pages.length; i++) {
            if (cache.get(i) != null) {
                foundAtLeastOne = true;
            }
        }
        assertTrue(foundAtLeastOne);
        cache.clear();
        foundAtLeastOne = false;
        for (int i = 0; i < pages.length; i++) {
            if (cache.get(i) != null) {
                foundAtLeastOne = true;
            }
        }
        assertFalse(foundAtLeastOne);
    }

}

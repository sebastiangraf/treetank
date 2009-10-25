package com.treetank.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.treetank.ITestConstants;
import com.treetank.io.TreetankIOException;
import com.treetank.page.AbstractPage;
import com.treetank.page.NodePage;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;

public class TransactionLogCacheTest {

    private final AbstractPage[] pages = new AbstractPage[LRUCache.CACHE_CAPACITY + 1];

    private ICache cache;

    @Before
    public void setUp() {
        try {
            Session.removeSession(new File(ITestConstants.PATH1));
            cache = new TransactionLogCache(new SessionConfiguration(
                    ITestConstants.PATH1));
            for (int i = 0; i < pages.length; i++) {
                final NodePage page = new NodePage(i);
                pages[i] = page;
                cache.put(i, page);
            }
        } catch (final TreetankIOException exc) {
            fail(exc.toString());
        }
    }

    @Test
    public void test() {
        for (int i = 0; i < pages.length; i++) {
            final NodePage page1 = (NodePage) pages[i];
            final NodePage page2 = (NodePage) cache.get(i);
            assertEquals(page1.getNodePageKey(), page2.getNodePageKey());
        }
        cache.clear();
    }
}

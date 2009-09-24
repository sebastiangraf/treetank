package com.treetank.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.treetank.page.AbstractPage;
import com.treetank.page.NodePage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class LRUCacheTest {

	private final AbstractPage[] pages = new AbstractPage[LRUCache.CACHE_CAPACITY + 1];

	private ICache cache;

	@Before
	public void setUp() {
		cache = new LRUCache();
		for (int i = 0; i < pages.length; i++) {
			final NodePage page = new NodePage(i);
			pages[i] = page;
			cache.put(i, page);
		}
	}

	@Test
	public void test() {
		for (int i = 1; i <= LRUCache.CACHE_CAPACITY; i++) {
			assertEquals(pages[i], cache.get(i));
		}

		final AbstractPage page = cache.get(0);
		assertTrue(page == null);

	}

}

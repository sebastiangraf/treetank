package com.treetank.cache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.treetank.page.AbstractPage;
import com.treetank.page.NodePage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class RAMCacheTest {

	private final AbstractPage[] pages = new AbstractPage[25];

	private ICache cache;

	@Before
	public void setUp() {
		cache = new RAMCache();
		for (int i = 0; i < pages.length; i++) {
			cache.put(i, new NodePage(i));
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

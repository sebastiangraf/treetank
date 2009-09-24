package com.treetank.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NamePageHashTest {

	@Test
	public void testGenerateHashCodes() {
		final int[] hashes = { 70, 25 };
		assertArrayEquals(hashes, NamePageHash
				.generateOffsets("foo".hashCode()));
	}

	@Test
	public void testGenerateHashForString() {
		final int hash = "foo".hashCode();
		assertEquals(hash, NamePageHash.generateHashForString("foo"));
	}

}

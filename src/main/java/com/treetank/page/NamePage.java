/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: NamePage.java 4442 2008-08-30 16:17:17Z kramis $
 */

package com.treetank.page;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.treetank.utils.TypedValue;

/**
 * <h1>NamePage</h1>
 * 
 * <p>
 * Name page holds all names and their keys for a revision.
 * </p>
 */
public final class NamePage extends AbstractPage {

	/** Map the hash of a name to its name. */
	private final Map<Integer, String> mNameMap;

	// /** Map the hash of a name to its name. */
	// private final Map<Integer, byte[]> mRawNameMap;

	/**
	 * Create name page.
	 */
	public NamePage() {
		super(0);
		mNameMap = new HashMap<Integer, String>();
		// mRawNameMap = new HashMap<Integer, byte[]>();
	}

	/**
	 * Read name page.
	 * 
	 * @param in
	 *            Input bytes to read from.
	 */
	public NamePage(final ByteBuffer in) {
		super(0, in);
		mNameMap = new HashMap<Integer, String>();
		// mRawNameMap = new HashMap<Integer, byte[]>();

		for (int i = 0, l = (int) in.getLong(); i < l; i++) {
			final int key = (int) in.getLong();
			final byte[] bytes = new byte[(int) in.getLong()];
			for (int j = 0; j < bytes.length; j++) {
				bytes[j] = in.get();
			}
			mNameMap.put(key, TypedValue.parseString(bytes));
			// mRawNameMap.put(key, bytes);
		}
	}

	/**
	 * Clone name page.
	 * 
	 * @param committedNamePage
	 *            Page to clone.
	 */
	public NamePage(final NamePage committedNamePage) {
		super(0, committedNamePage);
		mNameMap = new HashMap<Integer, String>(committedNamePage.mNameMap);
		// mRawNameMap = new HashMap<Integer, byte[]>(
		// committedNamePage.mRawNameMap);
	}

	/**
	 * Get name belonging to name key.
	 * 
	 * @param key
	 *            Name key identifying name.
	 * @return Name of name key.
	 */
	public final String getName(final int key) {
		return mNameMap.get(key);
	}

	/**
	 * Get raw name belonging to name key.
	 * 
	 * @param key
	 *            Name key identifying name.
	 * @return Raw name of name key.
	 */
	public final byte[] getRawName(final int key) {
		return TypedValue.getBytes(mNameMap.get(key));
	}

	/**
	 * Create name key given a name.
	 * 
	 * @param key
	 *            Key for given name.
	 * @param name
	 *            Name to create key for.
	 */
	public final void setName(final int key, final String name) {
		mNameMap.put(key, name);
		// mRawNameMap.put(key, TypedValue.getBytes(name));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void serialize(final ByteBuffer out) {
		super.serialize(out);

		out.putLong(mNameMap.size());

		for (final int key : mNameMap.keySet()) {
			out.putLong(key);
			byte[] tmp = TypedValue.getBytes(mNameMap.get(key));
			out.putLong(tmp.length);
			for (final byte byteVal : tmp) {
				out.put(byteVal);
			}

		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return super.toString() + ": nameCount=" + mNameMap.size()
				+ ", isDirty=" + isDirty();
	}

}

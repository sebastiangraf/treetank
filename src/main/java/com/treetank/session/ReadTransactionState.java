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
 * $Id: ReadTransactionState.java 4424 2008-08-28 09:15:01Z kramis $
 */

package com.treetank.session;

import com.treetank.api.IItem;
import com.treetank.api.IItemList;
import com.treetank.cache.ICache;
import com.treetank.cache.RAMCache;
import com.treetank.io.IReader;
import com.treetank.page.AbstractPage;
import com.treetank.page.IndirectPage;
import com.treetank.page.NamePage;
import com.treetank.page.NodePage;
import com.treetank.page.PageReference;
import com.treetank.page.RevisionRootPage;
import com.treetank.page.UberPage;
import com.treetank.utils.IConstants;

/**
 * <h1>ReadTransactionState</h1>
 * 
 * <p>
 * State of a reading transaction. The only thing shared amongst transactions is
 * the page cache. Everything else is exclusive to this transaction. It is
 * required that only a single thread has access to this transaction.
 * </p>
 * 
 * <p>
 * A path-like cache boosts sequential operations.
 * </p>
 */
public class ReadTransactionState {

	/** Session configuration. */
	private SessionConfiguration mSessionConfiguration;

	/** Page reader exclusively assigned to this transaction. */
	private IReader mPageReader;

	/** Uber page this transaction is bound to. */
	private UberPage mUberPage;

	/** Revision root page as root of this transaction. */
	private RevisionRootPage mRevisionRootPage;

	/** Cached name page of this revision. */
	private NamePage mNamePage;

	/** Read-transaction-exclusive item list. */
	private IItemList mItemList;

	/** Internal reference to cache */
	private final ICache mCache;

	/**
	 * Standard constructor.
	 * 
	 * @param sessionConfiguration
	 *            Configuration of session.
	 * @param pageCache
	 *            Shared page cache.
	 * @param uberPage
	 *            Uber page to start reading with.
	 * @param revisionKey
	 *            Key of revision to read from uber page.
	 * @param itemList
	 *            List of non-persistent items.
	 * @param reader
	 *            for this transaction
	 */
	protected ReadTransactionState(
			final SessionConfiguration sessionConfiguration,
			final UberPage uberPage, final long revisionKey,
			final IItemList itemList, final IReader reader) {
		mCache = new RAMCache();
		mSessionConfiguration = sessionConfiguration;
		mPageReader = reader;
		mUberPage = uberPage;
		mRevisionRootPage = getRevisionRootPage(revisionKey);
		mNamePage = getNamePage();
		mItemList = itemList;

	}

	/**
	 * {@inheritDoc}
	 */
	public final RevisionRootPage getRevisionRootPage() {
		return mRevisionRootPage;
	}

	/**
	 * {@inheritDoc}
	 */
	protected IItem getNode(final long nodeKey) {

		// Immediately return node from item list if node key negative.
		if (nodeKey < 0) {
			if (mItemList == null) {
				throw new IllegalStateException(
						"NodeKey="
								+ nodeKey
								+ ": negative node key encountered but no item list available.");
			}
			return mItemList.getItem(nodeKey);
		}

		// Calculate page and node part for given nodeKey.
		final long nodePageKey = nodePageKey(nodeKey);
		final int nodePageOffset = nodePageOffset(nodeKey);

		// Fetch node page if it is not yet in the state cache.
		final NodePage page = dereferenceNodePage(dereferenceLeafOfTree(
				mRevisionRootPage.getIndirectPageReference(), nodePageKey),
				nodePageKey);

		IItem returnVal;

		// If nodePage is a weak one, the moveto is not cached
		returnVal = page.getNode(nodePageOffset);

		return returnVal;

	}

	/**
	 * {@inheritDoc}
	 */
	protected String getName(final int nameKey) {
		return mNamePage.getName(nameKey);

	}

	/**
	 * {@inheritDoc}
	 */
	protected final byte[] getRawName(final int nameKey) {
		return mNamePage.getRawName(nameKey);

	}

	/**
	 * {@inheritDoc}
	 */
	protected void close() {
		mPageReader.close();
		mCache.clear();

		// Immediately release all references.
		mSessionConfiguration = null;
		mPageReader = null;
		mUberPage = null;
		mRevisionRootPage = null;
		mNamePage = null;
		mItemList = null;
	}

	/**
	 * Get revision root page belonging to revision key.
	 * 
	 * @param revisionKey
	 *            Key of revision to find revision root page for.
	 * @return Revision root page of this revision key.
	 */
	protected final RevisionRootPage getRevisionRootPage(final long revisionKey) {
		final PageReference<?> ref = dereferenceLeafOfTree(mUberPage
				.getIndirectPageReference(), revisionKey);
		RevisionRootPage page = (RevisionRootPage) dereferencePage(ref);

		// If there is no page, get it from the storage and cache it.
		if (page == null) {
			page = (RevisionRootPage) mPageReader.read(ref);
		}

		// Get revision root page which is the leaf of the indirect tree.
		return page;
	}

	protected final NamePage getNamePage() {
		final PageReference<NamePage> namePageRef = mRevisionRootPage
				.getNamePageReference();

		NamePage page = dereferencePage(namePageRef);

		// If there is no page, get it from the storage and cache it.
		if (page == null) {
			page = (NamePage) mPageReader.read(namePageRef);
		}

		return page;
	}

	/**
	 * @return The session configuration.
	 */
	protected final SessionConfiguration getSessionConfiguration() {
		return mSessionConfiguration;
	}

	/**
	 * @return The uber page.
	 */
	protected final UberPage getUberPage() {
		return mUberPage;
	}

	/**
	 * @return The item list.
	 */
	public final IItemList getItemList() {
		if (mItemList != null) {
			return mItemList;
		} else {
			throw new IllegalStateException(
					"No ItemList for transaction found.");
		}

	}

	/**
	 * @param revisionRootPage
	 *            The revision root page to set.
	 */
	protected final void setRevisionRootPage(
			final RevisionRootPage revisionRootPage) {
		mRevisionRootPage = revisionRootPage;
	}

	/**
	 * Calculate node page key from a given node key.
	 * 
	 * @param nodeKey
	 *            Node key to find node page key for.
	 * @return Node page key.
	 */
	protected static final long nodePageKey(final long nodeKey) {
		final long nodePageKey = nodeKey >> IConstants.NDP_NODE_COUNT_EXPONENT;
		return nodePageKey;
	}

	/**
	 * Calculate node page offset for a given node key.
	 * 
	 * @param nodeKey
	 *            Node key to find offset for.
	 * @return Offset into node page.
	 */
	protected static final int nodePageOffset(final long nodeKey) {
		final long nodePageOffset = (nodeKey - ((nodeKey >> IConstants.NDP_NODE_COUNT_EXPONENT) << IConstants.NDP_NODE_COUNT_EXPONENT));
		return (int) nodePageOffset;
	}

	/**
	 * Dereference node page reference.
	 * 
	 * @param reference
	 *            Reference to dereference.
	 * @param nodePageKey
	 *            Key of node page.
	 * @return Dereferenced page.
	 */
	protected final NodePage dereferenceNodePage(
			final PageReference<NodePage> reference, final long nodePageKey) {

		NodePage page = dereferencePage(reference);

		// If there is no page, get it from the storage and cache it.
		if (page == null) {
			page = (NodePage) mPageReader.read(reference);
			mCache.put(reference.getKey().getIdentifier(), page);
		}

		return page;

	}

	/**
	 * Dereference indirect page reference.
	 * 
	 * @param reference
	 *            Reference to dereference.
	 * @return Dereferenced page.
	 */
	protected final IndirectPage dereferenceIndirectPage(
			final PageReference<IndirectPage> reference) {

		IndirectPage page = dereferencePage(reference);

		// If there is no page, get it from the storage and cache it.
		if (page == null) {
			page = (IndirectPage) mPageReader.read(reference);
			reference.setPage(page);
		}

		return page;
	}

	private final <N extends AbstractPage> N dereferencePage(
			final PageReference<N> reference) {
		// Get page that was dereferenced or prepared earlier.
		N page = reference.getPage();

		// If there is no page, get it from the cache.
		if (page == null) {
			page = (N) mCache.get(reference.getKey().getIdentifier());
		}
		return page;
	}

	/**
	 * Find reference pointing to leaf page of an indirect tree.
	 * 
	 * @param startReference
	 *            Start reference pointing to the indirect tree.
	 * @param key
	 *            Key to look up in the indirect tree.
	 * @return Reference denoted by key pointing to the leaf page.
	 */
	protected final PageReference dereferenceLeafOfTree(
			final PageReference<IndirectPage> startReference, final long key) {

		// Initial state pointing to the indirect page of level 0.
		PageReference reference = startReference;
		int offset = 0;
		long levelKey = key;

		// Iterate through all levels.
		for (int level = 0, height = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; level < height; level++) {
			offset = (int) (levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level]);
			levelKey -= offset << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level];
			final AbstractPage page = dereferenceIndirectPage(reference);
			reference = page.getReference(offset);
		}

		// Return reference to leaf of indirect tree.
		return reference;
	}

}

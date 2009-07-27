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

import java.util.Map;

import com.treetank.api.IItem;
import com.treetank.api.IItemList;
import com.treetank.page.AbstractPage;
import com.treetank.page.IndirectPage;
import com.treetank.page.NamePage;
import com.treetank.page.NodePage;
import com.treetank.page.PageReader;
import com.treetank.page.PageReference;
import com.treetank.page.RevisionRootPage;
import com.treetank.page.UberPage;
import com.treetank.page.WeakNodePage;
import com.treetank.utils.IByteBuffer;
import com.treetank.utils.IConstants;
import com.treetank.utils.NamePageHash;

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

	/** Shared page cache mapping start address of page to IPage. */
	private Map<Long, AbstractPage> mPageCache;

	/** Page reader exclusively assigned to this transaction. */
	private PageReader mPageReader;

	/** Uber page this transaction is bound to. */
	private UberPage mUberPage;

	/** Revision root page as root of this transaction. */
	private RevisionRootPage mRevisionRootPage;

	/** Cached least recently touched node page. */
	private NodePage mNodePage;

	/** Cached name page of this revision. */
	private IndirectPage mNamePage;

	/** Read-transaction-exclusive item list. */
	private IItemList mItemList;

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
	 */
	protected ReadTransactionState(
			final SessionConfiguration sessionConfiguration,
			final Map<Long, AbstractPage> pageCache, final UberPage uberPage,
			final long revisionKey, final IItemList itemList) {
		mSessionConfiguration = sessionConfiguration;
		mPageCache = pageCache;
		mPageReader = new PageReader(sessionConfiguration);
		mUberPage = uberPage;
		mRevisionRootPage = getRevisionRootPage(revisionKey);
		mNodePage = null;
		mNamePage = null;
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
	protected final IItem getNode(final long nodeKey) {

		// Immediately return node from item list if node key negative.
		if (nodeKey < 0) {
			mNodePage = null;
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
		if (mNodePage == null || mNodePage.getNodePageKey() != nodePageKey) {
			mNodePage = dereferenceNodePage(dereferenceLeafOfTree(
					mRevisionRootPage.getIndirectPageReference(), nodePageKey),
					nodePageKey);
		}

		IItem returnVal;

		// If nodePage is a weak one, the moveto is not cached
		if (mNodePage instanceof WeakNodePage) {
			returnVal = ((WeakNodePage) mNodePage).getWeakNode(nodePageOffset);
		} else {
			returnVal = mNodePage.getNode(nodePageOffset);
		}


		return returnVal;

	}

	/**
	 * {@inheritDoc}
	 */
	protected String getName(final int nameKey) {
		if (nameKey == -1) {
			return null;
		}
		if (mNamePage == null) {
			mNamePage = dereferenceIndirectPage(mRevisionRootPage
					.getNamePageReference());
		}

		final NamePage namePage = dereferenceNamePage(getNamePageReference(nameKey));
		return namePage.getName(nameKey);

	}

	/**
	 * {@inheritDoc}
	 */
	protected final byte[] getRawName(final int nameKey) {
		if (mNamePage == null) {
			mNamePage = dereferenceIndirectPage(mRevisionRootPage
					.getNamePageReference());
		}

		final NamePage namePage = dereferenceNamePage(getNamePageReference(nameKey));
		return namePage.getRawName(nameKey);

	}

	/**
	 * Method to get the direct {@link NamePage} linked to the key.
	 * 
	 * @param nameKey
	 *            key for the string to be found
	 * @return the Reference to the corresponding NamePage
	 */
	protected final PageReference<NamePage> getNamePageReference(
			final int nameKey) {

		final int[] offsetsForKey = NamePageHash.generateOffsets(nameKey);

		final int firstLevelOffset = offsetsForKey[0];
		final int secondLevelOffset = offsetsForKey[1];

		final IndirectPage firstLevel = dereferenceIndirectPage(mNamePage
				.getReference(firstLevelOffset));
		final PageReference<NamePage> secondLevelRef = firstLevel
				.getReference(secondLevelOffset);

		return secondLevelRef;
	}

	/**
	 * @return the mPageReader
	 */
	protected final PageReader getPageReader() {
		return mPageReader;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void close() {
		mPageReader.close();

		// Immediately release all references.
		mSessionConfiguration = null;
		mPageCache = null;
		mPageReader = null;
		mUberPage = null;
		mRevisionRootPage = null;
		mNodePage = null;
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

		// Get revision root page which is the leaf of the indirect tree.
		return dereferenceRevisionRootPage(dereferenceLeafOfTree(mUberPage
				.getIndirectPageReference(), revisionKey), revisionKey);

	}

	/**
	 * @return The session configuration.
	 */
	public final SessionConfiguration getSessionConfiguration() {
		return mSessionConfiguration;
	}

	/**
	 * @return The page cache.
	 */
	public final Map<Long, AbstractPage> getPageCache() {
		return mPageCache;
	}

	/**
	 * @param nodePage
	 *            The node page to set.
	 */
	protected final void setNodePage(final NodePage nodePage) {
		mNodePage = nodePage;
	}

	/**
	 * @param namePage
	 *            The name page to set.
	 */
	protected final void setNamePage(final IndirectPage namePage) {
		mNamePage = namePage;
	}

	/**
	 * @return The uber page.
	 */
	public final UberPage getUberPage() {
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
			final IByteBuffer in = mPageReader.read(reference);
			page = new NodePage(in, nodePageKey);
			mPageCache.put(reference.getStart(), page);
		}

		return page;

	}

	/**
	 * Dereference name page reference.
	 * 
	 * @param reference
	 *            Reference to dereference.
	 * @return Dereferenced page.
	 */
	protected final NamePage dereferenceNamePage(
			final PageReference<NamePage> reference) {

		NamePage page = dereferencePage(reference);

		// If there is no page, get it from the storage and cache it.
		if (page == null) {
			final IByteBuffer in = mPageReader.read(reference);
			page = new NamePage(in);
			mPageCache.put(reference.getStart(), page);
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
			final IByteBuffer in = mPageReader.read(reference);
			page = new IndirectPage(in);
			mPageCache.put(reference.getStart(), page);
		}

		return page;
	}

	/**
	 * Dereference revision root page reference.
	 * 
	 * @param reference
	 *            Reference to dereference.
	 * @param revisionKey
	 *            Key of revision.
	 * @return Dereferenced page.
	 */
	protected final RevisionRootPage dereferenceRevisionRootPage(
			final PageReference<RevisionRootPage> reference,
			final long revisionKey) {

		RevisionRootPage page = dereferencePage(reference);

		// If there is no page, get it from the storage and cache it.
		if (page == null) {
			final IByteBuffer in = mPageReader.read(reference);
			page = new RevisionRootPage(in, revisionKey);
			mPageCache.put(reference.getStart(), page);
		}

		return page;
	}

	private final <N extends AbstractPage> N dereferencePage(
			final PageReference<N> reference) {
		// Get page that was dereferenced or prepared earlier.
		N page = reference.getPage();

		// If there is no page, get it from the cache.
		if (page == null) {
			page = (N) mPageCache.get(reference.getStart());
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
			reference = dereferenceIndirectPage(reference).getReference(offset);
		}

		// Return reference to leaf of indirect tree.
		return reference;
	}

}

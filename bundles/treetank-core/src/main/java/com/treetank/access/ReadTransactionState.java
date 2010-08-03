/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.access;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.treetank.api.IItem;
import com.treetank.api.IItemList;
import com.treetank.cache.ICache;
import com.treetank.cache.NodePageContainer;
import com.treetank.cache.RAMCache;
import com.treetank.exception.TreetankIOException;
import com.treetank.io.IReader;
import com.treetank.node.DeletedNode;
import com.treetank.page.AbstractPage;
import com.treetank.page.IndirectPage;
import com.treetank.page.NamePage;
import com.treetank.page.NodePage;
import com.treetank.page.PageReference;
import com.treetank.page.RevisionRootPage;
import com.treetank.page.UberPage;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.settings.ERevisioning;
import com.treetank.utils.IConstants;

/**
 * <h1>ReadTransactionState</h1>
 * 
 * <p>
 * State of a reading transaction. The only thing shared amongst transactions is the page cache. Everything
 * else is exclusive to this transaction. It is required that only a single thread has access to this
 * transaction.
 * </p>
 * 
 * <p>
 * A path-like cache boosts sequential operations.
 * </p>
 */
public class ReadTransactionState {

    /** Database configuration. */
    private final DatabaseConfiguration mDatabaseConfiguration;

    /** Page reader exclusively assigned to this transaction. */
    private final IReader mPageReader;

    /** Uber page this transaction is bound to. */
    private final UberPage mUberPage;

    /** Cached name page of this revision. */
    private final RevisionRootPage mRootPage;

    /** Read-transaction-exclusive item list. */
    private final IItemList mItemList;

    /** Internal reference to cache. */
    private final ICache mCache;

    /**
     * Standard constructor.
     * 
     * @param databaseConfiguration
     *            Configuration of database.
     * @param uberPage
     *            Uber page to start reading with.
     * @param revision
     *            Key of revision to read from uber page.
     * @param itemList
     *            List of non-persistent items.
     * @param reader
     *            for this transaction
     * @throws TreetankIOException
     *             if the read of the persistent storage fails
     */
    protected ReadTransactionState(final DatabaseConfiguration databaseConfiguration,
        final UberPage uberPage, final long revision, final IItemList itemList, final IReader reader)
        throws TreetankIOException {
        mCache = new RAMCache();
        mDatabaseConfiguration = databaseConfiguration;
        mPageReader = reader;
        mUberPage = uberPage;
        mRootPage = loadRevRoot(revision);
        initializeNamePage();
        mItemList = itemList;

    }

    /**
     * Getting the node related to the given node key.
     * 
     * @param mNodeKey
     *            searched for
     * @return the related Node
     * @throws TreetankIOException
     *             if the read to the persistent storage fails
     */
    protected IItem getNode(final long mNodeKey) throws TreetankIOException {

        // Immediately return node from item list if node key negative.
        if (mNodeKey < 0) {
            return mItemList.getItem(mNodeKey);
        }

        // Calculate page and node part for given nodeKey.
        final long nodePageKey = nodePageKey(mNodeKey);
        final int nodePageOffset = nodePageOffset(mNodeKey);

        NodePageContainer cont = mCache.get(nodePageKey);

        if (cont == null) {
            final NodePage[] revs = getSnapshotPages(nodePageKey);

            final int mileStoneRevision =
                Integer.parseInt(mDatabaseConfiguration.getProps().getProperty(
                    EDatabaseSetting.REVISION_TO_RESTORE.name()));

            // Build up the complete page.
            final ERevisioning revision =
                ERevisioning.valueOf(mDatabaseConfiguration.getProps().getProperty(
                    EDatabaseSetting.REVISION_TYPE.name()));
            final NodePage completePage = revision.combinePages(revs, mileStoneRevision);
            cont = new NodePageContainer(completePage);
            mCache.put(nodePageKey, cont);
        }
        // If nodePage is a weak one, the moveto is not cached
        final IItem returnVal = cont.getComplete().getNode(nodePageOffset);
        return checkItemIfDeleted(returnVal);
    }

    /**
     * Method to check if an {@link IItem} is a deleted one.
     * 
     * @param mToCheck
     *            of the IItem
     * @return the item if it is valid, null otherwise
     */
    protected final IItem checkItemIfDeleted(final IItem mToCheck) {
        if (mToCheck instanceof DeletedNode) {
            return null;
        } else {
            return mToCheck;
        }
    }

    /**
     * Getting the name corresponding to the given key.
     * 
     * @param mNameKey
     *            for the term searched
     * @return the name
     */
    protected String getName(final int mNameKey) {

        return ((NamePage)mRootPage.getNamePageReference().getPage()).getName(mNameKey);

    }

    /**
     * Getting the raw name related to the name key.
     * 
     * @param mNameKey
     *            for the raw name searched
     * @return a byte array containing the raw name
     */
    protected final byte[] getRawName(final int mNameKey) {
        return ((NamePage)mRootPage.getNamePageReference().getPage()).getRawName(mNameKey);

    }

    /**
     * Closing this Readtransaction.
     * 
     * @throws TreetankIOException
     *             if the closing to the persistent storage fails.
     */
    protected void close() throws TreetankIOException {
        mPageReader.close();
        mCache.clear();

    }

    /**
     * Get revision root page belonging to revision key.
     * 
     * @param revisionKey
     *            Key of revision to find revision root page for.
     * @return Revision root page of this revision key.
     * 
     * @throws TreetankIOException
     *             if something odd happens within the creation process.
     */
    protected final RevisionRootPage loadRevRoot(final long revisionKey) throws TreetankIOException {

        final PageReference ref = dereferenceLeafOfTree(mUberPage.getIndirectPageReference(), revisionKey);
        if (ref.getPage() == null && ref.getKey() == null) {
            throw new TreetankIOException(
                "Revision will not be loaded since neither the key nor the page is referencable.");
        }
        RevisionRootPage page = (RevisionRootPage)ref.getPage();

        // If there is no page, get it from the storage and cache it.
        if (page == null) {
            page = (RevisionRootPage)mPageReader.read(ref);
        }

        // Get revision root page which is the leaf of the indirect tree.
        return page;
    }

    protected final void initializeNamePage() throws TreetankIOException {
        final PageReference ref = mRootPage.getNamePageReference();
        if (ref.getPage() == null) {
            ref.setPage((NamePage)mPageReader.read(ref));
        }
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
            throw new IllegalStateException("No ItemList for transaction found.");
        }

    }

    // /**

    /**
     * Dereference node page reference.
     * 
     * @param mNodePageKey
     *            Key of node page.
     * @return Dereferenced page.
     * 
     * @throws TreetankIOException
     *             if something odd happens within the creation process.
     */
    protected final NodePage[] getSnapshotPages(final long mNodePageKey) throws TreetankIOException {

        // ..and get all leaves of nodepages from the revision-trees.
        final List<PageReference> refs = new ArrayList<PageReference>();
        final Set<Long> keys = new HashSet<Long>();

        for (long i = mRootPage.getRevision(); i >= 0; i--) {
            final PageReference ref =
                dereferenceLeafOfTree(loadRevRoot(i).getIndirectPageReference(), mNodePageKey);
            if (ref != null && (ref.getPage() != null || ref.getKey() != null)) {
                if (ref.getKey() == null || (!keys.contains(ref.getKey().getIdentifier()))) {
                    refs.add(ref);
                    if (ref.getKey() != null) {
                        keys.add(ref.getKey().getIdentifier());
                    }
                }
                if (refs.size() == Integer.parseInt(mDatabaseConfiguration.getProps().getProperty(
                    EDatabaseSetting.REVISION_TO_RESTORE.name()))) {
                    break;
                }

            } else {
                break;
            }
        }

        // Afterwards read the nodepages if they are not dereferences...
        final NodePage[] pages = new NodePage[refs.size()];
        for (int i = 0; i < pages.length; i++) {
            final PageReference rev = refs.get(i);
            pages[i] = (NodePage)rev.getPage();
            if (pages[i] == null) {
                pages[i] = (NodePage)mPageReader.read(rev);
            }
        }
        return pages;

    }

    /**
     * Dereference indirect page reference.
     * 
     * @param reference
     *            Reference to dereference.
     * @return Dereferenced page.
     * 
     * @throws TreetankIOException
     *             if something odd happens within the creation process.
     */
    protected final IndirectPage dereferenceIndirectPage(final PageReference reference)
        throws TreetankIOException {

        IndirectPage page = (IndirectPage)reference.getPage();

        // If there is no page, get it from the storage and cache it.
        if (page == null) {
            page = (IndirectPage)mPageReader.read(reference);
            reference.setPage(page);
        }

        return page;
    }

    /**
     * Find reference pointing to leaf page of an indirect tree.
     * 
     * @param mStartReference
     *            Start reference pointing to the indirect tree.
     * @param mKey
     *            Key to look up in the indirect tree.
     * @return Reference denoted by key pointing to the leaf page.
     * 
     * @throws TreetankIOException
     *             if something odd happens within the creation process.
     */
    protected final PageReference dereferenceLeafOfTree(final PageReference mStartReference, final long mKey)
        throws TreetankIOException {

        // Initial state pointing to the indirect page of level 0.
        PageReference reference = mStartReference;
        int offset = 0;
        long levelKey = mKey;

        // Iterate through all levels.
        for (int level = 0, height = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; level < height; level++) {
            offset = (int)(levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level]);
            levelKey -= offset << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level];
            final AbstractPage page = dereferenceIndirectPage(reference);
            if (page == null) {
                reference = null;
                break;
            } else {
                reference = page.getReference(offset);
            }
        }

        // Return reference to leaf of indirect tree.
        return reference;
    }

    /**
     * Calculate node page key from a given node key.
     * 
     * @param mNodeKey
     *            Node key to find node page key for.
     * @return Node page key.
     */
    protected static final long nodePageKey(final long mNodeKey) {
        final long nodePageKey = mNodeKey >> IConstants.NDP_NODE_COUNT_EXPONENT;
        return nodePageKey;
    }

    /**
     * Current reference to actual rev-root page.
     * 
     * @return the current revision root page
     * 
     * @throws TreetankIOException
     *             if something odd happens within the creation process.
     */
    protected RevisionRootPage getActualRevisionRootPage() throws TreetankIOException {
        return mRootPage;
    }

    /**
     * Getting the {@link DatabaseConfiguration} addicted to this state.
     * 
     * @return the {@link DatabaseConfiguration} bound to this state
     */
    protected DatabaseConfiguration getDatabaseConfiguration() {
        return mDatabaseConfiguration;
    }

    /**
     * Calculate node page offset for a given node key.
     * 
     * @param mNodeKey
     *            Node key to find offset for.
     * @return Offset into node page.
     */
    protected static final int nodePageOffset(final long mNodeKey) {
        final long nodePageOffset =
            (mNodeKey - ((mNodeKey >> IConstants.NDP_NODE_COUNT_EXPONENT) << IConstants.NDP_NODE_COUNT_EXPONENT));
        return (int)nodePageOffset;
    }

}

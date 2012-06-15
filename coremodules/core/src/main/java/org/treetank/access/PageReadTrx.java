/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.access;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.treetank.api.INode;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.ISession;
import org.treetank.cache.NodePageContainer;
import org.treetank.exception.TTIOException;
import org.treetank.io.IReader;
import org.treetank.node.DeletedNode;
import org.treetank.page.IConstants;
import org.treetank.page.IPage;
import org.treetank.page.IndirectPage;
import org.treetank.page.NamePage;
import org.treetank.page.NodePage;
import org.treetank.page.PageReference;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;
import org.treetank.settings.ERevisioning;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * <h1>PageReadTrx</h1>
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
public class PageReadTrx implements IPageReadTrx {

    /** Page reader exclusively assigned to this transaction. */
    private final IReader mPageReader;

    /** Uber page this transaction is bound to. */
    private final UberPage mUberPage;

    /** Cached name page of this revision. */
    private final RevisionRootPage mRootPage;

    /** Internal reference to cache. */
    private final Cache<Long, NodePageContainer> mCache;

    /** Configuration of the session */
    protected final ISession mSession;

    /** Boolean for determinc close. */
    private boolean mClose;

    /**
     * Standard constructor.
     * 
     * @param pSession
     *            State of state.
     * @param pUberpage
     *            Uber page to start reading with.
     * @param pRevision
     *            Key of revision to read from uber page.
     * @param paramItemList
     *            List of non-persistent items.
     * @param pReader
     *            for this transaction
     * @throws TTIOException
     *             if the read of the persistent storage fails
     */
    protected PageReadTrx(final ISession pSession, final UberPage pUberpage, final long pRevision,
        final IReader pReader) throws TTIOException {
        mCache = CacheBuilder.newBuilder().maximumSize(10000).build();
        mSession = pSession;
        mPageReader = pReader;
        mUberPage = pUberpage;
        mRootPage = loadRevRoot(pRevision);
        initializeNamePage();
        mClose = false;
    }

    /**
     * Getting the node related to the given node key.
     * 
     * @param paramNodeKey
     *            searched for
     * @return the related Node
     * @throws TTIOException
     *             if the read to the persistent storage fails
     */
    public INode getNode(final long paramNodeKey) throws TTIOException {

        // Immediately return node from item list if node key negative.
        if (paramNodeKey < 0) {
            throw new IllegalArgumentException();
        }

        // Calculate page and node part for given nodeKey.
        final long nodePageKey = nodePageKey(paramNodeKey);
        final int nodePageOffset = nodePageOffset(paramNodeKey);

        NodePageContainer cont = mCache.getIfPresent(nodePageKey);

        if (cont == null) {
            final NodePage[] revs = getSnapshotPages(nodePageKey);
            if (revs.length == 0) {
                return null;
            }
            final int mileStoneRevision = mSession.getConfig().mRevisionsToRestore;

            // Build up the complete page.
            final ERevisioning revision = mSession.getConfig().mRevision;
            final NodePage completePage = revision.combinePages(revs, mileStoneRevision);
            cont = new NodePageContainer(completePage);
            mCache.put(nodePageKey, cont);
        }
        // If nodePage is a weak one, the moveto is not cached
        final INode returnVal = cont.getComplete().getNode(nodePageOffset);
        return checkItemIfDeleted(returnVal);
    }

    /**
     * Getting the name corresponding to the given key.
     * 
     * @param mNameKey
     *            for the term searched
     * @return the name
     */
    public String getName(final int mNameKey) {
        return ((NamePage)mRootPage.getNamePageReference().getPage()).getName(mNameKey);

    }

    /**
     * Getting the raw name related to the name key.
     * 
     * @param mNameKey
     *            for the raw name searched
     * @return a byte array containing the raw name
     */
    public final byte[] getRawName(final int mNameKey) {
        return ((NamePage)mRootPage.getNamePageReference().getPage()).getRawName(mNameKey);

    }

    /**
     * Closing this Readtransaction.
     * 
     * @throws TTIOException
     *             if the closing to the persistent storage fails.
     */
    public void close() throws TTIOException {
        mSession.deregisterPageTrx(this);
        mPageReader.close();
        mCache.invalidateAll();
        mClose = true;
    }

    /**
     * Current reference to actual rev-root page.
     * 
     * @return the current revision root page
     * 
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    public RevisionRootPage getActualRevisionRootPage() throws TTIOException {
        return mRootPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder("PageReader: ").append(mPageReader).append("\nUberPage: ").append(mUberPage)
            .append("\nRevRootPage: ").append(mRootPage).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return mClose;
    }

    /**
     * Method to check if an {@link INode} is a deleted one.
     * 
     * @param mToCheck
     *            of the IItem
     * @return the item if it is valid, null otherwise
     */
    protected final INode checkItemIfDeleted(final INode mToCheck) {
        if (mToCheck instanceof DeletedNode) {
            return null;
        } else {
            return mToCheck;
        }
    }

    /**
     * Get revision root page belonging to revision key.
     * 
     * @param revisionKey
     *            Key of revision to find revision root page for.
     * @return Revision root page of this revision key.
     * 
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    protected final RevisionRootPage loadRevRoot(final long revisionKey) throws TTIOException {

        final PageReference ref = dereferenceLeafOfTree(mUberPage.getIndirectPageReference(), revisionKey);
        RevisionRootPage page = (RevisionRootPage)ref.getPage();

        // If there is no page, get it from the storage and cache it.
        if (page == null) {
            page = (RevisionRootPage)mPageReader.read(ref.getKey());
        }

        // Get revision root page which is the leaf of the indirect tree.
        return page;
    }

    /**
     * Initialize NamePage.
     * 
     * @throws TTIOException
     *             if something odd happens during initialization
     */
    protected final void initializeNamePage() throws TTIOException {
        final PageReference ref = mRootPage.getNamePageReference();
        if (ref.getPage() == null) {
            ref.setPage((NamePage)mPageReader.read(ref.getKey()));
        }
    }

    /**
     * Get UberPage.
     * 
     * @return The uber page.
     */
    protected final UberPage getUberPage() {
        return mUberPage;
    }

    /**
     * Dereference node page reference.
     * 
     * @param mNodePageKey
     *            Key of node page.
     * @return Dereferenced page.
     * 
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    protected final NodePage[] getSnapshotPages(final long mNodePageKey) throws TTIOException {

        // ..and get all leaves of nodepages from the revision-trees.
        final List<PageReference> refs = new ArrayList<PageReference>();
        final Set<Long> keys = new HashSet<Long>();

        for (long i = mRootPage.getRevision(); i >= 0; i--) {
            final PageReference ref =
                dereferenceLeafOfTree(loadRevRoot(i).getIndirectPageReference(), mNodePageKey);
            if (ref != null && (ref.getPage() != null || ref.getKey() != IConstants.NULL_ID)) {
                if (ref.getKey() == IConstants.NULL_ID || (!keys.contains(ref.getKey()))) {
                    refs.add(ref);
                    if (ref.getKey() != IConstants.NULL_ID) {
                        keys.add(ref.getKey());
                    }
                }
                if (refs.size() == mSession.getConfig().mRevisionsToRestore) {
                    break;
                }

            } else {
                break;
            }
        }

        // Afterwards read the nodepages if they are not dereferences...
        final NodePage[] pages = new NodePage[refs.size()];
        for (int i = 0; i < pages.length; i++) {
            final PageReference ref = refs.get(i);
            pages[i] = (NodePage)ref.getPage();
            if (pages[i] == null) {
                pages[i] = (NodePage)mPageReader.read(ref.getKey());
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
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    protected final IndirectPage dereferenceIndirectPage(final PageReference ref) throws TTIOException {

        IndirectPage page = (IndirectPage)ref.getPage();

        // If there is no page, get it from the storage and cache it.
        if (page == null) {
            if (ref.getPage() == null && ref.getKey() == IConstants.NULL_ID) {
                return null;
            }
            page = (IndirectPage)mPageReader.read(ref.getKey());
            ref.setPage(page);
        }

        return page;
    }

    /**
     * Find reference pointing to leaf page of an indirect tree.
     * 
     * @param paramStartReference
     *            Start reference pointing to the indirect tree.
     * @param paramKey
     *            Key to look up in the indirect tree.
     * @return Reference denoted by key pointing to the leaf page.
     * 
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    protected final PageReference dereferenceLeafOfTree(final PageReference paramStartReference,
        final long paramKey) throws TTIOException {

        // Initial state pointing to the indirect page of level 0.
        PageReference reference = paramStartReference;
        int offset = 0;
        long levelKey = paramKey;

        // Iterate through all levels.
        for (int level = 0, height = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; level < height; level++) {
            offset = (int)(levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level]);
            levelKey -= offset << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level];
            final IPage page = dereferenceIndirectPage(reference);
            if (page == null) {
                reference = null;
                break;
            } else {
                reference = page.getReferences()[offset];
            }
        }

        // Return reference to leaf of indirect tree.
        return reference;
    }

    /**
     * Calculate node page key from a given node key.
     * 
     * @param pNodeKey
     *            Node key to find node page key for.
     * @return Node page key.
     */
    protected static final long nodePageKey(final long pNodeKey) {
        final long nodePageKey = pNodeKey >> IConstants.NDP_NODE_COUNT_EXPONENT;
        return nodePageKey;
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

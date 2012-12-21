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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.treetank.api.INode;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendReader;
import org.treetank.page.IConstants;
import org.treetank.page.IndirectPage;
import org.treetank.page.NamePage;
import org.treetank.page.NodePage;
import org.treetank.page.NodePage.DeletedNode;
import org.treetank.page.interfaces.IReferencePage;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;
import org.treetank.revisioning.IRevisioning;

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
    private final IBackendReader mPageReader;

    /** Uber page this transaction is bound to. */
    private final UberPage mUberPage;

    /** Cached root page of this revision. */
    private final RevisionRootPage mRootPage;

    /** Cached name page of this revision. */
    private final NamePage mNamePage;

    /**
     * Internal reference to cache. This cache takes the sequential numbering of the pages instead of the
     * absolute, storage-relevant numbering.
     */
    protected final Cache<Long, NodePage> mNodePageCache;

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
     * @param pRevKey
     *            Key of revision to read from uber page.
     * @param pReader
     *            for this transaction
     * @throws TTIOException
     *             if the read of the persistent storage fails
     */
    protected PageReadTrx(final ISession pSession, final UberPage pUberpage, final long pRevKey,
        final IBackendReader pReader) throws TTException {
        mNodePageCache = CacheBuilder.newBuilder().maximumSize(10000).build();
        mSession = pSession;
        mPageReader = pReader;
        mUberPage = pUberpage;
        mRootPage =
            (RevisionRootPage)mPageReader.read(dereferenceLeafOfTree(
                mUberPage.getReferenceKeys()[IReferencePage.GUARANTEED_INDIRECT_OFFSET], pRevKey));
        mNamePage =
            (NamePage)mPageReader.read(mRootPage.getReferenceKeys()[RevisionRootPage.NAME_REFERENCE_OFFSET]);
        mClose = false;
    }

    /**
     * Getting the node related to the given node key.
     * 
     * @param pNodeKey
     *            searched for
     * @return the related Node
     * @throws TTIOException
     *             if the read to the persistent storage fails
     */
    public INode getNode(final long pNodeKey) throws TTException {

        // Immediately return node from item list if node key negative.
        if (pNodeKey < 0) {
            throw new IllegalArgumentException();
        }

        // Calculate page and node part for given nodeKey.
        final long seqNodePageKey = nodePageKey(pNodeKey);
        final int nodePageOffset = nodePageOffset(pNodeKey);
        NodePage page = (NodePage)mNodePageCache.getIfPresent(seqNodePageKey);

        if (page == null) {
            final NodePage[] revs = getSnapshotPages(seqNodePageKey);
            if (revs.length == 0) {
                return null;
            }

            // Build up the complete page.
            final IRevisioning revision = mSession.getConfig().mRevision;
            page = revision.combinePages(revs);
            mNodePageCache.put(seqNodePageKey, page);
        }
        final INode returnVal = page.getNode(nodePageOffset);
        return checkItemIfDeleted(returnVal);
    }

    /**
     * Getting the name corresponding to the given key.
     * 
     * @param pNameKey
     *            for the term searched
     * @return the name
     */
    public String getName(final int pNameKey) {
        return mNamePage.getName(pNameKey);

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
        mNodePageCache.invalidateAll();
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
        StringBuilder builder = new StringBuilder();
        builder.append("PageReadTrx [mPageReader=");
        builder.append(mPageReader);
        builder.append(", mUberPage=");
        builder.append(mUberPage);
        builder.append(", mRootPage=");
        builder.append(mRootPage);
        builder.append(", mCache=");
        builder.append(mNodePageCache);
        builder.append(", mClose=");
        builder.append(mClose);
        builder.append("]");
        return builder.toString();
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
     * Dereference node page reference.
     * 
     * @param pSeqNodePageKey
     *            Key of node page.
     * @return Dereferenced page.
     * 
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    protected final NodePage[] getSnapshotPages(final long pSeqNodePageKey) throws TTException {

        // Return Value, since the revision iterates a flexible number of version, this has to be a list
        // first.
        final List<NodePage> nodePages = new ArrayList<NodePage>();

        // Since same versions might be referenced starting different versions (if not touched), we have to
        // check against the keys as well.
        final Set<Long> nodePageKeys = new TreeSet<Long>();

        // Iterate through all versions starting with the most recent one.
        for (long i = mRootPage.getRevision(); i >= 0; i--) {
            RevisionRootPage rootPage =
                (RevisionRootPage)mPageReader.read(dereferenceLeafOfTree(
                    mUberPage.getReferenceKeys()[IReferencePage.GUARANTEED_INDIRECT_OFFSET], i));
            // Searching for the related NodePage within all referenced pages.
            final long nodePageKey =
                dereferenceLeafOfTree(rootPage.getReferenceKeys()[IReferencePage.GUARANTEED_INDIRECT_OFFSET],
                    pSeqNodePageKey);
            if (nodePageKey > 0 && !nodePageKeys.contains(nodePageKey)) {
                NodePage page = (NodePage)mNodePageCache.getIfPresent(nodePageKey);
                if (page == null) {
                    page = (NodePage)mPageReader.read(nodePageKey);
                    nodePages.add(page);
                }
                nodePageKeys.add(nodePageKey);
                if (nodePages.size() == mSession.getConfig().mRevision.getRevisionsToRestore()) {
                    break;
                }
            } else {
                break;
            }
        }

        return nodePages.toArray(new NodePage[nodePages.size()]);

    }

    /**
     * Find reference pointing to leaf page of an indirect tree.
     * 
     * @param pStartKey
     *            Start reference pointing to the indirect tree.
     * @param pSeqPageKey
     *            Key to look up in the indirect tree.
     * @return Reference denoted by key pointing to the leaf page.
     * 
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    protected final long dereferenceLeafOfTree(final long pStartKey, final long pSeqPageKey)
        throws TTException {

        // Initial state pointing to the indirect page of level 0.
        int offset = 0;
        long levelKey = pSeqPageKey;
        long pageKey = pStartKey;

        // Iterate through all levels.
        for (int level = 0; level < IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; level++) {
            offset = (int)(levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level]);
            levelKey -= offset << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level];
            pageKey = ((IndirectPage)mPageReader.read(pageKey)).getReferenceKeys()[offset];
        }

        // Return reference to leaf of indirect tree.
        return pageKey;
    }

    /**
     * Calculate node page key from a given node key.
     * 
     * @param pNodeKey
     *            Node key to find node page key for.
     * @return Node page key.
     */
    protected static final long nodePageKey(final long pNodeKey) {
        final long nodePageKey = pNodeKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3];
        return nodePageKey;
    }

    /**
     * Calculate node page offset for a given node key.
     * 
     * @param pNodeKey
     *            Node key to find offset for.
     * @return Offset into node page.
     */
    protected static final int nodePageOffset(final long pNodeKey) {
        final long nodePageOffset =
            (pNodeKey - ((pNodeKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3]) << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3]));
        return (int)nodePageOffset;
    }

}

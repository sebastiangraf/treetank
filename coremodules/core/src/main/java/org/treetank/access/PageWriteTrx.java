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

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.treetank.access.PageReadTrx.nodePageOffset;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.treetank.access.conf.ContructorProps;
import org.treetank.api.IMetaEntry;
import org.treetank.api.INode;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
import org.treetank.log.LRULog;
import org.treetank.log.LogKey;
import org.treetank.log.LogValue;
import org.treetank.page.IConstants;
import org.treetank.page.IndirectPage;
import org.treetank.page.MetaPage;
import org.treetank.page.NodePage;
import org.treetank.page.NodePage.DeletedNode;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IReferencePage;

/**
 * <h1>PageWriteTrx</h1>
 * 
 * <p>
 * See {@link PageReadTrx}.
 * </p>
 */
public final class PageWriteTrx implements IPageWriteTrx {

    /** Page writer to serialize. */
    private final IBackendWriter mPageWriter;

    /** Cache to store the changes in this writetransaction. */
    private LRULog mLog;

    /** Reference to the actual uberPage. */
    private UberPage mNewUber;

    /** Reference to the actual revRoot. */
    private RevisionRootPage mNewRoot;

    /** Last reference to the actual namePage. */
    private MetaPage mNewMeta;

    /** Delegate for read access. */
    private PageReadTrx mDelegate;

    /**
     * Standard constructor.
     * 
     * 
     * @param pSession
     *            {@link ISession} reference
     * @param pUberPage
     *            root of resource
     * @param pWriter
     *            writer where this transaction should write to
     * @param pRepresentRev
     *            revision represent
     * @param pStoreRev
     *            revision store
     * @throws TTIOException
     *             if IO Error
     */
    protected PageWriteTrx(final ISession pSession, final UberPage pUberPage, final IBackendWriter pWriter,
        final long pRepresentRev) throws TTException {

        mPageWriter = pWriter;
        setUpTransaction(pUberPage, pSession, pRepresentRev, pWriter);
    }

    /**
     * Prepare a node for modification. This is getting the node from the
     * (persistence) layer, storing the page in the cache and setting up the
     * node for upcoming modification. Note that this only occurs for {@link INode}s.
     * 
     * @param pNodeKey
     *            key of the node to be modified
     * @return an {@link INode} instance
     * @throws TTIOException
     *             if IO Error
     */
    public INode prepareNodeForModification(final long pNodeKey) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        checkArgument(pNodeKey >= 0);
        final int nodePageOffset = nodePageOffset(pNodeKey);
        LogValue container = prepareNodePage(pNodeKey);

        INode node = ((NodePage)container.getModified()).getNode(nodePageOffset);
        if (node == null) {
            final INode oldNode = ((NodePage)container.getComplete()).getNode(nodePageOffset);
            checkNotNull(oldNode);
            node = oldNode;
            ((NodePage)container.getModified()).setNode(nodePageOffset, node);
        }
        return node;
    }

    /**
     * Finishing the node modification. That is storing the node including the
     * page in the cache.
     * 
     * @param pNode
     *            the node to be modified
     * @throws TTIOException
     */
    public void finishNodeModification(final INode pNode) throws TTIOException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        final long seqNodePageKey = pNode.getNodeKey() >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3];
        final int nodePageOffset = nodePageOffset(pNode.getNodeKey());
        LogKey key = new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, seqNodePageKey);
        LogValue container = mLog.get(key);
        NodePage page = (NodePage)container.getModified();
        page.setNode(nodePageOffset, pNode);
        mLog.put(key, container);
    }

    /**
     * {@inheritDoc}
     */
    public long setNode(final INode pNode) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        // Allocate node key and increment node count.
        final long nodeKey = pNode.getNodeKey();
        final long seqPageKey = nodeKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3];
        final int nodePageOffset = nodePageOffset(nodeKey);
        LogValue container = prepareNodePage(nodeKey);
        final NodePage page = ((NodePage)container.getModified());
        page.setNode(nodePageOffset, pNode);
        mLog.put(new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, seqPageKey), container);
        return nodeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNode(final INode pNode) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        checkNotNull(pNode);
        final long nodePageKey = pNode.getNodeKey() >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3];
        LogValue container = prepareNodePage(pNode.getNodeKey());
        final INode delNode = new DeletedNode(pNode.getNodeKey());
        ((NodePage)container.getComplete()).setNode(nodePageOffset(pNode.getNodeKey()), delNode);
        ((NodePage)container.getModified()).setNode(nodePageOffset(pNode.getNodeKey()), delNode);

        mLog.put(new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, nodePageKey), container);
    }

    /**
     * {@inheritDoc}
     */
    public INode getNode(final long pNodeKey) throws TTIOException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        // Calculate page and node part for given nodeKey.
        final long nodePageKey = pNodeKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3];
        final int nodePageOffset = nodePageOffset(pNodeKey);

        final LogValue container =
            mLog.get(new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, nodePageKey));
        // Page was not modified yet, delegate to read or..
        if (container == null) {
            return mDelegate.getNode(pNodeKey);
        }// ...page was modified, but not this node, take the complete part, or...
        else if (((NodePage)container.getModified()).getNode(nodePageOffset) == null) {
            final INode item = ((NodePage)container.getComplete()).getNode(nodePageOffset);
            return mDelegate.checkItemIfDeleted(item);

        }// ...page was modified and the modification touched this node.
        else {
            final INode item = ((NodePage)container.getModified()).getNode(nodePageOffset);
            return mDelegate.checkItemIfDeleted(item);
        }

    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void commit() throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        Iterator<LogValue> entries = mLog.getIterator();
        while (entries.hasNext()) {
            LogValue next = entries.next();
            mPageWriter.write(next.getModified());
        }
        mPageWriter.write(mNewMeta);
        mPageWriter.write(mNewRoot);
        mPageWriter.writeUberPage(mNewUber);

        mLog.close();

        ((Session)mDelegate.mSession).setLastCommittedUberPage(mNewUber);
        setUpTransaction(mNewUber, mDelegate.mSession, mNewUber.getRevisionNumber(), mPageWriter);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean close() throws TTIOException {
        if (!mDelegate.isClosed()) {
            mDelegate.close();
            mLog.close();
            mDelegate.mSession.deregisterPageTrx(this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long incrementNodeKey() {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        return mNewRoot.incrementMaxNodeKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getRevision() throws TTIOException {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        return mNewRoot.getRevision();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return mDelegate.isClosed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaPage getMetaPage() {
        checkState(!mDelegate.isClosed(), "Transaction already closed");
        return mNewMeta;
    }

    private LogValue prepareNodePage(final long pNodeKey) throws TTException {

        final long seqNodePageKey = pNodeKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3];

        LogKey key = new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, seqNodePageKey);
        // See if on nodePageLevel, there are any pages...
        LogValue container = mLog.get(key);
        // ... and start dereferencing of not.
        if (container == null) {
            LogKey indirectKey = preparePathToLeaf(false, mNewRoot, pNodeKey);

            LogValue indirectContainer = mLog.get(indirectKey);
            int nodeOffset = nodePageOffset(seqNodePageKey);
            long pageKey = ((IndirectPage)indirectContainer.getModified()).getReferenceKeys()[nodeOffset];

            long newPageKey = mNewUber.incrementPageCounter();
            if (pageKey != 0) {
                NodePage[] pages = mDelegate.getSnapshotPages(seqNodePageKey);
                checkState(pages.length > 0);
                if (mNewRoot.getRevision()
                    % Integer.parseInt(mDelegate.mSession.getConfig().mProperties
                        .getProperty(ContructorProps.NUMBERTORESTORE)) == 0) {
                    container =
                        mDelegate.mSession.getConfig().mRevision.combinePagesForModification(newPageKey,
                            pages, true);
                } else {
                    container =
                        mDelegate.mSession.getConfig().mRevision.combinePagesForModification(newPageKey,
                            pages, false);
                }
            } else {
                NodePage newPage = new NodePage(newPageKey);
                container = new LogValue(newPage, newPage);
            }
            ((IndirectPage)indirectContainer.getModified()).setReferenceKey(nodeOffset, newPageKey);
            mLog.put(indirectKey, indirectContainer);
            mLog.put(key, container);
        }
        return container;
    }

    /**
     * Getting a NodePageContainer containing the last IndirectPage with the reference to any new/modified
     * page.
     * 
     * @param pIsRootLevel
     *            is this dereferencing walk based on the the search after a RevRoot or a NodePage. Needed
     *            because of the same keys in both subtrees.
     * @param pPage
     *            where to start the tree-walk: either from an UberPage (related to new
     *            RevisionRootPages) or from a RevisionRootPage (related to new NodePages).
     * @param pSeqPageKey
     *            the key of the page mapped to the layer
     * @return the key the container representing the last level
     * @throws TTException
     */
    private LogKey preparePathToLeaf(final boolean pIsRootLevel, final IReferencePage pPage,
        final long pElementKey) throws TTException {

        // computing the ordernumbers within all level. The ordernumbers are the position in the sequence of
        // all pages within the same level.
        // ranges are for level 0: 0-127; level 1: 0-16383; level 2: 0-2097151; level 3: 0-268435455; ;level
        // 4: 0-34359738367
        long seqPageKey = -1;
        // since the revision points to a page, the sequence-key bases on the last indirect-layer directly
        // within the search after a revision,...
        if (pIsRootLevel) {
            seqPageKey = pElementKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3];
        } // ...whereas one layer above is used for the nodes based on the offsets pointing to nodes
          // instead of pages.
        else {
            seqPageKey = pElementKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[2];
        }

        long[] orderNumber = new long[IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length];
        for (int level = 0; level < orderNumber.length; level++) {
            orderNumber[level] = seqPageKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level];
        }

        IReferencePage page = null;
        IReferencePage parentPage = pPage;
        LogKey key = null;
        LogKey parentKey = new LogKey(pIsRootLevel, -1, 0);

        // Iterate through all levels...
        for (int level = 0; level < orderNumber.length; level++) {
            // ...see if the actual page requested is already in the log
            key = new LogKey(pIsRootLevel, level, orderNumber[level]);
            LogValue container = mLog.get(key);
            // if the page is not existing,..
            if (container == null) {
                // ..create a new page
                final long newKey = mNewUber.incrementPageCounter();
                page = new IndirectPage(newKey);

                // compute the offset of the newpage
                int offset = nodePageOffset(orderNumber[level]);

                // if there existed the same page in former versions (referencable over the offset within
                // the parent)...
                if (parentPage.getReferenceKeys()[offset] != 0) {
                    IReferencePage oldPage =
                        (IReferencePage)mPageWriter.read(parentPage.getReferenceKeys()[offset]);
                    for (int i = 0; i < oldPage.getReferenceKeys().length; i++) {
                        page.setReferenceKey(i, oldPage.getReferenceKeys()[i]);
                    }
                }
                // Set the newKey on the computed offset
                parentPage.setReferenceKey(offset, newKey);
                // .. and put the parent-reference to the log as well as the reference of the..
                container = new LogValue(parentPage, parentPage);
                mLog.put(parentKey, container);
                // ...current page.
                container = new LogValue(page, page);
                mLog.put(key, container);

            } // if the page is already in the log, get it simply from the log.
            else {
                page = (IReferencePage)container.getModified();
            }
            // finally, set the new pagekey for the next level
            parentKey = key;
            parentPage = page;
        }

        // Return reference to leaf of indirect tree.
        return key;
    }

    private void setUpTransaction(final UberPage pUberPage, final ISession pSession,
        final long pRepresentRev, final IBackendWriter pWriter) throws TTException {

        mLog =
            new LRULog(new File(pSession.getConfig().mProperties
                .getProperty(org.treetank.access.conf.ContructorProps.STORAGEPATH)),
                pSession.getConfig().mNodeFac, pSession.getConfig().mMetaFac);

        mNewUber =
            new UberPage(pUberPage.incrementPageCounter(), pUberPage.getRevisionNumber() + 1, pUberPage
                .getPageCounter());
        mNewUber.setReferenceKey(IReferencePage.GUARANTEED_INDIRECT_OFFSET,
            pUberPage.getReferenceKeys()[IReferencePage.GUARANTEED_INDIRECT_OFFSET]);

        mDelegate = new PageReadTrx(pSession, pUberPage, pRepresentRev, pWriter);

        // Get previous revision root page..
        final RevisionRootPage previousRevRoot = mDelegate.mRootPage;
        // ...and using this data to initialize a fresh revision root including the pointers.
        mNewRoot =
            new RevisionRootPage(mNewUber.incrementPageCounter(), pRepresentRev + 1, previousRevRoot
                .getMaxNodeKey());
        mNewRoot.setReferenceKey(IReferencePage.GUARANTEED_INDIRECT_OFFSET, previousRevRoot
            .getReferenceKeys()[IReferencePage.GUARANTEED_INDIRECT_OFFSET]);

        // Prepare indirect tree to hold reference to prepared revision root
        // nodePageReference.
        LogKey indirectKey = preparePathToLeaf(true, mNewUber, mNewUber.getRevisionNumber());
        LogValue indirectContainer = mLog.get(indirectKey);
        int offset = nodePageOffset(mNewUber.getRevisionNumber());
        ((IndirectPage)indirectContainer.getModified()).setReferenceKey(offset, mNewRoot.getPageKey());
        mLog.put(indirectKey, indirectContainer);

        // Setting up a new namepage
        Map<IMetaEntry, IMetaEntry> oldMap = mDelegate.mMetaPage.getMetaMap();
        mNewMeta = new MetaPage(mNewUber.incrementPageCounter());

        for (IMetaEntry key : oldMap.keySet()) {
            mNewMeta.setEntry(key, oldMap.get(key));
        }

        mNewRoot.setReferenceKey(RevisionRootPage.NAME_REFERENCE_OFFSET, mNewMeta.getPageKey());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mPageWriter", mPageWriter).add("mLog", mLog).add("mRootPage",
            mNewRoot).add("mDelegate", mDelegate).toString();
    }

}

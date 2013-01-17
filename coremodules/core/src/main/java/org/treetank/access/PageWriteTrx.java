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
import static org.treetank.access.PageReadTrx.nodePageKey;
import static org.treetank.access.PageReadTrx.nodePageOffset;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.treetank.access.conf.ContructorProps;
import org.treetank.api.IMetaEntry;
import org.treetank.api.INode;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.cache.BerkeleyPersistenceLog;
import org.treetank.cache.LRUCache;
import org.treetank.cache.LogKey;
import org.treetank.cache.NodePageContainer;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
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
    private final LRUCache mLog;

    /** Reference to the actual uberPage. */
    private UberPage mNewUber;

    /** Reference to the actual revRoot. */
    private RevisionRootPage mNewRoot;

    /** Last reference to the actual namePage. */
    private MetaPage mNewName;

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
        mLog =
            new LRUCache(new BerkeleyPersistenceLog(new File(pSession.getConfig().mProperties
                .getProperty(org.treetank.access.conf.ContructorProps.STORAGEPATH)),
                pSession.getConfig().mNodeFac, pSession.getConfig().mMetaFac));
        setUpTransaction(pUberPage, pSession, pRepresentRev, pWriter);
    }

    private void setUpTransaction(final UberPage pUberPage, final ISession pSession,
        final long pRepresentRev, final IBackendWriter pWriter) throws TTException {
        mNewUber =
            new UberPage(pUberPage.incrementPageCounter(), pUberPage.getRevisionNumber() + 1, pUberPage
                .getPageCounter());
        mNewUber.setReferenceKey(IReferencePage.GUARANTEED_INDIRECT_OFFSET,
            pUberPage.getReferenceKeys()[IReferencePage.GUARANTEED_INDIRECT_OFFSET]);

        mDelegate = new PageReadTrx(pSession, pUberPage, pRepresentRev, pWriter);

        // Get previous revision root page..
        final RevisionRootPage previousRevRoot = mDelegate.getActualRevisionRootPage();
        // ...and using this data to initialize a fresh revision root including the pointers.
        mNewRoot =
            new RevisionRootPage(mNewUber.incrementPageCounter(), pRepresentRev + 1, previousRevRoot
                .getMaxNodeKey());
        mNewRoot.setReferenceKey(IReferencePage.GUARANTEED_INDIRECT_OFFSET, previousRevRoot
            .getReferenceKeys()[IReferencePage.GUARANTEED_INDIRECT_OFFSET]);

        // Prepare indirect tree to hold reference to prepared revision root
        // nodePageReference.
        long lastIndirectKey =
            (mNewUber.getRevisionNumber() - ((mNewUber.getRevisionNumber() >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[2]) << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[2]));
        LogKey indirectKey = preparePathToLeaf(true, mNewUber, lastIndirectKey);
        NodePageContainer indirectContainer = mLog.get(indirectKey);
        int offset = nodePageOffset(mNewUber.getRevisionNumber());
        ((IndirectPage)indirectContainer.getModified()).setReferenceKey(offset, mNewRoot.getPageKey());
        mLog.put(indirectKey, indirectContainer);

        // Setting up a new namepage
        Map<IMetaEntry, IMetaEntry> oldMap = mDelegate.mMetaPage.getMetaMap();
        mNewName = new MetaPage(mNewUber.incrementPageCounter());

        for (IMetaEntry key : oldMap.keySet()) {
            mNewName.setEntry(key, oldMap.get(key));
        }

        mNewRoot.setReferenceKey(RevisionRootPage.NAME_REFERENCE_OFFSET, mNewName.getPageKey());

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
        checkArgument(pNodeKey >= 0);
        final long seqNodePageKey = nodePageKey(pNodeKey);
        final int nodePageOffset = nodePageOffset(pNodeKey);
        final int lastIndirectOffset = (int)pNodeKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[2];
        NodePageContainer container = prepareNodePage(lastIndirectOffset, seqNodePageKey);

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
        final long seqNodePageKey = nodePageKey(pNode.getNodeKey());
        final int nodePageOffset = nodePageOffset(pNode.getNodeKey());
        LogKey key = new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, seqNodePageKey);
        NodePageContainer container = mLog.get(key);
        NodePage page = (NodePage)container.getModified();
        page.setNode(nodePageOffset, pNode);
        mLog.put(key, container);
    }

    /**
     * {@inheritDoc}
     */
    public long setNode(final INode pNode) throws TTException {
        // Allocate node key and increment node count.
        final long nodeKey = pNode.getNodeKey();
        final long seqPageKey = nodePageKey(nodeKey);
        final int nodePageOffset = nodePageOffset(nodeKey);
        final int lastIndirectOffset = (int)(nodeKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[2]);
        NodePageContainer container = prepareNodePage(lastIndirectOffset, seqPageKey);
        final NodePage page = ((NodePage)container.getModified());
        page.setNode(nodePageOffset, pNode);
        mLog.put(new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, seqPageKey), container);
        return nodeKey;
    }

    /**
     * Removing a node from the storage.
     * 
     * @param pNode
     *            {@link INode} to be removed
     * @throws TTIOException
     *             if the removal fails
     */
    public void removeNode(final INode pNode) throws TTException {
        assert pNode != null;
        final long nodePageKey = nodePageKey(pNode.getNodeKey());
        final int lastIndirectOffset = (int)nodePageKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[2];
        NodePageContainer container = prepareNodePage(lastIndirectOffset, nodePageKey);
        final INode delNode = new DeletedNode(pNode.getNodeKey());
        ((NodePage)container.getComplete()).setNode(nodePageOffset(pNode.getNodeKey()), delNode);
        ((NodePage)container.getModified()).setNode(nodePageOffset(pNode.getNodeKey()), delNode);

        mLog.put(new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, nodePageKey), container);
    }

    /**
     * {@inheritDoc}
     */
    public INode getNode(final long pNodeKey) throws TTIOException {

        // Calculate page and node part for given nodeKey.
        final long nodePageKey = nodePageKey(pNodeKey);
        final int nodePageOffset = nodePageOffset(pNodeKey);

        final NodePageContainer container =
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
     * Creating a namekey for a given name.
     * 
     * @param pName
     *            for which the key should be created.
     * @return an int, representing the namekey
     * @throws TTIOException
     *             if something odd happens while storing the new key
     */
    public void createEntry(final IMetaEntry key, IMetaEntry value) throws TTIOException {
        mNewName.setEntry(key, value);
    }

    public void commit() throws TTException {

        Iterator<Map.Entry<LogKey, NodePageContainer>> entries = mLog.getIterator();
        while (entries.hasNext()) {
            Map.Entry<LogKey, NodePageContainer> next = entries.next();
            mPageWriter.write(next.getValue().getModified());
        }
        mPageWriter.write(mNewName);
        mPageWriter.write(mNewRoot);
        mPageWriter.writeUberPage(mNewUber);

        mLog.clear();

        ((Session)mDelegate.mSession).setLastCommittedUberPage(mNewUber);
        setUpTransaction(mNewUber, mDelegate.mSession, mNewUber.getRevisionNumber(), mPageWriter);

    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTIOException
     *             if something weird happened in the storage
     */
    public boolean close() throws TTIOException {
        if (!mDelegate.isClosed()) {
            mDelegate.close();
            mLog.clear();
            mDelegate.mSession.deregisterPageTrx(this);
            // mPageWriter.close();
            return true;
        } else {
            return false;
        }

    }

    public long incrementNodeKey() {
        return mNewRoot.incrementMaxNodeKey();
    }

    private NodePageContainer prepareNodePage(final int pIndirectOffset, final long pSeqPageKey)
        throws TTException {
        LogKey key = new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, pSeqPageKey);
        // See if on nodePageLevel, there are any pages...
        NodePageContainer container = mLog.get(key);
        // ... and start dereferencing of not.
        if (container == null) {
            LogKey indirectKey = preparePathToLeaf(false, mNewRoot, pIndirectOffset);

            NodePageContainer indirectContainer = mLog.get(indirectKey);
            int nodeOffset = nodePageOffset(pSeqPageKey);
            long pageKey = ((IndirectPage)indirectContainer.getModified()).getReferenceKeys()[nodeOffset];

            long newPageKey = mNewUber.incrementPageCounter();
            if (pageKey != 0) {
                NodePage[] pages = mDelegate.getSnapshotPages(pSeqPageKey);
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
                container = new NodePageContainer(newPage, newPage);
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
        final long pSeqPageKey) throws TTException {

        // Initial state pointing to the indirect page of level 0.
        int offset = -1;
        int parentOffset = IReferencePage.GUARANTEED_INDIRECT_OFFSET;
        long levelKey = pSeqPageKey;
        IReferencePage page = null;
        IReferencePage parentPage = pPage;
        LogKey key = null;
        LogKey parentKey = new LogKey(pIsRootLevel, -1, 0);

        // Iterate through all levels.
        for (int level = 0; level < IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; level++) {
            offset = (int)(levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level]);
            levelKey -= offset << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level];

            // for each level, take a sharp look if the indirectpage was already modified within this
            // transaction
            key = new LogKey(pIsRootLevel, level, parentOffset * IConstants.CONTENT_COUNT + offset);
            NodePageContainer container = mLog.get(key);
            // if not...
            if (container == null) {
                // ...generating a new page and let it point to the last offset of the last page and ...
                final long newKey = mNewUber.incrementPageCounter();
                page = new IndirectPage(newKey);

                // ...check if there is an existing indirect page...
                if (parentPage.getReferenceKeys()[parentOffset] != 0) {
                    IndirectPage oldPage;
                    // ...try to retrieve the former page from the log..
                    LogKey oldKey =
                        new LogKey(pIsRootLevel, level, parentOffset * IConstants.CONTENT_COUNT + offset - 1);
                    NodePageContainer oldContainer = mLog.get(oldKey);
                    // ..since then the page was entirely filled. If not, read the page from a former
                    // revision..
                    if (oldContainer == null) {
                        // ..from the persistent storage ...
                        oldPage = (IndirectPage)mPageWriter.read(parentPage.getReferenceKeys()[parentOffset]);
                        // ...and copy all references and put it in the transaction log.
                        for (int i = 0; i < oldPage.getReferenceKeys().length; i++) {
                            page.setReferenceKey(i, oldPage.getReferenceKeys()[i]);
                        }
                    } else {
                        parentOffset = offset;
                    }
                }
                // Set the reference to the current revision..
                parentPage.setReferenceKey(parentOffset, newKey);
                container = new NodePageContainer(parentPage, parentPage);
                // .. and put the parent-reference to the log as well as the reference of the..
                mLog.put(parentKey, container);
                // ...current page.
                container = new NodePageContainer(page, page);
                mLog.put(key, container);
            } else {
                page = (IndirectPage)container.getModified();
            }

            // finally, set the new pagekey for the next level
            parentKey = key;
            parentPage = page;
            parentOffset = offset;
        }

        // Return reference to leaf of indirect tree.
        return key;
    }

    /**
     * Current reference to actual rev-root page.
     * 
     * @return the current revision root page
     */
    public RevisionRootPage getActualRevisionRootPage() {
        return mNewRoot;
    }

    /**
     * Building name consisting out of prefix and name. NamespaceUri is not used
     * over here.
     * 
     * @param pQName
     *            the {@link QName} of an element
     * @return a string with [prefix:]localname
     */
    public static String buildName(final QName pQName) {
        if (pQName == null) {
            throw new NullPointerException("mQName must not be null!");
        }
        String name;
        if (pQName.getPrefix().isEmpty()) {
            name = pQName.getLocalPart();
        } else {
            name = new StringBuilder(pQName.getPrefix()).append(":").append(pQName.getLocalPart()).toString();
        }
        return name;
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
    public String toString() {
        return toStringHelper(this).add("mPageWriter", mPageWriter).add("mLog", mLog).add("mRootPage",
            mNewRoot).add("mDelegate", mDelegate).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaPage getMetaPage() {
        return mNewName;
    }

}

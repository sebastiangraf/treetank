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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.treetank.access.PageReadTrx.nodePageKey;
import static org.treetank.access.PageReadTrx.nodePageOffset;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

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
import org.treetank.page.NamePage;
import org.treetank.page.NodePage;
import org.treetank.page.NodePage.DeletedNode;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IReferencePage;
import org.treetank.utils.NamePageHash;

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
    private final UberPage mNewUber;

    /** Reference to the actual revRoot. */
    private final RevisionRootPage mNewRoot;

    /** Last reference to the actual namePage. */
    private final NamePage mNewName;

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

        mNewUber =
            new UberPage(pUberPage.incrementPageCounter(), pUberPage.getRevisionNumber() + 1, pUberPage
                .getPageCounter());
        mNewUber.setReferenceKey(IReferencePage.GUARANTEED_INDIRECT_OFFSET,
            pUberPage.getReferenceKeys()[IReferencePage.GUARANTEED_INDIRECT_OFFSET]);

        mDelegate = new PageReadTrx(pSession, pUberPage, pRepresentRev, pWriter);
        mPageWriter = pWriter;
        mLog =
            new LRUCache(new BerkeleyPersistenceLog(new File(pSession.getConfig().mProperties
                .getProperty(org.treetank.access.conf.ContructorProps.STORAGEPATH)),
                pSession.getConfig().mNodeFac));

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
        mNewName = new NamePage(mNewUber.incrementPageCounter());
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
     * Create fresh node and prepare node nodePageReference for modifications
     * (COW).
     * 
     * @param pNode
     *            node to add
     * @return Unmodified node from parameter for convenience.
     * @throws TTIOException
     *             if IO Error
     */
    public <T extends INode> T createNode(final T pNode) throws TTException {
        // Allocate node key and increment node count.
        mNewRoot.incrementMaxNodeKey();
        final long nodeKey = mNewRoot.getMaxNodeKey();
        final long seqPageKey = nodePageKey(nodeKey);
        final int nodePageOffset = nodePageOffset(nodeKey);
        final int lastIndirectOffset = (int)nodeKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[2];
        NodePageContainer container = prepareNodePage(lastIndirectOffset, seqPageKey);
        final NodePage page = ((NodePage)container.getModified());
        page.setNode(nodePageOffset, pNode);
        mLog.put(new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, seqPageKey), container);
        return pNode;
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
        ((NodePage)container.getModified()).setNode(nodePageOffset(pNode.getNodeKey()), delNode);
        ((NodePage)container.getModified()).setNode(nodePageOffset(pNode.getNodeKey()), delNode);
        mLog.put(new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, nodePageKey), container);
    }

    /**
     * {@inheritDoc}
     */
    public INode getNode(final long pNodeKey) throws TTException {

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
     * Getting the name corresponding to the given key.
     * 
     * @param pNameKey
     *            for the term searched
     * @return the name
     */
    public String getName(final int pNameKey) {
        String returnVal;
        // if currentNamePage == null -> state was commited and no
        // prepareNodepage was invoked yet
        if (mNewName.getName(pNameKey) == null) {
            returnVal = mDelegate.getName(pNameKey);
        } else {
            returnVal = mNewName.getName(pNameKey);
        }
        return returnVal;

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
    public int createNameKey(final String pName) throws TTIOException {
        final String string = (pName == null ? "" : pName);
        final int nameKey = NamePageHash.generateHashForString(string);

        if (mNewName.getName(nameKey) == null) {
            mNewName.setName(nameKey, string);
        }
        return nameKey;
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

        ((Session)mDelegate.mSession).setLastCommittedUberPage(mNewUber);

    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTIOException
     *             if something weird happened in the storage
     */
    public void close() throws TTIOException {
        mDelegate.mSession.deregisterPageTrx(this);
        mDelegate.close();
        mLog.clear();
        // mPageWriter.close();
    }

    public long getMaxNodeKey() {
        return mNewRoot.getMaxNodeKey();
    }

    private NodePageContainer prepareNodePage(final int lastIndirectOffset, final long pSeqPageKey)
        throws TTException {
        LogKey key = new LogKey(false, IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length, pSeqPageKey);
        // See if on nodePageLevel, there are any pages...
        NodePageContainer container = mLog.get(key);
        // ... and start dereferencing of not.
        if (container == null) {
            LogKey indirectKey = preparePathToLeaf(false, mNewRoot, lastIndirectOffset);

            NodePageContainer indirectContainer = mLog.get(indirectKey);
            int offset = nodePageOffset(pSeqPageKey);
            long pageKey = ((IndirectPage)indirectContainer.getModified()).getReferenceKeys()[offset];

            long newPageKey = mNewUber.incrementPageCounter();
            if (pageKey != 0) {
                NodePage[] pages = mDelegate.getSnapshotPages(pSeqPageKey);
                container =
                    mDelegate.mSession.getConfig().mRevision.combinePagesForModification(newPageKey, pages);
                // NodePage oldPage = (NodePage)mPageWriter.read(pageKey);
                // container = new NodePageContainer(oldPage, newPage);
            } else {
                NodePage newPage = new NodePage(newPageKey);
                container = new NodePageContainer(newPage, newPage);
            }
            ((IndirectPage)indirectContainer.getModified()).setReferenceKey(offset, newPageKey);
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
        int lastOffset = IReferencePage.GUARANTEED_INDIRECT_OFFSET;
        long levelKey = pSeqPageKey;
        IReferencePage page = null;
        IReferencePage lastPage = pPage;
        LogKey key = null;
        LogKey lastKey = new LogKey(pIsRootLevel, -1, 0);

        // Iterate through all levels.
        for (int level = 0; level < IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; level++) {
            offset = (int)(levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level]);
            levelKey -= offset << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level];

            // for each level, take a sharp look if the indirectpage was already modified within this
            // transaction
            key = new LogKey(pIsRootLevel, level, lastOffset * IConstants.CONTENT_COUNT + offset);
            NodePageContainer container = mLog.get(key);
            // if not...
            if (container == null) {
                // ...generating a new page and let it point to the last offset of the last page and ...
                final long newKey = mNewUber.incrementPageCounter();
                page = new IndirectPage(newKey);

                // ...check if there is an existing indirect page...
                if (lastPage.getReferenceKeys()[lastOffset] != 0) {
                    // ..read it from the persistent storage ...
                    IndirectPage oldPage =
                        (IndirectPage)mPageWriter.read(lastPage.getReferenceKeys()[lastOffset]);
                    // ...and copy all references and put it in the transaction log.
                    for (int i = 0; i < oldPage.getReferenceKeys().length; i++) {
                        page.setReferenceKey(i, oldPage.getReferenceKeys()[i]);
                    }
                }
                lastPage.setReferenceKey(lastOffset, newKey);
                container = new NodePageContainer(lastPage, lastPage);
                mLog.put(lastKey, container);
                container = new NodePageContainer(page, page);
                mLog.put(key, container);
            } else {
                page = (IndirectPage)container.getModified();
            }

            // finally, set the new pagekey for the next level
            lastKey = key;
            lastPage = page;
            lastOffset = offset;
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
        StringBuilder builder = new StringBuilder();
        builder.append("PageWriteTrx [mPageWriter=");
        builder.append(mPageWriter);
        builder.append(", mLog=");
        builder.append(mLog);
        builder.append(", mNewRoot=");
        builder.append(mNewRoot);
        builder.append(", mDelegate=");
        builder.append(mDelegate);
        builder.append("]");
        return builder.toString();
    }

}

/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import javax.xml.namespace.QName;

import org.treetank.api.IItem;
import org.treetank.cache.ICache;
import org.treetank.cache.NodePageContainer;
import org.treetank.cache.TransactionLogCache;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IWriter;
import org.treetank.node.AbsNode;
import org.treetank.node.AttributeNode;
import org.treetank.node.DeletedNode;
import org.treetank.node.ElementNode;
import org.treetank.node.NamespaceNode;
import org.treetank.node.TextNode;
import org.treetank.page.AbsPage;
import org.treetank.page.IndirectPage;
import org.treetank.page.NamePage;
import org.treetank.page.NodePage;
import org.treetank.page.PageReference;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;
import org.treetank.settings.EDatabaseSetting;
import org.treetank.settings.EFixed;
import org.treetank.settings.ERevisioning;
import org.treetank.utils.IConstants;
import org.treetank.utils.ItemList;
import org.treetank.utils.NamePageHash;

/**
 * <h1>WriteTransactionState</h1>
 * 
 * <p>
 * See {@link ReadTransactionState}.
 * </p>
 */
public final class WriteTransactionState extends ReadTransactionState {

    /** Page writer to serialize. */
    private final IWriter mPageWriter;

    /** Cache to store the changes in this writetransaction. */
    private final ICache mLog;

    /** Last references to the Nodepage, needed for pre/postcondition check. */
    private NodePageContainer mNodePageCon;

    /** Last reference to the actual revRoot. */
    private final RevisionRootPage mNewRoot;

    /** State of session for synchronizing against other writetrans. */
    private final SessionState mSessionState;

    /** ID for current transaction. */
    private final long mTransactionID;

    /**
     * Standard constructor.
     * 
     * @param paramDatabaseConfig
     *            Database Configuration
     * @param paramSessionState
     *            Session State
     * @param paramUberPage
     *            Root of revision.
     * @param paramWriter
     *            Writer where this transaction should write to
     * @param paramParamId
     *            parameter ID
     * @param paramRepresentRev
     *            Revision Represent
     * @param paramStoreRev
     *            Revision Store
     * @throws TTIOException
     *             if IO Error
     */
    protected WriteTransactionState(final DatabaseConfiguration paramDatabaseConfig,
        final SessionState paramSessionState, final UberPage paramUberPage, final IWriter paramWriter,
        final long paramParamId, final long paramRepresentRev, final long paramStoreRev)
        throws TTIOException {
        super(paramDatabaseConfig, paramUberPage, paramRepresentRev, new ItemList(), paramWriter);
        mNewRoot = preparePreviousRevisionRootPage(paramRepresentRev, paramStoreRev);
        mSessionState = paramSessionState;
        mLog = new TransactionLogCache(paramDatabaseConfig, paramStoreRev);
        mPageWriter = paramWriter;
        mTransactionID = paramParamId;

    }

    /**
     * Prepare a node for modification. This is getting the node from the
     * (persistence) layer, storing the page in the cache and setting up the
     * node for upcoming modification. Note that this only occurs for {@link AbsNode}s.
     * 
     * @param mNodeKey
     *            key of the node to be modified
     * @return an {@link AbsNode} instance
     * @throws TTIOException
     *             if IO Error
     */
    protected AbsNode prepareNodeForModification(final long mNodeKey) throws TTIOException {
        if (mNodePageCon != null) {
            throw new IllegalStateException();
        }

        final long nodePageKey = nodePageKey(mNodeKey);
        final int nodePageOffset = nodePageOffset(mNodeKey);
        prepareNodePage(nodePageKey);

        AbsNode node = this.mNodePageCon.getModified().getNode(nodePageOffset);
        if (node == null) {
            final AbsNode oldNode = this.mNodePageCon.getComplete().getNode(nodePageOffset);
            if (oldNode == null) {
                throw new TTIOException("Cannot retrieve node from cache");
            }
            node = oldNode.clone();
            this.mNodePageCon.getModified().setNode(nodePageOffset, node);
        }
        return node;
    }

    /**
     * Finishing the node modification. That is storing the node including the
     * page in the cache.
     * 
     * @param mNode
     *            the node to be modified.
     */
    protected void finishNodeModification(final IItem mNode) {
        final long nodePageKey = nodePageKey(mNode.getNodeKey());
        if (mNodePageCon == null || mNode == null || mLog.get(nodePageKey) == null) {
            throw new IllegalStateException();
        }

        mLog.put(nodePageKey, mNodePageCon);

        this.mNodePageCon = null;

    }

    /**
     * Create fresh node and prepare node nodePageReference for modifications
     * (COW).
     * 
     * @param mNode
     *            node to add.
     * @return Unmodified node from parameter for convenience.
     * @throws TTIOException
     *             if IO Error
     */
    protected AbsNode createNode(final AbsNode mNode) throws TTIOException {
        // Allocate node key and increment node count.
        mNewRoot.incrementMaxNodeKey();
        // Prepare node nodePageReference (COW).
        final long nodeKey = mNewRoot.getMaxNodeKey();
        final long nodePageKey = nodePageKey(nodeKey);
        final int nodePageOffset = nodePageOffset(nodeKey);
        prepareNodePage(nodePageKey);
        final NodePage page = mNodePageCon.getModified();
        page.setNode(nodePageOffset, mNode);
        finishNodeModification(mNode);

        return mNode;
    }

    protected ElementNode createElementNode(final long parentKey, final long mLeftSibKey,
        final long rightSibKey, final long hash, final QName mName) throws TTIOException {

        final int nameKey = createNameKey(buildName(mName));
        final int namespaceKey = createNameKey(mName.getNamespaceURI());
        final int typeKey = createNameKey("xs:untyped");

        return (ElementNode)createNode(ElementNode.createData(mNewRoot.getMaxNodeKey() + 1, parentKey,
            mLeftSibKey, rightSibKey, (Long)EFixed.NULL_NODE_KEY.getStandardProperty(), 0, nameKey,
            namespaceKey, typeKey, hash));
    }

    protected TextNode createTextNode(final long mParentKey, final long mLeftSibKey, final long rightSibKey,
        final byte[] mValue) throws TTIOException {
        final int typeKey = createNameKey("xs:untyped");
        return (TextNode)createNode(TextNode.createData(mNewRoot.getMaxNodeKey() + 1, mParentKey,
            mLeftSibKey, rightSibKey, typeKey, mValue));
    }

    protected AttributeNode createAttributeNode(final long parentKey, final QName mName, final byte[] mValue)
        throws TTIOException {

        final int nameKey = createNameKey(buildName(mName));
        final int namespaceKey = createNameKey(mName.getNamespaceURI());
        final int typeKey = createNameKey("xs:untypedAtomic");
        return (AttributeNode)createNode(AttributeNode.createData(mNewRoot.getMaxNodeKey() + 1, parentKey,
            nameKey, namespaceKey, typeKey, mValue));
    }

    protected NamespaceNode createNamespaceNode(final long parentKey, final int mUriKey, final int prefixKey)
        throws TTIOException {
        return (NamespaceNode)createNode(NamespaceNode.createData(mNewRoot.getMaxNodeKey() + 1, parentKey,
            mUriKey, prefixKey));
    }

    /**
     * Removing a node from the storage.
     * 
     * @param mNode
     *            to be removed
     * @throws TTIOException
     *             if the removal fails
     */
    protected void removeNode(final AbsNode mNode) throws TTIOException {
        final long nodePageKey = nodePageKey(mNode.getNodeKey());
        prepareNodePage(nodePageKey);
        final AbsNode delNode = DeletedNode.createData(mNode.getNodeKey(), mNode.getParentKey());
        mNodePageCon.getModified().setNode(nodePageOffset(mNode.getNodeKey()), delNode);
        mNodePageCon.getComplete().setNode(nodePageOffset(mNode.getNodeKey()), delNode);
        finishNodeModification(mNode);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IItem getNode(final long mNodeKey) throws TTIOException {

        // Calculate page and node part for given nodeKey.
        final long nodePageKey = nodePageKey(mNodeKey);
        final int nodePageOffset = nodePageOffset(mNodeKey);

        final NodePageContainer pageCont = mLog.get(nodePageKey);
        if (pageCont == null) {
            return super.getNode(mNodeKey);
        } else if (pageCont.getModified().getNode(nodePageOffset) == null) {
            final IItem item = pageCont.getComplete().getNode(nodePageOffset);
            return checkItemIfDeleted(item);

        } else {
            final IItem item = pageCont.getModified().getNode(nodePageOffset);
            return checkItemIfDeleted(item);
        }

    }

    /**
     * Getting the name corresponding to the given key.
     * 
     * @param mNameKey
     *            for the term searched
     * @return the name
     */
    @Override
    protected String getName(final int mNameKey) {
        final NamePage currentNamePage = (NamePage)mNewRoot.getNamePageReference().getPage();
        String returnVal;
        // if currentNamePage == null -> state was commited and no
        // prepareNodepage was invoked yet
        if (currentNamePage == null || currentNamePage.getName(mNameKey) == null) {
            returnVal = super.getName(mNameKey);
        } else {
            returnVal = currentNamePage.getName(mNameKey);
        }
        return returnVal;

    }

    /**
     * Creating a namekey for a given name.
     * 
     * @param mName
     *            for which the key should be created.
     * @return an int, representing the namekey
     * @throws TTIOException
     *             if something odd happens while storing the new key
     */
    protected int createNameKey(final String mName) throws TTIOException {
        final String string = (mName == null ? "" : mName);
        final int nameKey = NamePageHash.generateHashForString(string);

        final NamePage namePage = (NamePage)mNewRoot.getNamePageReference().getPage();

        if (namePage.getName(nameKey) == null) {
            namePage.setName(nameKey, string);
        }
        return nameKey;
    }

    /**
     * Committing a a writetransaction. This method is recursivly invoked by all {@link PageReference}s.
     * 
     * @param reference
     *            to be commited
     * @throws AbsTTException
     *             if the write fails
     */
    public void commit(final PageReference reference) throws AbsTTException {
        AbsPage page = null;

        // if reference is not null, get one from the persistent storage.
        if (reference != null) {
            // first, try to get one from the log
            final NodePageContainer cont = mLog.get(reference.getNodePageKey());
            if (cont != null) {
                page = cont.getModified();
            }
            // if none is in the log, test if one is instantiated, if so, get the one flexible from the
            // reference
            if (page == null) {
                if (!reference.isInstantiated()) {
                    return;
                } else {
                    page = reference.getPage();
                }
            }

            reference.setPage(page);
            // Recursively commit indirectely referenced pages and then
            // write self.
            page.commit(this);
            mPageWriter.write(reference);
            reference.setPage(null);
            // afterwards synchronize all logs since the changes must to be written to the transaction log as
            // well
            if (cont != null) {
                mSessionState.syncLogs(cont, mTransactionID);
            }
        }
    }

    protected UberPage commit() throws AbsTTException {

        mSessionState.mCommitLock.lock();

        final PageReference uberPageReference = new PageReference();
        final UberPage uberPage = getUberPage();
        uberPageReference.setPage(uberPage);

        // // // /////////////
        // // // New code starts here
        // // // /////////////
        // final Stack<PageReference> refs = new Stack<PageReference>();
        // refs.push(uberPageReference);
        //
        // final Stack<Integer> refIndex = new Stack<Integer>();
        // refIndex.push(0);
        // refIndex.push(0);
        //
        // do {
        //
        // assert refs.size() + 1 == refIndex.size();
        //
        // // Getting the next ref
        // final PageReference currentRef = refs.peek();
        // final int currentIndex = refIndex.pop();
        //
        // // Check if referenced page is valid, if not, continue
        // AbstractPage page = log.get(currentRef.getNodePageKey());
        // boolean continueFlag = true;
        // if (page == null) {
        // if (currentRef.isInstantiated()) {
        // page = currentRef.getPage();
        // } else {
        // continueFlag = false;
        // }
        // } else {
        // currentRef.setPage(page);
        // }
        //
        // if (continueFlag) {
        //
        // if (currentIndex + 1 <= page.getReferences().length) {
        // // go down
        //
        // refIndex.push(currentIndex + 1);
        //
        // refs.push(page.getReference(currentIndex));
        // refIndex.push(0);
        //
        // } else {
        //
        // mPageWriter.write(currentRef);
        // refs.pop();
        // }
        //
        // } // ref is not designated to be serialized
        // else {
        // refs.pop();
        // }
        //
        // } while (!refs.empty());
        //
        // // // ///////////////
        // // // New code ends here
        // // // ///////////////

        // Recursively write indirectely referenced pages.
        uberPage.commit(this);

        uberPageReference.setPage(uberPage);
        mPageWriter.writeFirstReference(uberPageReference);
        uberPageReference.setPage(null);

        mSessionState.waitForFinishedSync(mTransactionID);
        // mPageWriter.close();
        mSessionState.mCommitLock.unlock();
        return uberPage;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTIOException
     *             if something weird happened in the storage
     */
    @Override
    protected void close() throws TTIOException {
        // super.close();
        mLog.clear();
        mPageWriter.close();
    }

    protected IndirectPage prepareIndirectPage(final PageReference reference) throws TTIOException {

        IndirectPage page = (IndirectPage)reference.getPage();
        if (!reference.isInstantiated()) {
            if (reference.isCommitted()) {
                page =
                    new IndirectPage((IndirectPage)dereferenceIndirectPage(reference),
                        mNewRoot.getRevision() + 1);
            } else {
                page = new IndirectPage(getUberPage().getRevision());

            }
            reference.setPage(page);
        } else {
            page = (IndirectPage)reference.getPage();
        }
        return page;
    }

    protected NodePageContainer prepareNodePage(final long mNodePageKey) throws TTIOException {

        // Last level points to node nodePageReference.
        NodePageContainer cont = mLog.get(mNodePageKey);
        if (cont == null) {

            // Indirect reference.
            final PageReference reference =
                prepareLeafOfTree(mNewRoot.getIndirectPageReference(), mNodePageKey);

            if (!reference.isInstantiated()) {

                if (reference.isCommitted()) {
                    cont = dereferenceNodePageForModification(mNodePageKey);
                } else {
                    cont =
                        new NodePageContainer(new NodePage(mNodePageKey, IConstants.UBP_ROOT_REVISION_NUMBER));
                }

            } else {
                // TODO Nodepage is just used as bootstrap-begin. Perhaps this
                // can be done otherwise
                final NodePage page = (NodePage)reference.getPage();
                cont = new NodePageContainer(page);

                reference.setPage(null);
            }

            reference.setNodePageKey(mNodePageKey);
            mLog.put(mNodePageKey, cont);
        }
        mNodePageCon = cont;
        return cont;
    }

    private RevisionRootPage preparePreviousRevisionRootPage(final long mBaseRevision,
        final long representRevision) throws TTIOException {

        if (getUberPage().isBootstrap()) {
            return super.loadRevRoot(mBaseRevision);
        } else {

            // Prepare revision root nodePageReference.
            final RevisionRootPage revisionRootPage =
                new RevisionRootPage(super.loadRevRoot(mBaseRevision), representRevision + 1);

            // Prepare indirect tree to hold reference to prepared revision root
            // nodePageReference.
            final PageReference revisionRootPageReference =
                prepareLeafOfTree(getUberPage().getIndirectPageReference(), getUberPage().getRevisionNumber());

            // Link the prepared revision root nodePageReference with the
            // prepared indirect tree.
            revisionRootPageReference.setPage(revisionRootPage);

            revisionRootPage.getNamePageReference().setPage(
                (NamePage)super.getActualRevisionRootPage().getNamePageReference().getPage());

            // Return prepared revision root nodePageReference.
            return revisionRootPage;
        }
    }

    protected PageReference prepareLeafOfTree(final PageReference mStartReference, final long mKey)
        throws TTIOException {

        // Initial state pointing to the indirect nodePageReference of level 0.

        PageReference reference = mStartReference;
        int offset = 0;
        long levelKey = mKey;

        // Iterate through all levels.
        for (int level = 0, height = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; level < height; level++) {
            offset = (int)(levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level]);
            levelKey -= offset << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level];
            final IndirectPage page = prepareIndirectPage(reference);
            reference = page.getReference(offset);

        }

        // Return reference to leaf of indirect tree.
        return reference;
    }

    /**
     * Dereference node page reference.
     * 
     * @param mNodePageKey
     *            Key of node page.
     * @return Dereferenced page.
     * @throws TTIOException
     *             If something happend in the node.
     */
    private NodePageContainer dereferenceNodePageForModification(final long mNodePageKey)
        throws TTIOException {
        final NodePage[] revs = getSnapshotPages(mNodePageKey);
        final ERevisioning revision =
            ERevisioning.valueOf(getDatabaseConfiguration().getProps().getProperty(
                EDatabaseSetting.REVISION_TYPE.name()));
        final int mileStoneRevision =
            Integer.parseInt(getDatabaseConfiguration().getProps().getProperty(
                EDatabaseSetting.REVISION_TO_RESTORE.name()));

        return revision.combinePagesForModification(revs, mileStoneRevision);
    }

    /**
     * Current reference to actual rev-root page.
     * 
     * @return the current revision root page
     */
    @Override
    protected RevisionRootPage getActualRevisionRootPage() {
        return this.mNewRoot;
    }

    /**
     * Updating a container in this transaction state.
     * 
     * @param mCont
     *            to be updated
     */
    protected void updateDateContainer(final NodePageContainer mCont) {
        synchronized (mLog) {
            // TODO implement for MultiWriteTrans
            // Refer to issue #203
        }
    }

    /**
     * Building name consisting out of prefix and name. NamespaceUri is not used
     * over here.
     * 
     * @param mQname
     *            the QName of an element
     * @return a string with [prefix:]localname
     */
    public static String buildName(final QName mQname) {
        String name;
        if (mQname.getPrefix().isEmpty()) {
            name = mQname.getLocalPart();
        } else {
            name = new StringBuilder(mQname.getPrefix()).append(":").append(mQname.getLocalPart()).toString();
        }
        return name;
    }

}

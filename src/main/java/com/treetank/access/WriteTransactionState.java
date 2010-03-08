/*
 * Copyright (c) 2009, Sebastian Graf (Ph.D. Thesis), University of Konstanz
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
 * $Id: WriteTransactionState.java 4543 2009-01-19 09:02:51Z graf $
 */

package com.treetank.access;

import com.treetank.api.IItem;
import com.treetank.cache.ICache;
import com.treetank.cache.NodePageContainer;
import com.treetank.cache.TransactionLogCache;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.io.IWriter;
import com.treetank.node.AbstractNode;
import com.treetank.node.AttributeNode;
import com.treetank.node.ElementNode;
import com.treetank.node.NamespaceNode;
import com.treetank.node.NodePersistenter;
import com.treetank.node.TextNode;
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
import com.treetank.utils.ItemList;
import com.treetank.utils.NamePageHash;

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
    private final ICache log;

    /** Last references to the Nodepage, needed for pre/postcondition check */
    private NodePageContainer nodePageCon;

    /** Last reference to the actual revRoot */
    private final RevisionRootPage mNewRoot;

    /** State of session for synchronizing against other writetrans */
    private final SessionState mSessionState;

    /** ID for current transaction */
    private final long transactionID;

    /**
     * Standard constructor.
     * 
     * @param sessionConfiguration
     *            Configuration of session.
     * @param uberPage
     *            Root of revision.
     * @param writer
     *            Writer where this transaction should write to
     */
    protected WriteTransactionState(
            final DatabaseConfiguration databaseConfiguration,
            final SessionState sessionState, final UberPage uberPage,
            final IWriter writer, final long paramId,
            final long representRevision, final long storeRevision)
            throws TreetankIOException {
        super(databaseConfiguration, uberPage, representRevision,
                new ItemList(), writer);
        mNewRoot = preparePreviousRevisionRootPage(representRevision,
                storeRevision);
        mSessionState = sessionState;
        log = new TransactionLogCache(databaseConfiguration, storeRevision);
        mPageWriter = writer;
        transactionID = paramId;

    }

    /**
     * Prepare a node for modification. This is getting the node from the
     * (persistence) layer, storing the page in the cache and setting up the
     * node for upcoming modification.
     * 
     * @param nodeKey
     *            key of the node to be modified
     * @return an {@link AbstractNode} instance
     */
    protected AbstractNode prepareNodeForModification(final long nodeKey)
            throws TreetankIOException {
        if (nodePageCon != null) {
            throw new IllegalStateException();
        }

        final long nodePageKey = nodePageKey(nodeKey);
        final int nodePageOffset = nodePageOffset(nodeKey);
        prepareNodePage(nodePageKey);

        AbstractNode node = this.nodePageCon.getModified().getNode(
                nodePageOffset);
        if (node == null) {
            final AbstractNode oldNode = this.nodePageCon.getComplete()
                    .getNode(nodePageOffset);
            node = NodePersistenter.createNode(oldNode);
            this.nodePageCon.getModified().setNode(nodePageOffset, node);
        }
        return node;
    }

    /**
     * Finishing the node modification. That is storing the node including the
     * page in the cache.
     * 
     * @param node
     *            the node to be modified.
     */
    protected void finishNodeModification(final AbstractNode node) {
        final long nodePageKey = nodePageKey(node.getNodeKey());
        if (nodePageCon == null || node == null || log.get(nodePageKey) == null) {
            throw new IllegalStateException();
        }

        log.put(nodePageKey, nodePageCon);

        this.nodePageCon = null;

    }

    /**
     * Create fresh node and prepare node nodePageReference for modifications
     * (COW).
     * 
     * @param <N>
     *            Subclass of AbstractNode.
     * @param node
     *            node to add.
     * @return Unmodified node from parameter for convenience.
     */
    protected <N extends AbstractNode> N createNode(final N node)
            throws TreetankIOException {
        // Allocate node key and increment node count.
        mNewRoot.incrementMaxNodeKey();
        // Prepare node nodePageReference (COW).
        final long nodeKey = mNewRoot.getMaxNodeKey();
        final long nodePageKey = nodePageKey(nodeKey);
        final int nodePageOffset = nodePageOffset(nodeKey);
        prepareNodePage(nodePageKey);
        final NodePage page = nodePageCon.getModified();
        page.setNode(nodePageOffset, node);
        finishNodeModification(node);

        return node;
    }

    protected ElementNode createElementNode(final long parentKey,
            final long firstChildKey, final long leftSiblingKey,
            final long rightSiblingKey, final int nameKey, final int uriKey,
            final int type) throws TreetankIOException {
        return createNode(new ElementNode(mNewRoot.getMaxNodeKey() + 1,
                parentKey, firstChildKey, leftSiblingKey, rightSiblingKey,
                nameKey, uriKey, type));
    }

    protected AttributeNode createAttributeNode(final long parentKey,
            final int nameKey, final int uriKey, final int type,
            final byte[] value) throws TreetankIOException {
        return createNode(new AttributeNode(mNewRoot.getMaxNodeKey() + 1,
                parentKey, nameKey, uriKey, type, value));
    }

    protected NamespaceNode createNamespaceNode(final long parentKey,
            final int uriKey, final int prefixKey) throws TreetankIOException {
        return createNode(new NamespaceNode(mNewRoot.getMaxNodeKey() + 1,
                parentKey, uriKey, prefixKey));
    }

    protected TextNode createTextNode(final long parentKey,
            final long leftSiblingKey, final long rightSiblingKey,
            final int valueType, final byte[] value) throws TreetankIOException {
        return createNode(new TextNode(mNewRoot.getMaxNodeKey() + 1, parentKey,
                leftSiblingKey, rightSiblingKey, valueType, value));
    }

    /**
     * Removing a node from the storage
     * 
     * @param node
     *            to be removed
     * @throws TreetankIOException
     *             if the removal fails
     */
    protected void removeNode(final AbstractNode node)
            throws TreetankIOException {
        final long nodePageKey = nodePageKey(node.getNodeKey());
        prepareNodePage(nodePageKey);
        // TODO check if null is working with sliding snapshot
        // I doubt it since there has to be some kind of placeholder for the
        // node to let the sliding snapshot know that the null is not only a
        // missing element
        nodePageCon.getModified().setNode(nodePageOffset(node.getNodeKey()),
                null);
        nodePageCon.getComplete().setNode(nodePageOffset(node.getNodeKey()),
                null);
        finishNodeModification(node);

        nodePageCon = null;
    }

    /**
     * {@inheritDoc}
     */
    protected IItem getNode(final long nodeKey) throws TreetankIOException {

        // Calculate page and node part for given nodeKey.
        final long nodePageKey = nodePageKey(nodeKey);
        final int nodePageOffset = nodePageOffset(nodeKey);

        NodePageContainer pageCont = log.get(nodePageKey);
        if (pageCont == null) {
            return super.getNode(nodeKey);
        } else if (pageCont.getModified().getNode(nodePageOffset) == null) {
            return pageCont.getComplete().getNode(nodePageOffset);
        } else {
            return pageCont.getModified().getNode(nodePageOffset);
        }

    }

    /**
     * Getting the name corresponding to the given key
     * 
     * @param nameKey
     *            for the term searched
     * @return the name
     */
    protected String getName(final int nameKey) {
        final NamePage currentNamePage = (NamePage) mNewRoot
                .getNamePageReference().getPage();
        String returnVal;
        // if currentNamePage == null -> state was commited and no
        // prepareNodepage was invoked yet
        if (currentNamePage == null || currentNamePage.getName(nameKey) == null) {
            returnVal = super.getName(nameKey);
        } else {
            returnVal = currentNamePage.getName(nameKey);
        }
        return returnVal;

    }

    /**
     * Creating a namekey for a given name
     * 
     * @param name
     *            for which the key should be created.
     * @return an int, representing the namekey
     * @throws TreetankIOException
     *             if something odd happens while storing the new key
     */
    protected int createNameKey(final String name) throws TreetankIOException {
        final String string = (name == null ? "" : name);
        final int nameKey = NamePageHash.generateHashForString(string);

        final NamePage namePage = (NamePage) mNewRoot.getNamePageReference()
                .getPage();

        if (namePage.getName(nameKey) == null) {
            namePage.setName(nameKey, string);
        }
        return nameKey;
    }

    /**
     * Committing a a writetransaction. This method is recursivly invoked by all
     * {@link PageReference}s.
     * 
     * @param reference
     *            to be commited
     * @throws TreetankIOException
     *             if the write fails
     */
    public void commit(final PageReference reference) throws TreetankException {
        AbstractPage page = null;

        if (reference != null) {
            final NodePageContainer cont = log.get(reference.getNodePageKey());
            if (cont != null) {
                page = cont.getModified();
            }
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
            if (cont != null) {
                mSessionState.syncLogs(cont, transactionID);
            }
        }
    }

    protected UberPage commit() throws TreetankException {

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

        mSessionState.waitForFinishedSync(transactionID);
        // mPageWriter.close();
        mSessionState.mCommitLock.unlock();
        return uberPage;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TreetankIOException
     *             if something happend in the storage
     */
    @Override
    protected void close() throws TreetankIOException {
        // super.close();
        log.clear();
        mPageWriter.close();
    }

    protected IndirectPage prepareIndirectPage(final PageReference reference)
            throws TreetankIOException {

        IndirectPage page = (IndirectPage) reference.getPage();
        if (!reference.isInstantiated()) {
            if (reference.isCommitted()) {
                page = new IndirectPage(
                        (IndirectPage) dereferenceIndirectPage(reference),
                        mNewRoot.getRevision() + 1);
            } else {
                page = new IndirectPage(getUberPage().getRevision());

            }
            reference.setPage(page);
        } else {
            page = (IndirectPage) reference.getPage();
        }
        return page;
    }

    protected NodePageContainer prepareNodePage(final long nodePageKey)
            throws TreetankIOException {

        // Last level points to node nodePageReference.
        NodePageContainer cont = log.get(nodePageKey);
        if (cont == null) {

            // Indirect reference.
            PageReference reference = prepareLeafOfTree(mNewRoot
                    .getIndirectPageReference(), nodePageKey);

            if (!reference.isInstantiated()) {

                if (reference.isCommitted()) {
                    cont = dereferenceNodePageForModification(nodePageKey);
                } else {
                    cont = new NodePageContainer(new NodePage(nodePageKey,
                            IConstants.UBP_ROOT_REVISION_NUMBER));
                }

            } else {
                // TODO Nodepage is just used as bootstrap-begin. Perhaps this
                // can be done otherwise
                final NodePage page = (NodePage) reference.getPage();
                cont = new NodePageContainer(page);

                reference.setPage(null);
            }

            reference.setNodePageKey(nodePageKey);
            log.put(nodePageKey, cont);
        }
        nodePageCon = cont;
        return cont;
    }

    private RevisionRootPage preparePreviousRevisionRootPage(
            final long baseRevision, final long representRevision)
            throws TreetankIOException {

        if (getUberPage().isBootstrap()) {
            return super.loadRevRoot(baseRevision);
        } else {

            // Prepare revision root nodePageReference.
            final RevisionRootPage revisionRootPage = new RevisionRootPage(
                    super.loadRevRoot(baseRevision), representRevision + 1);

            // Prepare indirect tree to hold reference to prepared revision root
            // nodePageReference.
            final PageReference revisionRootPageReference = prepareLeafOfTree(
                    getUberPage().getIndirectPageReference(), getUberPage()
                            .getRevisionNumber());

            // Link the prepared revision root nodePageReference with the
            // prepared indirect tree.
            revisionRootPageReference.setPage(revisionRootPage);

            revisionRootPage.getNamePageReference().setPage(
                    (NamePage) super.getActualRevisionRootPage()
                            .getNamePageReference().getPage());

            // Return prepared revision root nodePageReference.
            return revisionRootPage;
        }
    }

    protected PageReference prepareLeafOfTree(
            final PageReference startReference, final long key)
            throws TreetankIOException {

        // Initial state pointing to the indirect nodePageReference of level 0.

        PageReference reference = startReference;
        int offset = 0;
        long levelKey = key;

        // Iterate through all levels.
        for (int level = 0, height = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; level < height; level++) {
            offset = (int) (levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level]);
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
     * @param reference
     *            Reference to dereference.
     * @param nodePageKey
     *            Key of node page.
     * @return Dereferenced page.
     */
    private final NodePageContainer dereferenceNodePageForModification(
            final long nodePageKey) throws TreetankIOException {
        final NodePage[] revs = getSnapshotPages(nodePageKey);
        final ERevisioning revision = ERevisioning
                .valueOf(getDatabaseConfiguration().getProps().getProperty(
                        EDatabaseSetting.REVISION_TYPE.name()));
        final int mileStoneRevision = Integer
                .parseInt(getDatabaseConfiguration().getProps().getProperty(
                        EDatabaseSetting.REVISION_TO_RESTORE.name()));

        return revision.combinePagesForModification(revs, mileStoneRevision);
    }

    /**
     * Current reference to actual rev-root page
     * 
     * @return the current revision root page
     */
    protected RevisionRootPage getActualRevisionRootPage() {
        return this.mNewRoot;
    }

    /**
     * Updating a container in this transaction state
     * 
     * @param cont
     *            to be updated
     */
    protected void updateDateContainer(final NodePageContainer cont) {
        synchronized (log) {
            // TODO implement for MultiWriteTrans
            // Refer to issue #203
        }
    }

}

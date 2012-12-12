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

import static org.treetank.access.PageReadTrx.nodePageKey;
import static org.treetank.access.PageReadTrx.nodePageOffset;

import java.io.File;

import javax.xml.namespace.QName;

import org.treetank.api.INode;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.cache.ICache;
import org.treetank.cache.NodePageContainer;
import org.treetank.cache.TransactionLogCache;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackendWriter;
import org.treetank.page.IConstants;
import org.treetank.page.IndirectPage;
import org.treetank.page.NamePage;
import org.treetank.page.NodePage;
import org.treetank.page.NodePage.DeletedNode;
import org.treetank.page.interfaces.IPage;
import org.treetank.page.PageReference;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;
import org.treetank.revisioning.IRevisioning;
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
    private final ICache mLog;

    /** Last references to the Nodepage, needed for pre/postcondition check. */
    private NodePageContainer mNodePageCon;

    /** Last reference to the actual revRoot. */
    private final RevisionRootPage mNewRoot;

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
        final long pRepresentRev, final long pStoreRev) throws TTException {
        mDelegate = new PageReadTrx(pSession, pUberPage, pRepresentRev, pWriter);
        mNewRoot = preparePreviousRevisionRootPage(pRepresentRev, pStoreRev);
        mLog =
            new TransactionLogCache(new File(pSession.getConfig().mProperties
                .getProperty(org.treetank.access.conf.ContructorProps.STORAGEPATH)), pStoreRev, pSession
                .getConfig().mNodeFac);
        mPageWriter = pWriter;

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
    protected INode prepareNodeForModification(final long pNodeKey) throws TTException {
        if (pNodeKey < 0) {
            throw new IllegalArgumentException("paramNodeKey must be >= 0!");
        }
        if (mNodePageCon != null) {
            throw new IllegalStateException(
                "Another node page container is currently in the cache for updates!");
        }

        final long nodePageKey = nodePageKey(pNodeKey);
        final int nodePageOffset = nodePageOffset(pNodeKey);
        prepareNodePage(nodePageKey);

        INode node = mNodePageCon.getModified().getNode(nodePageOffset);
        if (node == null) {
            final INode oldNode = mNodePageCon.getComplete().getNode(nodePageOffset);
            if (oldNode == null) {
                throw new TTIOException("Cannot retrieve node from cache");
            }
            node = oldNode;
            mNodePageCon.getModified().setNode(nodePageOffset, node);
        }
        return node;
    }

    /**
     * Finishing the node modification. That is storing the node including the
     * page in the cache.
     * 
     * @param pNode
     *            the node to be modified
     */
    protected void finishNodeModification(final INode pNode) {
        final long nodePageKey = nodePageKey(pNode.getNodeKey());
        if (mNodePageCon == null || pNode == null || mLog.get(nodePageKey) == null) {
            throw new IllegalStateException();
        }

        mLog.put(nodePageKey, mNodePageCon);

        this.mNodePageCon = null;

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
        final long nodePageKey = nodePageKey(nodeKey);
        final int nodePageOffset = nodePageOffset(nodeKey);
        prepareNodePage(nodePageKey);
        final NodePage page = mNodePageCon.getModified();
        page.setNode(nodePageOffset, pNode);
        finishNodeModification(pNode);
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
    protected void removeNode(final INode pNode) throws TTException {
        assert pNode != null;
        final long nodePageKey = nodePageKey(pNode.getNodeKey());
        prepareNodePage(nodePageKey);
        final INode delNode = new DeletedNode(pNode.getNodeKey());
        mNodePageCon.getModified().setNode(nodePageOffset(pNode.getNodeKey()), delNode);
        mNodePageCon.getComplete().setNode(nodePageOffset(pNode.getNodeKey()), delNode);
        finishNodeModification(pNode);
    }

    /**
     * {@inheritDoc}
     */
    public INode getNode(final long pNodeKey) throws TTException {

        // Calculate page and node part for given nodeKey.
        final long nodePageKey = nodePageKey(pNodeKey);
        final int nodePageOffset = nodePageOffset(pNodeKey);

        final NodePageContainer pageCont = mLog.get(nodePageKey);
        if (pageCont == null) {
            return mDelegate.getNode(pNodeKey);
        } else if (pageCont.getModified().getNode(nodePageOffset) == null) {
            final INode item = pageCont.getComplete().getNode(nodePageOffset);
            return mDelegate.checkItemIfDeleted(item);

        } else {
            final INode item = pageCont.getModified().getNode(nodePageOffset);
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
        final NamePage currentNamePage = (NamePage)mNewRoot.getNamePageReference().getPage();
        String returnVal;
        // if currentNamePage == null -> state was commited and no
        // prepareNodepage was invoked yet
        if (currentNamePage == null || currentNamePage.getName(pNameKey) == null) {
            returnVal = mDelegate.getName(pNameKey);
        } else {
            returnVal = currentNamePage.getName(pNameKey);
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
    protected int createNameKey(final String pName) throws TTIOException {
        final String string = (pName == null ? "" : pName);
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
     * @param pRef
     *            to be commited
     * @throws TTException
     *             if the write fails
     */
    public void commit(final PageReference pRef) throws TTException {
        IPage page = null;

        // if reference is not null, get one from the persistent storage.
        if (pRef != null) {
            // first, try to get one from the log
            final NodePageContainer cont = mLog.get(pRef.getNodePageKey());
            if (cont != null) {
                page = cont.getModified();
            }
            // if none is in the log, test if one is instantiated, if so, get
            // the one flexible from the
            // reference
            if (page == null) {
                page = pRef.getPage();
                if (page == null) {
                    return;
                }
            }

            pRef.setPage(page);
            // Recursively commit indirectely referenced pages and then
            // write self.
            page.commit(this);
            mPageWriter.write(pRef);
            pRef.setPage(null);
        }
    }

    public boolean commit() throws TTException {

        final PageReference uberPageReference = new PageReference();
        final UberPage uberPage = mDelegate.getUberPage();
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
        // AbsPage page = mLog.get(currentRef.getNodePageKey()).getModified();
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

        // Remember succesfully committed uber page in session state.
        // TODO This is one of the dirtiest hacks I ever did! Sorry Future-ME!
        ((Session)mDelegate.mSession).setLastCommittedUberPage(uberPage);
        return true;

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

    protected IndirectPage prepareIndirectPage(final PageReference pRef) throws TTException {

        IndirectPage page = (IndirectPage)pRef.getPage();
        if (page == null) {
            page = new IndirectPage();
            if (pRef.getKey() != IConstants.NULL_ID) {
                IndirectPage formerIndirect = mDelegate.dereferenceIndirectPage(pRef);
                for (int i = 0; i < formerIndirect.getReferences().length; i++) {
                    page.getReferences()[i] = formerIndirect.getReferences()[i];
                }
            }
            pRef.setPage(page);
        }
        return page;
    }

    protected NodePageContainer prepareNodePage(final long pPageKey) throws TTException {

        // Last level points to node nodePageReference.
        NodePageContainer cont = mLog.get(pPageKey);
        if (cont == null) {

            // Indirect reference.
            final PageReference reference = prepareLeafOfTree(mNewRoot.getIndirectPageReference(), pPageKey);
            NodePage page = (NodePage)reference.getPage();

            if (page == null) {
                if (reference.getKey() == IConstants.NULL_ID) {
                    cont = new NodePageContainer(new NodePage(pPageKey), new NodePage(pPageKey));
                } else {
                    cont = dereferenceNodePageForModification(pPageKey);
                }
            } else {
                cont = new NodePageContainer(page, new NodePage(page.getNodePageKey()));
            }

            reference.setNodePageKey(pPageKey);
            mLog.put(pPageKey, cont);
        }
        mNodePageCon = cont;
        return cont;
    }

    private RevisionRootPage preparePreviousRevisionRootPage(final long pRev, final long pRepresentRev)
        throws TTException {
        // Prepare revision root nodePageReference.
        final RevisionRootPage previousRevRoot = mDelegate.loadRevRoot(pRev);

        final RevisionRootPage revisionRootPage =
            new RevisionRootPage(pRepresentRev + 1, previousRevRoot.getMaxNodeKey());
        for (int i = 0; i < previousRevRoot.getReferences().length; i++) {
            revisionRootPage.getReferences()[i] = previousRevRoot.getReferences()[i];
        }

        // Prepare indirect tree to hold reference to prepared revision root
        // nodePageReference.
        final PageReference revisionRootPageReference =
            prepareLeafOfTree(mDelegate.getUberPage().getReferences()[0], mDelegate.getUberPage()
                .getRevisionNumber() - 1);

        // Link the prepared revision root nodePageReference with the
        // prepared indirect tree.
        revisionRootPageReference.setPage(revisionRootPage);

        revisionRootPage.getNamePageReference().setPage(
            (NamePage)mDelegate.getActualRevisionRootPage().getNamePageReference().getPage());

        // Return prepared revision root nodePageReference.
        return revisionRootPage;
    }

    protected PageReference prepareLeafOfTree(final PageReference pStarRef, final long pKey)
        throws TTException {

        // Initial state pointing to the indirect nodePageReference of level 0.

        PageReference reference = pStarRef;
        int offset = 0;
        long levelKey = pKey;

        // Iterate through all levels.
        for (int level = 0, height = IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length; level < height; level++) {
            offset = (int)(levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level]);
            levelKey -= offset << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[level];
            final IndirectPage page = prepareIndirectPage(reference);
            reference = page.getReferences()[offset];

        }

        // Return reference to leaf of indirect tree.
        return reference;
    }

    /**
     * Dereference node page reference.
     * 
     * @param pNodeKey
     *            key of node page
     * @return dereferenced page
     * @throws TTIOException
     *             if something happened in the node
     */
    private NodePageContainer dereferenceNodePageForModification(final long pNodeKey) throws TTException {
        final NodePage[] revs = mDelegate.getSnapshotPages(pNodeKey);
        final IRevisioning revision = mDelegate.mSession.getConfig().mRevision;

        return revision.combinePagesForModification(revs);
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
    public UberPage getUberPage() {
        return mDelegate.getUberPage();
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
        builder.append(", mNodePageCon=");
        builder.append(mNodePageCon);
        builder.append(", mNewRoot=");
        builder.append(mNewRoot);
        builder.append(", mDelegate=");
        builder.append(mDelegate);
        builder.append("]");
        return builder.toString();
    }

}

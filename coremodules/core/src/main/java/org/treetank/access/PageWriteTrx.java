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

import javax.xml.namespace.QName;

import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.INode;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.cache.ICache;
import org.treetank.cache.NodePageContainer;
import org.treetank.cache.TransactionLogCache;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IWriter;
import org.treetank.page.IConstants;
import org.treetank.page.IPage;
import org.treetank.page.IndirectPage;
import org.treetank.page.NamePage;
import org.treetank.page.NodePage;
import org.treetank.page.NodePage.DeletedNode;
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
    private final IWriter mPageWriter;

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
     * @param paramSessionConfiguration
     *            {@link SessionConfiguration} reference
     * @param paramUberPage
     *            root of revision
     * @param paramWriter
     *            writer where this transaction should write to
     * @param paramParamId
     *            parameter ID
     * @param paramRepresentRev
     *            revision represent
     * @param paramStoreRev
     *            revision store
     * @throws TTIOException
     *             if IO Error
     */
    protected PageWriteTrx(final ISession pSession, final UberPage paramUberPage, final IWriter paramWriter,
        final long paramRepresentRev, final long paramStoreRev) throws TTException {
        mDelegate = new PageReadTrx(pSession, paramUberPage, paramRepresentRev, paramWriter);
        mNewRoot = preparePreviousRevisionRootPage(paramRepresentRev, paramStoreRev);
        mLog = new TransactionLogCache(pSession.getConfig().mPath, paramStoreRev);
        mPageWriter = paramWriter;

    }

    /**
     * Prepare a node for modification. This is getting the node from the
     * (persistence) layer, storing the page in the cache and setting up the
     * node for upcoming modification. Note that this only occurs for {@link INode}s.
     * 
     * @param paramNodeKey
     *            key of the node to be modified
     * @return an {@link INode} instance
     * @throws TTIOException
     *             if IO Error
     */
    protected INode prepareNodeForModification(final long paramNodeKey) throws TTException {
        if (paramNodeKey < 0) {
            throw new IllegalArgumentException("paramNodeKey must be >= 0!");
        }
        if (mNodePageCon != null) {
            throw new IllegalStateException(
                "Another node page container is currently in the cache for updates!");
        }

        final long nodePageKey = nodePageKey(paramNodeKey);
        final int nodePageOffset = nodePageOffset(paramNodeKey);
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
     * @param paramNode
     *            the node to be modified
     */
    protected void finishNodeModification(final INode paramNode) {
        final long nodePageKey = nodePageKey(paramNode.getNodeKey());
        if (mNodePageCon == null || paramNode == null || mLog.get(nodePageKey) == null) {
            throw new IllegalStateException();
        }

        mLog.put(nodePageKey, mNodePageCon);

        this.mNodePageCon = null;

    }

    /**
     * Create fresh node and prepare node nodePageReference for modifications
     * (COW).
     * 
     * @param paramNode
     *            node to add
     * @return Unmodified node from parameter for convenience.
     * @throws TTIOException
     *             if IO Error
     */
    public <T extends INode> T createNode(final T paramNode) throws TTException {
        // Allocate node key and increment node count.
        mNewRoot.incrementMaxNodeKey();
        final long nodeKey = mNewRoot.getMaxNodeKey();
        final long nodePageKey = nodePageKey(nodeKey);
        final int nodePageOffset = nodePageOffset(nodeKey);
        prepareNodePage(nodePageKey);
        final NodePage page = mNodePageCon.getModified();
        page.setNode(nodePageOffset, paramNode);
        finishNodeModification(paramNode);
        return paramNode;
    }

    /**
     * Removing a node from the storage.
     * 
     * @param paramNode
     *            {@link INode} to be removed
     * @throws TTIOException
     *             if the removal fails
     */
    protected void removeNode(final INode paramNode) throws TTException {
        assert paramNode != null;
        final long nodePageKey = nodePageKey(paramNode.getNodeKey());
        prepareNodePage(nodePageKey);
        final INode delNode = new DeletedNode(paramNode.getNodeKey());
        mNodePageCon.getModified().setNode(nodePageOffset(paramNode.getNodeKey()), delNode);
        mNodePageCon.getComplete().setNode(nodePageOffset(paramNode.getNodeKey()), delNode);
        finishNodeModification(paramNode);
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
     * @param mNameKey
     *            for the term searched
     * @return the name
     */
    public String getName(final int mNameKey) {
        final NamePage currentNamePage = (NamePage)mNewRoot.getNamePageReference().getPage();
        String returnVal;
        // if currentNamePage == null -> state was commited and no
        // prepareNodepage was invoked yet
        if (currentNamePage == null || currentNamePage.getName(mNameKey) == null) {
            returnVal = mDelegate.getName(mNameKey);
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
     * @throws TTException
     *             if the write fails
     */
    public void commit(final PageReference reference) throws TTException {
        IPage page = null;

        // if reference is not null, get one from the persistent storage.
        if (reference != null) {
            // first, try to get one from the log
            final NodePageContainer cont = mLog.get(reference.getNodePageKey());
            if (cont != null) {
                page = cont.getModified();
            }
            // if none is in the log, test if one is instantiated, if so, get
            // the one flexible from the
            // reference
            if (page == null) {
                page = reference.getPage();
                if (page == null) {
                    return;
                }
            }

            reference.setPage(page);
            // Recursively commit indirectely referenced pages and then
            // write self.
            page.commit(this);
            mPageWriter.write(reference);
            reference.setPage(null);
        }
    }

    protected UberPage commit() throws TTException {

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
        return uberPage;

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

    protected IndirectPage prepareIndirectPage(final PageReference paramReference) throws TTException {

        IndirectPage page = (IndirectPage)paramReference.getPage();
        if (page == null) {
            if (paramReference.getKey() == IConstants.NULL_ID) {
                page = new IndirectPage(mDelegate.getUberPage().getRevision());
            } else {
                page =
                    new IndirectPage((IndirectPage)mDelegate.dereferenceIndirectPage(paramReference),
                        mNewRoot.getRevision() + 1);

            }
            paramReference.setPage(page);
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
                    cont = new NodePageContainer(new NodePage(pPageKey, IConstants.UBP_ROOT_REVISION_NUMBER));
                } else {
                    cont = dereferenceNodePageForModification(pPageKey);
                }
            } else {
                cont = new NodePageContainer(page);
            }

            reference.setNodePageKey(pPageKey);
            mLog.put(pPageKey, cont);
        }
        mNodePageCon = cont;
        return cont;
    }

    private RevisionRootPage preparePreviousRevisionRootPage(final long mBaseRevision,
        final long representRevision) throws TTException {

        if (mDelegate.getUberPage().isBootstrap()) {
            return mDelegate.loadRevRoot(mBaseRevision);
        } else {

            // Prepare revision root nodePageReference.
            final RevisionRootPage revisionRootPage =
                new RevisionRootPage(mDelegate.loadRevRoot(mBaseRevision), representRevision + 1);

            // Prepare indirect tree to hold reference to prepared revision root
            // nodePageReference.
            final PageReference revisionRootPageReference =
                prepareLeafOfTree(mDelegate.getUberPage().getIndirectPageReference(), mDelegate.getUberPage()
                    .getRevisionNumber());

            // Link the prepared revision root nodePageReference with the
            // prepared indirect tree.
            revisionRootPageReference.setPage(revisionRootPage);

            revisionRootPage.getNamePageReference().setPage(
                (NamePage)mDelegate.getActualRevisionRootPage().getNamePageReference().getPage());

            // Return prepared revision root nodePageReference.
            return revisionRootPage;
        }
    }

    protected PageReference prepareLeafOfTree(final PageReference mStartReference, final long mKey)
        throws TTException {

        // Initial state pointing to the indirect nodePageReference of level 0.

        PageReference reference = mStartReference;
        int offset = 0;
        long levelKey = mKey;

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
     * @param paramNodePageKey
     *            key of node page
     * @return dereferenced page
     * @throws TTIOException
     *             if something happened in the node
     */
    private NodePageContainer dereferenceNodePageForModification(final long paramNodePageKey)
        throws TTException {
        final NodePage[] revs = mDelegate.getSnapshotPages(paramNodePageKey);
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
     * @param paramQname
     *            the {@link QName} of an element
     * @return a string with [prefix:]localname
     */
    public static String buildName(final QName paramQname) {
        if (paramQname == null) {
            throw new NullPointerException("mQName must not be null!");
        }
        String name;
        if (paramQname.getPrefix().isEmpty()) {
            name = paramQname.getLocalPart();
        } else {
            name =
                new StringBuilder(paramQname.getPrefix()).append(":").append(paramQname.getLocalPart())
                    .toString();
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawName(int pKey) {
        return mDelegate.getRawName(pKey);
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

}

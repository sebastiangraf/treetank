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

package com.treetank.session;

import java.util.Stack;

import com.treetank.api.IItem;
import com.treetank.cache.ICache;
import com.treetank.cache.TransactionLogCache;
import com.treetank.exception.TreetankIOException;
import com.treetank.io.IWriter;
import com.treetank.io.StorageProperties;
import com.treetank.node.AbstractNode;
import com.treetank.node.AttributeNode;
import com.treetank.node.ElementNode;
import com.treetank.node.NamespaceNode;
import com.treetank.node.TextNode;
import com.treetank.page.AbstractPage;
import com.treetank.page.IndirectPage;
import com.treetank.page.NamePage;
import com.treetank.page.NodePage;
import com.treetank.page.PageReference;
import com.treetank.page.RevisionRootPage;
import com.treetank.page.UberPage;
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
    private IWriter mPageWriter;

    /**
     * Cache to store the changes in this writetransaction.
     */
    private final ICache log;

    /**
     * Last references to the Nodepage-reference
     */
    private PageReference nodePageReference;

    /**
     * Last reference to the nodepage
     */
    private NodePage nodePage;

    /**
     * last reference to the node
     */
    private AbstractNode node;

    /**
     * Standard constructor.
     * 
     * @param sessionConfiguration
     *            Configuration of session.
     * @param pageCache
     *            Shared nodePageReference cache.
     * @param uberPage
     *            Root of revision.
     * @param reader
     *            Reader of transaction
     * @param writer
     *            Writer where this transaction should write to
     */
    protected WriteTransactionState(
            final SessionConfiguration sessionConfiguration,
            final UberPage uberPage, final IWriter writer)
            throws TreetankIOException {
        super(sessionConfiguration, uberPage, uberPage
                .getLastCommittedRevisionNumber(), new ItemList(), writer);
        log = new TransactionLogCache(sessionConfiguration);
        mPageWriter = writer;
        setRevisionRootPage(prepareRevisionRootPage());
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
    protected final AbstractNode prepareNodeForModification(final long nodeKey) {
        if (nodePageReference != null || nodePage != null || node != null) {
            throw new IllegalStateException();
        }
        final NodePage page = prepareNodePage(nodePageKey(nodeKey));

        final AbstractNode node = page.getNode(nodePageOffset(nodeKey));
        this.node = node;
        return node;
    }

    /**
     * Finishing the node modification. That is storing the node including the
     * page in the cache.
     * 
     * @param node
     *            the node to be modified.
     */
    protected final void finishNodeModification(final AbstractNode node) {
        if (nodePageReference == null || nodePage == null || node == null) {
            throw new IllegalStateException();
        }
        if (nodePage instanceof NodePage && !nodePageReference.isInstantiated()) {
            log.put(nodePageReference.getNodePageKey(), nodePage);
        }
        this.nodePage = null;
        this.nodePageReference = null;
        this.node = null;

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
    protected final <N extends AbstractNode> N createNode(final N node) {
        // Allocate node key and increment node count.
        getRevisionRootPage().incrementNodeCountAndMaxNodeKey();
        // Prepare node nodePageReference (COW).
        final long nodePageKey = nodePageKey(getRevisionRootPage()
                .getMaxNodeKey());
        final NodePage page = prepareNodePage(nodePageKey);
        page.setNode(nodePageOffset(getRevisionRootPage().getMaxNodeKey()),
                node);
        if (nodePage instanceof NodePage && !nodePageReference.isInstantiated()) {
            log.put(nodePageKey, nodePage);
        }
        nodePage = null;
        nodePageReference = null;

        return node;
    }

    protected final ElementNode createElementNode(final long parentKey,
            final long firstChildKey, final long leftSiblingKey,
            final long rightSiblingKey, final int nameKey, final int uriKey,
            final int type) {
        return createNode(new ElementNode(
                getRevisionRootPage().getMaxNodeKey() + 1, parentKey,
                firstChildKey, leftSiblingKey, rightSiblingKey, nameKey,
                uriKey, type));
    }

    protected final AttributeNode createAttributeNode(final long parentKey,
            final int nameKey, final int uriKey, final int type,
            final byte[] value) {
        return createNode(new AttributeNode(getRevisionRootPage()
                .getMaxNodeKey() + 1, parentKey, nameKey, uriKey, type, value));
    }

    protected final NamespaceNode createNamespaceNode(final long parentKey,
            final int uriKey, final int prefixKey) {
        return createNode(new NamespaceNode(getRevisionRootPage()
                .getMaxNodeKey() + 1, parentKey, uriKey, prefixKey));
    }

    protected final TextNode createTextNode(final long parentKey,
            final long leftSiblingKey, final long rightSiblingKey,
            final int valueType, final byte[] value) {
        return createNode(new TextNode(
                getRevisionRootPage().getMaxNodeKey() + 1, parentKey,
                leftSiblingKey, rightSiblingKey, valueType, value));
    }

    /**
     * {@inheritDoc}
     */
    protected final void removeNode(final AbstractNode node) {
        getRevisionRootPage().decrementNodeCount();
        final NodePage page = prepareNodePage(nodePageKey(node.getNodeKey()));
        page.setNode(nodePageOffset(node.getNodeKey()), null);
        if (nodePage instanceof NodePage && !nodePageReference.isInstantiated()) {
            log.put(nodePageReference.getNodePageKey(), nodePage);
        }
        nodePage = null;
        nodePageReference = null;
    }

    /**
     * {@inheritDoc}
     */
    protected IItem getNode(final long nodeKey) {

        // Calculate page and node part for given nodeKey.
        final long nodePageKey = nodePageKey(nodeKey);
        final int nodePageOffset = nodePageOffset(nodeKey);

        // Fetch node page if it is not yet in the state cache.
        final PageReference reference = dereferenceLeafOfTree(
                getRevisionRootPage().getIndirectPageReference(), nodePageKey);
        NodePage page = (NodePage) log.get(nodePageKey);
        if (page == null) {
            page = (NodePage) dereferenceNodePage(reference, nodePageKey);
        }

        final IItem returnVal = page.getNode(nodePageOffset);
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    protected final int createNameKey(final String name) {
        final String string = (name == null ? "" : name);
        final int nameKey = NamePageHash.generateHashForString(string);

        final PageReference namePageReference = getRevisionRootPage()
                .getNamePageReference();
        final NamePage namePage = prepareNamePage(namePageReference);

        if (namePage.getName(nameKey) == null) {
            namePage.setName(nameKey, string);
        }
        return nameKey;
    }

    /**
     * {@inheritDoc}
     */
    public final void commit(final PageReference reference) {
        AbstractPage page;

        if (reference != null) {
            page = log.get(reference.getNodePageKey());
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

            try {
                mPageWriter.write(reference);
            } catch (TreetankIOException e) {

                throw new RuntimeException(e);
            }

            reference.setPage(null);
        }
    }

    protected final UberPage commit(
            final SessionConfiguration sessionConfiguration)
            throws TreetankIOException {

        final PageReference uberPageReference = new PageReference();
        final UberPage uberPage = getUberPage();
        uberPageReference.setPage(uberPage);

        if (uberPage.isBootstrap()) {
            mPageWriter.setProps(new StorageProperties(
                    IConstants.LAST_VERSION_MAJOR,
                    IConstants.LAST_VERSION_MINOR, sessionConfiguration
                            .isChecksummed(), sessionConfiguration
                            .isEncrypted()));
        }

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

        // mPageWriter.close();

        return uberPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void close() {
        log.clear();
        try {
            mPageWriter.close();
        } catch (final TreetankIOException exc) {
            throw new RuntimeException(exc);

        }
        mPageWriter = null;
        // super.close();
    }

    protected final IndirectPage prepareIndirectPage(
            final PageReference reference) {

        IndirectPage page = (IndirectPage) reference.getPage();
        if (!reference.isInstantiated()) {
            if (reference.isCommitted()) {
                page = new IndirectPage(
                        (IndirectPage) dereferenceIndirectPage(reference));
            } else {
                page = new IndirectPage();
            }
            reference.setPage(page);
        } else {
            page = (IndirectPage) reference.getPage();
        }
        return page;
    }

    protected final NodePage prepareNodePage(final long nodePageKey) {

        // Indirect reference.
        PageReference reference = prepareLeafOfTree(getRevisionRootPage()
                .getIndirectPageReference(), nodePageKey);

        // Last level points to node nodePageReference.
        NodePage page = (NodePage) log.get(reference.getNodePageKey());
        if (page == null) {
            if (!reference.isInstantiated()) {
                if (reference.isCommitted()) {
                    page = new NodePage(dereferenceNodePage(reference,
                            nodePageKey));
                    reference.setNodePageKey(nodePageKey);
                    log.put(nodePageKey, page);
                } else {
                    page = new NodePage(nodePageKey);
                    reference.setNodePageKey(nodePageKey);
                    log.put(nodePageKey, page);
                }

            } else {
                page = (NodePage) reference.getPage();
                reference.setNodePageKey(nodePageKey);
                log.put(nodePageKey, page);
                reference.setPage(null);
            }
        }
        this.nodePage = page;
        this.nodePageReference = reference;
        return page;
    }

    protected final RevisionRootPage prepareRevisionRootPage() {

        if (getUberPage().isBootstrap()) {
            return getRevisionRootPage();
        }

        // Prepare revision root nodePageReference.
        final RevisionRootPage revisionRootPage = new RevisionRootPage(
                getRevisionRootPage(getUberPage()
                        .getLastCommittedRevisionNumber()));

        // Prepare indirect tree to hold reference to prepared revision root
        // nodePageReference.
        final PageReference revisionRootPageReference = prepareLeafOfTree(
                getUberPage().getIndirectPageReference(), getUberPage()
                        .getRevisionNumber());

        // Link the prepared revision root nodePageReference with the prepared
        // indirect tree.
        revisionRootPageReference.setPage(revisionRootPage);

        // Return prepared revision root nodePageReference.
        return revisionRootPage;
    }

    protected final PageReference prepareLeafOfTree(
            final PageReference startReference, final long key) {

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

    protected final NamePage prepareNamePage(final PageReference reference) {

        NamePage page = (NamePage) reference.getPage();

        if (!reference.isInstantiated()) {
            if (reference.isCommitted()) {
                page = new NamePage(getNamePage());
                reference.setPage(page);
            } else {
                page = new NamePage();
                reference.setPage(page);
            }
        }

        return page;
    }

}

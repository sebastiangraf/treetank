/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.saxon.wrapper;

import java.util.Collections;
import java.util.Iterator;

import com.treetank.api.IDatabase;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;

/**
 * <h1>DocumentWrapper</h1>
 * 
 * <p>
 * Wraps a Treetank document and represents a document node. Therefore it implements Saxon's DocumentInfo core
 * interface and also represents a Node in Saxon's internal node implementation. Thus it extends
 * <tt>NodeWrapper</tt>.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class DocumentWrapper extends NodeWrapper implements DocumentInfo {

    /** Base URI of the document. */
    protected transient String mBaseURI;

    /** Saxon configuration. */
    protected transient Configuration mConfig;

    /** Unique document number. */
    protected static int documentNumber;

    /**
     * Wrap a Treetank document.
     * 
     * @param session
     *            Treetank session.
     * @param config
     *            Configuration used.
     * @param baseURI
     *            BaseURI of the document (PATH).
     */
    public DocumentWrapper(final IDatabase database, final Configuration config) {
        super(database, 0);
        nodeKind = ENodes.ROOT_KIND;
        mBaseURI = database.getFile().getAbsolutePath();
        mDocWrapper = this;
        setConfiguration(config);
    }

    /**
     * Wrap a node in the Treetank document.
     * 
     * @return The wrapped Treetank transaction in form of a NodeInfo object.
     */
    public NodeInfo wrap() {
        return makeWrapper(this, 0);
    }

    /**
     * Wrap a node in the Treetank document.
     * 
     * @param nodeKey
     *            Node key to start wrapping.
     * @return The wrapping NodeWrapper object.
     */
    public NodeInfo wrap(final long nodeKey) {
        return makeWrapper(this, nodeKey);
    }

    @Override
    public String[] getUnparsedEntity(final String name) {
        throw new UnsupportedOperationException("Currently not supported by Treetank!");
    }

    /**
     * Get the unparsed entity with a given name.
     * 
     * @param name
     *            The name of the entity.
     * @return null: TreeTank does not provide access to unparsed entities.
     */
    @SuppressWarnings("unchecked")
    public Iterator<String> getUnparsedEntityNames() {
        return (Iterator<String>)Collections.EMPTY_LIST.iterator();
    }

    /**
     * {@inheritDoc}
     * 
     * No check if the attribute is unique among all nodes and on the element.
     */
    public NodeInfo selectID(final String ID, final boolean getParent) {
        final AbsAxis axis = new DescendantAxis(mRTX, true);
        while (axis.hasNext()) {
            if (mRTX.getNode().getKind() == ENodes.ELEMENT_KIND) {
                final int attCount = ((ElementNode)mRTX.getNode()).getAttributeCount();

                if (attCount > 0) {
                    final long nodeKey = mRTX.getNode().getNodeKey();

                    for (int index = 0; index < attCount; index++) {
                        mRTX.moveToAttribute(index);

                        if ("xml:id".equalsIgnoreCase(mRTX.getQNameOfCurrentNode().getLocalPart())
                            && ID.equals(mRTX.getValueOfCurrentNode())) {
                            if (getParent) {
                                mRTX.moveToParent();
                                return wrap(mRTX.getNode().getNodeKey());
                            } else {
                                return wrap(mRTX.getNode().getNodeKey());
                            }
                        }

                        mRTX.moveTo(nodeKey);
                    }
                }
            }
            axis.next();
        }

        return null;
    }

    /**
     * Get the name pool used for the names in this document.
     * 
     * @return namepool
     */
    @Override
    public NamePool getNamePool() {
        return mConfig.getNamePool();
    }

    /**
     * Set the configuration (containing the name pool used for all names in
     * this document). Calling this method allocates a unique number to the
     * document (unique within the Configuration); this will form the basis for
     * testing node identity.
     * 
     * @param config
     *            The configuration.
     */
    public void setConfiguration(final Configuration config) {
        mConfig = config;
        documentNumber = config.getDocumentNumberAllocator().allocateDocumentNumber();
    }

    /**
     * Get the configuration previously set using setConfiguration (or the
     * default configuraton allocated automatically).
     * 
     * @return configuration
     */
    @Override
    public Configuration getConfiguration() {
        return mConfig;
    }

    /**
     * Return BaseURI of the current document.
     * 
     * @return BaseURI.
     */
    @Override
    public String getBaseURI() {
        return mBaseURI;
    }

    /**
     * Set the baseURI of the current document.
     * 
     * @param baseURI
     *            Usually the absoulte path of the document.
     */
    protected void setBaseURI(final String baseURI) {
        mBaseURI = baseURI;
    }
}

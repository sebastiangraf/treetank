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

package org.treetank.saxon.wrapper;

import java.util.Collections;
import java.util.Iterator;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;

import org.treetank.api.IDatabase;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;

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
    protected static long documentNumber;

    /**
     * Wrap a Treetank document.
     * 
     * @param paramDatabase
     *            Treetank database.
     * @param paramConfig
     *            Configuration used.
     */
    public DocumentWrapper(final IDatabase paramDatabase, final Configuration paramConfig) {
        super(paramDatabase, 0);
        nodeKind = ENodes.ROOT_KIND;
        mBaseURI = paramDatabase.getFile().getAbsolutePath();
        mDocWrapper = this;
        setConfiguration(paramConfig);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getUnparsedEntity(final String name) {
        throw new UnsupportedOperationException("Currently not supported by Treetank!");
    }

    /**
     * Get the unparsed entity with a given name.
     * @return null: TreeTank does not provide access to unparsed entities.
     */
    @SuppressWarnings("unchecked")
    public Iterator<String> getUnparsedEntityNames() {
        return (Iterator<String>)Collections.EMPTY_LIST.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() {
        return mConfig;
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getUserData(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserData(String arg0, Object arg1) {
        // TODO Auto-generated method stub

    }
}

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
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.api.INodeReadTransaction;
import org.treetank.api.ISession;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ENode;
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
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class DocumentWrapper implements DocumentInfo {

    /**
     * Log wrapper for better output.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(DocumentWrapper.class);

    /** Treetank database. */
    protected transient final ISession mSession;

    /** Base URI of the document. */
    protected transient String mBaseURI;

    /** Saxon configuration. */
    protected transient Configuration mConfig;

    /** Unique document number. */
    protected transient long documentNumber;

    /**
     * Instance to {@link NodeWrapper}-implementation
     */
    private final NodeWrapper mNodeWrapper;

    /**
     * Wrap a Treetank document.
     * 
     * @param paramDatabase
     *            Treetank database.
     * @param paramConfig
     *            Configuration used.
     * @throws AbsTTException
     */
    public DocumentWrapper(final ISession paramSession, final Configuration paramConfig)
        throws AbsTTException {
        this.mSession = paramSession;
        mBaseURI = paramSession.toString();
        setConfiguration(paramConfig);
        mNodeWrapper = new NodeWrapper(this, 0);
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
     * 
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
        try {
            final INodeReadTransaction rtx = mSession.beginReadTransaction();
            final AbsAxis axis = new DescendantAxis(rtx, true);
            while (axis.hasNext()) {
                if (rtx.getNode().getKind() == ENode.ELEMENT_KIND) {
                    final int attCount = ((ElementNode)rtx.getNode()).getAttributeCount();

                    if (attCount > 0) {
                        final long nodeKey = rtx.getNode().getNodeKey();

                        for (int index = 0; index < attCount; index++) {
                            rtx.moveToAttribute(index);

                            if ("xml:id".equalsIgnoreCase(rtx.getQNameOfCurrentNode().getLocalPart())
                                && ID.equals(rtx.getValueOfCurrentNode())) {
                                if (getParent) {
                                    rtx.moveTo(rtx.getNode().getParentKey());
                                }
                                return new NodeWrapper(this, rtx.getNode().getNodeKey());
                            }
                            rtx.moveTo(nodeKey);
                        }
                    }
                }
                axis.next();
            }
            rtx.close();
        } catch (final AbsTTException exc) {
            LOGGER.error(exc.toString());
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
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserData(String arg0, Object arg1) {

    }

    @Override
    public Value atomize() throws XPathException {
        return getNodeWrapper().atomize();
    }

    @Override
    public int compareOrder(NodeInfo arg0) {
        return getNodeWrapper().compareOrder(arg0);
    }

    @Override
    public void copy(Receiver arg0, int arg1, int arg2) throws XPathException {
        getNodeWrapper().copy(arg0, arg1, arg2);

    }

    @Override
    public void generateId(FastStringBuffer arg0) {
        getNodeWrapper().generateId(arg0);

    }

    @Override
    public String getAttributeValue(int arg0) {
        return getNodeWrapper().getAttributeValue(arg0);
    }

    @Override
    public int getColumnNumber() {
        return getNodeWrapper().getColumnNumber();
    }

    @Override
    public int[] getDeclaredNamespaces(int[] arg0) {
        return getNodeWrapper().getDeclaredNamespaces(arg0);
    }

    @Override
    public String getDisplayName() {
        return getNodeWrapper().getDisplayName();
    }

    @Override
    public long getDocumentNumber() {
        return getNodeWrapper().getDocumentNumber();
    }

    @Override
    public DocumentInfo getDocumentRoot() {
        return getNodeWrapper().getDocumentRoot();
    }

    @Override
    public int getFingerprint() {
        return getNodeWrapper().getFingerprint();
    }

    @Override
    public int getLineNumber() {
        return getNodeWrapper().getLineNumber();
    }

    @Override
    public String getLocalPart() {
        return getNodeWrapper().getLocalPart();
    }

    @Override
    public int getNameCode() {
        return getNodeWrapper().getNameCode();
    }

    @Override
    public int getNodeKind() {
        return getNodeWrapper().getNodeKind();
    }

    @Override
    public NodeInfo getParent() {
        return getNodeWrapper().getParent();
    }

    @Override
    public String getPrefix() {
        return getNodeWrapper().getPrefix();
    }

    @Override
    public NodeInfo getRoot() {
        return getNodeWrapper().getRoot();
    }

    @Override
    public String getStringValue() {
        return getNodeWrapper().getStringValue();
    }

    @Override
    public String getSystemId() {
        return getNodeWrapper().getSystemId();
    }

    @Override
    public int getTypeAnnotation() {
        return getNodeWrapper().getTypeAnnotation();
    }

    @Override
    public String getURI() {
        return getNodeWrapper().getURI();
    }

    @Override
    public boolean hasChildNodes() {
        return getNodeWrapper().hasChildNodes();
    }

    @Override
    public boolean isId() {
        return getNodeWrapper().isId();
    }

    @Override
    public boolean isIdref() {
        return getNodeWrapper().isIdref();
    }

    @Override
    public boolean isNilled() {
        return getNodeWrapper().isNilled();
    }

    @Override
    public boolean isSameNodeInfo(NodeInfo arg0) {
        return getNodeWrapper().isSameNodeInfo(arg0);
    }

    @Override
    public AxisIterator iterateAxis(byte arg0) {
        return getNodeWrapper().iterateAxis(arg0);
    }

    @Override
    public AxisIterator iterateAxis(byte arg0, NodeTest arg1) {
        return getNodeWrapper().iterateAxis(arg0, arg1);
    }

    @Override
    public void setSystemId(String arg0) {
        getNodeWrapper().setSystemId(arg0);
    }

    @Override
    public CharSequence getStringValueCS() {
        return getNodeWrapper().getStringValueCS();
    }

    @Override
    public SequenceIterator getTypedValue() throws XPathException {
        return getNodeWrapper().getTypedValue();
    }

    public NodeWrapper getNodeWrapper() {
        return mNodeWrapper;
    }
}

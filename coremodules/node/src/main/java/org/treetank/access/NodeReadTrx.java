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

import static com.google.common.base.Preconditions.checkState;
import static org.treetank.node.IConstants.NULL_NODE;
import static org.treetank.node.IConstants.ROOT_NODE;

import javax.xml.namespace.QName;

import org.treetank.api.INodeReadTrx;
import org.treetank.api.IPageReadTrx;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.node.ElementNode;
import org.treetank.node.IConstants;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IValNode;

/**
 * <h1>NodeReadTrx</h1>
 * 
 * <p>
 * Read-only transaction with single-threaded cursor semantics. Each read-only transaction works on a given
 * revision key.
 * </p>
 */
public class NodeReadTrx implements INodeReadTrx {

    /** State of transaction including all cached stuff. */
    protected IPageReadTrx mPageReadTrx;

    /** Strong reference to currently selected node. */
    private INode mCurrentNode;

    /**
     * Constructor.
     * 
     * 
     * @param pPageTrx
     *            Transaction state to work with.
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    public NodeReadTrx(final IPageReadTrx pPageTrx) throws TTException {
        mPageReadTrx = pPageTrx;
        mCurrentNode = (org.treetank.node.interfaces.INode)mPageReadTrx.getNode(ROOT_NODE);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTIOException
     */
    @Override
    public final boolean moveTo(final long pNodeKey) throws TTIOException {
        assertNotClosed();
        if (pNodeKey == NULL_NODE) {
            return false;
        } else {
            // Remember old node and fetch new one.
            final INode oldNode = mCurrentNode;
            mCurrentNode = (org.treetank.node.interfaces.INode)mPageReadTrx.getNode(pNodeKey);

            if (mCurrentNode == null) {
                mCurrentNode = oldNode;
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean moveToAttribute(final int mIndex) throws TTIOException {
        assertNotClosed();
        if (mCurrentNode.getKind() == IConstants.ELEMENT) {
            return moveTo(((ElementNode)mCurrentNode).getAttributeKey(mIndex));
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean moveToNamespace(final int mIndex) throws TTIOException {
        assertNotClosed();
        if (mCurrentNode.getKind() == IConstants.ELEMENT) {
            return moveTo(((ElementNode)mCurrentNode).getNamespaceKey(mIndex));
        } else {
            return false;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getValueOfCurrentNode() {
        assertNotClosed();
        String returnVal;
        if (mCurrentNode instanceof IValNode) {
            returnVal = new String(((IValNode)mCurrentNode).getRawValue());
        } else {
            returnVal = "";
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final QName getQNameOfCurrentNode() {
        assertNotClosed();
        String name = "";
        String uri = "";
        if (mCurrentNode instanceof INameNode) {
            name = mPageReadTrx.getName(((INameNode)mCurrentNode).getNameKey());
            uri = mPageReadTrx.getName(((INameNode)mCurrentNode).getURIKey());
        }
        return buildQName(uri, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getTypeOfCurrentNode() {
        assertNotClosed();
        return mPageReadTrx.getName(mCurrentNode.getTypeKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String nameForKey(final int mKey) {
        assertNotClosed();
        return mPageReadTrx.getName(mKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTException {
        if (!mPageReadTrx.isClosed()) {
            // Close own state.
            mPageReadTrx.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NodeReadTrx [mPageReadTrx=");
        builder.append(mPageReadTrx);
        builder.append(", mCurrentNode=");
        builder.append(mCurrentNode);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Is the transaction closed?
     * 
     * @return True if the transaction was closed.
     */
    public final boolean isClosed() {
        return mPageReadTrx.isClosed();
    }

    /**
     * Make sure that the session is not yet closed when calling this method.
     */
    protected final void assertNotClosed() {
        checkState(!mPageReadTrx.isClosed(), "Transaction is already closed.");
    }

    /**
     * Replace the state of the transaction.
     * 
     * @param pPageTrx
     *            Page Read Trx
     */
    protected final void setPageTransaction(final IPageReadTrx pPageTrx) {
        mPageReadTrx = pPageTrx;
    }

    /**
     * Getter for superclasses.
     * 
     * @return The current node.
     */
    protected final INode getCurrentNode() {
        return mCurrentNode;
    }

    /**
     * Setter for superclasses.
     * 
     * @param paramCurrentNode
     *            The current node to set.
     */
    protected final void setCurrentNode(final INode paramCurrentNode) {
        assertNotClosed();
        mCurrentNode = paramCurrentNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final INode getNode() {
        assertNotClosed();
        return mCurrentNode;
    }

    /**
     * Building QName out of uri and name. The name can have the prefix denoted
     * with ":";
     * 
     * @param paramUri
     *            the namespaceuri
     * @param paramName
     *            the name including a possible prefix
     * @return the QName obj
     */
    public static final QName buildQName(final String paramUri, final String paramName) {
        QName qname;
        if (paramName.contains(":")) {
            qname = new QName(paramUri, paramName.split(":")[1], paramName.split(":")[0]);
        } else {
            qname = new QName(paramUri, paramName);
        }
        return qname;
    }
}

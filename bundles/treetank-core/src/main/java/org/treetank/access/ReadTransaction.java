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

import javax.xml.namespace.QName;

import org.treetank.annotations.NotNull;
import org.treetank.api.IItemList;
import org.treetank.api.IReadTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.node.interfaces.IValNode;
import org.treetank.settings.EFixed;
import org.treetank.utils.NamePageHash;

/**
 * <h1>ReadTransaction</h1>
 * 
 * <p>
 * Read-only transaction wiht single-threaded cursor semantics. Each read-only transaction works on a given
 * revision key.
 * </p>
 */
public class ReadTransaction implements IReadTransaction {

    /** ID of transaction. */
    private final long mId;

    /** Session state this write transaction is bound to. */
    protected final Session mSession;

    /** State of transaction including all cached stuff. */
    private ReadTransactionState mState;

    /** Strong reference to currently selected node. */
    private INode mCurrentNode;

    /** Tracks whether the transaction is closed. */
    private boolean mClosed;

    /**
     * Constructor.
     * 
     * @param paramSession
     *            state of the session
     * @param paramTransactionID
     *            ID of transaction.
     * 
     * @param paramTransactionState
     *            Transaction state to work with.
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    protected ReadTransaction(final Session paramSession, final long paramTransactionID,
        final ReadTransactionState paramTransactionState) throws TTIOException {
        mSession = paramSession;
        mId = paramTransactionID;
        mState = paramTransactionState;
        mCurrentNode = getTransactionState().getNode((Long)EFixed.ROOT_NODE_KEY.getStandardProperty());
        mClosed = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long getTransactionID() {
        return mId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long getRevisionNumber() throws TTIOException {
        assertNotClosed();
        return mState.getActualRevisionRootPage().getRevision();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long getRevisionTimestamp() throws TTIOException {
        assertNotClosed();
        return mState.getActualRevisionRootPage().getRevisionTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean moveTo(final long paramNodeKey) {
        assertNotClosed();
        if (paramNodeKey == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
            return false;
        } else {
            // Remember old node and fetch new one.
            final INode oldNode = mCurrentNode;
            try {
                mCurrentNode = mState.getNode(paramNodeKey);
            } catch (final Exception e) {
                mCurrentNode = null;
            }

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
    public final boolean moveToDocumentRoot() {
        return moveTo((Long)EFixed.ROOT_NODE_KEY.getStandardProperty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean moveToParent() {
        return moveTo(mCurrentNode.getParentKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean moveToFirstChild() {
        final IStructNode node = getStructuralNode();
        return node == null ? false : moveTo(node.getFirstChildKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean moveToLeftSibling() {
        final IStructNode node = getStructuralNode();
        return node == null ? false : moveTo(node.getLeftSiblingKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean moveToRightSibling() {
        final IStructNode node = getStructuralNode();
        return node == null ? false : moveTo(node.getRightSiblingKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean moveToAttribute(final int mIndex) {
        if (mCurrentNode.getKind() == ENodes.ELEMENT_KIND) {
            return moveTo(((ElementNode)mCurrentNode).getAttributeKey(mIndex));
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean moveToNamespace(final int mIndex) {
        if (mCurrentNode.getKind() == ENodes.ELEMENT_KIND) {
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
            name = mState.getName(((INameNode)mCurrentNode).getNameKey());
            uri = mState.getName(((INameNode)mCurrentNode).getURIKey());
        }
        return buildQName(uri, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getTypeOfCurrentNode() {
        assertNotClosed();
        return mState.getName(mCurrentNode.getTypeKey());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int keyForName(final String mName) {
        assertNotClosed();
        return NamePageHash.generateHashForString(mName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String nameForKey(final int mKey) {
        assertNotClosed();
        return mState.getName(mKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final byte[] rawNameForKey(final int paramKey) {
        assertNotClosed();
        return mState.getRawName(paramKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IItemList getItemList() {
        return mState.getItemList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws AbsTTException {
        if (!mClosed) {
            // Close own state.
            mState.close();

            // Callback on session to make sure everything is cleaned up.
            mSession.closeReadTransaction(mId);

            // Immediately release all references.
            mState = null;
            mCurrentNode = null;

            mClosed = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        assertNotClosed();
        final StringBuilder builder = new StringBuilder();
        if (getNode().getKind() == ENodes.ATTRIBUTE_KIND || getNode().getKind() == ENodes.ELEMENT_KIND) {
            builder.append("Name of Node: ");
            builder.append(getQNameOfCurrentNode().toString());
            builder.append("\n");
        }
        if (getNode().getKind() == ENodes.ATTRIBUTE_KIND || getNode().getKind() == ENodes.TEXT_KIND) {
            builder.append("Value of Node: ");
            builder.append(getValueOfCurrentNode());
            builder.append("\n");
        }
        if (getNode().getKind() == ENodes.ROOT_KIND) {
            builder.append("Node is DocumentRoot");
            builder.append("\n");
        }
        builder.append(getNode().toString());

        return builder.toString();
    }

    /**
     * Set state to closed.
     */
    protected final void setClosed() {
        mClosed = true;
    }

    /**
     * Is the transaction closed?
     * 
     * @return True if the transaction was closed.
     */
    public final boolean isClosed() {
        return mClosed;
    }

    /**
     * Make sure that the session is not yet closed when calling this method.
     */
    protected final void assertNotClosed() {
        if (mClosed) {
            throw new IllegalStateException("Transaction is already closed.");
        }
    }

    /**
     * Getter for superclasses.
     * 
     * @return The state of this transaction.
     */
    public ReadTransactionState getTransactionState() {
        return mState;
    }

    /**
     * Replace the state of the transaction.
     * 
     * @param paramTransactionState
     *            State of transaction.
     */
    protected final void setTransactionState(final ReadTransactionState paramTransactionState) {
        mState = paramTransactionState;
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
        mCurrentNode = paramCurrentNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final INode getNode() {
        return mCurrentNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long getMaxNodeKey() throws TTIOException {
        return getTransactionState().getActualRevisionRootPage().getMaxNodeKey();
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
    @NotNull
    protected static final QName buildQName(final String paramUri, final String paramName) {
        QName qname;
        if (paramName.contains(":")) {
            qname = new QName(paramUri, paramName.split(":")[1], paramName.split(":")[0]);
        } else {
            qname = new QName(paramUri, paramName);
        }
        return qname;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final IStructNode getStructuralNode() {
        if (mCurrentNode instanceof IStructNode) {
            return (IStructNode)mCurrentNode;
        } else {
            return null;
        }
    }
}

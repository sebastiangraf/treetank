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

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.treetank.node.IConstants.NULL_NODE;
import static org.treetank.node.IConstants.ROOT_NODE;

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.treetank.api.INodeWriteTrx;
import org.treetank.api.IBucketWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.node.AttributeNode;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.ElementNode;
import org.treetank.node.IConstants;
import org.treetank.node.NamespaceNode;
import org.treetank.node.NodeMetaPageFactory;
import org.treetank.node.TextNode;
import org.treetank.node.delegates.NameNodeDelegate;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.delegates.ValNodeDelegate;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.node.interfaces.IValNode;
import org.treetank.utils.NamePageHash;
import org.treetank.utils.TypedValue;

/**
 * <h1>NodeWriteTrx</h1>
 * 
 * <p>
 * Single-threaded instance of only write transaction per session.
 * </p>
 * 
 * <p>
 * All methods throw {@link NullPointerException}s in case of null values for reference parameters.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public class NodeWriteTrx implements INodeWriteTrx {

    /** Session for abort/commit. */
    private final ISession mSession;

    /**
     * How is the Hash for this storage computed?
     */
    public enum HashKind {
        /** Rolling hash, only nodes on ancestor axis are touched. */
        Rolling,
        /**
         * Postorder hash, all nodes on ancestor plus postorder are at least
         * read.
         */
        Postorder,
        /** No hash structure after all. */
        None;
    }

    /** Prime for computing the hash. */
    private static final int PRIME = 77081;

    /** Hash kind of Structure. */
    private final HashKind mHashKind;

    /** Delegate for the read access. */
    private NodeReadTrx mDelegate;

    /**
     * Constructor.
     * 
     * @param pSession
     *            state of the session
     * @param pPageWriteTrx
     *            state of this transaction
     * 
     * @throws TTIOException
     *             if the reading of the props is failing
     * @throws TTUsageException
     *             if paramMaxNodeCount < 0 or paramMaxTime < 0
     */
    public NodeWriteTrx(final ISession pSession, final IBucketWriteTrx pPageWriteTrx, final HashKind kind)
        throws TTException {

        mHashKind = kind;
        mDelegate = new NodeReadTrx(pPageWriteTrx);
        mSession = pSession;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertElementAsFirstChild(final QName pQName) throws TTException, NullPointerException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkNotNull(pQName);
        checkState(mDelegate.getCurrentNode() instanceof ElementNode
            || mDelegate.getCurrentNode() instanceof DocumentRootNode,
            "Insert is not allowed if current node is not an ElementNode, but was %s", mDelegate
                .getCurrentNode());

        final long parentKey = mDelegate.getCurrentNode().getDataKey();
        final long leftSibKey = NULL_NODE;
        final long rightSibKey = ((IStructNode)mDelegate.getCurrentNode()).getFirstChildKey();
        final ElementNode node = createElementNode(parentKey, leftSibKey, rightSibKey, 0, pQName);

        mDelegate.setCurrentNode(node);
        adaptForInsert(node, true);
        adaptHashesWithAdd();

        return node.getDataKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertElementAsRightSibling(final QName pQName) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkNotNull(pQName);
        checkState(
            mDelegate.getCurrentNode() instanceof IStructNode,
            "Insert is not allowed if current node is not an StructuralNode (either Text or Element), but was %s",
            mDelegate.getCurrentNode());

        final long parentKey = mDelegate.getCurrentNode().getParentKey();
        final long leftSibKey = mDelegate.getCurrentNode().getDataKey();
        final long rightSibKey = ((IStructNode)mDelegate.getCurrentNode()).getRightSiblingKey();
        final ElementNode node = createElementNode(parentKey, leftSibKey, rightSibKey, 0, pQName);

        mDelegate.setCurrentNode(node);
        adaptForInsert(node, false);
        adaptHashesWithAdd();

        return node.getDataKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertTextAsFirstChild(final String pValue) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkNotNull(pValue);
        checkState(mDelegate.getCurrentNode() instanceof ElementNode
            || mDelegate.getCurrentNode() instanceof DocumentRootNode,
            "Insert is not allowed if current node is not an ElementNode, but was %s", mDelegate
                .getCurrentNode());

        final byte[] value = TypedValue.getBytes(pValue);
        final long parentKey = mDelegate.getCurrentNode().getDataKey();
        final long leftSibKey = NULL_NODE;
        final long rightSibKey = ((IStructNode)mDelegate.getCurrentNode()).getFirstChildKey();
        final TextNode node = createTextNode(parentKey, leftSibKey, rightSibKey, value);

        mDelegate.setCurrentNode(node);
        adaptForInsert(node, true);
        adaptHashesWithAdd();

        return node.getDataKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertTextAsRightSibling(final String pValue) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkNotNull(pValue);
        checkState(mDelegate.getCurrentNode() instanceof ElementNode,
            "Insert is not allowed if current node is not an ElementNode, but was %s", mDelegate
                .getCurrentNode());

        final byte[] value = TypedValue.getBytes(pValue);
        final long parentKey = mDelegate.getCurrentNode().getParentKey();
        final long leftSibKey = mDelegate.getCurrentNode().getDataKey();
        final long rightSibKey = ((IStructNode)mDelegate.getCurrentNode()).getRightSiblingKey();
        final TextNode node = createTextNode(parentKey, leftSibKey, rightSibKey, value);

        mDelegate.setCurrentNode(node);
        adaptForInsert(node, false);
        adaptHashesWithAdd();

        return node.getDataKey();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertAttribute(final QName pQName, final String pValue) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkState(mDelegate.getCurrentNode() instanceof ElementNode,
            "Insert is not allowed if current node is not an ElementNode, but was %s", mDelegate
                .getCurrentNode());

        final byte[] value = TypedValue.getBytes(pValue);
        final long elementKey = mDelegate.getCurrentNode().getDataKey();

        final int nameKey = insertName(buildName(pQName));
        final int namespaceKey = insertName(pQName.getNamespaceURI());
        final NodeDelegate nodeDel = new NodeDelegate(getPtx().incrementDataKey(), elementKey, 0);
        final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel, nameKey, namespaceKey);
        final ValNodeDelegate valDel = new ValNodeDelegate(nodeDel, value);

        final AttributeNode node = new AttributeNode(nodeDel, nameDel, valDel);
        getPtx().setData(node);

        final INode parentNode = (org.treetank.node.interfaces.INode)getPtx().getData(node.getParentKey());
        ((ElementNode)parentNode).insertAttribute(node.getDataKey());
        getPtx().setData(parentNode);

        mDelegate.setCurrentNode(node);
        adaptForInsert(node, false);

        adaptHashesWithAdd();
        return node.getDataKey();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long insertNamespace(final QName pQName) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkNotNull(pQName);
        checkState(mDelegate.getCurrentNode() instanceof ElementNode,
            "Insert is not allowed if current node is not an ElementNode, but was %s", mDelegate
                .getCurrentNode());

        final int uriKey = insertName(pQName.getNamespaceURI());
        final int prefixKey = insertName(pQName.getPrefix());
        final long elementKey = mDelegate.getCurrentNode().getDataKey();

        final NodeDelegate nodeDel = new NodeDelegate(getPtx().incrementDataKey(), elementKey, 0);
        final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel, prefixKey, uriKey);

        final NamespaceNode node = new NamespaceNode(nodeDel, nameDel);
        getPtx().setData(node);

        final INode parentNode = (org.treetank.node.interfaces.INode)getPtx().getData(node.getParentKey());
        ((ElementNode)parentNode).insertNamespace(node.getDataKey());
        getPtx().setData(parentNode);

        mDelegate.setCurrentNode(node);
        adaptForInsert(node, false);
        adaptHashesWithAdd();
        return node.getDataKey();
    }

    private ElementNode createElementNode(final long parentKey, final long mLeftSibKey,
        final long rightSibKey, final long hash, final QName mName) throws TTException {

        final int nameKey = insertName(buildName(mName));
        final int namespaceKey = insertName(mName.getNamespaceURI());

        final NodeDelegate nodeDel = new NodeDelegate(getPtx().incrementDataKey(), parentKey, 0);
        final StructNodeDelegate structDel =
            new StructNodeDelegate(nodeDel, NULL_NODE, rightSibKey, mLeftSibKey, 0);
        final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel, nameKey, namespaceKey);
        final ElementNode node =
            new ElementNode(nodeDel, structDel, nameDel, new ArrayList<Long>(), new ArrayList<Long>());
        getPtx().setData(node);
        return node;
    }

    private TextNode createTextNode(final long mParentKey, final long mLeftSibKey, final long rightSibKey,
        final byte[] mValue) throws TTException {
        final NodeDelegate nodeDel = new NodeDelegate(getPtx().incrementDataKey(), mParentKey, 0);
        final ValNodeDelegate valDel = new ValNodeDelegate(nodeDel, mValue);
        final StructNodeDelegate structDel =
            new StructNodeDelegate(nodeDel, NULL_NODE, rightSibKey, mLeftSibKey, 0);
        final TextNode node = new TextNode(nodeDel, structDel, valDel);
        getPtx().setData(node);
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkState(mDelegate.getCurrentNode().getKind() != IConstants.ROOT,
            "Document root can not be removed.");
        if (mDelegate.getCurrentNode() instanceof IStructNode) {
            final IStructNode node = (IStructNode)mDelegate.getCurrentNode();
            if (node.getKind() == IConstants.ELEMENT) {
                long currentKey = node.getDataKey();
                ElementNode element = (ElementNode)node;
                for (int i = 0; i < element.getAttributeCount(); i++) {
                    moveTo(element.getAttributeKey(i));
                    getPtx().removeData(mDelegate.getCurrentNode());
                }
                for (int i = 0; i < element.getNamespaceCount(); i++) {
                    moveTo(element.getNamespaceKey(i));
                    getPtx().removeData(mDelegate.getCurrentNode());
                }
                moveTo(currentKey);
            }
            moveTo(node.getDataKey());
            adaptForRemove(node);
            adaptHashesWithRemove();

            // Set current node.
            if (node.hasRightSibling()) {
                moveTo(node.getRightSiblingKey());
            } else if (node.hasLeftSibling()) {
                moveTo(node.getLeftSiblingKey());
            } else {
                moveTo(node.getParentKey());
            }
        } else if (mDelegate.getCurrentNode().getKind() == IConstants.ATTRIBUTE) {
            final INode node = mDelegate.getCurrentNode();

            final ElementNode parentNode = (ElementNode)getPtx().getData(node.getParentKey());
            parentNode.removeAttribute(node.getDataKey());
            getPtx().setData(parentNode);
            adaptHashesWithRemove();
            moveTo(mDelegate.getCurrentNode().getParentKey());
        } else if (mDelegate.getCurrentNode().getKind() == IConstants.NAMESPACE) {
            final INode node = mDelegate.getCurrentNode();
            final ElementNode parentNode = (ElementNode)getPtx().getData(node.getParentKey());
            parentNode.removeNamespace(node.getDataKey());
            getPtx().setData(parentNode);
            adaptHashesWithRemove();
            moveTo(mDelegate.getCurrentNode().getParentKey());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQName(final QName paramName) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkState(mDelegate.getCurrentNode() instanceof INameNode,
            "setQName is not allowed if current node is not an INameNode implementation, but was %s",
            mDelegate.getCurrentNode());

        final long oldHash = mDelegate.getCurrentNode().hashCode();

        final INameNode node = (INameNode)getPtx().getData(mDelegate.getCurrentNode().getDataKey());
        node.setNameKey(insertName(buildName(paramName)));
        getPtx().setData(node);

        mDelegate.setCurrentNode((INode)node);
        adaptHashedWithUpdate(oldHash);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURI(final String paramUri) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkState(mDelegate.getCurrentNode() instanceof INameNode,
            "setURI is not allowed if current node is not an INameNode implementation, but was %s", mDelegate
                .getCurrentNode());

        final long oldHash = mDelegate.getCurrentNode().hashCode();

        final INameNode node = (INameNode)getPtx().getData(mDelegate.getCurrentNode().getDataKey());
        node.setURIKey(insertName(paramUri));
        getPtx().setData(node);

        mDelegate.setCurrentNode((INode)node);
        adaptHashedWithUpdate(oldHash);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final String pValue) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkState(mDelegate.getCurrentNode() instanceof IValNode,
            "setValue is not allowed if current node is not an IValNode implementation, but was %s",
            mDelegate.getCurrentNode());

        final long oldHash = mDelegate.getCurrentNode().hashCode();

        final IValNode node = (IValNode)getPtx().getData(mDelegate.getCurrentNode().getDataKey());
        node.setValue(TypedValue.getBytes(pValue));
        getPtx().setData(node);

        mDelegate.setCurrentNode((INode)node);
        adaptHashedWithUpdate(oldHash);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTUsageException
     *             if paramRevision < 0 or paramRevision > maxCommitedRev
     * @throws TTIOException
     *             if an I/O operation fails
     */
    @Override
    public void revertTo(final long pRevision) throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        checkArgument(pRevision >= 0, "Parameter must be >= 0, but was %s", pRevision);
        getPtx().close();
        // Reset internal transaction state to new uber page.
        mDelegate.setPageTransaction(mSession.beginBucketWtx(pRevision));
        moveTo(ROOT_NODE);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        // ICommitStrategy uber page.
        getPtx().commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort() throws TTException {
        checkState(!mDelegate.isClosed(), "Transaction is already closed.");
        long revisionToSet = 0;
        revisionToSet = mDelegate.mPageReadTrx.getRevision() - 1;

        getPtx().close();

        // Reset internal transaction state to last committed uber page.
        mDelegate.setPageTransaction(mSession.beginBucketWtx(revisionToSet));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTException {
        if (!isClosed()) {
            mDelegate.close();
        }
    }

    /**
     * Setting a new name in the metapage.
     * 
     * @param pName
     *            to be set
     * @throws TTException
     */
    private int insertName(final String pName) throws TTException {
        final String string = (pName == null ? "" : pName);
        final int nameKey = NamePageHash.generateHashForString(string);
        NodeMetaPageFactory.MetaKey key = new NodeMetaPageFactory.MetaKey(nameKey);
        NodeMetaPageFactory.MetaValue value = new NodeMetaPageFactory.MetaValue(string);
        getPageWtx().getMetaBucket().put(key, value);
        return nameKey;
    }

    // ////////////////////////////////////////////////////////////
    // insert operation
    // //////////////////////////////////////////////////////////

    /**
     * Adapting everything for insert operations.
     * 
     * @param paramNewNode
     *            pointer of the new node to be inserted
     * @param addAsFirstChild
     *            determines the position where to insert
     * @throws TTIOException
     *             if anything weird happens
     */
    private void adaptForInsert(final INode paramNewNode, final boolean addAsFirstChild) throws TTException {
        assert paramNewNode != null;

        if (paramNewNode instanceof IStructNode) {
            final IStructNode strucNode = (IStructNode)paramNewNode;
            final IStructNode parent = (IStructNode)getPtx().getData(paramNewNode.getParentKey());
            parent.incrementChildCount();
            if (addAsFirstChild) {
                parent.setFirstChildKey(paramNewNode.getDataKey());
            }
            getPtx().setData(parent);

            if (strucNode.hasRightSibling()) {
                final IStructNode rightSiblingNode =
                    (IStructNode)getPtx().getData(strucNode.getRightSiblingKey());
                rightSiblingNode.setLeftSiblingKey(paramNewNode.getDataKey());
                getPtx().setData(rightSiblingNode);
            }
            if (strucNode.hasLeftSibling()) {
                final IStructNode leftSiblingNode =
                    (IStructNode)getPtx().getData(strucNode.getLeftSiblingKey());
                leftSiblingNode.setRightSiblingKey(paramNewNode.getDataKey());
                getPtx().setData(leftSiblingNode);
            }
        }

    }

    // ////////////////////////////////////////////////////////////
    // end of insert operation
    // ////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////
    // remove operation
    // ////////////////////////////////////////////////////////////

    /**
     * Adapting everything for remove operations.
     * 
     * @param pOldNode
     *            pointer of the old node to be replaces
     * @throws TTIOException
     *             if anything weird happens
     */
    private void adaptForRemove(final IStructNode pOldNode) throws TTException {
        assert pOldNode != null;

        // Adapt left sibling node if there is one.
        if (pOldNode.hasLeftSibling()) {
            final IStructNode leftSibling = (IStructNode)getPtx().getData(pOldNode.getLeftSiblingKey());
            leftSibling.setRightSiblingKey(pOldNode.getRightSiblingKey());
            getPtx().setData(leftSibling);
        }

        // Adapt right sibling node if there is one.
        if (pOldNode.hasRightSibling()) {
            final IStructNode rightSibling = (IStructNode)getPtx().getData(pOldNode.getRightSiblingKey());
            rightSibling.setLeftSiblingKey(pOldNode.getLeftSiblingKey());
            getPtx().setData(rightSibling);
        }

        // Adapt parent, if node has now left sibling it is a first child.
        final IStructNode parent = (IStructNode)getPtx().getData(pOldNode.getParentKey());
        if (!pOldNode.hasLeftSibling()) {
            parent.setFirstChildKey(pOldNode.getRightSiblingKey());
        }
        parent.decrementChildCount();
        getPtx().setData(parent);

        if (pOldNode.getKind() == IConstants.ELEMENT) {
            // removing attributes
            for (int i = 0; i < ((ElementNode)pOldNode).getAttributeCount(); i++) {
                moveTo(((ElementNode)pOldNode).getAttributeKey(i));
                getPtx().removeData(mDelegate.getCurrentNode());
            }
            // removing namespaces
            moveTo(pOldNode.getDataKey());
            for (int i = 0; i < ((ElementNode)pOldNode).getNamespaceCount(); i++) {
                moveTo(((ElementNode)pOldNode).getNamespaceKey(i));
                getPtx().removeData(mDelegate.getCurrentNode());
            }
        }

        // Remove old node.
        getPtx().removeData(pOldNode);
    }

    // ////////////////////////////////////////////////////////////
    // end of remove operation
    // ////////////////////////////////////////////////////////////

    /**
     * Getter for superclasses.
     * 
     * @return The state of this transaction.
     */
    private BucketWriteTrx getPtx() {
        return (BucketWriteTrx)mDelegate.mPageReadTrx;
    }

    /**
     * Adapting the structure with a hash for all ancestors only with insert.
     * 
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void adaptHashesWithAdd() throws TTException {
        switch (mHashKind) {
        case Rolling:
            rollingAdd();
            break;
        case Postorder:
            postorderAdd();
            break;
        default:
        }

    }

    /**
     * Adapting the structure with a hash for all ancestors only with remove.
     * 
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void adaptHashesWithRemove() throws TTException {
        switch (mHashKind) {
        case Rolling:
            rollingRemove();
            break;
        case Postorder:
            postorderRemove();
            break;
        default:
        }
    }

    /**
     * Adapting the structure with a hash for all ancestors only with update.
     * 
     * @param paramOldHash
     *            paramOldHash to be removed
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void adaptHashedWithUpdate(final long paramOldHash) throws TTException {
        switch (mHashKind) {
        case Rolling:
            rollingUpdate(paramOldHash);
            break;
        case Postorder:
            postorderAdd();
            break;
        default:
        }
    }

    /**
     * Removal operation for postorder hash computation.
     * 
     * @throws TTIOException
     *             if anything weird happens
     */
    private void postorderRemove() throws TTException {
        moveTo(mDelegate.getCurrentNode().getParentKey());
        postorderAdd();
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with
     * insert.
     * 
     * @throws TTIOException
     *             if anything weird happened
     */
    private void postorderAdd() throws TTException {
        // start with hash to add
        final long startKey = mDelegate.getCurrentNode().getDataKey();
        // long for adapting the hash of the parent
        long hashCodeForParent = 0;
        // adapting the parent if the current node is no structural one.
        if (!(mDelegate.getCurrentNode() instanceof IStructNode)) {
            getPtx().getData(mDelegate.getCurrentNode().getDataKey());
            mDelegate.getCurrentNode().setHash(mDelegate.getCurrentNode().hashCode());
            getPtx().setData(mDelegate.getCurrentNode());
            moveTo(mDelegate.getCurrentNode().getParentKey());
        }
        // Cursor to root
        IStructNode cursorToRoot;
        do {
            synchronized (mDelegate.getCurrentNode()) {
                cursorToRoot = (IStructNode)getPtx().getData(mDelegate.getCurrentNode().getDataKey());
                hashCodeForParent = mDelegate.getCurrentNode().hashCode() + hashCodeForParent * PRIME;
                // Caring about attributes and namespaces if node is an element.
                if (cursorToRoot.getKind() == IConstants.ELEMENT) {
                    final ElementNode currentElement = (ElementNode)cursorToRoot;
                    // setting the attributes and namespaces
                    for (int i = 0; i < ((ElementNode)cursorToRoot).getAttributeCount(); i++) {
                        moveTo(currentElement.getAttributeKey(i));
                        hashCodeForParent = mDelegate.getCurrentNode().hashCode() + hashCodeForParent * PRIME;
                    }
                    for (int i = 0; i < ((ElementNode)cursorToRoot).getNamespaceCount(); i++) {
                        moveTo(currentElement.getNamespaceKey(i));
                        hashCodeForParent = mDelegate.getCurrentNode().hashCode() + hashCodeForParent * PRIME;
                    }
                    moveTo(cursorToRoot.getDataKey());
                }

                // Caring about the children of a node
                if (moveTo(((IStructNode)getNode()).getFirstChildKey())) {
                    do {
                        hashCodeForParent = mDelegate.getCurrentNode().getHash() + hashCodeForParent * PRIME;
                    } while (moveTo(((IStructNode)getNode()).getRightSiblingKey()));
                    moveTo(((IStructNode)getNode()).getParentKey());
                }

                // setting hash and resetting hash
                cursorToRoot.setHash(hashCodeForParent);
                getPtx().setData(cursorToRoot);
                hashCodeForParent = 0;
            }
        } while (moveTo(cursorToRoot.getParentKey()));

        moveTo(startKey);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with
     * update.
     * 
     * @param paramOldHash
     *            paramOldHash to be removed
     * @throws TTIOException
     *             if anything weird happened
     */
    private void rollingUpdate(final long paramOldHash) throws TTException {
        final INode newNode = mDelegate.getCurrentNode();
        final long newNodeHash = newNode.hashCode();
        long resultNew = newNode.hashCode();

        // go the path to the root
        do {
            synchronized (mDelegate.getCurrentNode()) {
                getPtx().getData(mDelegate.getCurrentNode().getDataKey());
                if (mDelegate.getCurrentNode().getDataKey() == newNode.getDataKey()) {
                    resultNew = mDelegate.getCurrentNode().getHash() - paramOldHash;
                    resultNew = resultNew + newNodeHash;
                } else {
                    resultNew = mDelegate.getCurrentNode().getHash() - (paramOldHash * PRIME);
                    resultNew = resultNew + newNodeHash * PRIME;
                }
                mDelegate.getCurrentNode().setHash(resultNew);
                getPtx().setData(mDelegate.getCurrentNode());
            }
        } while (moveTo(mDelegate.getCurrentNode().getParentKey()));

        mDelegate.setCurrentNode(newNode);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with
     * remove.
     * 
     * @throws TTIOException
     *             if anything weird happened
     */
    private void rollingRemove() throws TTException {
        final INode startNode = mDelegate.getCurrentNode();
        long hashToRemove = startNode.getHash();
        long hashToAdd = 0;
        long newHash = 0;
        // go the path to the root
        do {
            synchronized (mDelegate.getCurrentNode()) {
                getPtx().getData(mDelegate.getCurrentNode().getDataKey());
                if (mDelegate.getCurrentNode().getDataKey() == startNode.getDataKey()) {
                    // the begin node is always null
                    newHash = 0;
                } else if (mDelegate.getCurrentNode().getDataKey() == startNode.getParentKey()) {
                    // the parent node is just removed
                    newHash = mDelegate.getCurrentNode().getHash() - (hashToRemove * PRIME);
                    hashToRemove = mDelegate.getCurrentNode().getHash();
                } else {
                    // the ancestors are all touched regarding the modification
                    newHash = mDelegate.getCurrentNode().getHash() - (hashToRemove * PRIME);
                    newHash = newHash + hashToAdd * PRIME;
                    hashToRemove = mDelegate.getCurrentNode().getHash();
                }
                mDelegate.getCurrentNode().setHash(newHash);
                hashToAdd = newHash;
                getPtx().setData(mDelegate.getCurrentNode());
            }
        } while (moveTo(mDelegate.getCurrentNode().getParentKey()));

        mDelegate.setCurrentNode(startNode);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with
     * insert.
     * 
     * @throws TTIOException
     *             if anything weird happened
     */
    private void rollingAdd() throws TTException {
        // start with hash to add
        final INode startNode = mDelegate.getCurrentNode();
        long hashToAdd = startNode.hashCode();
        long newHash = 0;
        long possibleOldHash = 0;
        // go the path to the root
        do {
            synchronized (mDelegate.getCurrentNode()) {
                getPtx().getData(mDelegate.getCurrentNode().getDataKey());
                if (mDelegate.getCurrentNode().getDataKey() == startNode.getDataKey()) {
                    // at the beginning, take the hashcode of the node only
                    newHash = hashToAdd;
                } else if (mDelegate.getCurrentNode().getDataKey() == startNode.getParentKey()) {
                    // at the parent level, just add the node
                    possibleOldHash = mDelegate.getCurrentNode().getHash();
                    newHash = possibleOldHash + hashToAdd * PRIME;
                    hashToAdd = newHash;
                } else {
                    // at the rest, remove the existing old key for this element
                    // and add the new one
                    newHash = mDelegate.getCurrentNode().getHash() - (possibleOldHash * PRIME);
                    newHash = newHash + hashToAdd * PRIME;
                    hashToAdd = newHash;
                    possibleOldHash = mDelegate.getCurrentNode().getHash();
                }
                mDelegate.getCurrentNode().setHash(newHash);
                getPtx().setData(mDelegate.getCurrentNode());
            }
        } while (moveTo(mDelegate.getCurrentNode().getParentKey()));
        mDelegate.setCurrentNode(startNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveTo(long pKey) throws TTIOException {
        return mDelegate.moveTo(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToAttribute(int pIndex) throws TTIOException {
        return mDelegate.moveToAttribute(pIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveToNamespace(int pIndex) throws TTIOException {
        return mDelegate.moveToNamespace(pIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueOfCurrentNode() {
        return mDelegate.getValueOfCurrentNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QName getQNameOfCurrentNode() {
        return mDelegate.getQNameOfCurrentNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTypeOfCurrentNode() {
        return mDelegate.getTypeOfCurrentNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String nameForKey(int pKey) {
        return mDelegate.nameForKey(pKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public INode getNode() {
        return mDelegate.getNode();
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
    public IBucketWriteTrx getPageWtx() throws TTException {
        return getPtx();
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
    public String toString() {
        return toStringHelper(this).add("mSession", mSession).add("mHashKind", mHashKind).add("mDelegate",
            mDelegate).toString();
    }
}

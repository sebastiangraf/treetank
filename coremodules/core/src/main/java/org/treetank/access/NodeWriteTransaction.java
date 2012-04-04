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

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.treetank.api.INodeWriteTransaction;
import org.treetank.api.IPageWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.node.AttributeNode;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.ENode;
import org.treetank.node.ElementNode;
import org.treetank.node.NamespaceNode;
import org.treetank.node.TextNode;
import org.treetank.node.delegates.NameNodeDelegate;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.delegates.ValNodeDelegate;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.node.interfaces.IValNode;
import org.treetank.page.UberPage;
import org.treetank.utils.TypedValue;

/**
 * <h1>NodeWriteTransaction</h1>
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
public class NodeWriteTransaction extends NodeReadTransaction implements INodeWriteTransaction {

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

    /** Maximum number of node modifications before auto commit. */
    private final int mMaxNodeCount;

    /** Modification counter. */
    private long mModificationCount;

    /** Hash kind of Structure. */
    private final HashKind mHashKind;

    /**
     * Constructor.
     * 
     * @param paramTransactionID
     *            ID of transaction
     * @param paramSessionState
     *            state of the session
     * @param paramTransactionState
     *            state of this transaction
     * @param paramMaxNodeCount
     *            maximum number of node modifications before auto commit
     * @param paramMaxTime
     *            maximum number of seconds before auto commit
     * @throws TTIOException
     *             if the reading of the props is failing
     * @throws TTUsageException
     *             if paramMaxNodeCount < 0 or paramMaxTime < 0
     */
    protected NodeWriteTransaction(final long paramTransactionID, final Session paramSessionState,
        final IPageWriteTransaction paramTransactionState, final int paramMaxNodeCount, final int paramMaxTime)
        throws TTIOException, TTUsageException {
        super(paramSessionState, paramTransactionID, paramTransactionState);

        // Do not accept negative values.
        if ((paramMaxNodeCount < 0) || (paramMaxTime < 0)) {
            throw new TTUsageException("Negative arguments are not accepted.");
        }

        // Only auto commit by node modifications if it is more then 0.
        mMaxNodeCount = paramMaxNodeCount;
        mModificationCount = 0L;

        mHashKind = paramSessionState.mResourceConfig.mHashKind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsFirstChild(final QName mQName) throws AbsTTException,
        NullPointerException {
        if (mQName == null) {
            throw new NullPointerException("mQName may not be null!");
        }
        if (getCurrentNode() instanceof ElementNode || getCurrentNode() instanceof DocumentRootNode) {

            checkAccessAndCommit();

            final long parentKey = getCurrentNode().getNodeKey();
            final long leftSibKey = NULL_NODE;
            final long rightSibKey = ((IStructNode)getCurrentNode()).getFirstChildKey();
            final ElementNode node = createElementNode(parentKey, leftSibKey, rightSibKey, 0, mQName);

            setCurrentNode(node);
            adaptForInsert(node, true);
            adaptHashesWithAdd();

            return node.getNodeKey();
        } else {
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsRightSibling(final QName paramQName) throws AbsTTException {
        if (paramQName == null) {
            throw new NullPointerException("paramQName may not be null!");
        }
        if (getCurrentNode() instanceof IStructNode) {

            checkAccessAndCommit();

            final long parentKey = getCurrentNode().getParentKey();
            final long leftSibKey = getCurrentNode().getNodeKey();
            final long rightSibKey = ((IStructNode)getCurrentNode()).getRightSiblingKey();
            final ElementNode node = createElementNode(parentKey, leftSibKey, rightSibKey, 0, paramQName);

            setCurrentNode(node);
            adaptForInsert(node, false);
            adaptHashesWithAdd();

            return node.getNodeKey();
        } else {
            throw new TTUsageException(
                "Insert is not allowed if current node is not an StructuralNode (either Text or Element)!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsFirstChild(final String paramValueAsString) throws AbsTTException {
        if (paramValueAsString == null) {
            throw new NullPointerException("paramValueAsString may not be null!");
        }
        if (getCurrentNode() instanceof ElementNode || getCurrentNode() instanceof DocumentRootNode) {

            checkAccessAndCommit();

            final byte[] value = TypedValue.getBytes(paramValueAsString);
            final long parentKey = getCurrentNode().getNodeKey();
            final long leftSibKey = NULL_NODE;
            final long rightSibKey = ((IStructNode)getCurrentNode()).getFirstChildKey();
            final TextNode node = createTextNode(parentKey, leftSibKey, rightSibKey, value);

            setCurrentNode(node);
            adaptForInsert(node, true);
            adaptHashesWithAdd();

            return node.getNodeKey();
        } else {
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsRightSibling(final String paramValueAsString) throws AbsTTException {
        if (paramValueAsString == null) {
            throw new NullPointerException("paramValueAsString may not be null!");
        }

        if (getCurrentNode().getKind() == ENode.ELEMENT_KIND) {
            checkAccessAndCommit();

            final byte[] value = TypedValue.getBytes(paramValueAsString);
            final long parentKey = getCurrentNode().getParentKey();
            final long leftSibKey = getCurrentNode().getNodeKey();
            final long rightSibKey = ((IStructNode)getCurrentNode()).getRightSiblingKey();
            final TextNode node = createTextNode(parentKey, leftSibKey, rightSibKey, value);

            setCurrentNode(node);
            adaptForInsert(node, false);
            adaptHashesWithAdd();

            return node.getNodeKey();

        } else {
            throw new TTUsageException("Insert is not allowed if current node is not an element node!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertAttribute(final QName paramQName, final String paramValueAsString)
        throws AbsTTException {
        if (getCurrentNode() instanceof ElementNode) {

            checkAccessAndCommit();

            final byte[] value = TypedValue.getBytes(paramValueAsString);
            final long elementKey = getCurrentNode().getNodeKey();

            final int nameKey =
                getPageTransaction().createNameKey(PageWriteTransaction.buildName(paramQName));
            final int namespaceKey = getPageTransaction().createNameKey(paramQName.getNamespaceURI());
            final NodeDelegate nodeDel =
                new NodeDelegate(getPageTransaction().getMaxNodeKey() + 1, elementKey, 0);
            final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel, nameKey, namespaceKey);
            final ValNodeDelegate valDel = new ValNodeDelegate(nodeDel, value);

            final AttributeNode node =
                getPageTransaction().createNode(new AttributeNode(nodeDel, nameDel, valDel));

            final INode parentNode = getPageTransaction().prepareNodeForModification(node.getParentKey());
            ((ElementNode)parentNode).insertAttribute(node.getNodeKey());
            getPageTransaction().finishNodeModification(parentNode);

            setCurrentNode(node);
            adaptForInsert(node, false);

            adaptHashesWithAdd();
            return node.getNodeKey();

        } else {
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertNamespace(final QName paramQName) throws AbsTTException {
        if (paramQName == null) {
            throw new NullPointerException("QName may not be null!");
        }
        if (getCurrentNode() instanceof ElementNode) {

            checkAccessAndCommit();

            final int uriKey = getPageTransaction().createNameKey(paramQName.getNamespaceURI());
            // final String name =
            // paramQName.getPrefix().isEmpty() ? "xmlns" : "xmlns:" +
            // paramQName.getPrefix();
            final int prefixKey = getPageTransaction().createNameKey(paramQName.getPrefix());
            final long elementKey = getCurrentNode().getNodeKey();

            final NodeDelegate nodeDel =
                new NodeDelegate(getPageTransaction().getMaxNodeKey() + 1, elementKey, 0);
            final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel, prefixKey, uriKey);

            final NamespaceNode node = getPageTransaction().createNode(new NamespaceNode(nodeDel, nameDel));

            final INode parentNode = getPageTransaction().prepareNodeForModification(node.getParentKey());
            ((ElementNode)parentNode).insertNamespace(node.getNodeKey());
            getPageTransaction().finishNodeModification(parentNode);

            setCurrentNode(node);
            adaptForInsert(node, false);
            adaptHashesWithAdd();
            return node.getNodeKey();
        } else {
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    private ElementNode createElementNode(final long parentKey, final long mLeftSibKey,
        final long rightSibKey, final long hash, final QName mName) throws TTIOException {

        final int nameKey = getPageTransaction().createNameKey(PageWriteTransaction.buildName(mName));
        final int namespaceKey = getPageTransaction().createNameKey(mName.getNamespaceURI());

        final NodeDelegate nodeDel = new NodeDelegate(getPageTransaction().getMaxNodeKey() + 1, parentKey, 0);
        final StructNodeDelegate structDel =
            new StructNodeDelegate(nodeDel, NULL_NODE, rightSibKey, mLeftSibKey, 0);
        final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel, nameKey, namespaceKey);

        return getPageTransaction().createNode(
            new ElementNode(nodeDel, structDel, nameDel, new ArrayList<Long>(), new ArrayList<Long>()));
    }

    private TextNode createTextNode(final long mParentKey, final long mLeftSibKey, final long rightSibKey,
        final byte[] mValue) throws TTIOException {
        final NodeDelegate nodeDel =
            new NodeDelegate(getPageTransaction().getMaxNodeKey() + 1, mParentKey, 0);
        final ValNodeDelegate valDel = new ValNodeDelegate(nodeDel, mValue);
        final StructNodeDelegate structDel =
            new StructNodeDelegate(nodeDel, NULL_NODE, rightSibKey, mLeftSibKey, 0);

        return getPageTransaction().createNode(new TextNode(nodeDel, valDel, structDel));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void remove() throws AbsTTException {
        checkAccessAndCommit();
        if (getCurrentNode().getKind() == ENode.ROOT_KIND) {
            throw new TTUsageException("Document root can not be removed.");
        } else if (getCurrentNode() instanceof IStructNode) {
            final IStructNode node = (IStructNode)getCurrentNode();
            // Remove subtree, excluded since 1. axis is now moved to extra
            // bundle and 2. attributes and
            // namespaces are ignored
            // for (final AbsAxis desc = new DescendantAxis(this, false); desc
            // .hasNext(); desc.next()) {
            // getTransactionState().removeNode(getCurrentNode());
            // }
            moveTo(node.getNodeKey());
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
        } else if (getCurrentNode().getKind() == ENode.ATTRIBUTE_KIND) {
            final INode node = getCurrentNode();

            final ElementNode parent =
                (ElementNode)getPageTransaction().prepareNodeForModification(node.getParentKey());
            parent.removeAttribute(node.getNodeKey());
            getPageTransaction().finishNodeModification(parent);
            adaptHashesWithRemove();
            moveTo(getCurrentNode().getParentKey());
        } else if (getCurrentNode().getKind() == ENode.NAMESPACE_KIND) {
            final INode node = getCurrentNode();

            final ElementNode parent =
                (ElementNode)getPageTransaction().prepareNodeForModification(node.getParentKey());
            parent.removeNamespace(node.getNodeKey());
            getPageTransaction().finishNodeModification(parent);
            adaptHashesWithRemove();
            moveTo(getCurrentNode().getParentKey());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setQName(final QName paramName) throws AbsTTException {
        if (getCurrentNode() instanceof INameNode) {
            assertNotClosed();
            mModificationCount++;
            final long oldHash = getCurrentNode().hashCode();

            final INameNode node =
                (INameNode)getPageTransaction().prepareNodeForModification(getCurrentNode().getNodeKey());
            node.setNameKey(getPageTransaction().createNameKey(PageWriteTransaction.buildName(paramName)));
            getPageTransaction().finishNodeModification(node);

            setCurrentNode(node);
            adaptHashedWithUpdate(oldHash);
        } else {
            throw new TTUsageException(
                "setQName is not allowed if current node is not an INameNode implementation!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setURI(final String paramUri) throws AbsTTException {
        if (getCurrentNode() instanceof INameNode) {
            assertNotClosed();
            mModificationCount++;
            final long oldHash = getCurrentNode().hashCode();

            final INameNode node =
                (INameNode)getPageTransaction().prepareNodeForModification(getCurrentNode().getNodeKey());
            node.setURIKey(getPageTransaction().createNameKey(paramUri));
            getPageTransaction().finishNodeModification(node);

            setCurrentNode(node);
            adaptHashedWithUpdate(oldHash);
        } else {
            throw new TTUsageException(
                "setURI is not allowed if current node is not an INameNode implementation!");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(final String paramValue) throws AbsTTException {
        if (getCurrentNode() instanceof IValNode) {
            assertNotClosed();
            mModificationCount++;
            final long oldHash = getCurrentNode().hashCode();

            final IValNode node =
                (IValNode)getPageTransaction().prepareNodeForModification(getCurrentNode().getNodeKey());
            node.setValue(TypedValue.getBytes(paramValue));
            getPageTransaction().finishNodeModification(node);

            setCurrentNode(node);
            adaptHashedWithUpdate(oldHash);
        } else {
            throw new TTUsageException(
                "SetValue is not allowed if current node is not an IValNode implementation!");
        }
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
    public void revertTo(final long paramRevision) throws TTUsageException, TTIOException {
        if (paramRevision < 0) {
            throw new IllegalArgumentException("paramRevision parameter must be >= 0");
        }
        assertNotClosed();
        mSession.assertAccess(paramRevision);
        getPageTransaction().close();
        // Reset internal transaction state to new uber page.
        setPageTransaction(mSession.createWriteTransactionState(getTransactionID(), paramRevision,
            getRevisionNumber() - 1));
        // Reset modification counter.
        mModificationCount = 0L;
        moveTo(NodeReadTransaction.ROOT_NODE);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void commit() throws AbsTTException {

        assertNotClosed();

        // Commit uber page.
        final UberPage uberPage = getPageTransaction().commit();

        // Remember succesfully committed uber page in session state.
        mSession.setLastCommittedUberPage(uberPage);

        // Reset modification counter.
        mModificationCount = 0L;

        getPageTransaction().close();
        // Reset internal transaction state to new uber page.
        setPageTransaction(mSession.createWriteTransactionState(getTransactionID(), getRevisionNumber(),
            getRevisionNumber()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void abort() throws TTIOException {

        assertNotClosed();

        // Reset modification counter.
        mModificationCount = 0L;

        getPageTransaction().close();

        long revisionToSet = 0;
        if (!getPageTransaction().getUberPage().isBootstrap()) {
            revisionToSet = getRevisionNumber() - 1;
        }

        // Reset internal transaction state to last committed uber page.
        setPageTransaction(mSession.createWriteTransactionState(getTransactionID(), revisionToSet,
            revisionToSet));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws AbsTTException {
        if (!isClosed()) {
            // Make sure to commit all dirty data.
            if (mModificationCount > 0) {
                throw new TTUsageException("Must commit/abort transaction first");
            }
            // Release all state immediately.
            getPageTransaction().close();
            mSession.closeWriteTransaction(getTransactionID());
            setPageTransaction(null);
            setCurrentNode(null);
            // Remember that we are closed.
            setClosed();
        }
    }

    /**
     * Checking write access and intermediate commit.
     * 
     * @throws AbsTTException
     *             if anything weird happens
     */
    private void checkAccessAndCommit() throws AbsTTException {
        assertNotClosed();
        mModificationCount++;
        intermediateCommitIfRequired();
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
    private void adaptForInsert(final INode paramNewNode, final boolean addAsFirstChild) throws TTIOException {
        assert paramNewNode != null;

        if (paramNewNode instanceof IStructNode) {
            final IStructNode strucNode = (IStructNode)paramNewNode;
            final IStructNode parent =
                (IStructNode)getPageTransaction().prepareNodeForModification(paramNewNode.getParentKey());
            parent.incrementChildCount();
            if (addAsFirstChild) {
                parent.setFirstChildKey(paramNewNode.getNodeKey());
            }
            getPageTransaction().finishNodeModification(parent);

            if (strucNode.hasRightSibling()) {
                final IStructNode rightSiblingNode =
                    (IStructNode)getPageTransaction().prepareNodeForModification(
                        strucNode.getRightSiblingKey());
                rightSiblingNode.setLeftSiblingKey(paramNewNode.getNodeKey());
                getPageTransaction().finishNodeModification(rightSiblingNode);
            }
            if (strucNode.hasLeftSibling()) {
                final IStructNode leftSiblingNode =
                    (IStructNode)getPageTransaction().prepareNodeForModification(
                        strucNode.getLeftSiblingKey());
                leftSiblingNode.setRightSiblingKey(paramNewNode.getNodeKey());
                getPageTransaction().finishNodeModification(leftSiblingNode);
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
     * @param paramOldNode
     *            pointer of the old node to be replaces
     * @throws TTIOException
     *             if anything weird happens
     */
    private void adaptForRemove(final IStructNode paramOldNode) throws TTIOException {
        assert paramOldNode != null;

        // Adapt left sibling node if there is one.
        if (paramOldNode.hasLeftSibling()) {
            final IStructNode leftSibling =
                (IStructNode)getPageTransaction()
                    .prepareNodeForModification(paramOldNode.getLeftSiblingKey());
            leftSibling.setRightSiblingKey(paramOldNode.getRightSiblingKey());
            getPageTransaction().finishNodeModification(leftSibling);
        }

        // Adapt right sibling node if there is one.
        if (paramOldNode.hasRightSibling()) {
            final IStructNode rightSibling =
                (IStructNode)getPageTransaction().prepareNodeForModification(
                    paramOldNode.getRightSiblingKey());
            rightSibling.setLeftSiblingKey(paramOldNode.getLeftSiblingKey());
            getPageTransaction().finishNodeModification(rightSibling);
        }

        // Adapt parent, if node has now left sibling it is a first child.
        final IStructNode parent =
            (IStructNode)getPageTransaction().prepareNodeForModification(paramOldNode.getParentKey());
        if (!paramOldNode.hasLeftSibling()) {
            parent.setFirstChildKey(paramOldNode.getRightSiblingKey());
        }
        parent.decrementChildCount();
        getPageTransaction().finishNodeModification(parent);

        if (paramOldNode.getKind() == ENode.ELEMENT_KIND) {
            // removing attributes
            for (int i = 0; i < ((ElementNode)paramOldNode).getAttributeCount(); i++) {
                moveTo(((ElementNode)paramOldNode).getAttributeKey(i));
                getPageTransaction().removeNode(this.getCurrentNode());
            }
            // removing namespaces
            moveTo(paramOldNode.getNodeKey());
            for (int i = 0; i < ((ElementNode)paramOldNode).getNamespaceCount(); i++) {
                moveTo(((ElementNode)paramOldNode).getNamespaceKey(i));
                getPageTransaction().removeNode(this.getCurrentNode());
            }
        }

        // Remove old node.
        getPageTransaction().removeNode(paramOldNode);
    }

    // ////////////////////////////////////////////////////////////
    // end of remove operation
    // ////////////////////////////////////////////////////////////

    /**
     * Making an intermediate commit based on set attributes.
     * 
     * @throws AbsTTException
     *             if commit fails.
     */
    private void intermediateCommitIfRequired() throws AbsTTException {
        assertNotClosed();
        if ((mMaxNodeCount > 0) && (mModificationCount > mMaxNodeCount)) {
            commit();
        }
    }

    /**
     * Getter for superclasses.
     * 
     * @return The state of this transaction.
     */
    private PageWriteTransaction getPageTransaction() {
        return (PageWriteTransaction)super.mPageReadTransaction;
    }

    /**
     * Adapting the structure with a hash for all ancestors only with insert.
     * 
     * @throws TTIOException
     *             of anything weird happened.
     */
    private void adaptHashesWithAdd() throws TTIOException {
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
    private void adaptHashesWithRemove() throws TTIOException {
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
    private void adaptHashedWithUpdate(final long paramOldHash) throws TTIOException {
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
    private void postorderRemove() throws TTIOException {
        moveTo(getCurrentNode().getParentKey());
        postorderAdd();
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with
     * insert.
     * 
     * @throws TTIOException
     *             if anything weird happened
     */
    private void postorderAdd() throws TTIOException {
        // start with hash to add
        final INode startNode = getCurrentNode();
        // long for adapting the hash of the parent
        long hashCodeForParent = 0;
        // adapting the parent if the current node is no structural one.
        if (!(getCurrentNode() instanceof IStructNode)) {
            getPageTransaction().prepareNodeForModification(getCurrentNode().getNodeKey());
            getCurrentNode().setHash(getCurrentNode().hashCode());
            getPageTransaction().finishNodeModification(getCurrentNode());
            moveTo(getCurrentNode().getParentKey());
        }
        // Cursor to root
        IStructNode cursorToRoot;
        do {
            synchronized (getCurrentNode()) {
                cursorToRoot =
                    (IStructNode)getPageTransaction().prepareNodeForModification(
                        getCurrentNode().getNodeKey());
                hashCodeForParent = getCurrentNode().hashCode() + hashCodeForParent * PRIME;
                // Caring about attributes and namespaces if node is an element.
                if (cursorToRoot.getKind() == ENode.ELEMENT_KIND) {
                    final ElementNode currentElement = (ElementNode)cursorToRoot;
                    // setting the attributes and namespaces
                    for (int i = 0; i < ((ElementNode)cursorToRoot).getAttributeCount(); i++) {
                        moveTo(currentElement.getAttributeKey(i));
                        hashCodeForParent = getCurrentNode().hashCode() + hashCodeForParent * PRIME;
                    }
                    for (int i = 0; i < ((ElementNode)cursorToRoot).getNamespaceCount(); i++) {
                        moveTo(currentElement.getNamespaceKey(i));
                        hashCodeForParent = getCurrentNode().hashCode() + hashCodeForParent * PRIME;
                    }
                    moveTo(cursorToRoot.getNodeKey());
                }

                // Caring about the children of a node
                if (moveTo(((IStructNode)getNode()).getFirstChildKey())) {
                    do {
                        hashCodeForParent = getCurrentNode().getHash() + hashCodeForParent * PRIME;
                    } while (moveTo(((IStructNode)getNode()).getRightSiblingKey()));
                    moveTo(((IStructNode)getNode()).getParentKey());
                }

                // setting hash and resetting hash
                cursorToRoot.setHash(hashCodeForParent);
                getPageTransaction().finishNodeModification(cursorToRoot);
                hashCodeForParent = 0;
            }
        } while (moveTo(cursorToRoot.getParentKey()));

        setCurrentNode(startNode);
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
    private void rollingUpdate(final long paramOldHash) throws TTIOException {
        final INode newNode = getCurrentNode();
        final long newNodeHash = newNode.hashCode();
        long resultNew = newNode.hashCode();

        // go the path to the root
        do {
            synchronized (getCurrentNode()) {
                getPageTransaction().prepareNodeForModification(getCurrentNode().getNodeKey());
                if (getCurrentNode().getNodeKey() == newNode.getNodeKey()) {
                    resultNew = getCurrentNode().getHash() - paramOldHash;
                    resultNew = resultNew + newNodeHash;
                } else {
                    resultNew = getCurrentNode().getHash() - (paramOldHash * PRIME);
                    resultNew = resultNew + newNodeHash * PRIME;
                }
                getCurrentNode().setHash(resultNew);
                getPageTransaction().finishNodeModification(getCurrentNode());
            }
        } while (moveTo(getCurrentNode().getParentKey()));

        setCurrentNode(newNode);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with
     * remove.
     * 
     * @throws TTIOException
     *             if anything weird happened
     */
    private void rollingRemove() throws TTIOException {
        final INode startNode = getCurrentNode();
        long hashToRemove = startNode.getHash();
        long hashToAdd = 0;
        long newHash = 0;
        // go the path to the root
        do {
            synchronized (getCurrentNode()) {
                getPageTransaction().prepareNodeForModification(getCurrentNode().getNodeKey());
                if (getCurrentNode().getNodeKey() == startNode.getNodeKey()) {
                    // the begin node is always null
                    newHash = 0;
                } else if (getCurrentNode().getNodeKey() == startNode.getParentKey()) {
                    // the parent node is just removed
                    newHash = getCurrentNode().getHash() - (hashToRemove * PRIME);
                    hashToRemove = getCurrentNode().getHash();
                } else {
                    // the ancestors are all touched regarding the modification
                    newHash = getCurrentNode().getHash() - (hashToRemove * PRIME);
                    newHash = newHash + hashToAdd * PRIME;
                    hashToRemove = getCurrentNode().getHash();
                }
                getCurrentNode().setHash(newHash);
                hashToAdd = newHash;
                getPageTransaction().finishNodeModification(getCurrentNode());
            }
        } while (moveTo(getCurrentNode().getParentKey()));

        setCurrentNode(startNode);
    }

    /**
     * Adapting the structure with a rolling hash for all ancestors only with
     * insert.
     * 
     * @throws TTIOException
     *             if anything weird happened
     */
    private void rollingAdd() throws TTIOException {
        // start with hash to add
        final INode startNode = getCurrentNode();
        long hashToAdd = startNode.hashCode();
        long newHash = 0;
        long possibleOldHash = 0;
        // go the path to the root
        do {
            synchronized (getCurrentNode()) {
                getPageTransaction().prepareNodeForModification(getCurrentNode().getNodeKey());
                if (getCurrentNode().getNodeKey() == startNode.getNodeKey()) {
                    // at the beginning, take the hashcode of the node only
                    newHash = hashToAdd;
                } else if (getCurrentNode().getNodeKey() == startNode.getParentKey()) {
                    // at the parent level, just add the node
                    possibleOldHash = getCurrentNode().getHash();
                    newHash = possibleOldHash + hashToAdd * PRIME;
                    hashToAdd = newHash;
                } else {
                    // at the rest, remove the existing old key for this element
                    // and add the new one
                    newHash = getCurrentNode().getHash() - (possibleOldHash * PRIME);
                    newHash = newHash + hashToAdd * PRIME;
                    hashToAdd = newHash;
                    possibleOldHash = getCurrentNode().getHash();
                }
                getCurrentNode().setHash(newHash);
                getPageTransaction().finishNodeModification(getCurrentNode());
            }
        } while (moveTo(getCurrentNode().getParentKey()));
        setCurrentNode(startNode);
    }
}

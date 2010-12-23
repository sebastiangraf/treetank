package com.treetank.access;

import java.util.concurrent.locks.Lock;

import javax.xml.namespace.QName;

import com.treetank.api.IAxis;
import com.treetank.api.IItem;
import com.treetank.api.IStructuralItem;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TTException;
import com.treetank.exception.TTIOException;
import com.treetank.exception.TTUsageException;
import com.treetank.node.AbsNode;
import com.treetank.node.AbsStructNode;
import com.treetank.node.AttributeNode;
import com.treetank.node.DocumentRootNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.node.NamespaceNode;
import com.treetank.node.TextNode;
import com.treetank.page.UberPage;
import com.treetank.settings.EFixed;
import com.treetank.utils.TypedValue;

public class SynchWriteTransaction extends WriteTransaction {

    private LockManager lock;
    protected SynchWriteTransaction(long mTransactionID, SessionState mSessionState,
        WriteTransactionState mTransactionState, int maxNodeCount, int maxTime) throws TTException {
        super(mTransactionID, mSessionState, mTransactionState, maxNodeCount, maxTime);
       lock = LockManager.getLockManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsFirstChild(final QName mQname) throws TTException {
       lock.getWritePermission(getCurrentNode().getNodeKey(), this);
       return super.insertElementAsFirstChild(mQname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsRightSibling(final QName mQname) throws TTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return super.insertElementAsRightSibling(mQname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsFirstChild(final String mValueAsString) throws TTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return super.insertTextAsFirstChild(mValueAsString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsRightSibling(final String mValueAsString) throws TTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return insertTextAsRightSibling(mValueAsString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertAttribute(final QName mQname, final String mValueAsString)
        throws TTException {
        if (getCurrentNode() instanceof ElementNode) {
            lock.getWritePermission(getCurrentNode().getNodeKey(), this);
            return super.insertAttribute(mQname, mValueAsString);
        } else {
            throw new TTUsageException("Insert is not allowed if current node is not an ElementNode!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertNamespace(final String mUri, final String mPrefix)
        throws TTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return super.insertNamespace(mUri, mPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void remove() throws TTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        super.remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setName(final String mName) throws TTIOException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        super.setName(mName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setURI(final String mUri) throws TTIOException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        super.setURI(mUri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(final int mValueType, final byte[] mValue) throws TTIOException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        super.setValue(mValueType, mValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setValue(final String mValue) throws TTIOException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        super.setValue(mValue);
    }
}

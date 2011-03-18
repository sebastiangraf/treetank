package org.treetank.access;

import java.util.concurrent.locks.Lock;

import javax.xml.namespace.QName;

import org.treetank.api.IItem;
import org.treetank.api.IStructuralItem;
import org.treetank.axis.DescendantAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.node.AbsNode;
import org.treetank.node.AbsStructNode;
import org.treetank.node.AttributeNode;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.ENodes;
import org.treetank.node.ElementNode;
import org.treetank.node.NamespaceNode;
import org.treetank.node.TextNode;
import org.treetank.page.UberPage;
import org.treetank.settings.EFixed;
import org.treetank.utils.TypedValue;

public class SynchWriteTransaction extends WriteTransaction {

    private LockManager lock;
    protected SynchWriteTransaction(long mTransactionID, SessionState mSessionState,
        WriteTransactionState mTransactionState, int maxNodeCount, int maxTime) throws AbsTTException {
        super(mTransactionID, mSessionState, mTransactionState, maxNodeCount, maxTime);
       lock = LockManager.getLockManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsFirstChild(final QName mQname) throws AbsTTException {
       lock.getWritePermission(getCurrentNode().getNodeKey(), this);
       return super.insertElementAsFirstChild(mQname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertElementAsRightSibling(final QName mQname) throws AbsTTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return super.insertElementAsRightSibling(mQname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsFirstChild(final String mValueAsString) throws AbsTTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return super.insertTextAsFirstChild(mValueAsString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertTextAsRightSibling(final String mValueAsString) throws AbsTTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return insertTextAsRightSibling(mValueAsString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized long insertAttribute(final QName mQname, final String mValueAsString)
        throws AbsTTException {
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
        throws AbsTTException {
        lock.getWritePermission(getCurrentNode().getNodeKey(), this);
        return super.insertNamespace(mUri, mPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void remove() throws AbsTTException {
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

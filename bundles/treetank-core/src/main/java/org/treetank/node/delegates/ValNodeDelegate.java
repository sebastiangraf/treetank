package org.treetank.node.delegates;

import java.util.Arrays;

import org.treetank.api.IVisitor;
import org.treetank.node.ENodes;
import org.treetank.node.interfaces.IValNode;

/**
 * Delegate method for all nodes containing \"value\"-data. That means that
 * independent values are stored by the nodes delegating the calls of the
 * interface {@link IValNode} to this class.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class ValNodeDelegate implements IValNode {

    /** Delegate for common node information. */
    private NodeDelegate mDelegate;
    /** Storing the value. */
    private byte[] mVal;

    /**
     * Constructor
     * 
     * @param pNodeDel
     *            the common data.
     * @param pVal
     *            the own value.
     */
    public ValNodeDelegate(final NodeDelegate pNodeDel, final byte[] pVal) {
        this.mDelegate = pNodeDel;
        mVal = pVal;
    }

    /**
     * Delegate method for getKind.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getKind()
     */
    public ENodes getKind() {
        return mDelegate.getKind();
    }

    /**
     * Delegate method for getNodeKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getNodeKey()
     */
    public long getNodeKey() {
        return mDelegate.getNodeKey();
    }

    /**
     * Delegate method for setNodeKey.
     * 
     * @param pNodeKey
     * @see org.treetank.node.delegates.NodeDelegate#setNodeKey(long)
     */
    public void setNodeKey(long pNodeKey) {
        mDelegate.setNodeKey(pNodeKey);
    }

    /**
     * Delegate method for getParentKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDelegate.getParentKey();
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param pParentKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(long pParentKey) {
        mDelegate.setParentKey(pParentKey);
    }

    /**
     * Delegate method for getHash.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getHash()
     */
    public long getHash() {
        return mDelegate.getHash();
    }

    /**
     * Delegate method for setHash.
     * 
     * @param pHash
     * @see org.treetank.node.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(long pHash) {
        mDelegate.setHash(pHash);
    }

    /**
     * Delegate method for acceptVisitor.
     * 
     * @param pVisitor
     * @see org.treetank.node.delegates.NodeDelegate#acceptVisitor(org.treetank.api.IVisitor)
     */
    public void acceptVisitor(IVisitor pVisitor) {
        mDelegate.acceptVisitor(pVisitor);
    }

    /**
     * Delegate method for getTypeKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDelegate.getTypeKey();
    }

    /**
     * Delegate method for setTypeKey.
     * 
     * @param pTypeKey
     * @see org.treetank.node.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(int pTypeKey) {
        mDelegate.setTypeKey(pTypeKey);
    }

    /**
     * Delegate method for hasParent.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#hasParent()
     */
    public boolean hasParent() {
        return mDelegate.hasParent();
    }

    /**
     * Delegate method for clone.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#clone()
     */
    public ValNodeDelegate clone() {
        final byte[] newVal = new byte[mVal.length];
        System.arraycopy(mVal, 0, newVal, 0, newVal.length);
        return new ValNodeDelegate(mDelegate.clone(), newVal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawValue() {
        return mVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final byte[] pVal) {
        mVal = pVal;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(mVal);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ValNodeDelegate other = (ValNodeDelegate) obj;
        if (!Arrays.equals(mVal, other.mVal))
            return false;
        return true;
    }

    /**
     * Delegate method for toString.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#toString()
     */
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("value: ");
        builder.append(new String(mVal));
        return builder.toString();
    }
}

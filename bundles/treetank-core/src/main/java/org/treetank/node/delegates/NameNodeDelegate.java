/**
 * 
 */
package org.treetank.node.delegates;

import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.node.ENodes;
import org.treetank.node.interfaces.INameNode;

/**
 * Delegate method for all nodes containing \"naming\"-data. That means that
 * different fixed defined names are represented by the nodes delegating the
 * calls of the interface {@link INameNode} to this class. Mainly, keys are
 * stored referencing later on to the string stored in dedicated pages.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NameNodeDelegate implements INameNode {

    /** Node delegate, containing basic node information. */
    private final NodeDelegate mDelegate;
    /** Key of the name. The name contains the prefix as well. */
    private int mNameKey;
    /** URI of the related namespace. */
    private int mUriKey;

    /**
     * Constructor.
     * 
     * @param pDel
     *            page delegator
     * @param pNameKey
     *            namekey to be stored
     * @param pUriKey
     *            urikey to be stored
     */
    public NameNodeDelegate(final NodeDelegate pDel, final int pNameKey,
            final int pUriKey) {
        mDelegate = pDel;
        mNameKey = pNameKey;
        mUriKey = pUriKey;
    }

    /**
     * Delegate method for setHash.
     * 
     * @param pHash
     * @see org.treetank.node.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(final long pHash) {
        mDelegate.setHash(pHash);
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
     * Delegate method for setNodeKey.
     * 
     * @param pNodeKey
     * @see org.treetank.node.delegates.NodeDelegate#setNodeKey(long)
     */
    public void setNodeKey(final long pNodeKey) {
        mDelegate.setNodeKey(pNodeKey);
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
     * Delegate method for getParentKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getParentKey()
     */
    public long getParentKey() {
        return mDelegate.getParentKey();
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
     * Delegate method for getTypeKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getTypeKey()
     */
    public int getTypeKey() {
        return mDelegate.getTypeKey();
    }

    /**
     * Delegate method for serialize.
     * 
     * @param pSink
     * @see org.treetank.node.delegates.NodeDelegate#serialize(org.treetank.io.ITTSink)
     */
    public void serialize(final ITTSink pSink) {
        pSink.writeInt(mNameKey);
        pSink.writeInt(mUriKey);
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param pNodeKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(final long pNodeKey) {
        mDelegate.setParentKey(pNodeKey);
    }

    /**
     * Delegate method for setType.
     * 
     * @param pTypeKey
     * @see org.treetank.node.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(final int pTypeKey) {
        mDelegate.setTypeKey(pTypeKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.NAMESPACE_KIND;
    }

    /** {@inheritDoc} */
    @Override
    public NameNodeDelegate clone() {
        final NodeDelegate clone = mDelegate.clone();
        return new NameNodeDelegate(clone, mNameKey, mUriKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptVisitor(final IVisitor pVisitor) {
        mDelegate.acceptVisitor(pVisitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameKey() {
        return mNameKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return mUriKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int pNameKey) {
        mNameKey = pNameKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int pUriKey) {
        mUriKey = pUriKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mNameKey;
        result = prime * result + mUriKey;
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
        NameNodeDelegate other = (NameNodeDelegate) obj;
        if (mNameKey != other.mNameKey)
            return false;
        if (mUriKey != other.mUriKey)
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
        builder.append("\nuri key: ");
        builder.append(mUriKey);
        builder.append("\nname key: ");
        builder.append(mNameKey);
        return builder.toString();
    }
}

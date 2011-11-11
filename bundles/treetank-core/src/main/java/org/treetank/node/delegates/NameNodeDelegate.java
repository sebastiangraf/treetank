/**
 * 
 */
package org.treetank.node.delegates;

import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.node.ENodes;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.INode;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NameNodeDelegate implements INameNode {

    private final NodeDelegate mDelegate;
    private int mNameKey;
    private int mUriKey;

    public NameNodeDelegate(final NodeDelegate paramDelegate, final int paramNameKey, final int paramUriKey) {
        mDelegate = paramDelegate;
        mNameKey = paramNameKey;
        mUriKey = paramUriKey;
    }

    /**
     * Delegate method for setHash.
     * 
     * @param paramHash
     * @see org.treetank.node.delegates.NodeDelegate#setHash(long)
     */
    public void setHash(long paramHash) {
        mDelegate.setHash(paramHash);
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
     * @param paramKey
     * @see org.treetank.node.delegates.NodeDelegate#setNodeKey(long)
     */
    public void setNodeKey(long paramKey) {
        mDelegate.setNodeKey(paramKey);
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
     * @param paramSink
     * @see org.treetank.node.delegates.NodeDelegate#serialize(org.treetank.io.ITTSink)
     */
    public void serialize(ITTSink paramSink) {
        paramSink.writeInt(mNameKey);
        paramSink.writeInt(mUriKey);
    }

    /**
     * Delegate method for setParentKey.
     * 
     * @param paramKey
     * @see org.treetank.node.delegates.NodeDelegate#setParentKey(long)
     */
    public void setParentKey(long paramKey) {
        mDelegate.setParentKey(paramKey);
    }

    /**
     * Delegate method for setType.
     * 
     * @param paramType
     * @see org.treetank.node.delegates.NodeDelegate#setTypeKey(int)
     */
    public void setTypeKey(int paramType) {
        mDelegate.setTypeKey(paramType);
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
    public void acceptVisitor(IVisitor paramVisitor) {
        mDelegate.acceptVisitor(paramVisitor);
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
    public void setNameKey(int paramNameKey) {
        mNameKey = paramNameKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(int paramUriKey) {
        mUriKey = paramUriKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mDelegate == null) ? 0 : mDelegate.hashCode());
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
        NameNodeDelegate other = (NameNodeDelegate)obj;
        if (mDelegate == null) {
            if (other.mDelegate != null)
                return false;
        } else if (!mDelegate.equals(other.mDelegate))
            return false;
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
        builder.append("uri key: ");
        builder.append(mUriKey);
        builder.append("\nname key: ");
        builder.append(mNameKey);
        return builder.toString();
    }
}

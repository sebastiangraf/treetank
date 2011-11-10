package org.treetank.node.delegates;

import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.node.ENodes;
import org.treetank.node.interfaces.IValNode;

public class ValNodeDelegate implements IValNode {

    private NodeDelegate mDelegate;
    private byte[] mVal;

    public ValNodeDelegate(final NodeDelegate paramNodeDelegate, final byte[] paramVal) {
        this.mDelegate = paramNodeDelegate;
        mVal = paramVal;
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
     * Delegate method for getKind.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getKind()
     */
    public ENodes getKind() {
        return mDelegate.getKind();
    }

    /**
     * Delegate method for getNameKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getNameKey()
     */
    public int getNameKey() {
        return mDelegate.getNameKey();
    }

    /**
     * Delegate method for getURIKey.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#getURIKey()
     */
    public int getURIKey() {
        return mDelegate.getURIKey();
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
     * Delegate method for acceptVisitor.
     * 
     * @param paramVisitor
     * @see org.treetank.node.delegates.NodeDelegate#acceptVisitor(org.treetank.api.IVisitor)
     */
    public void acceptVisitor(IVisitor paramVisitor) {
        mDelegate.acceptVisitor(paramVisitor);
    }

    /**
     * Delegate method for serialize.
     * 
     * @param paramSink
     * @see org.treetank.node.delegates.NodeDelegate#serialize(org.treetank.io.ITTSink)
     */
    public void serialize(ITTSink paramSink) {
        mDelegate.serialize(paramSink);
    }

    /**
     * Delegate method for setNameKey.
     * 
     * @param paramNameKey
     * @see org.treetank.node.delegates.NodeDelegate#setNameKey(int)
     */
    public void setNameKey(int paramNameKey) {
        mDelegate.setNameKey(paramNameKey);
    }

    /**
     * Delegate method for setURIKey.
     * 
     * @param paramUriKey
     * @see org.treetank.node.delegates.NodeDelegate#setURIKey(int)
     */
    public void setURIKey(int paramUriKey) {
        mDelegate.setURIKey(paramUriKey);
    }

    /**
     * Delegate method for clone.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#clone()
     */
    public NodeDelegate clone() {
        return mDelegate.clone();
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
     * @see org.treetank.node.delegates.NodeDelegate#setType(int)
     */
    public void setType(int paramType) {
        mDelegate.setType(paramType);
    }

    /**
     * Delegate method for toString.
     * 
     * @return
     * @see org.treetank.node.delegates.NodeDelegate#toString()
     */
    public String toString() {
        return mDelegate.toString();
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
    public void setValue(byte[] paramVal) {
        mVal = paramVal;

    }

}

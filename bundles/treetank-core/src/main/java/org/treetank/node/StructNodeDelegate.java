package org.treetank.node;

import org.treetank.api.IStructuralItem;
import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.settings.EFixed;

public class StructNodeDelegate implements IStructuralItem {

    private long mFirstChild;
    private long mRightSibling;
    private long mLeftSibling;
    private long mChildCount;

    private final NodeDelegate mNodeDelegate;

    public StructNodeDelegate(final NodeDelegate paramNodeDelegate) {
        mNodeDelegate = paramNodeDelegate;
    }

    @Override
    public void setHash(long paramHash) {
        mNodeDelegate.setHash(paramHash);
    }

    @Override
    public long getHash() {
        return mNodeDelegate.getHash();
    }

    @Override
    public void setNodeKey(long paramKey) {
        mNodeDelegate.setNodeKey(paramKey);
    }

    @Override
    public long getNodeKey() {
        return mNodeDelegate.getNodeKey();
    }

    @Override
    public long getParentKey() {
        return mNodeDelegate.getParentKey();
    }

    @Override
    public boolean hasParent() {
        return mNodeDelegate.hasParent();
    }

    @Override
    public byte[] getRawValue() {
        return mNodeDelegate.getRawValue();
    }

    @Override
    public ENodes getKind() {
        return mNodeDelegate.getKind();
    }

    @Override
    public int getNameKey() {
        return mNodeDelegate.getNameKey();
    }

    @Override
    public int getURIKey() {
        return mNodeDelegate.getURIKey();
    }

    @Override
    public int getTypeKey() {
        return mNodeDelegate.getTypeKey();
    }

    @Override
    public void acceptVisitor(IVisitor paramVisitor) {
        mNodeDelegate.acceptVisitor(paramVisitor);
    }

    @Override
    public boolean hasFirstChild() {
        return mFirstChild != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    @Override
    public boolean hasLeftSibling() {
        return mLeftSibling != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    @Override
    public boolean hasRightSibling() {
        return mRightSibling != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    @Override
    public long getChildCount() {
        return mChildCount;
    }

    @Override
    public long getFirstChildKey() {
        return mFirstChild;
    }

    @Override
    public long getLeftSiblingKey() {
        return mLeftSibling;
    }

    @Override
    public long getRightSiblingKey() {
        return mRightSibling;
    }

    @Override
    public void serialize(ITTSink paramSink) {
        mNodeDelegate.serialize(paramSink);
        paramSink.writeLong(mFirstChild);
        paramSink.writeLong(mRightSibling);
        paramSink.writeLong(mLeftSibling);
        paramSink.writeLong(mChildCount);
    }

    public StructNodeDelegate clone() {
        return this;
    }

    @Override
    public void setNameKey(int paramNameKey) {
        mNodeDelegate.setNameKey(paramNameKey);
    }

    @Override
    public void setURIKey(int paramUriKey) {
        mNodeDelegate.setURIKey(paramUriKey);

    }

    @Override
    public void setValue(int paramUriKey, byte[] paramVal) {
        mNodeDelegate.setValue(paramUriKey, paramVal);
    }

}

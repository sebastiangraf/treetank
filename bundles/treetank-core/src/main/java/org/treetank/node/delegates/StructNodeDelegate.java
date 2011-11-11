package org.treetank.node.delegates;

import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.node.ENodes;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.settings.EFixed;

public class StructNodeDelegate implements IStructNode {

    private long mFirstChild;
    private long mRightSibling;
    private long mLeftSibling;
    private long mChildCount;

    private final NodeDelegate mDelegate;

    public StructNodeDelegate(final NodeDelegate paramDel, final long paramFirstChild,
        final long paramRightSibling, final long paramLeftSibling, final long paramChildCount) {
        mDelegate = paramDel;
        mFirstChild = paramFirstChild;
        mRightSibling = paramRightSibling;
        mLeftSibling = paramLeftSibling;
        mChildCount = paramChildCount;
    }

    @Override
    public void setHash(long paramHash) {
        mDelegate.setHash(paramHash);
    }

    @Override
    public long getHash() {
        return mDelegate.getHash();
    }

    @Override
    public void setNodeKey(long paramKey) {
        mDelegate.setNodeKey(paramKey);
    }

    @Override
    public long getNodeKey() {
        return mDelegate.getNodeKey();
    }

    @Override
    public long getParentKey() {
        return mDelegate.getParentKey();
    }

    @Override
    public boolean hasParent() {
        return mDelegate.hasParent();
    }

    @Override
    public ENodes getKind() {
        return mDelegate.getKind();
    }

    @Override
    public int getTypeKey() {
        return mDelegate.getTypeKey();
    }

    @Override
    public void acceptVisitor(IVisitor paramVisitor) {
        mDelegate.acceptVisitor(paramVisitor);
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
        paramSink.writeLong(mFirstChild);
        paramSink.writeLong(mRightSibling);
        paramSink.writeLong(mLeftSibling);
        paramSink.writeLong(mChildCount);
    }

    public StructNodeDelegate clone() {
        return new StructNodeDelegate(mDelegate.clone(), mFirstChild, mRightSibling, mLeftSibling,
            mChildCount);
    }

    @Override
    public void setRightSiblingKey(long paramKey) {
        mRightSibling = paramKey;
    }

    @Override
    public void setLeftSiblingKey(long paramKey) {
        mLeftSibling = paramKey;
    }

    @Override
    public void setFirstChildKey(long paramKey) {
        mFirstChild = paramKey;
    }

    @Override
    public void decrementChildCount() {
        mChildCount--;
    }

    @Override
    public void incrementChildCount() {
        mChildCount++;
    }

    @Override
    public void setParentKey(long paramKey) {
        mDelegate.setParentKey(paramKey);
    }

    @Override
    public void setType(int paramType) {
        mDelegate.setType(paramType);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("first child: ");
        builder.append(getFirstChildKey());
        builder.append("\nleft sib: ");
        builder.append(getLeftSiblingKey());
        builder.append(getLeftSiblingKey());
        builder.append("\nright sib: ");
        builder.append(getRightSiblingKey());
        builder.append("\nfirst child: ");
        builder.append(getFirstChildKey());
        builder.append("\nchild count: ");
        builder.append(getChildCount());
        return builder.toString();
    }
}

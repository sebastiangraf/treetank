package org.treetank.node;

import org.treetank.api.IItem;
import org.treetank.api.IVisitor;
import org.treetank.settings.EFixed;

public class NodeDelegate implements IItem {

    private long mNodeKey;
    private long mParentKey;
    private long mHash;

    private int mType;

    public NodeDelegate() {

    }

    @Override
    public void setHash(long paramHash) {
        mHash = paramHash;
    }

    @Override
    public long getHash() {
        return mHash;
    }

    @Override
    public void setNodeKey(long paramKey) {
        this.mNodeKey = paramKey;

    }

    @Override
    public long getNodeKey() {
        return mNodeKey;
    }

    @Override
    public long getParentKey() {
        return mParentKey;
    }

    @Override
    public boolean hasParent() {
        return mParentKey != (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
    }

    @Override
    public ENodes getKind() {
        return ENodes.UNKOWN_KIND;
    }

    @Override
    public int getNameKey() {
        return (Integer)EFixed.NULL_INT_KEY.getStandardProperty();
    }

    @Override
    public int getURIKey() {
        return (Integer)EFixed.NULL_INT_KEY.getStandardProperty();

    }

    @Override
    public int getTypeKey() {
        return mType;
    }

    @Override
    public void acceptVisitor(IVisitor paramVisitor) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] getRawValue() {
        // TODO Auto-generated method stub
        return null;
    }

}

package org.treetank.node;

import org.treetank.api.IItem;
import org.treetank.api.IVisitor;
import org.treetank.settings.EFixed;

public class NodeDelegate implements IItem, Comparable<IItem> {

    private long mNodeKey;
    private long mParentKey;

    public NodeDelegate() {

    }

    @Override
    public void setHash(long paramHash) {
        // TODO Auto-generated method stub

    }

    @Override
    public long getHash() {
        // TODO Auto-generated method stub
        return 0;
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
        return 0;
    }

    @Override
    public void acceptVisitor(IVisitor paramVisitor) {
        // TODO Auto-generated method stub

    }

    @Override
    public int compareTo(IItem o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getRawValue() {
        // TODO Auto-generated method stub
        return null;
    }

}

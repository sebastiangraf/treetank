package org.treetank.node;

import org.treetank.api.IItem;
import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.settings.EFixed;

public class NodeDelegate implements IItem {

    private long mNodeKey;
    private long mParentKey;
    private long mHash;
    private int mType;

    public NodeDelegate(final long paramNodeKey, final long paramParentKey, final long paramHash,
        final int paramType) {
        mNodeKey = paramNodeKey;
        mParentKey = paramParentKey;
        mHash = paramHash;
        mType = paramType;
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
        // TODO Do nothing, only stub
    }

    @Override
    public byte[] getRawValue() {
        return null;
    }

    @Override
    public void serialize(ITTSink paramSink) {
        paramSink.writeLong(mNodeKey);
        paramSink.writeLong(mParentKey);
        paramSink.writeLong(mHash);
        paramSink.writeInt(mType);
    }

    @Override
    public void setNameKey(int paramNameKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setURIKey(int paramUriKey) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setValue(int paramUriKey, byte[] paramVal) {
        // TODO Auto-generated method stub

    }

    public NodeDelegate clone() {
        return new NodeDelegate(mNodeKey, mParentKey, mHash, mType);
    }

    @Override
    public void setParentKey(long paramKey) {
        mParentKey = paramKey;
    }

    @Override
    public void setType(int paramType) {
        // TODO Auto-generated method stub

    }

}

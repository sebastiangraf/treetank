package org.treetank.node;

import org.treetank.api.IItem;
import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.settings.EFixed;

public class NodeDelegate implements IItem {

    private final static int NULL_VAL = 0;

    private long mNodeKey;
    private long mParentKey;
    private long mHash;

    public NodeDelegate(final long paramNodeKey, final long paramParentKey, final long paramHash) {
        mNodeKey = paramNodeKey;
        mParentKey = paramParentKey;
        mHash = paramHash;
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
        // Do nothing, only stub
        return NULL_VAL;
    }

    @Override
    public void acceptVisitor(IVisitor paramVisitor) {
        // Do nothing, only stub
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
    }

    @Override
    public void setNameKey(int paramNameKey) {
        // Do nothing, only stub
    }

    @Override
    public void setURIKey(int paramUriKey) {
        // Do nothing, only stub
    }

    @Override
    public void setValue(int paramUriKey, byte[] paramVal) {
        // Do nothing, only stub
    }

    public NodeDelegate clone() {
        return new NodeDelegate(mNodeKey, mParentKey, mHash);
    }

    @Override
    public void setParentKey(long paramKey) {
        mParentKey = paramKey;
    }

    @Override
    public void setType(int paramType) {
        // Do nothing, only stub
    }

    @Override
    public String toString() {
        return new StringBuilder("node key: ").append(getNodeKey()).append("\nparentKey: ").append(
            getParentKey()).append("\ntypeKey: ").append(getTypeKey()).append("\nhash: ").append(getHash())
            .toString();
    }

}

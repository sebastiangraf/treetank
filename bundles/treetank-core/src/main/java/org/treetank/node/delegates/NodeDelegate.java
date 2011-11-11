package org.treetank.node.delegates;

import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.node.ENodes;
import org.treetank.node.interfaces.INode;
import org.treetank.settings.EFixed;
import org.treetank.utils.NamePageHash;

public class NodeDelegate implements INode {

    private long mNodeKey;
    private long mParentKey;
    private long mHash;
    private int mType;

    public NodeDelegate(final long paramNodeKey, final long paramParentKey, final long paramHash) {
        mNodeKey = paramNodeKey;
        mParentKey = paramParentKey;
        mHash = paramHash;
        mType = NamePageHash.generateHashForString("xs:untyped");
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
    public int getTypeKey() {
        return mType;
    }

    @Override
    public void acceptVisitor(IVisitor paramVisitor) {
        // Do nothing, only stub
    }

    @Override
    public void serialize(ITTSink paramSink) {
        paramSink.writeLong(mNodeKey);
        paramSink.writeLong(mParentKey);
        paramSink.writeLong(mHash);
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
        mType = paramType;
    }

    @Override
    public String toString() {
        return new StringBuilder("node key: ").append(getNodeKey()).append("\nparentKey: ").append(
            getParentKey()).append("\ntypeKey: ").append(getTypeKey()).append("\nhash: ").append(getHash())
            .toString();
    }

}

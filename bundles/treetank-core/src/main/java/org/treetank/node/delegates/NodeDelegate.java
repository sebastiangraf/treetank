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
    public void setTypeKey(int paramType) {
        mType = paramType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(mHash ^ (mHash >>> 32));
        result = prime * result + (int)(mNodeKey ^ (mNodeKey >>> 32));
        result = prime * result + (int)(mParentKey ^ (mParentKey >>> 32));
        result = prime * result + mType;
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
        NodeDelegate other = (NodeDelegate)obj;
        if (mHash != other.mHash)
            return false;
        if (mNodeKey != other.mNodeKey)
            return false;
        if (mParentKey != other.mParentKey)
            return false;
        if (mType != other.mType)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("node key: ");
        builder.append(mNodeKey);
        builder.append("\nparent key: ");
        builder.append(mParentKey);
        builder.append("\ntype key: ");
        builder.append(mType);
        builder.append("\nhash: ");
        builder.append(mHash);
        return builder.toString();
    }

}

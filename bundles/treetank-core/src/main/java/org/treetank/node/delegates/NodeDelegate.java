package org.treetank.node.delegates;

import org.treetank.api.IVisitor;
import org.treetank.io.ITTSink;
import org.treetank.node.ENodes;
import org.treetank.node.interfaces.INode;
import org.treetank.settings.EFixed;
import org.treetank.utils.NamePageHash;

/**
 * Delegate method for all nodes. That means that all nodes stored in Treetank
 * are represented by an instance of the interface {@link INode} namely
 * containing the position in the tree related to a parent-node, the related
 * type and the corresponding hash recursivly computed.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NodeDelegate implements INode {

    /** Key of the current node. Must be unique for all nodes. */
    private long mNodeKey;
    /** Key of the parent node. */
    private long mParentKey;
    /** Hash of the parent node. */
    private long mHash;
    /**
     * TypeKey of the parent node. Can be referenced later on over special
     * pages.
     */
    private int mTypeKey;

    /**
     * Constructor.
     * 
     * @param pNodeKey
     *            to be represented by this delegate.
     * @param pParentKey
     *            to be represented by this delegate
     * @param pHash
     *            to be represented by this delegate
     */
    public NodeDelegate(final long pNodeKey, final long pParentKey,
            final long pHash) {
        setNodeKey(pNodeKey);
        setParentKey(pParentKey);
        setHash(pHash);
        setTypeKey(NamePageHash.generateHashForString("xs:untyped"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.UNKOWN_KIND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNodeKey() {
        return mNodeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodeKey(final long pNodeKey) {
        this.mNodeKey = pNodeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getParentKey() {
        return mParentKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentKey(final long pParentKey) {
        this.mParentKey = pParentKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getHash() {
        return mHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHash(final long pHash) {
        this.mHash = pHash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptVisitor(final IVisitor pVisitor) {
        // Do nothing, only stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink pSink) {
        pSink.writeLong(getNodeKey());
        pSink.writeLong(getParentKey());
        pSink.writeLong(getHash());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDelegate clone() {
        return new NodeDelegate(getNodeKey(), getParentKey(), getHash());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (getHash() ^ (getHash() >>> 32));
        result = prime * result + (int) (getNodeKey() ^ (getNodeKey() >>> 32));
        result = prime * result
                + (int) (getParentKey() ^ (getParentKey() >>> 32));
        result = prime * result + getTypeKey();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object pObj) {
        if (this == pObj)
            return true;
        if (pObj == null)
            return false;
        if (getClass() != pObj.getClass())
            return false;
        NodeDelegate other = (NodeDelegate) pObj;
        if (getHash() != other.getHash())
            return false;
        if (getNodeKey() != other.getNodeKey())
            return false;
        if (getParentKey() != other.getParentKey())
            return false;
        if (getTypeKey() != other.getTypeKey())
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("node key: ");
        builder.append(getNodeKey());
        builder.append("\nparent key: ");
        builder.append(getParentKey());
        builder.append("\ntype key: ");
        builder.append(getTypeKey());
        builder.append("\nhash: ");
        builder.append(getHash());
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTypeKey() {
        return mTypeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTypeKey(int pTypeKey) {
        this.mTypeKey = pTypeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParent() {
        return mParentKey != (Long) EFixed.NULL_NODE_KEY.getStandardProperty();
    }

}

package com.treetank.node;

import com.treetank.io.ITTSink;

/**
 * If a node is deleted, it will be encapsulated over this class.
 * 
 * @author Sebastian Graf
 * 
 */
public final class DeletedNode extends AbsNode {

    DeletedNode(final long[] builder) {
        super(builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink out) {
        super.serialize(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.DELETE_KIND;
    }

    @Override
    public AbsNode clone() {
        final AbsNode toClone = new DeletedNode(AbsNode.cloneData(mData));
        return toClone;
    }

    public final static long[] createData(final long nodeKey,
            final long parentKey) {
        final long[] data = new long[ENodes.DELETE_KIND.getSize()];
        data[AbsNode.NODE_KEY] = nodeKey;
        data[AbsNode.PARENT_KEY] = parentKey;
        return data;
    }

}

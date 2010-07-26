package com.treetank.node;

import com.treetank.io.ITTSink;

/**
 * If a node is deleted, it will be encapsulated over this class.
 * 
 * @author Sebastian Graf
 * 
 */
public final class DeletedNode extends AbsNode {

    DeletedNode(final long[] longBuilder, final int[] intBuilder) {
        super(longBuilder, intBuilder);
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
        final AbsNode toClone = new DeletedNode(AbsNode.cloneData(mLongData), AbsNode.cloneData(mIntData));
        return toClone;
    }

    public final static AbsNode createData(final long nodeKey, final long parentKey) {
        final long[] longData = new long[ENodes.DELETE_KIND.getLongSize()];
        final int[] intData = new int[ENodes.DELETE_KIND.getIntSize()];
        longData[AbsNode.NODE_KEY] = nodeKey;
        longData[AbsNode.PARENT_KEY] = parentKey;
        return new DeletedNode(longData, intData);
    }
}

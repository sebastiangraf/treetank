package com.treetank.node;

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.settings.ENodes;

/**
 * If a node is deleted, it will be encapsulated over this class.
 * 
 * @author Sebastian Graf
 * 
 */
public final class DeletedNode extends AbstractNode {

    private static final int SIZE = 1;

    public DeletedNode(final long nodeKey) {
        super(SIZE, nodeKey);
    }

    /**
     * Read delete node.
     * 
     * @param in
     *            Input bytes to read from.
     */
    protected DeletedNode(final ITTSource in) {
        super(SIZE, in);
    }

    /**
     * Clone delete node.
     * 
     * @param node
     *            Text node to clone.
     */
    protected DeletedNode(final AbstractNode node) {
        super(node);
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

}

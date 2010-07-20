package com.treetank.utils;

import java.util.Iterator;

import com.treetank.api.IReadTransaction;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.node.NamespaceNode;

/**
 * Implements a namespace iterator, which is needed for the StAX implementation.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class NamespaceIterator implements Iterator<NamespaceNode> {

    /**
     * Treetank reading transaction.
     * 
     * @see ReadTransaction
     */
    private final IReadTransaction mRTX;

    /** Number of namespace nodes. */
    private final int namespCount;

    /** Index of namespace node. */
    private static int index;

    /** Node key. */
    private final long nodeKey;

    /**
     * Constructor.
     * 
     * @param rtx
     *            Treetank reading transaction.
     */
    public NamespaceIterator(final IReadTransaction rtx) {
        mRTX = rtx;
        nodeKey = mRTX.getNode().getNodeKey();
        index = 0;

        if (mRTX.getNode().getKind() == ENodes.ELEMENT_KIND) {
            namespCount = ((ElementNode) mRTX.getNode()).getNamespaceCount();
        } else {
            namespCount = 0;
        }
    }

    @Override
    public boolean hasNext() {
        boolean retVal = false;

        if (index < namespCount) {
            retVal = true;
        }

        return retVal;
    }

    @Override
    public NamespaceNode next() {
        mRTX.moveTo(nodeKey);
        mRTX.moveToNamespace(index);
        assert mRTX.getNode().getKind() == ENodes.NAMESPACE_KIND;
        return (NamespaceNode) mRTX.getNode();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported!");
    }

}

/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: NodePage.java 4443 2008-08-30 16:28:14Z kramis $
 */

package com.treetank.page;

import java.util.Arrays;

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.node.AbsNode;
import com.treetank.node.ENodes;
import com.treetank.utils.IConstants;

/**
 * <h1>NodePage</h1>
 * 
 * <p>
 * A node page stores a set of nodes.
 * </p>
 */
public class NodePage extends AbstractPage {

    /** Key of node page. This is the base key of all contained nodes. */
    private final long mNodePageKey;

    /** Array of nodes. This can have null nodes that were removed. */
    private final AbsNode[] mNodes;

    /**
     * Create node page.
     * 
     * @param nodePageKey
     *            Base key assigned to this node page.
     */
    public NodePage(final long nodePageKey, final long revision) {
        super(0, revision);
        mNodePageKey = nodePageKey;
        mNodes = new AbsNode[IConstants.NDP_NODE_COUNT];
    }

    /**
     * Read node page.
     * 
     * @param in
     *            Input bytes to read page from.
     */
    protected NodePage(final ITTSource in) {
        super(0, in);
        mNodePageKey = in.readLong();
        mNodes = new AbsNode[IConstants.NDP_NODE_COUNT];

        final int[] values = new int[IConstants.NDP_NODE_COUNT];
        for (int i = 0; i < values.length; i++) {
            values[i] = in.readInt();
        }

        for (int offset = 0; offset < IConstants.NDP_NODE_COUNT; offset++) {
            final int kind = values[offset];
            final ENodes enumKind = ENodes.getEnumKind(kind);
            if (enumKind == ENodes.UNKOWN_KIND) {
                break;
            } else {
                getNodes()[offset] = enumKind.createNodeFromPersistence(in);
            }
        }
    }

    /**
     * Clone node page.
     * 
     * @param committedNodePage
     *            Node page to clone.
     */
    protected NodePage(final NodePage committedNodePage, final long revisionToUse) {
        super(0, committedNodePage, revisionToUse);
        mNodePageKey = committedNodePage.mNodePageKey;
        mNodes = new AbsNode[IConstants.NDP_NODE_COUNT];
        // Deep-copy all nodes.
        for (int offset = 0; offset < IConstants.NDP_NODE_COUNT; offset++) {
            final AbsNode node = committedNodePage.getNodes()[offset];
            if (node != null) {
                getNodes()[offset] = node.clone();
                // getNodes()[offset] = NodePersistenter.createNode(node);
            }
        }
    }

    /**
     * Get key of node page.
     * 
     * @return Node page key.
     */
    public final long getNodePageKey() {
        return mNodePageKey;
    }

    /**
     * Get node at a given offset.
     * 
     * @param offset
     *            Offset of node within local node page.
     * @return Node at given offset.
     */
    public AbsNode getNode(final int offset) {
        return getNodes()[offset];
    }

    /**
     * Overwrite a single node at a given offset.
     * 
     * @param offset
     *            Offset of node to overwrite in this node page.
     * @param node
     *            Node to store at given nodeOffset.
     */
    public void setNode(final int offset, final AbsNode node) {
        getNodes()[offset] = node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void serialize(final ITTSink out) {
        super.serialize(out);
        out.writeLong(mNodePageKey);
        for (int i = 0; i < getNodes().length; i++) {
            if (getNodes()[i] != null) {
                final int kind = getNodes()[i].getKind().getNodeIdentifier();
                out.writeInt(kind);
            } else {
                out.writeInt(ENodes.UNKOWN_KIND.getNodeIdentifier());
            }
        }

        for (final AbsNode node : getNodes()) {
            if (node != null) {
                node.serialize(out);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        final StringBuilder returnString = new StringBuilder();
        returnString.append(": nodePageKey=");
        returnString.append(mNodePageKey);
        returnString.append(" nodes: \n");
        for (final AbsNode node : getNodes()) {
            if (node != null) {
                returnString.append(node.getNodeKey());
                returnString.append(",");
            }
        }
        returnString.append("\n");
        return returnString.toString();
    }

    /**
     * @return the mNodes
     */
    public AbsNode[] getNodes() {
        return mNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int)(mNodePageKey ^ (mNodePageKey >>> 32));
        result = prime * result + Arrays.hashCode(mNodes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodePage other = (NodePage)obj;
        if (mNodePageKey != other.mNodePageKey)
            return false;
        if (!Arrays.equals(mNodes, other.mNodes))
            return false;
        return true;
    }

}

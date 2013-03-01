/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.treetank.page;

import static com.google.common.base.Objects.toStringHelper;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.treetank.api.INode;
import org.treetank.exception.TTIOException;
import org.treetank.page.interfaces.IPage;

/**
 * <h1>NodePage</h1>
 * 
 * <p>
 * A node page stores a set of nodes.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 * @author Marc Kramis, University of Konstanz
 */
public final class NodePage implements IPage {

    /** Key of node page. This is the base key of all contained nodes. */
    private final long mPageKey;

    /** Array of nodes. This can have null nodes that were removed. */
    private final INode[] mNodes;

    /** Pointer to last node page representing the same amount of data. */
    private final long mLastPageKey;

    /**
     * Create node page.
     * 
     * @param pPageKey
     *            Base key assigned to this node page.
     */
    public NodePage(final long pPageKey, final long pLastPageKey) {
        mPageKey = pPageKey;
        mLastPageKey = pLastPageKey;
        mNodes = new INode[IConstants.CONTENT_COUNT];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPageKey() {
        return mPageKey;
    }

    /**
     * Getting the pointer to the former representation of the same nodepage.
     * 
     * @return the pointer to the last page.
     */
    public long getLastPagePointer() {
        return mLastPageKey;
    }

    /**
     * Get node at a given offset.
     * 
     * @param pOffset
     *            Offset of node within local node page.
     * @return Node at given offset.
     */
    public INode getNode(final int pOffset) {
        return getNodes()[pOffset];
    }

    /**
     * Overwrite a single node at a given offset.
     * 
     * @param pOffset
     *            Offset of node to overwrite in this node page.
     * @param pNode
     *            Node to store at given nodeOffset.
     */
    public void setNode(final int pOffset, final INode pNode) {
        getNodes()[pOffset] = pNode;
    }

    /**
     * Getter for nodes
     * 
     * @return the mNodes
     */
    public INode[] getNodes() {
        return mNodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final DataOutput pOutput) throws TTIOException {
        try {
            pOutput.writeInt(IConstants.NODEPAGE);
            pOutput.writeLong(mPageKey);
            pOutput.writeLong(mLastPageKey);
            for (final INode node : getNodes()) {
                if (node == null) {
                    pOutput.writeInt(IConstants.NULL_NODE);
                } else {
                    if (node instanceof DeletedNode) {
                        pOutput.writeInt(IConstants.DELETEDNODE);
                    } else {
                        pOutput.writeInt(IConstants.INTERFACENODE);
                    }
                    node.serialize(pOutput);
                }
            }
        } catch (final IOException exc) {
            throw new TTIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this).add("mPageKey", mPageKey).add("mNodes", mNodes).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(mPageKey, Arrays.hashCode(mNodes));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    /**
     * Static class to mark deleted entries within the page.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static class DeletedNode implements INode {
        /** Hash for this node. */
        private long mHash;

        /**
         * Node key of the deleted node.
         */
        private long mNodeKey;

        /**
         * Constructor.
         * 
         * @param pNodeKey
         *            nodekey to be replaced with a deletednode
         */
        public DeletedNode(final long pNodeKey) {
            mNodeKey = pNodeKey;
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
        public void serialize(final DataOutput pOutput) throws TTIOException {
            try {
                pOutput.writeLong(mNodeKey);
            } catch (final IOException exc) {
                throw new TTIOException(exc);
            }
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
        public String toString() {
            return toStringHelper(this).add("mNodeKey", mNodeKey).add("mHash", mHash).toString();
        }

    }
}

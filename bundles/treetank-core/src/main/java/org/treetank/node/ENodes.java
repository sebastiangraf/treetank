/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package org.treetank.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.treetank.io.ITTSource;
import org.treetank.node.delegates.NameNodeDelegate;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.node.delegates.ValNodeDelegate;
import org.treetank.node.interfaces.INode;

/**
 * Enumeration for different nodes. All nodes are determined by a unique id.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum ENodes {

    /** Unknown kind. */
    UNKOWN_KIND(0) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is element. */
    ELEMENT_KIND(1) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {

            final List<Long> attrKeys = new ArrayList<Long>();
            final List<Long> namespKeys = new ArrayList<Long>();

            // node delegate
            final NodeDelegate nodeDel = new NodeDelegate(
                    paramSource.readLong(), paramSource.readLong(),
                    paramSource.readLong());

            // struct delegate
            final StructNodeDelegate structDel = new StructNodeDelegate(
                    nodeDel, paramSource.readLong(), paramSource.readLong(),
                    paramSource.readLong(), paramSource.readLong());

            // name delegate
            final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel,
                    paramSource.readInt(), paramSource.readInt());

            // Attributes getting
            int attrCount = paramSource.readInt();
            for (int i = 0; i < attrCount; i++) {
                attrKeys.add(paramSource.readLong());
            }

            // Namespace getting
            int nsCount = paramSource.readInt();
            for (int i = 0; i < nsCount; i++) {
                namespKeys.add(paramSource.readLong());
            }

            return new ElementNode(nodeDel, structDel, nameDel, attrKeys,
                    namespKeys);
        }

    },
    /** Node kind is attribute. */
    ATTRIBUTE_KIND(2) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            // node delegate
            final NodeDelegate nodeDel = new NodeDelegate(
                    paramSource.readLong(), paramSource.readLong(),
                    paramSource.readLong());
            // name delegate
            final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel,
                    paramSource.readInt(), paramSource.readInt());
            // val delegate
            final byte[] vals = new byte[paramSource.readInt()];
            for (int i = 0; i < vals.length; i++) {
                vals[i] = paramSource.readByte();
            }
            final ValNodeDelegate valDel = new ValNodeDelegate(nodeDel, vals);

            return new AttributeNode(nodeDel, nameDel, valDel);
        }

    },
    /** Node kind is text. */
    TEXT_KIND(3) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            // node delegate
            final NodeDelegate nodeDel = new NodeDelegate(
                    paramSource.readLong(), paramSource.readLong(),
                    paramSource.readLong());
            // val delegate
            final byte[] vals = new byte[paramSource.readInt()];
            for (int i = 0; i < vals.length; i++) {
                vals[i] = paramSource.readByte();
            }
            final ValNodeDelegate valDel = new ValNodeDelegate(nodeDel, vals);
            // struct delegate
            final StructNodeDelegate structDel = new StructNodeDelegate(
                    nodeDel, paramSource.readLong(), paramSource.readLong(),
                    paramSource.readLong(), paramSource.readLong());
            // returning the data
            return new TextNode(nodeDel, valDel, structDel);
        }

    },
    /** Node kind is namespace. */
    NAMESPACE_KIND(13) {

        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            // node delegate
            final NodeDelegate nodeDel = new NodeDelegate(
                    paramSource.readLong(), paramSource.readLong(),
                    paramSource.readLong());
            // name delegate
            final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel,
                    paramSource.readInt(), paramSource.readInt());
            return new NamespaceNode(nodeDel, nameDel);
        }

    },
    /** Node kind is processing instruction. */
    PROCESSING_KIND(7) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is comment. */
    COMMENT_KIND(8) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is document root. */
    ROOT_KIND(9) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            final NodeDelegate nodeDel = new NodeDelegate(
                    paramSource.readLong(), paramSource.readLong(),
                    paramSource.readLong());
            final StructNodeDelegate structDel = new StructNodeDelegate(
                    nodeDel, paramSource.readLong(), paramSource.readLong(),
                    paramSource.readLong(), paramSource.readLong());
            return new DocumentRootNode(nodeDel, structDel);
        }

    },
    /** Whitespace text. */
    WHITESPACE_KIND(4) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is deleted node. */
    DELETE_KIND(5) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            final NodeDelegate delegate = new NodeDelegate(
                    paramSource.readLong(), paramSource.readLong(),
                    paramSource.readLong());
            final DeletedNode node = new DeletedNode(delegate);
            return node;
        }

    };

    /** Identifier. */
    private final int mKind;

    /** Mapping of keys -> Nodes */
    private final static Map<Integer, ENodes> MAPPING = new HashMap<Integer, ENodes>();
    static {
        for (final ENodes node : values()) {
            MAPPING.put(node.mKind, node);
        }
    }

    /**
     * Constructor.
     * 
     * @param paramKind
     *            the identifier
     * @param paramLongSize
     *            the identifier
     * @param paramIntSize
     *            the identifier
     */
    private ENodes(final int paramKind) {
        mKind = paramKind;
    }

    /**
     * Getter for the identifier.
     * 
     * @return the unique identifier
     */
    public int getNodeIdentifier() {
        return mKind;
    }

    public abstract INode createNodeFromPersistence(final ITTSource paramSource);

    /**
     * Public method to get the related node based on the identifier.
     * 
     * @param paramKind
     *            the identifier for the node
     * @return the related node
     */
    public static ENodes getEnumKind(final int paramKind) {
        return MAPPING.get(paramKind);
    }

}

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

package com.treetank.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.treetank.io.ITTSource;

/**
 * Enumeration for different nodes. All nodes are determined by a unique id.
 * 
 * @author Sebastian Graf, University of Konstanzs
 * 
 */
public enum ENodes {

    /** Unknown kind. */
    UNKOWN_KIND(0, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource mSource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] mData, final int[] mIntData, final byte[] mValue) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is element. */
    ELEMENT_KIND(1, 7, 5) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource mSource) {
            final long[] longData = readLongData(mSource);
            final int[] intData = readIntData(mSource);

            final List<Long> attrKeys = new ArrayList<Long>();
            final List<Long> namespKeys = new ArrayList<Long>();
            if (intData[ElementNode.ATTRIBUTE_COUNT] > 0) {
                for (int i = 0; i < intData[ElementNode.ATTRIBUTE_COUNT]; i++) {
                    attrKeys.add(mSource.readLong());
                }
            }
            if (intData[ElementNode.NAMESPACE_COUNT] > 0) {
                for (int i = 0; i < intData[ElementNode.NAMESPACE_COUNT]; i++) {
                    namespKeys.add(mSource.readLong());
                }
            }
            return new ElementNode(longData, intData, attrKeys, namespKeys);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] mData, final int[] mIntData, final byte[] mValue) {
            return new ElementNode(mData, mIntData, new ArrayList<Long>(), new ArrayList<Long>());
        }
    },
    /** Node kind is attribute. */
    ATTRIBUTE_KIND(2, 3, 4) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource mSource) {
            final long[] longData = readLongData(mSource);
            final int[] intData = readIntData(mSource);
            final byte[] value = new byte[intData[AttributeNode.VALUE_LENGTH]];
            for (int i = 0; i < value.length; i++) {
                value[i] = mSource.readByte();
            }
            return new AttributeNode(longData, intData, value);
        }

        @Override
        public AbsNode
            createNodeFromScratch(final long[] mLongData, final int[] mIntData, final byte[] mValue) {
            return new AttributeNode(mLongData, mIntData, mValue);
        }
    },
    /** Node kind is text. */
    TEXT_KIND(3, 7, 2) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource mSource) {
            final long[] longData = readLongData(mSource);
            final int[] intData = readIntData(mSource);
            final byte[] value = new byte[intData[TextNode.VALUE_LENGTH]];
            for (int i = 0; i < value.length; i++) {
                value[i] = mSource.readByte();
            }
            return new TextNode(longData, intData, value);
        }

        @Override
        public AbsNode
            createNodeFromScratch(final long[] mLongData, final int[] mIntData, final byte[] mValue) {
            return new TextNode(mLongData, mIntData, mValue);
        }
    },
    /** Node kind is namespace. */
    NAMESPACE_KIND(13, 3, 3) {

        @Override
        public AbsNode createNodeFromPersistence(final ITTSource mSource) {
            final long[] data = readLongData(mSource);
            final int[] intData = readIntData(mSource);
            return new NamespaceNode(data, intData);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] mData, final int[] mIntData, final byte[] mValue) {
            return new NamespaceNode(mData, mIntData);
        }
    },
    /** Node kind is processing instruction. */
    PROCESSING_KIND(7, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource mSource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] mData, final int[] mIntData, final byte[] mValue) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is comment. */
    COMMENT_KIND(8, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource mSource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] mData, final int[] mIntData, final byte[] mValue) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is document root. */
    ROOT_KIND(9, 7, 1) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource mSource) {
            final long[] data = readLongData(mSource);
            final int[] intData = readIntData(mSource);
            return new DocumentRootNode(data, intData);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] mData, final int[] mIntData, final byte[] mValue) {
            return new DocumentRootNode(mData, mIntData);
        }
    },
    /** Whitespace text. */
    WHITESPACE_KIND(4, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource mSource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] mData, final int[] mIntData, final byte[] mValue) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is deleted node. */
    DELETE_KIND(5, 3, 1) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource mSource) {
            final long[] longData = readLongData(mSource);
            final int[] intData = readIntData(mSource);
            return new DeletedNode(longData, intData);
        }

        @Override
        public AbsNode
            createNodeFromScratch(final long[] mLongData, final int[] mIntData, final byte[] mValue) {
            return new DeletedNode(mLongData, mIntData);
        }
    };

    /** Identifier. */
    private final int mKind;

    /** Size in the long data array. */
    private final int mLongSize;

    /** Size in the int data array. */
    private final int mIntSize;

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
     * @param mKind
     *            the identifier
     * @param mLongSize
     *            the identifier
     * @param mIntSize
     *            the identifier
     */
    private ENodes(final int mKind, final int mLongSize, final int mIntSize) {
        this.mKind = mKind;
        this.mLongSize = mLongSize;
        this.mIntSize = mIntSize;
    }

    /**
     * Getter for the identifier.
     * 
     * @return the unique identifier
     */
    public int getNodeIdentifier() {
        return mKind;
    }

    public abstract AbsNode createNodeFromPersistence(final ITTSource mSource);

    public abstract AbsNode createNodeFromScratch(final long[] mLongData, final int[] mIntData,
        final byte[] mValue);

    /**
     * @return the mSize
     */
    int getLongSize() {
        return mLongSize;
    }

    /**
     * @return the mIntSize
     */
    int getIntSize() {
        return mIntSize;
    }

    long[] readLongData(final ITTSource mSource) {
        final long[] data = new long[getLongSize()];
        for (int i = 0; i < data.length; i++) {
            data[i] = mSource.readLong();
        }
        return data;
    }

    int[] readIntData(final ITTSource mSource) {
        final int[] data = new int[getIntSize()];
        for (int i = 0; i < data.length; i++) {
            data[i] = mSource.readInt();
        }
        return data;
    }

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

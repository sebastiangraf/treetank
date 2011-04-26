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

import org.treetank.node.io.NodeSource;

/**
 * Enumeration for different nodes. All nodes are determined by a unique id.
 * 
 * @author Sebastian Graf, University of Konstanzs
 * 
 */
public enum ENodes {

    /** Dummy kind. */
    DUMMY_KIND(-1, 7, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            return null;
        }

    },

    /** Unknown kind. */
    UNKOWN_KIND(0, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is element. */
    ELEMENT_KIND(1, 7, 5) {
        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            final long[] longData = readLongData(paramSource);
            final int[] intData = readIntData(paramSource);

            final List<Long> attrKeys = new ArrayList<Long>();
            final List<Long> namespKeys = new ArrayList<Long>();
            if (intData[ElementNode.ATTRIBUTE_COUNT] > 0) {
                for (int i = 0; i < intData[ElementNode.ATTRIBUTE_COUNT]; i++) {
                    attrKeys.add(paramSource.readLong());
                }
            }
            if (intData[ElementNode.NAMESPACE_COUNT] > 0) {
                for (int i = 0; i < intData[ElementNode.NAMESPACE_COUNT]; i++) {
                    namespKeys.add(paramSource.readLong());
                }
            }
            
            return new ElementNode(longData, intData, attrKeys, namespKeys);
        }

    },
    /** Node kind is attribute. */
    ATTRIBUTE_KIND(2, 3, 4) {
        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            final long[] longData = readLongData(paramSource);
            final int[] intData = readIntData(paramSource);
            final byte[] value = new byte[intData[AttributeNode.VALUE_LENGTH]];
            for (int i = 0; i < value.length; i++) {
                value[i] = paramSource.readByte();
            }
            return new AttributeNode(longData, intData, value);
        }

    },
    /** Node kind is text. */
    TEXT_KIND(3, 7, 2) {
        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            final long[] longData = readLongData(paramSource);
            final int[] intData = readIntData(paramSource);
            final byte[] value = new byte[intData[TextNode.VALUE_LENGTH]];
            for (int i = 0; i < value.length; i++) {
                value[i] = paramSource.readByte();
            }
            return new TextNode(longData, intData, value);
        }

    },
    /** Node kind is namespace. */
    NAMESPACE_KIND(13, 3, 3) {

        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            final long[] data = readLongData(paramSource);
            final int[] intData = readIntData(paramSource);
            return new NamespaceNode(data, intData);
        }

    },
    /** Node kind is processing instruction. */
    PROCESSING_KIND(7, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is comment. */
    COMMENT_KIND(8, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is document root. */
    ROOT_KIND(9, 7, 1) {
        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            final long[] data = readLongData(paramSource);
            final int[] intData = readIntData(paramSource);
            return new DocumentRootNode(data, intData);
        }

    },
    /** Whitespace text. */
    WHITESPACE_KIND(4, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is deleted node. */
    DELETE_KIND(5, 3, 1) {
        @Override
        public AbsNode createNodeFromPersistence(final NodeSource paramSource) {
            final long[] longData = readLongData(paramSource);
            final int[] intData = readIntData(paramSource);
            return new DeletedNode(longData, intData);
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
     * @param paramKind
     *            the identifier
     * @param paramLongSize
     *            the identifier
     * @param paramIntSize
     *            the identifier
     */
    private ENodes(final int paramKind, final int paramLongSize, final int paramIntSize) {
        mKind = paramKind;
        mLongSize = paramLongSize;
        mIntSize = paramIntSize;
    }

    /**
     * Getter for the identifier.
     * 
     * @return the unique identifier
     */
    public int getNodeIdentifier() {
        return mKind;
    }

    public abstract AbsNode createNodeFromPersistence(final NodeSource paramSource);
    
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

    long[] readLongData(final NodeSource mSource) {
        final long[] data = new long[getLongSize()];
        for (int i = 0; i < data.length; i++) {
            data[i] = mSource.readLong();
        }
        return data;
    }

    int[] readIntData(final NodeSource mSource) {
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

    /**
     * Cloning long-array
     * 
     * @param paramInput
     * @return the cloned array
     */
    public static long[] cloneData(final long[] paramInput) {
        final long[] data = new long[paramInput.length];
        System.arraycopy(paramInput, 0, data, 0, data.length);
        return data;
    }
    
    /**
     * Cloning int-array
     * @param paramInput
     * @return the cloned array
     */
    public static int[] cloneData(final int[] paramInput) {
        final int[] data = new int[paramInput.length];
        System.arraycopy(paramInput, 0, data, 0, data.length);
        return data;
    }
    
    /**
     * Cloning byte-array
     * @param paramInput
     * @return the cloned array
     */
    public static byte[] cloneData(final byte[] paramInput) {
        final byte[] value = new byte[paramInput.length];
        System.arraycopy(paramInput, 0, value, 0, value.length);
        return value;
    }

}

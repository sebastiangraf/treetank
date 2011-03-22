/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.treetank.io.ITTSource;

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
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
            return null;
        }

    },

    /** Unknown kind. */
    UNKOWN_KIND(0, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is element. */
    ELEMENT_KIND(1, 7, 5) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
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
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
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
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
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
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
            final long[] data = readLongData(paramSource);
            final int[] intData = readIntData(paramSource);
            return new NamespaceNode(data, intData);
        }

    },
    /** Node kind is processing instruction. */
    PROCESSING_KIND(7, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is comment. */
    COMMENT_KIND(8, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is document root. */
    ROOT_KIND(9, 7, 1) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
            final long[] data = readLongData(paramSource);
            final int[] intData = readIntData(paramSource);
            return new DocumentRootNode(data, intData);
        }

    },
    /** Whitespace text. */
    WHITESPACE_KIND(4, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is deleted node. */
    DELETE_KIND(5, 3, 1) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource paramSource) {
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

    public abstract AbsNode createNodeFromPersistence(final ITTSource paramSource);
    
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

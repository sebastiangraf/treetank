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
 * @author Sebastian Graf, University of Konstanzs
 * 
 */
public enum ENodes {

    /** Unknown kind. */
    UNKOWN_KIND(0, 0, 0) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is element. */
    ELEMENT_KIND(1, 7, 5) {
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
    ATTRIBUTE_KIND(2, 3, 4) {
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
    TEXT_KIND(3, 7, 2) {
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
    NAMESPACE_KIND(13, 3, 3) {

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
    PROCESSING_KIND(7, 0, 0) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is comment. */
    COMMENT_KIND(8, 0, 0) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is document root. */
    ROOT_KIND(9, 7, 1) {
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
    WHITESPACE_KIND(4, 0, 0) {
        @Override
        public INode createNodeFromPersistence(final ITTSource paramSource) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is deleted node. */
    DELETE_KIND(5, 3, 1) {
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

    /** Size in the long data array. */
    private final int mIntSize;

    private final int mLongSize;

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
    private ENodes(final int paramKind, final int paramLongSize,
            final int paramIntSize) {
        mKind = paramKind;
        mIntSize = paramIntSize * 4;
        mLongSize = paramLongSize * 8;
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
     * @return the byte size
     */
    int getIntSize() {
        return mIntSize;
    }

    /**
     * @return the pointer size
     */
    int getLongSize() {
        return mLongSize;
    }

    byte[] readByteData(final ITTSource mSource) {
        final byte[] mData = new byte[getIntSize()];
        for (int i = 0; i < mData.length; i++) {
            mData[i] = mSource.readByte();
        }
        return mData;
    }

    byte[] readPointerData(final ITTSource mSource) {
        final byte[] mData = new byte[getLongSize()];
        for (int i = 0; i < mData.length; i++) {
            mData[i] = mSource.readByte();
        }
        return mData;
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
     * 
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
     * 
     * @param paramInput
     * @return the cloned array
     */
    public static byte[] cloneData(final byte[] paramInput) {
        final byte[] value = new byte[paramInput.length];
        System.arraycopy(paramInput, 0, value, 0, value.length);
        return value;
    }

    /**
     * Converting a byte array to integer.
     * 
     * @param mByteArray
     *            Byte array to convert.
     * @return converted integer value.
     */
    protected static int byteArrayToInt(final byte[] mByteArray) {
        final int mConvInt = ((mByteArray[0] & 0xff) << 24)
                | ((mByteArray[1] & 0xff) << 16)
                | ((mByteArray[2] & 0xff) << 8) | (mByteArray[3] & 0xff);

        return mConvInt;
    }

    /**
     * Converting a byte array to long.
     * 
     * @param mByteArray
     *            Byte array to convert.
     * @return converted long value.
     */
    protected static long byteArrayToLong(final byte[] mByteArray) {
        final long mConvLong = ((long) (mByteArray[0] & 0xff) << 56)
                | ((long) (mByteArray[1] & 0xff) << 48)
                | ((long) (mByteArray[2] & 0xff) << 40)
                | ((long) (mByteArray[3] & 0xff) << 32)
                | ((long) (mByteArray[4] & 0xff) << 24)
                | ((long) (mByteArray[5] & 0xff) << 16)
                | ((long) (mByteArray[6] & 0xff) << 8)
                | ((long) (mByteArray[7] & 0xff));

        return mConvLong;
    }

    public long readLongBytes(final int mOffset, final byte[] mByteData) {
        byte[] mBuffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            mBuffer[i] = mByteData[mOffset + i];
        }
        return byteArrayToLong(mBuffer);
    }

    public int readIntBytes(final int mOffset, final byte[] mByteData) {
        byte[] mBuffer = new byte[4];
        for (int i = 0; i < 4; i++) {
            mBuffer[i] = mByteData[mOffset + i];
        }
        return byteArrayToInt(mBuffer);
    }

}

package com.treetank.node;

import java.util.ArrayList;
import java.util.List;

import com.treetank.io.ITTSource;

/**
 * Enumeration for different nodes. All nodes are determined by a unique id.
 * 
 * @author Sebastian Graf, University of Konstanzs
 * 
 */
public enum ENodes {

    /** Unknown kind */
    UNKOWN_KIND(0, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final int[] intData, final byte[] value) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is element. */
    ELEMENT_KIND(1, 6, 5) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] longData = readLongData(source);
            final int[] intData = readIntData(source);

            final List<Long> attrKeys = new ArrayList<Long>();
            final List<Long> namespKeys = new ArrayList<Long>();
            if (intData[ElementNode.ATTRIBUTE_COUNT] > 0) {
                for (int i = 0; i < intData[ElementNode.ATTRIBUTE_COUNT]; i++) {
                    attrKeys.add(source.readLong());
                }
            }
            if (intData[ElementNode.NAMESPACE_COUNT] > 0) {
                for (int i = 0; i < intData[ElementNode.NAMESPACE_COUNT]; i++) {
                    namespKeys.add(source.readLong());
                }
            }
            return new ElementNode(longData, intData, attrKeys, namespKeys);
        }
 
        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final int[] intData, final byte[] value) {
            return new ElementNode(data, intData, new ArrayList<Long>(),
                    new ArrayList<Long>());
        }
    },
    /** Node kind is attribute. */
    ATTRIBUTE_KIND(2, 2, 4) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] longData = readLongData(source);
            final int[] intData = readIntData(source);
            final byte[] value = new byte[intData[AttributeNode.VALUE_LENGTH]];
            for (int i = 0; i < value.length; i++) {
                value[i] = source.readByte();
            }
            return new AttributeNode(longData, intData, value);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] longData,
                final int[] intData, final byte[] value) {
            return new AttributeNode(longData, intData, value);
        }
    },
    /** Node kind is text. */
    TEXT_KIND(3, 6, 2) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] longData = readLongData(source);
            final int[] intData = readIntData(source);
            final byte[] value = new byte[intData[TextNode.VALUE_LENGTH]];
            for (int i = 0; i < value.length; i++) {
                value[i] = source.readByte();
            }
            return new TextNode(longData, intData, value);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] longData,
                final int[] intData, final byte[] value) {
            return new TextNode(longData, intData, value);
        }
    },
    /** Node kind is namespace. */
    NAMESPACE_KIND(13, 2, 3) {

        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] data = readLongData(source);
            final int[] intData = readIntData(source);
            return new NamespaceNode(data, intData);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final int[] intData, final byte[] value) {
            return new NamespaceNode(data, intData);
        }
    },
    /** Node kind is processing instruction. */
    PROCESSING_KIND(7, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final int[] intData, final byte[] value) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is comment. */
    COMMENT_KIND(8, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final int[] intData, final byte[] value) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is document root. */
    ROOT_KIND(9, 6, 1) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] data = readLongData(source);
            final int[] intData = readIntData(source);
            return new DocumentRootNode(data, intData);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final int[] intData, final byte[] value) {
            return new DocumentRootNode(data, intData);
        }
    },
    /** Whitespace text */
    WHITESPACE_KIND(4, 0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final int[] intData, final byte[] value) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is deleted node. */
    DELETE_KIND(5, 2, 1) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] longData = readLongData(source);
            final int[] intData = readIntData(source);
            return new DeletedNode(longData, intData);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] longData,
                final int[] intData, final byte[] value) {
            return new DeletedNode(longData, intData);
        }
    };

    /** Identifier */
    private final int mKind;

    /** Size in the long data array */
    private final int mLongSize;

    /** Size in the int data array */
    private final int mIntSize;

    /**
     * Constructor
     * 
     * @param kind
     *            the identifier
     */
    private ENodes(final int kind, final int longSize, final int intSize) {
        this.mKind = kind;
        this.mLongSize = longSize;
        this.mIntSize = intSize;
    }

    /**
     * Getter for the identifier
     * 
     * @return the unique identifier
     */
    public int getNodeIdentifier() {
        return mKind;
    }

    public abstract AbsNode createNodeFromPersistence(final ITTSource source);

    public abstract AbsNode createNodeFromScratch(final long[] longData,
            final int[] intData, final byte[] value);

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

    long[] readLongData(final ITTSource source) {
        final long[] data = new long[getLongSize()];
        for (int i = 0; i < data.length; i++) {
            data[i] = source.readLong();
        }
        return data;
    }

    int[] readIntData(final ITTSource source) {
        final int[] data = new int[getIntSize()];
        for (int i = 0; i < data.length; i++) {
            data[i] = source.readInt();
        }
        return data;
    }

    /**
     * Public method to get the related node based on the identifier
     * 
     * @param intKind
     *            the identifier for the node
     * @return the related nodekind
     */
    public static ENodes getEnumKind(final int intKind) {
        ENodes returnVal;
        switch (intKind) {
        case 0:
            returnVal = UNKOWN_KIND;
            break;
        case 1:
            returnVal = ELEMENT_KIND;
            break;
        case 2:
            returnVal = ATTRIBUTE_KIND;
            break;
        case 3:
            returnVal = TEXT_KIND;
            break;
        case 13:
            returnVal = NAMESPACE_KIND;
            break;
        case 7:
            returnVal = PROCESSING_KIND;
            break;
        case 8:
            returnVal = COMMENT_KIND;
            break;
        case 9:
            returnVal = ROOT_KIND;
            break;
        case 5:
            returnVal = DELETE_KIND;
            break;
        case 4:
            returnVal = WHITESPACE_KIND;
            break;
        default:
            returnVal = null;
            break;
        }
        return returnVal;
    }

}

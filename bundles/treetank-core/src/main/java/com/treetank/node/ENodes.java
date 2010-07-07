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
    UNKOWN_KIND(0, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final byte[] value) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is element. */
    ELEMENT_KIND(1, 11) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] data = readSourceData(source);

            final List<Long> attrKeys = new ArrayList<Long>();
            final List<Long> namespKeys = new ArrayList<Long>();
            if (data[ElementNode.ATTRIBUTE_COUNT] > 0) {
                for (int i = 0; i < data[ElementNode.ATTRIBUTE_COUNT]; i++) {
                    attrKeys.add(source.readLong());
                }
            }
            if (data[ElementNode.NAMESPACE_COUNT] > 0) {
                for (int i = 0; i < data[ElementNode.NAMESPACE_COUNT]; i++) {
                    namespKeys.add(source.readLong());
                }
            }
            return new ElementNode(data, attrKeys, namespKeys);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final byte[] value) {
            return new ElementNode(data, new ArrayList<Long>(),
                    new ArrayList<Long>());
        }
    },
    /** Node kind is attribute. */
    ATTRIBUTE_KIND(2, 6) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] data = readSourceData(source);
            final byte[] value = new byte[(int) data[AttributeNode.VALUE_LENGTH]];
            for (int i = 0; i < value.length; i++) {
                value[i] = source.readByte();
            }
            return new AttributeNode(data, value);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final byte[] value) {
            return new AttributeNode(data, value);
        }
    },
    /** Node kind is text. */
    TEXT_KIND(3, 8) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] data = readSourceData(source);
            final byte[] value = new byte[(int) data[TextNode.VALUE_LENGTH]];
            for (int i = 0; i < value.length; i++) {
                value[i] = source.readByte();
            }
            return new TextNode(data, value);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final byte[] value) {
            return new TextNode(data, value);
        }
    },
    /** Node kind is namespace. */
    NAMESPACE_KIND(13, 4) {

        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] data = readSourceData(source);
            return new NamespaceNode(data);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final byte[] value) {
            return new NamespaceNode(data);
        }
    },
    /** Node kind is processing instruction. */
    PROCESSING_KIND(7, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final byte[] value) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is comment. */
    COMMENT_KIND(8, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final byte[] value) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is document root. */
    ROOT_KIND(9, 6) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] data = readSourceData(source);
            return new DocumentRootNode(data);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final byte[] value) {
            return new DocumentRootNode(data);
        }
    },
    /** Whitespace text */
    WHITESPACE_KIND(4, 0) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final byte[] value) {
            throw new UnsupportedOperationException();
        }
    },
    /** Node kind is deleted node. */
    DELETE_KIND(5, 2) {
        @Override
        public AbsNode createNodeFromPersistence(final ITTSource source) {
            final long[] data = readSourceData(source);
            return new DeletedNode(data);
        }

        @Override
        public AbsNode createNodeFromScratch(final long[] data,
                final byte[] value) {
            return new DeletedNode(data);
        }
    };

    /** Identifier */
    private final int mKind;

    /** Size in the data array */
    private final int mSize;

    /**
     * Constructor
     * 
     * @param kind
     *            the identifier
     */
    private ENodes(final int kind, final int size) {
        this.mKind = kind;
        this.mSize = size;
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

    public abstract AbsNode createNodeFromScratch(final long[] data,
            final byte[] value);

    /**
     * @return the mSize
     */
    int getSize() {
        return mSize;
    }

    long[] readSourceData(final ITTSource source) {
        final long[] data = new long[getSize()];
        for (int i = 0; i < data.length; i++) {
            data[i] = source.readLong();
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

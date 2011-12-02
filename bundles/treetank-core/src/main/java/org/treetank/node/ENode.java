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

import org.treetank.io.ITTSink;
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
public enum ENode {

    /** Unknown kind. */
    UNKOWN_KIND(0, null) {
        @Override
        public INode deserialize(final ITTSource pSource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void serialize(final ITTSink pSink, final INode pToSerialize) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is element. */
    ELEMENT_KIND(1, ElementNode.class) {
        @Override
        public INode deserialize(final ITTSource pSource) {

            final List<Long> attrKeys = new ArrayList<Long>();
            final List<Long> namespKeys = new ArrayList<Long>();

            // node delegate
            final NodeDelegate nodeDel = new NodeDelegate(pSource.readLong(),
                    pSource.readLong(), pSource.readLong());

            // struct delegate
            final StructNodeDelegate structDel = new StructNodeDelegate(
                    nodeDel, pSource.readLong(), pSource.readLong(),
                    pSource.readLong(), pSource.readLong());

            // name delegate
            final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel,
                    pSource.readInt(), pSource.readInt());

            // Attributes getting
            int attrCount = pSource.readInt();
            for (int i = 0; i < attrCount; i++) {
                attrKeys.add(pSource.readLong());
            }

            // Namespace getting
            int nsCount = pSource.readInt();
            for (int i = 0; i < nsCount; i++) {
                namespKeys.add(pSource.readLong());
            }

            return new ElementNode(nodeDel, structDel, nameDel, attrKeys,
                    namespKeys);
        }

        @Override
        public void serialize(final ITTSink pSink, final INode pToSerialize) {
            ElementNode node = (ElementNode) pToSerialize;
            serializeDelegate(node.getNodeDelegate(), pSink);
            serializeStrucDelegate(node.getStrucNodeDelegate(), pSink);
            serializeNameDelegate(node.getNameNodeDelegate(), pSink);
            pSink.writeInt(node.getAttributeCount());
            for (int i = 0; i < node.getAttributeCount(); i++) {
                pSink.writeLong(node.getAttributeKey(i));
            }
            pSink.writeInt(node.getNamespaceCount());
            for (int i = 0; i < node.getNamespaceCount(); i++) {
                pSink.writeLong(node.getNamespaceKey(i));
            }
        }

    },
    /** Node kind is attribute. */
    ATTRIBUTE_KIND(2, AttributeNode.class) {
        @Override
        public INode deserialize(final ITTSource pSource) {
            // node delegate
            final NodeDelegate nodeDel = new NodeDelegate(pSource.readLong(),
                    pSource.readLong(), pSource.readLong());
            // name delegate
            final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel,
                    pSource.readInt(), pSource.readInt());
            // val delegate
            final byte[] vals = new byte[pSource.readInt()];
            for (int i = 0; i < vals.length; i++) {
                vals[i] = pSource.readByte();
            }
            final ValNodeDelegate valDel = new ValNodeDelegate(nodeDel, vals);

            return new AttributeNode(nodeDel, nameDel, valDel);
        }

        @Override
        public void serialize(final ITTSink pSink, final INode pToSerialize) {
            AttributeNode node = (AttributeNode) pToSerialize;
            serializeDelegate(node.getNodeDelegate(), pSink);
            serializeNameDelegate(node.getNameNodeDelegate(), pSink);
            serializeValDelegate(node.getValNodeDelegate(), pSink);
        }

    },
    /** Node kind is text. */
    TEXT_KIND(3, TextNode.class) {
        @Override
        public INode deserialize(final ITTSource pSource) {
            // node delegate
            final NodeDelegate nodeDel = new NodeDelegate(pSource.readLong(),
                    pSource.readLong(), pSource.readLong());
            // val delegate
            final byte[] vals = new byte[pSource.readInt()];
            for (int i = 0; i < vals.length; i++) {
                vals[i] = pSource.readByte();
            }
            final ValNodeDelegate valDel = new ValNodeDelegate(nodeDel, vals);
            // struct delegate
            final StructNodeDelegate structDel = new StructNodeDelegate(
                    nodeDel, pSource.readLong(), pSource.readLong(),
                    pSource.readLong(), pSource.readLong());
            // returning the data
            return new TextNode(nodeDel, valDel, structDel);
        }

        @Override
        public void serialize(final ITTSink pSink, final INode pToSerialize) {
            TextNode node = (TextNode) pToSerialize;
            serializeDelegate(node.getNodeDelegate(), pSink);
            serializeValDelegate(node.getValNodeDelegate(), pSink);
            serializeStrucDelegate(node.getStrucNodeDelegate(), pSink);
        }

    },
    /** Node kind is namespace. */
    NAMESPACE_KIND(13, NamespaceNode.class) {

        @Override
        public INode deserialize(final ITTSource pSource) {
            // node delegate
            final NodeDelegate nodeDel = new NodeDelegate(pSource.readLong(),
                    pSource.readLong(), pSource.readLong());
            // name delegate
            final NameNodeDelegate nameDel = new NameNodeDelegate(nodeDel,
                    pSource.readInt(), pSource.readInt());
            return new NamespaceNode(nodeDel, nameDel);
        }

        @Override
        public void serialize(final ITTSink pSink, final INode pToSerialize) {
            NamespaceNode node = (NamespaceNode) pToSerialize;
            serializeDelegate(node.getNodeDelegate(), pSink);
            serializeNameDelegate(node.getNameNodeDelegate(), pSink);
        }

    },
    /** Node kind is processing instruction. */
    PROCESSING_KIND(7, null) {
        @Override
        public INode deserialize(final ITTSource parapSource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void serialize(final ITTSink pSink, final INode pToSerialize) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is comment. */
    COMMENT_KIND(8, null) {
        @Override
        public INode deserialize(final ITTSource pSource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void serialize(final ITTSink pSink, final INode pToSerialize) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is document root. */
    ROOT_KIND(9, DocumentRootNode.class) {
        @Override
        public INode deserialize(final ITTSource pSource) {
            final NodeDelegate nodeDel = new NodeDelegate(pSource.readLong(),
                    pSource.readLong(), pSource.readLong());
            final StructNodeDelegate structDel = new StructNodeDelegate(
                    nodeDel, pSource.readLong(), pSource.readLong(),
                    pSource.readLong(), pSource.readLong());
            return new DocumentRootNode(nodeDel, structDel);
        }

        @Override
        public void serialize(final ITTSink pSink, final INode pToSerialize) {
            DocumentRootNode node = (DocumentRootNode) pToSerialize;
            serializeDelegate(node.getNodeDelegate(), pSink);
            serializeStrucDelegate(node.getStrucNodeDelegate(), pSink);
        }

    },
    /** Whitespace text. */
    WHITESPACE_KIND(4, null) {
        @Override
        public INode deserialize(final ITTSource pSource) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void serialize(final ITTSink pSink, final INode pToSerialize) {
            throw new UnsupportedOperationException();
        }

    },
    /** Node kind is deleted node. */
    DELETE_KIND(5, DeletedNode.class) {
        @Override
        public INode deserialize(final ITTSource pSource) {
            final NodeDelegate delegate = new NodeDelegate(pSource.readLong(),
                    pSource.readLong(), pSource.readLong());
            final DeletedNode node = new DeletedNode(delegate);
            return node;
        }

        @Override
        public void serialize(final ITTSink pSink, final INode pToSerialize) {
            DeletedNode node = (DeletedNode) pToSerialize;
            serializeDelegate(node.getNodeDelegate(), pSink);
        }

    };

    /** Identifier. */
    private final int mKind;
    private final Class<? extends INode> mClass;

    /** Mapping of keys -> Nodes */
    private final static Map<Integer, ENode> INSTANCEFORID = new HashMap<Integer, ENode>();
    private final static Map<Class<? extends INode>, ENode> INSTANCEFORCLASS = new HashMap<Class<? extends INode>, ENode>();
    static {
        for (final ENode node : values()) {
            INSTANCEFORID.put(node.mKind, node);
            INSTANCEFORCLASS.put(node.mClass, node);
        }
    }

    /**
     * Constructor.
     * 
     * @param pUd
     *            the identifier
     * @param paramLongSize
     *            the identifier
     * @param paramIntSize
     *            the identifier
     */
    private ENode(final int pUd, final Class<? extends INode> pClass) {
        mKind = pUd;
        mClass = pClass;
    }

    /**
     * Getter for the identifier.
     * 
     * @return the unique identifier
     */
    public int getId() {
        return mKind;
    }

    /**
     * Deserializing a node out of a given {@link ITTSource}.
     * 
     * @param pSource
     *            of the data where the obj should be build up.
     * @return a resulting {@link INode} instance
     */
    public abstract INode deserialize(final ITTSource pSource);

    /**
     * Serializing a node out to a given {@link ITTSink}.
     * 
     * @param pSink
     *            where the data should be serialized to.
     * @param pToSerialize
     *            the data to be serialized
     */
    public abstract void serialize(final ITTSink pSink, final INode pToSerialize);

    /**
     * Public method to get the related node based on the identifier.
     * 
     * @param pId
     *            the identifier for the node
     * @return the related node
     */
    public static ENode getKind(final int pId) {
        return INSTANCEFORID.get(pId);
    }

    /**
     * Public method to get the related node based on the class.
     * 
     * @param pClass
     *            the class for the node
     * @return the related node
     */
    public static ENode getKind(final Class<? extends INode> pClass) {
        return INSTANCEFORCLASS.get(pClass);
    }

    /**
     * Serializing the {@link NodeDelegate} instance.
     * 
     * @param pDel
     *            to be serialize
     * @param pSink
     *            to serialize to.
     */
    private static final void serializeDelegate(final NodeDelegate pDel,
            final ITTSink pSink) {
        pSink.writeLong(pDel.getNodeKey());
        pSink.writeLong(pDel.getParentKey());
        pSink.writeLong(pDel.getHash());
    }

    /**
     * Serializing the {@link StructNodeDelegate} instance.
     * 
     * @param pDel
     *            to be serialize
     * @param pSink
     *            to serialize to.
     */
    private static final void serializeStrucDelegate(
            final StructNodeDelegate pDel, final ITTSink pSink) {
        pSink.writeLong(pDel.getFirstChildKey());
        pSink.writeLong(pDel.getRightSiblingKey());
        pSink.writeLong(pDel.getLeftSiblingKey());
        pSink.writeLong(pDel.getChildCount());
    }

    /**
     * Serializing the {@link NameNodeDelegate} instance.
     * 
     * @param pDel
     *            to be serialize
     * @param pSink
     *            to serialize to.
     */
    private static final void serializeNameDelegate(
            final NameNodeDelegate pDel, final ITTSink pSink) {
        pSink.writeInt(pDel.getNameKey());
        pSink.writeInt(pDel.getURIKey());
    }

    /**
     * Serializing the {@link ValNodeDelegate} instance.
     * 
     * @param pDel
     *            to be serialize
     * @param pSink
     *            to serialize to.
     */
    private static final void serializeValDelegate(final ValNodeDelegate pDel,
            final ITTSink pSink) {
        pSink.writeInt(pDel.getRawValue().length);
        for (byte value : pDel.getRawValue()) {
            pSink.writeByte(value);
        }
    }

}

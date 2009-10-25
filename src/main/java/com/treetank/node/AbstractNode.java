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
 * $Id: AbstractNode.java 4550 2009-02-05 09:25:46Z graf $
 */

package com.treetank.node;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.utils.IConstants;

/**
 * <h1>AbstractNode</h1>
 * 
 * <p>
 * Abstract node class to implement all methods required with INode. To reduce
 * implementation overhead in subclasses it implements all methods but does
 * silently not do anything there. A subclass must only implement those methods
 * that are required to provide proper subclass functionality.
 * </p>
 */
public abstract class AbstractNode implements IItem, Comparable<AbstractNode> {

    /** standard NODE_KEY. */
    protected static final int NODE_KEY = 0;

    /** Node key is common to all node kinds. */
    protected final long[] mData;

    /**
     * Constructor to set node key.
     * 
     * @param nodeKey
     *            Key of node.
     * @param size
     *            Size of the data.
     */
    public AbstractNode(final int size, final long nodeKey) {
        mData = new long[size];
        mData[NODE_KEY] = nodeKey;
    }

    /**
     * Constructor to set node key.
     * 
     * @param node
     *            to be set
     */
    public AbstractNode(final AbstractNode node) {
        mData = new long[node.mData.length];
        System.arraycopy(node.mData, 0, mData, 0, mData.length);
    }

    /**
     * Read node.
     * 
     * @param nodeKey
     *            Key of text node.
     * @param in
     *            Input bytes to read node from.
     * @param size
     *            Size of the data.
     */
    public AbstractNode(final int size, final long nodeKey, final ITTSource in) {
        mData = new long[size];
        for (int i = 0; i < size; i++) {
            mData[i] = in.readLong();
        }
    }

    /**
     * Read node.
     * 
     * @param nodeKey
     *            Key of text node.
     * @param in
     *            Input bytes to read node from.
     * @param size
     *            Size of the data.
     */
    public AbstractNode(final int size, final long nodeKey, final TupleInput in) {
        mData = new long[size];
        for (int i = 0; i < size; i++) {
            mData[i] = in.readLong();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isNode() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDocumentRoot() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isElement() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAttribute() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isText() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final long getNodeKey() {
        return mData[NODE_KEY];
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasParent() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public long getParentKey() {
        return IReadTransaction.NULL_NODE_KEY;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasFirstChild() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public long getFirstChildKey() {
        return IReadTransaction.NULL_NODE_KEY;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasLeftSibling() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public long getLeftSiblingKey() {
        return IReadTransaction.NULL_NODE_KEY;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRightSibling() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public long getRightSiblingKey() {
        return IReadTransaction.NULL_NODE_KEY;
    }

    /**
     * {@inheritDoc}
     */
    public long getChildCount() {
        return 0L;
    }

    /**
     * {@inheritDoc}
     */
    public int getAttributeCount() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getNamespaceCount() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public long getAttributeKey(final int index) {
        return IReadTransaction.NULL_NODE_KEY;
    }

    /**
     * {@inheritDoc}
     */
    public long getNamespaceKey(final int index) {
        return IReadTransaction.NULL_NODE_KEY;
    }

    /**
     * {@inheritDoc}
     */
    public int getKind() {
        return IConstants.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    public int getNameKey() {
        return IReadTransaction.NULL_NAME_KEY;
    }

    /**
     * {@inheritDoc}
     */
    public int getURIKey() {
        return IReadTransaction.NULL_NAME_KEY;
    }

    /**
     * {@inheritDoc}
     */
    public int getTypeKey() {
        return IConstants.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getRawValue() {
        return null;
    }

    /**
     * Serializing the data.
     * 
     * @param out
     *            target to serialize.
     */
    public void serialize(final ITTSink out) {
        for (final long longVal : mData) {
            out.writeLong(longVal);
        }

    }

    /**
     * Serializing the data.
     * 
     * @param out
     *            target to serialize.
     */
    public void serialize(final TupleOutput out) {
        for (final long longVal : mData) {
            out.writeLong(longVal);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setNodeKey(final long nodeKey) {
        mData[NODE_KEY] = nodeKey;
    }

    /**
     * Setting the parent key.
     * 
     * @param parentKey
     *            the key for the parent.
     */
    public void setParentKey(final long parentKey) {
    }

    /**
     * Setting the first child key.
     * 
     * @param firstChildKey
     *            the key for the first child.
     */
    public void setFirstChildKey(final long firstChildKey) {
    }

    /**
     * Setting the left sibling key.
     * 
     * @param leftSiblingKey
     *            the key for the left sibling.
     */
    public void setLeftSiblingKey(final long leftSiblingKey) {
    }

    /**
     * Setting the right sibling key.
     * 
     * @param rightSiblingKey
     *            the key for the right sibling.
     */
    public void setRightSiblingKey(final long rightSiblingKey) {
    }

    /**
     * Setting the child count.
     * 
     * @param childCount
     *            to be set.
     */
    public void setChildCount(final long childCount) {
    }

    /**
     * Incrementing the child count.
     */
    public void incrementChildCount() {
    }

    /**
     * Decrementing the child count.
     */
    public void decrementChildCount() {
    }

    /**
     * Inserting attribute to this node.
     * 
     * @param attributeKey
     *            to be inserted
     */
    public void insertAttribute(final long attributeKey) {
    }

    /**
     * Inserting namespace to this node.
     * 
     * @param namespaceKey
     *            to be added.
     */
    public void insertNamespace(final long namespaceKey) {
    }

    /**
     * Setting the kind of this node.
     * 
     * @param kind
     *            to be set.
     */
    public void setKind(final byte kind) {
    }

    /**
     * Setting the name key for this node.
     * 
     * @param nameKey
     *            to be set.
     */
    public void setNameKey(final int nameKey) {
    }

    /**
     * Setting the uri for this node.
     * 
     * @param uriKey
     *            to be set.
     */
    public void setURIKey(final int uriKey) {
    }

    /**
     * Setting the value for this node.
     * 
     * @param valueType
     *            type of value to be set.
     * @param value
     *            the value to be set.
     */
    public void setValue(final int valueType, final byte[] value) {
    }

    /**
     * Setting the type of this node.
     * 
     * @param valueType
     *            to be set.
     */
    public void setType(final int valueType) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) mData[NODE_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return ((obj != null) && (mData[NODE_KEY] == ((AbstractNode) obj).mData[NODE_KEY]));
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final AbstractNode node) {
        final long nodeKey = (node).getNodeKey();
        if (mData[NODE_KEY] < nodeKey) {
            return -1;
        } else if (mData[NODE_KEY] == nodeKey) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "\n\tnode key: " + getNodeKey() + "\n\tchildcount: "
                + getChildCount() + "\n\tparentKey: " + getParentKey()
                + "\n\tfirstChildKey: " + getFirstChildKey()
                + "\n\tleftSiblingKey: " + getLeftSiblingKey()
                + "\n\trightSiblingKey: " + getRightSiblingKey() + "\n";
    }
}

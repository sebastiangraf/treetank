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

import com.treetank.api.IItem;
import com.treetank.io.ITTSink;
import com.treetank.settings.EFixed;

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
public abstract class AbsNode implements IItem, Comparable<AbsNode> {

    /** standard NODE_KEY. */
    protected static final int NODE_KEY = 0;

    /** standard PARENT_KEY */
    protected static final int PARENT_KEY = 1;

    /** Node key is common to all node kinds. */
    protected final long[] mData;

    /**
     * Constructor for inserting node.
     * 
     * @param nodeKey
     *            Key of node.
     * @param size
     *            Size of the data.
     */
    AbsNode(final long[] builder) {
        mData = builder;
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
        return mData[PARENT_KEY] != (Long) EFixed.NULL_NODE_KEY
                .getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    public long getParentKey() {
        return mData[PARENT_KEY];
    }

    /**
     * {@inheritDoc}
     */
    public ENodes getKind() {
        return ENodes.UNKOWN_KIND;
    }

    /**
     * {@inheritDoc}
     */
    public int getNameKey() {
        return (Integer) EFixed.NULL_INT_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    public int getURIKey() {
        return (Integer) EFixed.NULL_INT_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    public int getTypeKey() {
        return (Integer) EFixed.NULL_INT_KEY.getStandardProperty();
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
        mData[PARENT_KEY] = parentKey;
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
        return ((obj != null) && (mData[NODE_KEY] == ((AbsNode) obj).mData[NODE_KEY]));
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final AbsNode node) {
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
        return new StringBuilder("\n").append(this.getClass().getName())
                .append("\n\tnode key: ").append(getNodeKey())
                .append("\n\tparentKey: ").append(getParentKey()).toString();
    }

    public abstract AbsNode clone();

    protected static long[] cloneData(final long[] input) {
        final long[] data = new long[input.length];
        System.arraycopy(input, 0, data, 0, data.length);
        return data;
    }

    protected static byte[] cloneValue(final byte[] input) {
        final byte[] value = new byte[input.length];
        System.arraycopy(input, 0, value, 0, value.length);
        return value;
    }

}

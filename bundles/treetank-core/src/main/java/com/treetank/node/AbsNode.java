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

import java.util.Arrays;

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

    /** standard TYPE_KEY */
    protected static final int TYPE_KEY = 0;

    /** Node key is common to all node kinds. */
    protected final long[] mLongData;

    /** Node key is common to all node data. */
    protected final int[] mIntData;

    /**
     * Constructor for inserting node.
     * 
     * @param longBuilder
     *            longData to build
     * @param intBuilder
     *            intData to build
     */
    AbsNode(final long[] longBuilder, final int[] intBuilder) {
        mLongData = longBuilder;
        mIntData = intBuilder;
    }

    /**
     * {@inheritDoc}
     */
    public final long getNodeKey() {
        return mLongData[NODE_KEY];
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasParent() {
        return mLongData[PARENT_KEY] != (Long) EFixed.NULL_NODE_KEY
                .getStandardProperty();
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
    public long getParentKey() {
        return mLongData[PARENT_KEY];
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
    public int getURIKey() {
        return (Integer) EFixed.NULL_INT_KEY.getStandardProperty();
    }

    /**
     * {@inheritDoc}
     */
    public int getTypeKey() {
        return mIntData[TYPE_KEY];
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
        for (final long longVal : mLongData) {
            out.writeLong(longVal);
        }
        for (final int intVal : mIntData) {
            out.writeInt(intVal);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void setNodeKey(final long nodeKey) {
        mLongData[NODE_KEY] = nodeKey;
    }

    /**
     * Setting the parent key.
     * 
     * @param parentKey
     *            the key for the parent.
     */
    public void setParentKey(final long parentKey) {
        mLongData[PARENT_KEY] = parentKey;
    }

    /**
     * Setting the type of this node.
     * 
     * @param valueType
     *            to be set.
     */
    public void setType(final int valueType) {
        mIntData[TYPE_KEY] = valueType;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(mLongData);
        result = prime * result + Arrays.hashCode(mIntData);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final AbsNode node) {
        final long nodeKey = (node).getNodeKey();
        if (mLongData[NODE_KEY] < nodeKey) {
            return -1;
        } else if (mLongData[NODE_KEY] == nodeKey) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("\n").append(this.getClass().getName())
                .append("\n\tnode key: ").append(getNodeKey())
                .append("\n\tparentKey: ").append(getParentKey())
                .append("\n\ttypeKey: ").append(getTypeKey()).toString();
    }

    @Override
    public abstract AbsNode clone();

    protected static long[] cloneData(final long[] input) {
        final long[] data = new long[input.length];
        System.arraycopy(input, 0, data, 0, data.length);
        return data;
    }

    protected static int[] cloneData(final int[] input) {
        final int[] data = new int[input.length];
        System.arraycopy(input, 0, data, 0, data.length);
        return data;
    }

    protected static byte[] cloneData(final byte[] input) {
        final byte[] value = new byte[input.length];
        System.arraycopy(input, 0, value, 0, value.length);
        return value;
    }

}

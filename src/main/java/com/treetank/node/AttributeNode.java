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
 * $Id: AttributeNode.java 4550 2009-02-05 09:25:46Z graf $
 */

package com.treetank.node;

import com.treetank.constants.ENodes;
import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;

/**
 * <h1>AttributeNode</h1>
 * 
 * <p>
 * Node representing an attribute.
 * </p>
 */
public final class AttributeNode extends AbstractNode {

    private static final int SIZE = 6;

    private static final int PARENT_KEY = 1;

    private static final int NAME_KEY = 2;

    private static final int URI_KEY = 3;

    private static final int TYPE = 4;

    private static final int VALUE_LENGTH = 5;

    /** Value of attribute. */
    private byte[] mValue;

    /**
     * Constructor to create attribute.
     * 
     * @param nodeKey
     *            Key of node.
     * @param parentKey
     *            Parent of this node.
     * @param nameKey
     *            Key of qualified name.
     * @param uriKey
     *            Key of URI.
     * @param type
     *            Type of attribute value.
     * @param value
     *            Value of attribute.
     */
    public AttributeNode(final long nodeKey, final long parentKey,
            final int nameKey, final int uriKey, final int type,
            final byte[] value) {
        super(SIZE, nodeKey);
        mData[PARENT_KEY] = nodeKey - parentKey;
        mData[NAME_KEY] = nameKey;
        mData[URI_KEY] = uriKey;
        mData[TYPE] = type;
        mData[VALUE_LENGTH] = value.length;
        mValue = value;
    }

    /**
     * Constructor to clone attribute.
     * 
     * @param attribute
     *            Attribute to clone.
     */
    protected AttributeNode(final AbstractNode attribute) {
        super(attribute);
        mValue = attribute.getRawValue();
    }

    /**
     * 
     * Constructor.
     * 
     * @param in
     *            buffer for the data
     */
    protected AttributeNode(final ITTSource in) {
        super(SIZE, in);
        mValue = new byte[(int) mData[VALUE_LENGTH]];
        for (int i = 0; i < mValue.length; i++) {
            mValue[i] = in.readByte();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttribute() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameKey() {
        return (int) mData[NAME_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNameKey(final int nameKey) {
        this.mData[NAME_KEY] = nameKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasParent() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getParentKey() {
        return mData[NODE_KEY] - mData[PARENT_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentKey(final long parentKey) {
        mData[PARENT_KEY] = mData[NODE_KEY] - parentKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getURIKey() {
        return (int) mData[URI_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setURIKey(final int uriKey) {
        mData[URI_KEY] = uriKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTypeKey() {
        return (int) mData[TYPE];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRawValue() {
        return mValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final int valueType, final byte[] value) {
        mData[TYPE] = valueType;
        mData[VALUE_LENGTH] = value.length;
        mValue = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setType(final int valueType) {
        mData[TYPE] = valueType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ENodes getKind() {
        return ENodes.ATTRIBUTE_KIND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink out) {
        super.serialize(out);
        for (final byte byteVal : mValue) {
            out.writeByte(byteVal);
        }
    }

}

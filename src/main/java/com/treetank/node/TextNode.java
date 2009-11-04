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
 * $Id: TextNode.java 4448 2008-08-31 07:41:34Z kramis $
 */

package com.treetank.node;

import com.treetank.api.IReadTransaction;
import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.utils.IConstants;

/**
 * <h1>TextNode</h1>
 * 
 * <p>
 * Node representing a text node.
 * </p>
 */
public final class TextNode extends AbstractNode {

    private static final int SIZE = 6;

    private static final int PARENT_KEY = 1;

    private static final int LEFT_SIBLING_KEY = 2;

    private static final int RIGHT_SIBLING_KEY = 3;

    private static final int TYPE = 4;

    private static final int VALUE_LENGTH = 5;

    /** Typed value of node. */
    private byte[] mValue;

    /**
     * Create text node.
     * 
     * @param nodeKey
     *            Key of node.
     * @param parentKey
     *            Key of parent.
     * @param leftSiblingKey
     *            Key of left sibling.
     * @param rightSiblingKey
     *            Key of right sibling.
     * @param type
     *            Type of value.
     * @param value
     *            Text value.
     */
    public TextNode(final long nodeKey, final long parentKey,
            final long leftSiblingKey, final long rightSiblingKey,
            final int type, final byte[] value) {
        super(SIZE, nodeKey);
        if (value.length > IConstants.MAX_TEXTNODE_LENGTH) {
            throw new IllegalArgumentException(
                    "Only handling textnodes with length <= "
                            + IConstants.MAX_TEXTNODE_LENGTH);
        }
        mData[PARENT_KEY] = nodeKey - parentKey;
        mData[LEFT_SIBLING_KEY] = leftSiblingKey;
        mData[RIGHT_SIBLING_KEY] = rightSiblingKey;
        mData[TYPE] = type;
        mData[VALUE_LENGTH] = value.length;
        mValue = value;
    }

    /**
     * Clone text node.
     * 
     * @param node
     *            Text node to clone.
     */
    public TextNode(final AbstractNode node) {
        super(node);
        if (node.getRawValue().length > IConstants.MAX_TEXTNODE_LENGTH) {
            throw new IllegalArgumentException(
                    "Only handling textnodes with length <= "
                            + IConstants.MAX_TEXTNODE_LENGTH);
        }
        mValue = node.getRawValue();
    }

    /**
     * Read text node.
     * 
     * @param nodeKey
     *            Key of text node.
     * @param in
     *            Input bytes to read node from.
     */
    public TextNode(final ITTSource in) {
        super(SIZE, in);
        mValue = new byte[(int) mData[VALUE_LENGTH]];
        for (int i = 0; i < mData[VALUE_LENGTH]; i++) {
            mValue[i] = in.readByte();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isText() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasParent() {
        return ((mData[NODE_KEY] - mData[PARENT_KEY]) != IReadTransaction.NULL_NODE_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long getParentKey() {
        return mData[NODE_KEY] - mData[PARENT_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setParentKey(final long parentKey) {
        mData[PARENT_KEY] = mData[NODE_KEY] - parentKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasLeftSibling() {
        return (mData[LEFT_SIBLING_KEY] != IReadTransaction.NULL_NODE_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long getLeftSiblingKey() {
        return mData[LEFT_SIBLING_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setLeftSiblingKey(final long leftSiblingKey) {
        mData[LEFT_SIBLING_KEY] = leftSiblingKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean hasRightSibling() {
        return (mData[RIGHT_SIBLING_KEY] != IReadTransaction.NULL_NODE_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long getRightSiblingKey() {
        return mData[RIGHT_SIBLING_KEY];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setRightSiblingKey(final long rightSiblingKey) {
        mData[RIGHT_SIBLING_KEY] = rightSiblingKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getKind() {
        return IReadTransaction.TEXT_KIND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getTypeKey() {
        return (int) mData[TYPE];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final byte[] getRawValue() {
        return mValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setValue(final int valueType, final byte[] value) {
        mData[TYPE] = valueType;
        mData[VALUE_LENGTH] = value.length;
        mValue = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setType(final int valueType) {
        mData[TYPE] = valueType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void serialize(final ITTSink out) {
        super.serialize(out);
        for (final byte byteVal : mValue) {
            out.writeByte(byteVal);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TextNode " + super.toString();
    }

}

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

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.settings.EFixed;
import com.treetank.settings.ENodes;

/**
 * <h1>TextNode</h1>
 * 
 * <p>
 * Node representing a text node.
 * </p>
 */
public final class TextNode extends AbstractNode implements IStructuralNode {

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
	protected TextNode(final AbstractNode node) {
		super(node);
		mValue = node.getRawValue();
	}

	/**
	 * Read text node.
	 * 
	 * @param in
	 *            Input bytes to read node from.
	 */
	protected TextNode(final ITTSource in) {
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
	public boolean hasParent() {
		return ((mData[NODE_KEY] - mData[PARENT_KEY]) != (Long) EFixed.NULL_NODE_KEY
				.getStandardProperty());
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
	public boolean hasLeftSibling() {
		return (mData[LEFT_SIBLING_KEY] != (Long) EFixed.NULL_NODE_KEY
				.getStandardProperty());
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLeftSiblingKey() {
		return mData[LEFT_SIBLING_KEY];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLeftSiblingKey(final long leftSiblingKey) {
		mData[LEFT_SIBLING_KEY] = leftSiblingKey;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasRightSibling() {
		return (mData[RIGHT_SIBLING_KEY] != (Long) EFixed.NULL_NODE_KEY
				.getStandardProperty());
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRightSiblingKey() {
		return mData[RIGHT_SIBLING_KEY];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRightSiblingKey(final long rightSiblingKey) {
		mData[RIGHT_SIBLING_KEY] = rightSiblingKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ENodes getKind() {
		return ENodes.TEXT_KIND;
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
	public void serialize(final ITTSink out) {
		super.serialize(out);
		for (final byte byteVal : mValue) {
			out.writeByte(byteVal);
		}
	}

	@Override
	public long getFirstChildKey() {
		return (Long) EFixed.NULL_NODE_KEY.getStandardProperty();
	}

	@Override
	public boolean hasFirstChild() {
		return false;
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public void setFirstChildKey(final long firstChildKey) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void decrementChildCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getChildCount() {
		return 0;
	}

	@Override
	public void incrementChildCount() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setChildCount(long childCount) {
		throw new UnsupportedOperationException();
	}

}

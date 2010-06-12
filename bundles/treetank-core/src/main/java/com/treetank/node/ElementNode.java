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
 * $Id: ElementNode.java 4550 2009-02-05 09:25:46Z graf $
 */

package com.treetank.node;

import java.util.ArrayList;
import java.util.List;

import com.treetank.io.ITTSink;
import com.treetank.io.ITTSource;
import com.treetank.settings.EFixed;
import com.treetank.settings.ENodes;

/**
 * <h1>ElementNode</h1>
 * 
 * <p>
 * Node representing an XML element.
 * </p>
 */
public final class ElementNode extends AbstractNode implements IStructuralNode {

	private static final int SIZE = 11;

	private static final int PARENT_KEY = 1;

	private static final int FIRST_CHILD_KEY = 2;

	private static final int LEFT_SIBLING_KEY = 3;

	private static final int RIGHT_SIBLING_KEY = 4;

	private static final int CHILD_COUNT = 5;

	private static final int NAME_KEY = 6;

	private static final int URI_KEY = 7;

	private static final int TYPE = 8;

	private static final int ATTRIBUTE_COUNT = 9;

	private static final int NAMESPACE_COUNT = 10;

	/** Keys of attributes. */
	private List<Long> mAttributeKeys;

	/** Keys of namespace declarations. */
	private List<Long> mNamespaceKeys;

	/**
	 * Create new element node.
	 * 
	 * @param nodeKey
	 *            Key of node.
	 * @param parentKey
	 *            Key of parent.
	 * @param firstChildKey
	 *            Key of first child.
	 * @param leftSiblingKey
	 *            Key of left sibling.
	 * @param rightSiblingKey
	 *            Key of right sibling.
	 * @param nameKey
	 *            Key of local part.
	 * @param uriKey
	 *            Key of URI.
	 * @param type
	 *            the type of the element.
	 */
	public ElementNode(final long nodeKey, final long parentKey,
			final long firstChildKey, final long leftSiblingKey,
			final long rightSiblingKey, final int nameKey, final int uriKey,
			final int type) {
		super(SIZE, nodeKey);
		mData[PARENT_KEY] = nodeKey - parentKey;
		mData[FIRST_CHILD_KEY] = nodeKey - firstChildKey;
		mData[LEFT_SIBLING_KEY] = nodeKey - leftSiblingKey;
		mData[RIGHT_SIBLING_KEY] = nodeKey - rightSiblingKey;
		mData[CHILD_COUNT] = 0;
		mData[NAME_KEY] = nameKey;
		mData[URI_KEY] = uriKey;
		mData[TYPE] = type;
		mData[ATTRIBUTE_COUNT] = 0;
		mData[NAMESPACE_COUNT] = 0;
		mAttributeKeys = null;
		mNamespaceKeys = null;
	}

	/**
	 * Clone element node.
	 * 
	 * @param node
	 *            Element node to clone.
	 */
	protected ElementNode(final ElementNode node) {
		super(node);
		if (mData[ATTRIBUTE_COUNT] > 0) {
			mAttributeKeys = new ArrayList<Long>((int) mData[ATTRIBUTE_COUNT]);
			for (int i = 0, l = (int) mData[ATTRIBUTE_COUNT]; i < l; i++) {
				mAttributeKeys.add(node.getAttributeKey(i));
			}
		}
		if (mData[NAMESPACE_COUNT] > 0) {
			mNamespaceKeys = new ArrayList<Long>((int) mData[NAMESPACE_COUNT]);
			for (int i = 0, l = (int) mData[NAMESPACE_COUNT]; i < l; i++) {
				mNamespaceKeys.add(node.getNamespaceKey(i));
			}
		}
	}

	/**
	 * Read element node.
	 * 
	 * @param in
	 *            Input bytes to read from.
	 */
	protected ElementNode(final ITTSource in) {
		super(SIZE, in);

		if (mData[ATTRIBUTE_COUNT] > 0) {
			mAttributeKeys = new ArrayList<Long>((int) mData[ATTRIBUTE_COUNT]);

			for (int i = 0; i < mData[ATTRIBUTE_COUNT]; i++) {
				mAttributeKeys.add(in.readLong());
			}
		}
		if (mData[NAMESPACE_COUNT] > 0) {
			mNamespaceKeys = new ArrayList<Long>((int) mData[NAMESPACE_COUNT]);
			for (int i = 0; i < mData[NAMESPACE_COUNT]; i++) {
				mNamespaceKeys.add(in.readLong());
			}
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
	public boolean hasFirstChild() {
		return ((mData[NODE_KEY] - mData[FIRST_CHILD_KEY]) != (Long) EFixed.NULL_NODE_KEY
				.getStandardProperty());
	}

	/**
	 * {@inheritDoc}
	 */
	public long getFirstChildKey() {
		return mData[NODE_KEY] - mData[FIRST_CHILD_KEY];
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFirstChildKey(final long firstChildKey) {
		mData[FIRST_CHILD_KEY] = mData[NODE_KEY] - firstChildKey;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasLeftSibling() {
		return ((mData[NODE_KEY] - mData[LEFT_SIBLING_KEY]) != (Long) EFixed.NULL_NODE_KEY
				.getStandardProperty());
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLeftSiblingKey() {
		return mData[NODE_KEY] - mData[LEFT_SIBLING_KEY];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLeftSiblingKey(final long leftSiblingKey) {
		mData[LEFT_SIBLING_KEY] = mData[NODE_KEY] - leftSiblingKey;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasRightSibling() {
		return ((mData[NODE_KEY] - mData[RIGHT_SIBLING_KEY]) != (Long) EFixed.NULL_NODE_KEY
				.getStandardProperty());
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRightSiblingKey() {
		return mData[NODE_KEY] - mData[RIGHT_SIBLING_KEY];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRightSiblingKey(final long rightSiblingKey) {
		mData[RIGHT_SIBLING_KEY] = mData[NODE_KEY] - rightSiblingKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getChildCount() {
		return mData[CHILD_COUNT];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setChildCount(final long childCount) {
		mData[CHILD_COUNT] = childCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void incrementChildCount() {
		mData[CHILD_COUNT] += 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decrementChildCount() {
		mData[CHILD_COUNT] -= 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getAttributeCount() {
		return (int) mData[ATTRIBUTE_COUNT];
	}

	/**
	 * {@inheritDoc}
	 */
	public long getAttributeKey(final int index) {
		if (mAttributeKeys == null) {
			return (Long) EFixed.NULL_NODE_KEY.getStandardProperty();
		}
		return mAttributeKeys.get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public void insertAttribute(final long attributeKey) {
		if (mAttributeKeys == null) {
			mAttributeKeys = new ArrayList<Long>(1);
		}
		mAttributeKeys.add(attributeKey);
		mData[ATTRIBUTE_COUNT] += 1;
	}

	/**
	 * Removing an attribute
	 * 
	 * @param attributeKey
	 *            the key of the attribute to be removed
	 */
	public void removeAttribute(final long attributeKey) {
		mAttributeKeys.remove(attributeKey);
		mData[ATTRIBUTE_COUNT] -= 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNamespaceCount() {
		return (int) mData[NAMESPACE_COUNT];
	}

	/**
	 * {@inheritDoc}
	 */
	public long getNamespaceKey(final int index) {
		if (mNamespaceKeys == null) {
			return (Long) EFixed.NULL_NODE_KEY.getStandardProperty();
		}
		return mNamespaceKeys.get(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public void insertNamespace(final long namespaceKey) {
		if (mNamespaceKeys == null) {
			mNamespaceKeys = new ArrayList<Long>(1);
		}
		mNamespaceKeys.add(namespaceKey);
		mData[NAMESPACE_COUNT] += 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ENodes getKind() {
		return ENodes.ELEMENT_KIND;
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
	public void setNameKey(final int localPartKey) {
		mData[NAME_KEY] = localPartKey;
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
	public void setType(final int valueType) {
		mData[TYPE] = valueType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(final ITTSink out) {
		super.serialize(out);
		if (mAttributeKeys != null) {
			for (int i = 0, l = mAttributeKeys.size(); i < l; i++) {
				out.writeLong(mAttributeKeys.get(i));
			}
		}
		if (mNamespaceKeys != null) {
			for (int i = 0, l = mNamespaceKeys.size(); i < l; i++) {
				out.writeLong(mNamespaceKeys.get(i));
			}
		}
	}

	@Override
	public boolean isLeaf() {
		return false;
	}

}

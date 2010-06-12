package com.treetank.node;

import com.treetank.api.IItem;

public interface IStructuralNode extends IItem {

	/**
	 * Setting the first child key.
	 * 
	 * @param firstChildKey
	 *            the key for the first child.
	 */
	void setFirstChildKey(final long firstChildKey);

	/**
	 * Setting the left sibling key.
	 * 
	 * @param leftSiblingKey
	 *            the key for the left sibling.
	 */
	void setLeftSiblingKey(final long leftSiblingKey);

	/**
	 * Setting the right sibling key.
	 * 
	 * @param rightSiblingKey
	 *            the key for the right sibling.
	 */
	void setRightSiblingKey(final long rightSiblingKey);

	/**
	 * Gets key of the context item's first child.
	 * 
	 * @return first child's key
	 */
	long getFirstChildKey();

	/**
	 * Gets key of the context item's left sibling.
	 * 
	 * @return left sibling key
	 */
	long getLeftSiblingKey();

	/**
	 * Gets key of the context item's right sibling.
	 * 
	 * @return right sibling key
	 */
	long getRightSiblingKey();

	/**
	 * Declares, whether the item has a first child.
	 * 
	 * @return true, if item has a first child
	 */
	boolean hasFirstChild();

	/**
	 * Declares, whether the item has a left sibling.
	 * 
	 * @return true, if item has a left sibling
	 */
	boolean hasLeftSibling();

	/**
	 * Declares, whether the item has a right sibling.
	 * 
	 * @return true, if item has a right sibling
	 */
	boolean hasRightSibling();

	/**
	 * Gets the number of children of the item.
	 * 
	 * @return item's number of children
	 */
	long getChildCount();

	/**
	 * Setting the child count.
	 * 
	 * @param childCount
	 *            to be set.
	 */
	void setChildCount(final long childCount);

	/**
	 * Incrementing the child count.
	 */
	void incrementChildCount();

	/**
	 * Decrementing the child count.
	 */
	void decrementChildCount();

}

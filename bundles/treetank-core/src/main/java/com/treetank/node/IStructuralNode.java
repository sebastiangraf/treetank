package com.treetank.node;

import com.treetank.api.IItem;

public interface IStructuralNode extends IItem {

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

}

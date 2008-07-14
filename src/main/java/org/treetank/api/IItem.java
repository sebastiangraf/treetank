/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
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
 * $Id: IItemList.java 4246 2008-07-08 08:54:09Z scherer $
 */

package org.treetank.api;

import org.treetank.nodelayer.AbstractNode;

/**
 * <h1>IItem</h1>
 * <p>
 * Common interface for all item kinds. An item can be a node or an atomic 
 * value.
 * </p>
 * 
 * @author Tina Scherer
 *
 */
public interface IItem {
  
  /**
   * Sets unique node key.
   * 
   * @param key Unique (negative) key of item
   */
  public void setNodeKey(final long key);

  /**
   * Gets unique node key.
   * TODO: maybe this should be renamed in "getItemKey()"
   * 
   * @return node key
   */
  public long getNodeKey();

  /**
   * Gets key of the context item's parent.
   * 
   * @return parent key
   */
  public long getParentKey();

  /**
   * Gets key of the context item's first child.
   * 
   * @return first child's key
   */
  public long getFirstChildKey();

  /**
   * Gets key of the context item's left sibling.
   * 
   * @return left sibling key
   */
  public long getLeftSiblingKey();

  /**
   * Gets key of the context item's right sibling.
   * 
   * @return right sibling key
   */
  public long getRightSiblingKey();

  /**
   * Gets the nodes attribute with the specified index.
   * 
   * @param index  index of the attribute to get
   * @return  attribute at index
   */
  public AbstractNode getAttribute(int index);

  /**
   * Declares, whether the item has a parent.
   * 
   * @return true, if item has a parent
   */
  public boolean hasParent();

  /**
   * Declares, whether the item has a first child.
   * 
   * @return true, if item has a first child
   */
  public boolean hasFirstChild();

  /**
   * Declares, whether the item has a left sibling.
   * 
   * @return true, if item has a left sibling
   */
  public boolean hasLeftSibling();

  /**
   * Declares, whether the item has a right sibling.
   * 
   * @return true, if item has a right sibling
   */
  public boolean hasRightSibling();

  /**
   * Return a byte array representation of the item's value.
   * 
   * @return returns the value of the item
   */
  public byte[] getRawValue();

  /**
   * Gets the number of children of the item.
   * 
   * @return item's number of children
   */
  public long getChildCount();

  /**
   * Gets the number of attributes of the item.
   * 
   * @return item's number of attributes.
   */
  public int getAttributeCount();

  /**
   * Gets the number of namespaces of the item.
   * 
   * @return item's number of namespaces.
   */
  public int getNamespaceCount();

  /**
   * Gets namespace of the item at the specified position.
   * 
   * @param index index of the namespace to get
   * @return item's namespace at the given index
   */
  public AbstractNode getNamespace(final int index);

  /**
   * Gets the kind of the item (atomic value, element node, attribute node....).
   * 
   * @return kind of item
   */
  public int getKind();
  
  /**
   * Declaresm whether the item is a a node.
   * 
   * @return true if item is a node.
   */
  public boolean isNode();

  /**
   * Declares, whether the item is the document root.
   * 
   * @return true, if item is the document root
   */
  public boolean isDocumentRoot();

  /**
   * Declares, whether the item an element node.
   * 
   * @return true, if item is an element node
   */
  public boolean isElement();

  /**
   * Declares, whether the item an attribute node.
   * 
   * @return true, if item is an attribute node
   */
  public boolean isAttribute();

  /**
   * Declares, whether the item a text node.
   * 
   * @return true, if item is a text node
   */
  public boolean isText();

  /**
   * Declares, whether the item a full text node.
   * 
   * @return true, if item is a full text node
   */
  public boolean isFullText();

  /**
   * Declares, whether the item a full text leaf.
   * 
   * @return true, if item is a full text leaf
   */
  public boolean isFullTextLeaf();

  /**
   * Declares, whether the item a full text root.
   * 
   * @return true, if item is a full text root
   */
  public boolean isFullTextRoot();

  /**
   * Gets key of qualified name.
   * 
   * @return  key of qualified name
   */
  public int getNameKey();

  /**
   * Gets key of the URI.
   * 
   * @return URI key
   */
  public int getURIKey();

  /**
   * Gets value type of the item.
   * 
   * @return  value type
   */
  public int getTypeKey();

}

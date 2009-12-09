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
 * $Id: IItem.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.api;

import com.treetank.settings.ENodes;

/**
 * <h1>IItem</h1>
 * <p>
 * Common interface for all item kinds. An item can be a node or an atomic
 * value.
 */
public interface IItem {

    /**
     * Sets unique node key.
     * 
     * @param key
     *            Unique (negative) key of item
     */
    void setNodeKey(final long key);

    /**
     * Gets unique node key. TODO: maybe this should be renamed in
     * "getItemKey()"
     * 
     * @return node key
     */
    long getNodeKey();

    /**
     * Gets key of the context item's parent.
     * 
     * @return parent key
     */
    long getParentKey();

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
     * Gets the nodes attribute with the specified index.
     * 
     * @param index
     *            index of the attribute to get
     * @return attribute key at index
     */
    long getAttributeKey(int index);

    /**
     * Declares, whether the item has a parent.
     * 
     * @return true, if item has a parent
     */
    boolean hasParent();

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
     * Return a byte array representation of the item's value.
     * 
     * @return returns the value of the item
     */
    byte[] getRawValue();

    /**
     * Gets the number of children of the item.
     * 
     * @return item's number of children
     */
    long getChildCount();

    /**
     * Gets the number of attributes of the item.
     * 
     * @return item's number of attributes.
     */
    int getAttributeCount();

    /**
     * Gets the number of namespaces of the item.
     * 
     * @return item's number of namespaces.
     */
    int getNamespaceCount();

    /**
     * Gets namespace of the item at the specified position.
     * 
     * @param index
     *            index of the namespace to get
     * @return item's namespace key at the given index
     */
    long getNamespaceKey(final int index);

    /**
     * Gets the kind of the item (atomic value, element node, attribute
     * node....).
     * 
     * @return kind of item
     */
    ENodes getKind();

    /**
     * Declaresm whether the item is a a node.
     * 
     * @return true if item is a node.
     */
    boolean isNode();

    /**
     * Declares, whether the item is the document root.
     * 
     * @return true, if item is the document root
     */
    boolean isDocumentRoot();

    /**
     * Declares, whether the item an element node.
     * 
     * @return true, if item is an element node
     */
    boolean isElement();

    /**
     * Declares, whether the item an attribute node.
     * 
     * @return true, if item is an attribute node
     */
    boolean isAttribute();

    /**
     * Declares, whether the item a text node.
     * 
     * @return true, if item is a text node
     */
    boolean isText();

    /**
     * Gets key of qualified name.
     * 
     * @return key of qualified name
     */
    int getNameKey();

    /**
     * Gets key of the URI.
     * 
     * @return URI key
     */
    int getURIKey();

    /**
     * Gets value type of the item.
     * 
     * @return value type
     */
    int getTypeKey();

}

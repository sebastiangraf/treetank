/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.api;

/**
 * Class to denote that an {@link IItem} has structural attributes. That means that a class can have pointer
 * to neighbours.
 */
public interface IStructuralItem extends IItem {

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

}

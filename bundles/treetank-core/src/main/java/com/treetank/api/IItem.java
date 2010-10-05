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

import com.treetank.node.ENodes;

/**
 * <h1>IItem</h1>
 * <p>
 * Common interface for all item kinds. An item can be a node or an atomic value.
 */
public interface IItem {

    void setHash(final long hash);

    long getHash();

    /**
     * Sets unique node key.
     * 
     * 
     * @param mKey
     *            Unique (negative) key of item
     */
    void setNodeKey(final long mKey);

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
     * Declares, whether the item has a parent.
     * 
     * @return true, if item has a parent
     */
    boolean hasParent();

    /**
     * Return a byte array representation of the item's value.
     * 
     * @return returns the value of the item
     */
    byte[] getRawValue();

    /**
     * Gets the kind of the item (atomic value, element node, attribute
     * node....).
     * 
     * @return kind of item
     */
    ENodes getKind();

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

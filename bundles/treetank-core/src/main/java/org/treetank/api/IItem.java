/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.api;

import org.treetank.node.AbsNode;
import org.treetank.node.ENodes;

/**
 * <h1>IItem</h1>
 * <p>
 * Common interface for all item kinds. An item can be a node or an atomic value.
 */
public interface IItem {

    /**
     * Setting the actual hash of the structure. The hash of one node should have the entire integrity of the
     * related subtree.
     * 
     * @param paramHash
     *            hash to be set for this node
     * 
     */
    void setHash(final long paramHash);

    /**
     * Getting the persistent stored hash.
     * 
     * @return the hash of this node
     */
    long getHash();

    /**
     * Sets unique node key.
     * 
     * 
     * @param paramKey
     *            Unique (negative) key of item
     */
    void setNodeKey(final long paramKey);

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

    /**
     * Accepts a visitor which is a {@link IReadTransaction}.
     * 
     * @param <T>
     *            type which extends {@link IItem}
     * @param paramTransaction
     *            {@link IReadTransaction}
     * @return instance of a type which extends {@link IItem}
     */
    <T extends IItem> T accept(final IReadTransaction paramTransaction);

    /**
     * Accept a visitor and use double dispatching to invoke the visitor method.
     * 
     * @param paramVisitor
     *            implementation of the {@link IVisitor} interface
     */
    void acceptVisitor(final IVisitor paramVisitor);
}

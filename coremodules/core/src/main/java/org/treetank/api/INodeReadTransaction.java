/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

import javax.xml.namespace.QName;

import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.node.interfaces.INode;
import org.treetank.node.interfaces.IStructNode;

/**

 */
public interface INodeReadTransaction {

    /**
     * Get ID of transaction.
     * 
     * @return ID of transaction.
     */
    long getTransactionID();

    /**
     * What is the revision number of this transaction?
     * 
     * @throws TTIOException
     *             if can't get Max Node Key.
     * @return Immutable revision number of this INodeReadTransaction.
     */
    long getRevisionNumber() throws TTIOException;

    /**
     * UNIX-style timestamp of the commit of the revision.
     * 
     * @throws TTIOException
     *             if can't get Max Node Key.
     * @return Timestamp of revision commit.
     */
    long getRevisionTimestamp() throws TTIOException;

    /**
     * Getting the maximum nodekey avaliable in this revision.
     * 
     * @throws TTIOException
     *             if can't get Max Node Key.
     * @return the maximum nodekey
     */
    long getMaxNodeKey() throws TTIOException;

    // --- Node Selectors
    // --------------------------------------------------------

    /**
     * Move cursor to a node by its node key.
     * 
     * @param pKey
     *            Key of node to select.
     * @return True if the node with the given node key is selected.
     */
    boolean moveTo(final long pKey);

    /**
     * Move cursor to document root node.
     * 
     * @return True if the document root node is selected.
     */
    boolean moveToDocumentRoot();

    /**
     * Move cursor to parent node of currently selected node.
     * 
     * @return True if the parent node is selected.
     */
    boolean moveToParent();

    /**
     * Move cursor to first child node of currently selected node.
     * 
     * @return True if the first child node is selected.
     */
    boolean moveToFirstChild();

    /**
     * Move cursor to left sibling node of the currently selected node.
     * 
     * @return True if the left sibling node is selected.
     */
    boolean moveToLeftSibling();

    /**
     * Move cursor to right sibling node of the currently selected node.
     * 
     * @return True if the right sibling node is selected.
     */
    boolean moveToRightSibling();

    /**
     * Move cursor to attribute by its index.
     * 
     * @param pIndex
     *            Index of attribute to move to.
     * @return True if the attribute node is selected.
     */
    boolean moveToAttribute(final int pIndex);

    /**
     * Move cursor to namespace declaration by its index.
     * 
     * @param pIndex
     *            Index of attribute to move to.
     * @return True if the namespace node is selected.
     */
    boolean moveToNamespace(final int pIndex);

    // --- Node Getters
    // ----------------------------------------------------------

    /**
     * Getting the value of the current node.
     * 
     * @return the current value of the node
     */
    String getValueOfCurrentNode();

    /**
     * Getting the name of a current node.
     * 
     * @return the {@link QName} of the node
     */
    QName getQNameOfCurrentNode();

    /**
     * Getting the type of the current node.
     * 
     * @return the normal type of the node
     */
    String getTypeOfCurrentNode();

    /**
     * Get name for key. This is used for efficient key testing.
     * 
     * @param pKey
     *            Key, i.e., local part key, URI key, or prefix key.
     * @return String containing name for given key.
     */
    String nameForKey(final int pKey);

    /**
     * Get raw name for key. This is used for efficient key testing.
     * 
     * @param pKey
     *            Key, i.e., local part key, URI key, or prefix key.
     * @return Byte array containing name for given key.
     */
    byte[] rawNameForKey(final int pKey);

    /**
     * Getting the current node.
     * 
     * @return the node
     */
    INode getNode();

    /**
     * Close shared read transaction and immediately release all resources.
     * 
     * This is an idempotent operation and does nothing if the transaction is
     * already closed.
     * 
     * @throws AbsTTException
     *             If can't close Read Transaction.
     */
    void close() throws AbsTTException;

    /**
     * Is this transaction closed?
     * 
     * @return true if closed, false otherwise
     */
    boolean isClosed();

    /**
     * This method returns the current {@link INode} as a {@link IStructNode}.
     * 
     * @return the current node as {@link IStructNode} if possible,
     *         otherwise null.
     */
    IStructNode getStructuralNode();
}

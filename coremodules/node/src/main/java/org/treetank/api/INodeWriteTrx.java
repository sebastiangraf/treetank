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

import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

/**
 * <h1>INodeWriteTrx</h1>
 * 
 * <h2>Description</h2>
 * 
 * <p>
 * Interface to access nodes based on the
 * Key/ParentKey/FirstChildKey/LeftSiblingKey/RightSiblingKey/ChildCount encoding. This encoding keeps the
 * children ordered but has no knowledge of the global node ordering. The underlying tree is accessed in a
 * cursor-like fashion.
 * </p>
 * 
 */
public interface INodeWriteTrx extends INodeReadTrx {

    // --- Node Modifiers
    // --------------------------------------------------------

    /**
     * Insert new element node as first child of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param pName
     *            {@link QName} of node to insert
     * @throws TTException
     *             if element node couldn't be inserted as first child
     * @return key of inserted node
     */
    long insertElementAsFirstChild(final QName pName) throws TTException;

    /**
     * Insert new text node as first child of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param pValue
     *            value of node to insert
     * @throws TTException
     *             if text node couldn't be inserted as first child.
     * @return Key of inserted node. Already has a first child.
     */
    long insertTextAsFirstChild(final String pValue) throws TTException;

    /**
     * Insert new element node as right sibling of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param pFromKey
     *            {@link QName} of the new node
     * @throws TTException
     *             if element node couldn't be inserted as right sibling
     * @return Key of inserted node. Already has a first child.
     */
    long insertElementAsRightSibling(final QName pFromKey) throws TTException;

    /**
     * Insert new text node as right sibling of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param pValue
     *            value of node to insert
     * @throws TTException
     *             if text node couldn't be inserted as right sibling
     * @return Key of inserted node. the root node which is not allowed to have
     *         right siblings.
     */
    long insertTextAsRightSibling(final String pValue) throws TTException;

    /**
     * Insert attribute in currently selected node. The cursor is moved to the
     * inserted node.
     * 
     * @param pName
     *            {@link QName} reference
     * @param pValue
     *            value of inserted node
     * @throws TTException
     *             if attribute couldn't be inserted.
     * @return key of inserted node
     */
    long insertAttribute(final QName pName, final String pValue) throws TTException;

    /**
     * Insert namespace declaration in currently selected node. The cursor is
     * moved to the inserted node.
     * 
     * @param pName
     *            {@link QName} reference
     * @throws TTException
     *             if attribute couldn't be inserted.
     * @return key of inserted node
     */
    long insertNamespace(final QName pName) throws TTException;

    /**
     * Remove currently selected node. This does automatically remove
     * descendants.
     * 
     * The cursor is located at the former right sibling. If there was no right
     * sibling, it is located at the former left sibling. If there was no left
     * sibling, it is located at the former parent.
     * 
     * @throws TTException
     *             if node couldn't be removed
     */
    void remove() throws TTException;

    // --- Node Setters
    // -----------------------------------------------------------

    /**
     * Set QName of node.
     * 
     * @param pName
     *            New qualified name of node.
     * @throws TTIOException
     *             If can't set Name in node.
     */
    void setQName(final QName pName) throws TTException;

    /**
     * Set URI of node.
     * 
     * @param pUri
     *            new URI of node
     * @throws TTIOException
     *             if URI of node couldn't be set
     */
    void setURI(final String pUri) throws TTException;

    /**
     * Set value of node.
     * 
     * @param pValue
     *            new value of node
     * @throws TTIOException
     *             if value couldn't be set
     */
    void setValue(final String pValue) throws TTException;

    /**
     * CommitStrategy all modifications of the exclusive write transaction. Even commit
     * if there are no modification at all.
     * 
     * @throws TTException
     *             if this revision couldn't be commited
     */
    void commit() throws TTException;

    /**
     * Abort all modifications of the exclusive write transaction.
     * 
     * @throws TTIOException
     *             if this revision couldn't be aborted
     */
    void abort() throws TTException;

    /**
     * Reverting all changes to the revision defined. This command has to be
     * finalized with a commit.
     * 
     * @param pRev
     *            revert to the revision
     * @throws TTException
     *             if couldn't revert to revision
     */
    void revertTo(final long pRev) throws TTException;

    /**
     * Closing current NodeWriteTrx.
     * 
     * @throws TTException
     *             if write transaction couldn't be closed
     */
    @Override
    void close() throws TTException;

    /**
     * Getting the Page Transaction
     * 
     * @return the stored {@link IPageWriteTrx}.
     * @throws TTException
     */
    IPageWriteTrx getPageWtx() throws TTException;

}

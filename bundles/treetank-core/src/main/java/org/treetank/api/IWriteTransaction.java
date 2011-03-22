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

import javax.xml.namespace.QName;

import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;

/**
 * <h1>IWriteTransaction</h1>
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
 * <p>
 * Each commit at least adds <code>10kB</code> to the TreeTank file. It is thus recommended to work with the
 * auto commit mode only committing after a given amount of node modifications or elapsed time. For very
 * update-intensive data, a value of one million modifications and ten seconds is recommended. Note that this
 * might require to increment to available heap.
 * </p>
 * 
 * <h2>Convention</h2>
 * 
 * <p>
 * <ol>
 * <li>Only a single thread accesses the single IWriteTransaction instance.</li>
 * <li><strong>Precondition</strong> before moving cursor: <code>IWriteTransaction.getNodeKey() == n</code>.
 * </li>
 * <li><strong>Postcondition</strong> after modifying the cursor: <code>(IWriteTransaction.insertX() == m &&
 *       IWriteTransaction.getNodeKey() == m)</code>.</li>
 * </ol>
 * </p>
 * 
 * <h2>User Example</h2>
 * 
 * <p>
 * 
 * <pre>
 * // Without auto commit.
 * final IWriteTransaction wtx = session.beginWriteTransaction();
 * wtx.insertElementAsFirstChild(&quot;foo&quot;);
 * // Explicit forced commit.
 * wtx.commit();
 * wtx.close();
 * 
 * // With auto commit after every 10th modification.
 * final IWriteTransaction wtx = session.beginWriteTransaction(10, 0);
 * wtx.insertElementAsFirstChild(new QName(&quot;foo&quot;));
 * // Implicit commit.
 * wtx.close();
 * 
 * // With auto commit after every second.
 * final IWriteTransaction wtx = session.beginWriteTransaction(0, 1);
 * wtx.insertElementAsFirstChild(new QName(&quot;foo&quot;));
 * // Implicit commit.
 * wtx.close();
 * 
 * // With auto commit after every 10th modification and every second.
 * final IWriteTransaction wtx = session.beginWriteTransaction(10, 1);
 * wtx.insertElementAsFirstChild(new QName(&quot;foo&quot;));
 * // Implicit commit.
 * wtx.close();
 * </pre>
 * 
 * </p>
 * 
 * <h2>Developer Example</h2>
 * 
 * <p>
 * 
 * <pre>
 *   public final void someIWriteTransactionMethod() {
 *     // This must be called to make sure the transaction is not closed.
 *     assertNotClosed();
 *     // This must be called to track the modifications.
 *     mModificationCount++;
 *     ...
 *   }
 * </pre>
 * 
 * </p>
 */
public interface IWriteTransaction extends IReadTransaction {

    // --- Node Modifiers
    // --------------------------------------------------------

    /**
     * Insert new element node as first child of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param mQname
     *            Qualified name of inserted node.
     * @throws AbsTTException
     *             If can't insert Element as first child.
     * @return Key of inserted node. already has a first child.
     */
    long insertElementAsFirstChild(final QName mQname) throws AbsTTException;

    /**
     * Insert new text node as first child of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param mValue
     *            Value of inserted node.
     * @throws AbsTTException
     *             If can't insert Text node as first child.
     * @return Key of inserted node. already has a first child.
     */
    long insertTextAsFirstChild(final String mValue) throws AbsTTException;

    /**
     * Insert new element node as right sibling of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param mQname
     *            name of the new node
     * @throws AbsTTException
     *             If can't insert Element node as right sibling.
     * @return Key of inserted node. already has a first child.
     */
    long insertElementAsRightSibling(final QName mQname) throws AbsTTException;

    /**
     * Insert new text node as right sibling of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param mValue
     *            Value of inserted node.
     * @throws AbsTTException
     *             If can't insert Text node as right sibling.
     * @return Key of inserted node. the root node which is not allowed to have
     *         right siblings.
     */
    long insertTextAsRightSibling(final String mValue) throws AbsTTException;

    /**
     * Insert attribute in currently selected node. The cursor is moved to the
     * inserted node.
     * 
     * @param mQname
     *            qname
     * @param mValue
     *            Value of inserted node.
     * @throws AbsTTException
     *             If can't insert Attribute to node.
     * @return Key of inserted node.
     */
    long insertAttribute(final QName mQname, final String mValue) throws AbsTTException;

    /**
     * Insert namespace declaration in currently selected node. The cursor is
     * moved to the inserted node.
     * 
     * @param mUri
     *            URI of inserted node.
     * @param mName
     *            Prefix of inserted node.
     * @throws AbsTTException
     *             If can't insert Namespace to node.
     * @return Key of inserted node.
     */
    long insertNamespace(final String mUri, final String mName) throws AbsTTException;

    /**
     * Remove currently selected node. This does automatically remove
     * descendants.
     * 
     * The cursor is located at the former right sibling. If there was no right
     * sibling, it is located at the former left sibling. If there was no left
     * sibling, it is located at the former parent.
     * 
     * @throws AbsTTException
     *             If can't remove node.
     */
    void remove() throws AbsTTException;

    // --- Node Setters
    // -----------------------------------------------------------

    /**
     * Set local part of node.
     * 
     * @param mName
     *            New qualified name of node.
     * @throws TTIOException
     *             If can't set Name in node.
     */
    void setName(final String mName) throws TTIOException;

    /**
     * Set URI of node.
     * 
     * @param mUri
     *            New URI of node.
     * @throws TTIOException
     *             If can't set URI in node.
     */
    void setURI(final String mUri) throws TTIOException;

    /**
     * Set value of node.
     * 
     * @param mValueType
     *            Type of value.
     * @param mValue
     *            New value of node.
     * @throws TTIOException
     *             If can't set Value in node.
     */
    void setValue(final int mValueType, final byte[] mValue) throws TTIOException;

    /**
     * Set value of node.
     * 
     * @param mValue
     *            New value of node.
     * @throws TTIOException
     *             If can't set Value in node.
     */
    void setValue(final String mValue) throws TTIOException;

    /**
     * Commit all modifications of the exclusive write transaction. Even commit
     * if there are no modification at all.
     * 
     * @throws AbsTTException
     *             If can't commit this revision.
     */
    void commit() throws AbsTTException;

    /**
     * Abort all modifications of the exclusive write transaction.
     * 
     * @throws TTIOException
     *             If can't abort modification.
     */
    void abort() throws TTIOException;

    /**
     * Reverting all changes to the revision defined. This command has to be
     * finalized with a commit. A revert is always bound to a {@link IReadTransaction#moveToDocumentRoot()}.
     * 
     * @param revision
     *            revert for the revision
     * @throws AbsTTException
     *             If can't revert to revision.
     */
    void revertTo(final long revision) throws AbsTTException;

    /**
     * Closing current WriteTransaction.
     * 
     * @throws AbsTTException
     *             If can't close Write Transaction.
     */
    @Override
    void close() throws AbsTTException;

}

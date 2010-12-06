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

import javax.xml.namespace.QName;

import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;

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
     * @throws TreetankException
     *             If can't insert Element as first child.
     * @return Key of inserted node. already has a first child.
     */
    long insertElementAsFirstChild(final QName mQname) throws TreetankException;

    /**
     * Insert new text node as first child of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param mValue
     *            Value of inserted node.
     * @throws TreetankException
     *             If can't insert Text node as first child.
     * @return Key of inserted node. already has a first child.
     */
    long insertTextAsFirstChild(final String mValue) throws TreetankException;

    /**
     * Insert new element node as right sibling of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param mQname
     *            name of the new node
     * @throws TreetankException
     *             If can't insert Element node as right sibling.
     * @return Key of inserted node. already has a first child.
     */
    long insertElementAsRightSibling(final QName mQname) throws TreetankException;

    /**
     * Insert new text node as right sibling of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param mValue
     *            Value of inserted node.
     * @throws TreetankException
     *             If can't insert Text node as right sibling.
     * @return Key of inserted node. the root node which is not allowed to have
     *         right siblings.
     */
    long insertTextAsRightSibling(final String mValue) throws TreetankException;

    /**
     * Insert attribute in currently selected node. The cursor is moved to the
     * inserted node.
     * 
     * @param mQname
     *            qname
     * @param mValue
     *            Value of inserted node.
     * @throws TreetankException
     *             If can't insert Attribute to node.
     * @return Key of inserted node.
     */
    long insertAttribute(final QName mQname, final String mValue) throws TreetankException;

    /**
     * Insert namespace declaration in currently selected node. The cursor is
     * moved to the inserted node.
     * 
     * @param mUri
     *            URI of inserted node.
     * @param mName
     *            Prefix of inserted node.
     * @throws TreetankException
     *             If can't insert Namespace to node.
     * @return Key of inserted node.
     */
    long insertNamespace(final String mUri, final String mName) throws TreetankException;

    /**
     * Remove currently selected node. This does automatically remove
     * descendants.
     * 
     * The cursor is located at the former right sibling. If there was no right
     * sibling, it is located at the former left sibling. If there was no left
     * sibling, it is located at the former parent.
     * 
     * @throws TreetankException
     *             If can't remove node.
     */
    void remove() throws TreetankException;

    // --- Node Setters
    // -----------------------------------------------------------

    /**
     * Set local part of node.
     * 
     * @param mName
     *            New qualified name of node.
     * @throws TreetankIOException
     *             If can't set Name in node.
     */
    void setName(final String mName) throws TreetankIOException;

    /**
     * Set URI of node.
     * 
     * @param mUri
     *            New URI of node.
     * @throws TreetankIOException
     *             If can't set URI in node.
     */
    void setURI(final String mUri) throws TreetankIOException;

    /**
     * Set value of node.
     * 
     * @param mValueType
     *            Type of value.
     * @param mValue
     *            New value of node.
     * @throws TreetankIOException
     *             If can't set Value in node.
     */
    void setValue(final int mValueType, final byte[] mValue) throws TreetankIOException;

    /**
     * Set value of node.
     * 
     * @param mValue
     *            New value of node.
     * @throws TreetankIOException
     *             If can't set Value in node.
     */
    void setValue(final String mValue) throws TreetankIOException;

    /**
     * Commit all modifications of the exclusive write transaction. Even commit
     * if there are no modification at all.
     * 
     * @throws TreetankException
     *             If can't commit this revision.
     */
    void commit() throws TreetankException;

    /**
     * Abort all modifications of the exclusive write transaction.
     * 
     * @throws TreetankIOException
     *             If can't abort modification.
     */
    void abort() throws TreetankIOException;

    /**
     * Reverting all changes to the revision defined. This command has to be
     * finalized with a commit. A revert is always bound to a {@link IReadTransaction#moveToDocumentRoot()}.
     * 
     * @param revision
     *            revert for the revision
     * @throws TreetankException
     *             If can't revert to revision.
     */
    void revertTo(final long revision) throws TreetankException;

    /**
     * Closing current WriteTransaction.
     * 
     * @throws TreetankException
     *             If can't close Write Transaction.
     */
    void close() throws TreetankException;

}

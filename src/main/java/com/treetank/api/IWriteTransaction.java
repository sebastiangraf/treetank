/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: IWriteTransaction.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.api;

import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;

/**
 * <h1>IWriteTransaction</h1>
 * 
 * <h2>Description</h2>
 * 
 * <p>
 * Interface to access nodes based on the
 * Key/ParentKey/FirstChildKey/LeftSiblingKey/RightSiblingKey/ChildCount
 * encoding. This encoding keeps the children ordered but has no knowledge of
 * the global node ordering. The underlying tree is accessed in a cursor-like
 * fashion.
 * </p>
 * 
 * <p>
 * Each commit at least adds <code>10kB</code> to the TreeTank file. It is thus
 * recommended to work with the auto commit mode only committing after a given
 * amount of node modifications or elapsed time. For very update-intensive data,
 * a value of one million modifications and ten seconds is recommended. Note
 * that this might require to increment to available heap.
 * </p>
 * 
 * <h2>Convention</h2>
 * 
 * <p>
 * <ol>
 * <li>Only a single thread accesses the single IWriteTransaction instance.</li>
 * <li><strong>Precondition</strong> before moving cursor:
 * <code>IWriteTransaction.getNodeKey() == n</code>.</li>
 * <li><strong>Postcondition</strong> after modifying the cursor:
 * <code>(IWriteTransaction.insertX() == m &&
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
 * wtx.insertElementAsFirstChild(&quot;foo&quot;);
 * // Implicit commit.
 * wtx.close();
 * 
 * // With auto commit after every second.
 * final IWriteTransaction wtx = session.beginWriteTransaction(0, 1);
 * wtx.insertElementAsFirstChild(&quot;foo&quot;);
 * // Implicit commit.
 * wtx.close();
 * 
 * // With auto commit after every 10th modification and every second.
 * final IWriteTransaction wtx = session.beginWriteTransaction(10, 1);
 * wtx.insertElementAsFirstChild(&quot;foo&quot;);
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
     * @param name
     *            Qualified name of inserted node.
     * @param uri
     *            URI of inserted node.
     * @return Key of inserted node. already has a first child.
     */
    long insertElementAsFirstChild(final String name, final String uri)
            throws TreetankIOException;

    /**
     * Insert new text node as first child of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param valueType
     *            Type of value.
     * @param value
     *            Value of inserted node.
     * @return Key of inserted node. already has a first child.
     */
    long insertTextAsFirstChild(final int valueType, final byte[] value)
            throws TreetankIOException;

    /**
     * Insert new text node as first child of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param value
     *            Value of inserted node.
     * @return Key of inserted node. already has a first child.
     */
    long insertTextAsFirstChild(final String value) throws TreetankIOException;

    /**
     * Insert new element node as right sibling of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param name
     *            Qualified name of inserted node.
     * @param uri
     *            URI of inserted node.
     * @return Key of inserted node. already has a first child.
     */
    long insertElementAsRightSibling(final String name, final String uri)
            throws TreetankIOException;

    /**
     * Insert new element node as right sibling of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param valueType
     *            Type of value.
     * @param value
     *            Value of inserted node.
     * @return Key of inserted node. the root node which is not allowed to have
     *         right siblings.
     */
    long insertTextAsRightSibling(final int valueType, final byte[] value)
            throws TreetankIOException;

    /**
     * Insert new element node as right sibling of currently selected node. The
     * cursor is moved to the inserted node.
     * 
     * @param value
     *            Value of inserted node.
     * @return Key of inserted node. the root node which is not allowed to have
     *         right siblings.
     */
    long insertTextAsRightSibling(final String value)
            throws TreetankIOException;

    /**
     * Insert attribute in currently selected node. The cursor is moved to the
     * inserted node.
     * 
     * @param name
     *            Qualified name of inserted node.
     * @param uri
     *            URI of inserted node.
     * @param valueType
     *            Type of value.
     * @param value
     *            Value of inserted node.
     * @return Key of inserted node.
     */
    long insertAttribute(final String name, final String uri,
            final int valueType, final byte[] value) throws TreetankIOException;

    /**
     * Insert attribute in currently selected node. The cursor is moved to the
     * inserted node.
     * 
     * @param name
     *            Qualified name of inserted node.
     * @param uri
     *            URI of inserted node.
     * @param value
     *            Value of inserted node.
     * @return Key of inserted node.
     */
    long insertAttribute(final String name, final String uri, final String value)
            throws TreetankIOException;

    /**
     * Insert namespace declaration in currently selected node. The cursor is
     * moved to the inserted node.
     * 
     * @param uri
     *            URI of inserted node.
     * @param name
     *            Prefix of inserted node.
     * @return Key of inserted node.
     */
    long insertNamespace(final String uri, final String name)
            throws TreetankIOException;

    /**
     * Remove currently selected node. This does automatically remove
     * descendants.
     * 
     * The cursor is located at the former right sibling. If there was no right
     * sibling, it is located at the former left sibling. If there was no left
     * sibling, it is located at the former parent.
     */
    void remove() throws TreetankException;

    // --- Node Setters
    // -----------------------------------------------------------

    /**
     * Set local part of node.
     * 
     * @param name
     *            New qualified name of node.
     */
    void setName(final String name) throws TreetankIOException;

    /**
     * Set URI of node.
     * 
     * @param uri
     *            New URI of node.
     */
    void setURI(final String uri) throws TreetankIOException;

    /**
     * Set value of node.
     * 
     * @param valueType
     *            Type of value.
     * @param value
     *            New value of node.
     */
    void setValue(final int valueType, final byte[] value)
            throws TreetankIOException;

    /**
     * Set value of node.
     * 
     * @param value
     *            New value of node.
     */
    void setValue(final String value) throws TreetankIOException;

    /**
     * Commit all modifications of the exclusive write transaction. Even commit
     * if there are no modification at all.
     */
    void commit() throws TreetankIOException;

    /**
     * Abort all modifications of the exclusive write transaction.
     */
    void abort() throws TreetankIOException;

}

/*
 * Copyright (c) 2007, Marc Kramis
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
 * $Id$
 */

package org.treetank.api;

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
 * Each commit at least adds <code>10kB</code> to the TreeTank file. It is
 * thus recommended to work with the auto commit mode only committing after
 * a given amount of node modifications or elapsed time. For very
 * update-intensive data, a value of one million modifications and ten seconds
 * is recommended. Note that this might require to increment to available
 * heap.
 * </p>
 * 
 * <h2>Convention</h2>
 * 
 * <p>
 *  <ol>
 *   <li>Only a single thread accesses the single IWriteTransaction
 *       instance.</li>
 *   <li><strong>Precondition</strong> before moving cursor:
 *       <code>IWriteTransaction.getNodeKey() == n</code>.</li>
 *   <li><strong>Postcondition</strong> after modifying the cursor:
 *       <code>(IWriteTransaction.insertX() == m &&
 *       IWriteTransaction.getNodeKey() == m)</code>.</li>
 *  </ol>
 * </p>
 * 
 * <h2>User Example</h2>
 * 
 * <p>
 *  <pre>
 *   // Without auto commit.
 *   final IWriteTransaction wtx = session.beginWriteTransaction();
 *   wtx.insertElementAsFirstChild("foo");
 *   // Explicit forced commit.
 *   wtx.commit();
 *   wtx.close();
 *   
 *   // With auto commit after every 10th modification.
 *   final IWriteTransaction wtx = session.beginWriteTransaction(10, 0);
 *   wtx.insertElementAsFirstChild("foo");
 *   // Implicit commit.
 *   wtx.close();
 *   
 *   // With auto commit after every second.
 *   final IWriteTransaction wtx = session.beginWriteTransaction(0, 1);
 *   wtx.insertElementAsFirstChild("foo");
 *   // Implicit commit.
 *   wtx.close();
 *   
 *   // With auto commit after every 10th modification and every second.
 *   final IWriteTransaction wtx = session.beginWriteTransaction(10, 1);
 *   wtx.insertElementAsFirstChild("foo");
 *   // Implicit commit.
 *   wtx.close();
 *  </pre>
 * </p>
 * 
 * <h2>Developer Example</h2>
 * 
 * <p>
 *  <pre>
 *   public final void someIWriteTransactionMethod() {
 *     // This must be called to make sure the transaction is not closed.
 *     assertNotClosed();
 *     // This must be called to track the modifications.
 *     mModificationCount++;
 *     ...
 *   }
 *  </pre>
 * </p>
 */
public interface IWriteTransaction extends IReadTransaction {

  //--- FullText Support -------------------------------------------------------

  /**
   * Index the given token occurring in the provided node key. The caller
   * must make sure that the token is efficiently filtered and trimmed.
   * 
   * @param token Token to store (unmodified) in the inverted index.
   * @param nodeKey Key of node which contains the token.
   * @return Key of token (node key of full text node matching last character
   *         of token).
   */
  public long insertToken(final String token, final long nodeKey);

  /**
   * Remove key from list under token.
   * 
   * @param token Token from which to remove key.
   * @param nodeKey Key of node which contains the token.
   */
  public void removeToken(final String token, final long nodeKey);

  // --- Node Modifiers --------------------------------------------------------

  /**
   * Insert new element node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param name Qualified name of inserted node.
   * @param uri URI of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertElementAsFirstChild(final String name, final String uri);

  /**
   * Insert new text node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param valueType Type of value.
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertTextAsFirstChild(final int valueType, final byte[] value);

  /**
   * Insert new text node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertTextAsFirstChild(final String value);

  /**
   * Insert new text node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertTextAsFirstChild(final int value);

  /**
   * Insert new text node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertTextAsFirstChild(final long value);

  /**
   * Insert new text node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertTextAsFirstChild(final boolean value);

  /**
   * Insert new fulltext node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param nameKey Name key of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertFullTextAsFirstChild(final int nameKey);

  /**
   * Insert new element node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param name Qualified name of inserted node.
   * @param uri URI of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertElementAsRightSibling(final String name, final String uri);

  /**
   * Insert new element node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param valueType Type of value.
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * the root node which is not allowed to have right siblings.
   */
  public long insertTextAsRightSibling(final int valueType, final byte[] value);

  /**
   * Insert new element node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * the root node which is not allowed to have right siblings.
   */
  public long insertTextAsRightSibling(final String value);

  /**
   * Insert new element node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * the root node which is not allowed to have right siblings.
   */
  public long insertTextAsRightSibling(final int value);

  /**
   * Insert new element node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * the root node which is not allowed to have right siblings.
   */
  public long insertTextAsRightSibling(final long value);

  /**
   * Insert new element node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * the root node which is not allowed to have right siblings.
   */
  public long insertTextAsRightSibling(final boolean value);

  /**
   * Insert new fulltext node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param nameKey Name key of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertFullTextAsRightSibling(final int nameKey);

  /**
   * Insert attribute in currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param name Qualified name of inserted node.
   * @param uri URI of inserted node.
   * @param valueType Type of value.
   * @param value Value of inserted node.
   */
  public void insertAttribute(
      final String name,
      final String uri,
      final int valueType,
      final byte[] value);

  /**
   * Insert attribute in currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param name Qualified name of inserted node.
   * @param uri URI of inserted node.
   * @param value Value of inserted node.
   */
  public void insertAttribute(
      final String name,
      final String uri,
      final String value);

  /**
   * Insert attribute in currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param name Qualified name of inserted node.
   * @param uri URI of inserted node.
   * @param value Value of inserted node.
   */
  public void insertAttribute(
      final String name,
      final String uri,
      final int value);

  /**
   * Insert attribute in currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param name Qualified name of inserted node.
   * @param uri URI of inserted node.
   * @param value Value of inserted node.
   */
  public void insertAttribute(
      final String name,
      final String uri,
      final long value);

  /**
   * Insert attribute in currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param name Qualified name of inserted node.
   * @param uri URI of inserted node.
   * @param value Value of inserted node.
   */
  public void insertAttribute(
      final String name,
      final String uri,
      final boolean value);

  /**
   * Insert namespace declaration in currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   */
  public void insertNamespace(final String uri, final String prefix);

  /**
   * Remove currently selected node. This does automatically remove descendants.
   * 
   * The cursor is located at the former right sibling. If there was no right
   * sibling, it is located at the former left sibling. If there was no left
   * sibling, it is located at the former parent.
   */
  public void remove();

  //--- Node Setters -----------------------------------------------------------

  /**
   * Set an attribute of the currently selected node.
   * 
   * @param index Index of attribute to set.
   * @param name Qualified name of inserted node.
   * @param uri URI of inserted node.
   * @param valueType Type of value.
   * @param value Value of inserted node.
   */
  public void setAttribute(
      final int index,
      final String name,
      final String uri,
      final int valueType,
      final byte[] value);

  /**
   * Set a namespace of the currently selected node.
   * 
   * @param index Index of attribute to set.
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   */
  public void setNamespace(
      final int index,
      final String uri,
      final String prefix);

  /**
   * Set local part of node.
   * 
   * @param name New qualified name of node.
   */
  public void setName(final String name);

  /**
   * Set URI of node.
   * 
   * @param uri New URI of node.
   */
  public void setURI(final String uri);

  /**
   * Set value of node.
   * 
   * @param valueType Type of value.
   * @param value New value of node.
   */
  public void setValue(final int valueType, final byte[] value);

  /**
   * Set value of node.
   * 
   * @param value New value of node.
   */
  public void setValue(final String value);

  /**
   * Set value of node.
   * 
   * @param value New value of node.
   */
  public void setValue(final int value);

  /**
   * Set value of node.
   * 
   * @param value New value of node.
   */
  public void setValue(final long value);

  /**
   * Set value of node.
   * 
   * @param value New value of node.
   */
  public void setValue(final boolean value);

  /**
   * Commit all modifications of the exclusive write transaction. Even commit
   * if there are no modification at all.
   */
  public void commit();

  /**
   * Abort all modifications of the exclusive write transaction.
   */
  public void abort();

}

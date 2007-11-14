/*
 * Copyright 2007, Marc Kramis
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * $Id$
 */

package org.treetank.api;

/**
 * <h1>IWriteTransaction</h1>
 * 
 * <p>
 * Interface to access and modify nodes based on the
 * Key/ParentKey/FirstChildKey/LeftSiblingKey/RightSiblingKey/ChildCount
 * encoding. This encoding keeps the children ordered but has no knowledge of
 * the global node ordering. Nodes must first be selected before they can be
 * read. Modifying methods always work from the current node and select the 
 * newly inserted node.
 * </p>
 * 
 * <p>
 * The interface has a single-threaded semantics, this is, each thread accessing
 * the ISession in a modifying way needs its own IWriteTransaction instance.
 * </p>
 * 
 * <p>
 * Exceptions are only thrown if an internal error occurred which must be
 * resolved at a higher layer.
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
  public long index(final String token, final long nodeKey);

  // --- Node Modifiers --------------------------------------------------------

  /**
   * Insert new element node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param localPart Local part of inserted node.
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertElementAsFirstChild(
      final String localPart,
      final String uri,
      final String prefix);

  /**
   * Insert new text node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertTextAsFirstChild(final byte[] value);

  /**
   * Insert new fulltext node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param localPartKey Local part key of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertFullTextAsFirstChild(final int localPartKey);

  /**
   * Insert new element node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param localPart Local part of inserted node.
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertElementAsRightSibling(
      final String localPart,
      final String uri,
      final String prefix);

  /**
   * Insert new element node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * the root node which is not allowed to have right siblings.
   */
  public long insertTextAsRightSibling(final byte[] value);

  /**
   * Insert new fulltext node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param localPartKey Local part key of inserted node.
   * @return Key of inserted node.
   * already has a first child.
   */
  public long insertFullTextAsRightSibling(final int localPartKey);

  /**
   * Insert attribute in currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param localPart Local part of inserted node.
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   * @param value Value of inserted node.
   */
  public void insertAttribute(
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value);

  /**
   * Insert namespace declaration in currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   */
  public void insertNamespace(final String uri, final String prefix);

  /**
   * Remove currently selected node. This does not automatically remove
   * descendants.
   */
  public void remove();

  //--- Node Setters -----------------------------------------------------------

  /**
   * Set an attribute of the currently selected node.
   * 
   * @param index Index of attribute to set.
   * @param localPart Local part of inserted node.
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   * @param value Value of inserted node.
   */
  public void setAttribute(
      final int index,
      final String localPart,
      final String uri,
      final String prefix,
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
   * @param localPart New local part of node.
   */
  public void setLocalPart(final String localPart);

  /**
   * Set URI of node.
   * 
   * @param uri New URI of node.
   */
  public void setURI(final String uri);

  /**
   * Set prefix of node.
   * 
   * @param prefix New prefix of node.
   */
  public void setPrefix(final String prefix);

  /**
   * Set value of node.
   * 
   * @param value New value of node.
   */
  public void setValue(final byte[] value);

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

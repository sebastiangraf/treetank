/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
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

  // --- Node Modifiers --------------------------------------------------------

  /**
   * Insert root node. The cursor is moved to the root node. The kind is
   * automatically set to DOCUMENT.
   * 
   * @param document Document identifier.
   * @return Key of inserted node always equal to IConstants.ROOT_KEY.
   * @throws Exception of any kind and if there already is a root node.
   */
  public long insertRoot(final String document) throws Exception;

  /**
   * Insert new node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param kind Kind of inserted node.
   * @param localPart Local part of inserted node.
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * @throws Exception of any kind and if no node is selected or the node
   * already has a first child.
   */
  public long insertFirstChild(
      final int kind,
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) throws Exception;

  /**
   * Insert new node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param kind Kind of inserted node.
   * @param localPart Local part of inserted node.
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * @throws Exception of any kind and if no node is selected or the node is
   * the root node which is not allowed to have right siblings.
   */
  public long insertRightSibling(
      final int kind,
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) throws Exception;

  /**
   * Insert attribute in currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param localPart Local part of inserted node.
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   * @param value Value of inserted node.
   * @throws Exception of any kind and if no node is selected.
   */
  public void insertAttribute(
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) throws Exception;

  /**
   * Insert namespace declaration in currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   * @throws Exception of any kind and if no node is selected.
   */
  public void insertNamespace(final String uri, final String prefix)
      throws Exception;

  /**
   * Remove currently selected node. This does not automatically remove
   * descendants.
   * 
   * @throws Exception of any kind and if no node is selected or the node
   * has children.
   */
  public void remove() throws Exception;

  //--- Node Setters -----------------------------------------------------------

  /**
   * Set an attribute of the currently selected node.
   * 
   * @param index Index of attribute to set.
   * @param localPart Local part of inserted node.
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   * @param value Value of inserted node.
   * @throws Exception of any kind and if no node is selected.
   */
  public void setAttribute(
      final int index,
      final String localPart,
      final String uri,
      final String prefix,
      final byte[] value) throws Exception;

  /**
   * Set a namespace of the currently selected node.
   * 
   * @param index Index of attribute to set.
   * @param uri URI of inserted node.
   * @param prefix Prefix of inserted node.
   * @throws Exception of any kind and if no node is selected.
   */
  public void setNamespace(
      final int index,
      final String uri,
      final String prefix) throws Exception;

  /**
   * Set local part of node.
   * 
   * @param localPart New local part of node.
   * @throws Exception of any kind and if no node is selected.
   */
  public void setLocalPart(final String localPart) throws Exception;

  /**
   * Set URI of node.
   * 
   * @param uri New URI of node.
   * @throws Exception of any kind and if no node is selected.
   */
  public void setURI(final String uri) throws Exception;

  /**
   * Set prefix of node.
   * 
   * @param prefix New prefix of node.
   * @throws Exception of any kind and if no node is selected.
   */
  public void setPrefix(final String prefix) throws Exception;

  /**
   * Set value of node.
   * 
   * @param value New value of node.
   * @throws Exception of any kind and if no node is selected.
   */
  public void setValue(final byte[] value) throws Exception;

}

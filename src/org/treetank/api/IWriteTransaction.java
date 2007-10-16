/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

  // --- Node Modifiers --------------------------------------------------------

  /**
   * Insert root node. The cursor is moved to the root node. The kind is
   * automatically set to DOCUMENT.
   * 
   * @return Key of inserted node always equal to IConstants.ROOT_KEY.
   * @throws Exception of any kind and if there already is a root node.
   */
  public long insertRoot() throws Exception;

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
   * Insert new text node as first child of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * @throws Exception of any kind and if no node is selected or the node
   * already has a first child.
   */
  public long insertTextAsFirstChild(final byte[] value) throws Exception;

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
   * Insert new text node as right sibling of currently selected node.
   * The cursor is moved to the inserted node.
   * 
   * @param value Value of inserted node.
   * @return Key of inserted node.
   * @throws Exception of any kind and if no node is selected or the node is
   * the root node which is not allowed to have right siblings.
   */
  public long insertTextAsRightSibling(final byte[] value) throws Exception;

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

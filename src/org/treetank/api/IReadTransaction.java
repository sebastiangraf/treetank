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
 * <h1>IReadTransaction</h1>
 * 
 * <p>
 * Interface to access nodes based on the
 * Key/ParentKey/FirstChildKey/LeftSiblingKey/RightSiblingKey/ChildCount
 * encoding. This encoding keeps the children ordered but has no knowledge of
 * the global node ordering. Nodes must first be selected before they can be
 * read.
 * </p>
 * 
 * <p>
 * The interface has a single-threaded semantics, this is, each thread accessing
 * the ISession in a read-only way needs its own IReadTransaction instance.
 * </p>
 * 
 * <p>
 * Exceptions are only thrown if an internal error occurred which must be
 * resolved at a higher layer.
 * </p>
 */
public interface IReadTransaction {

  /**
   * What is the revision key of this IReadTransaction?
   * 
   * @return Immutable revision key of this IReadTransaction.
   */
  public long revisionKey();

  /**
   * How many nodes are stored in the revision of this IReadTransaction?
   * 
   * @return Immutable number of nodes of this IReadTransaction.
   */
  public long revisionSize();

  /**
   * Is a node selected?
   * 
   * @return True if a node is selected.
   */
  public boolean isSelected();

  // --- Node Selectors --------------------------------------------------------

  /**
   * Move cursor to root node.
   * 
   * @return True if the root node is selected.
   * @throws Exception of any kind.
   */
  public boolean moveToRoot() throws Exception;

  /**
   * Move cursor to a node by its node key.
   * 
   * @param nodeKey Key of node to select.
   * @return True if the node with the given node key is selected.
   * @throws Exception of any kind.
   */
  public boolean moveTo(final long nodeKey) throws Exception;

  /**
   * Move cursor to parent node of currently selected node.
   * 
   * @return True if the parent node is selected.
   * @throws Exception of any kind.
   */
  public boolean moveToParent() throws Exception;

  /**
   * Move cursor to first child node of currently selected node.
   * 
   * @return True if the first child node is selected.
   * @throws Exception of any kind.
   */
  public boolean moveToFirstChild() throws Exception;

  /**
   * Move cursor to left sibling node of the currently selected node.
   * 
   * @return True if the left sibling node is selected.
   * @throws Exception of any kind.
   */
  public boolean moveToLeftSibling() throws Exception;

  /**
   * Move cursor to right sibling node of the currently selected node.
   * 
   * @return True if the right sibling node is selected.
   * @throws Exception of any kind.
   */
  public boolean moveToRightSibling() throws Exception;
  
  public boolean moveToAttribute(final int index) throws Exception;

  // --- Node Getters ----------------------------------------------------------

  /**
   * Expose getters of internal node object.
   * 
   * @return Interface for getting all node values.
   */
  public INode getNode();

  /**
   * Get node key of currently selected node.
   * 
   * @return INode key of currently selected node.
   */
  public long getNodeKey();

  /**
   * Get parent key of currently selected node.
   * 
   * @return Parent key of currently selected node.
   */
  public long getParentKey();

  /**
   * Get first child key of currently selected node.
   * 
   * @return First child key of currently selected node.
   */
  public long getFirstChildKey();

  /**
   * Get left sibling key of currently selected node.
   * 
   * @return Left sibling key of currently selected node.
   */
  public long getLeftSiblingKey();

  /**
   * Get right sibling key of currently selected node.
   * 
   * @return Right sibling key of currently selected node.
   */
  public long getRightSiblingKey();

  /**
   * Get child count (including element and text nodes) of currently selected
   * node.
   * 
   * @return Child count of currently selected node.
   */
  public long getChildCount();

  /**
   * Get attribute count (including attribute nodes) of currently selected
   * node.
   * 
   * @return Attribute count of currently selected node.
   */
  public int getAttributeCount();
  
  /**
   * Get namespace declaration count of currently selected node.
   * 
   * @return Namespace declaration count of currently selected node.
   */
  public int getNamespaceCount();

  /**
   * Get kind of node.
   * 
   * @return Kind of node.
   */
  public int getKind();

  /**
   * Get local part key of node.
   * 
   * @return Local part key of node.
   */
  public int getLocalPartKey();
  
  /**
   * Get local part of node.
   * 
   * @return Local part of node.
   */
  public String getLocalPart() throws Exception;

  /**
   * Get URI key of node. Note that this actually is an IRI but the
   * W3C decided to continue using URI not to confuse anyone.
   * 
   * @return URI key of node.
   */
  public int getURIKey();
  
  /**
   * Get URI of node. Note that this actually is an IRI but the
   * W3C decided to continue using URI not to confuse anyone.
   * 
   * @return URI of node.
   */
  public String getURI() throws Exception;

  /**
   * Get prefix key of node.
   * 
   * @return Prefix key of node.
   */
  public int getPrefixKey();
  
  /**
   * Get prefix of node.
   * 
   * @return Prefix of node.
   */
  public String getPrefix() throws Exception;

  /**
   * Get value of node.
   * 
   * @return Value of node.
   */
  public byte[] getValue();

  /**
   * Get key for given name. This is used for efficient name testing.
   * 
   * @param name Name, i.e., local part, URI, or prefix.
   * @return Internal key assigned to given name.
   */
  public int keyForName(final String name);

  /**
   * Get name for key. This is used for efficient key testing.
   * 
   * @param key Key, i.e., local part key, URI key, or prefix key.
   * @return Byte array containing name for given key.
   */
  public String nameForKey(final int key) throws Exception;

}

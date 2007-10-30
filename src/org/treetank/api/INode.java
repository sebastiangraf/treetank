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
 * <h1>INode</h1>
 * 
 * Provide read-only access to node.
 */
public interface INode {

  /**
   * Is this node the document root node?
   * 
   * @return True if it is the document root node, false else.
   */
  public boolean isDocumentRoot();

  /**
   * Is node a element?
   * 
   * @return True if node is element. False else.
   */
  public boolean isElement();

  /**
   * Is node a attribute?
   * 
   * @return True if node is attribute. False else.
   */
  public boolean isAttribute();

  /**
   * Is node a text?
   * 
   * @return True if node is text. False else.
   */
  public boolean isText();

  /**
   * Is node a full text?
   * 
   * @return True if node is full text. False else.
   */
  public boolean isFullText();

  /**
   * Get node key of currently selected node.
   * 
   * @return INode key of currently selected node.
   */
  public long getNodeKey();

  /**
   * Is there a parent?
   * 
   * @return True if there is a parent. False else.
   */
  public boolean hasParent();

  /**
   * Get parent key of currently selected node.
   * 
   * @return Parent key of currently selected node.
   */
  public long getParentKey();

  /**
   * Get parent of currently selected node.
   * 
   * @param rtx Transaction.
   * @return Parent of currently selected node.
   */
  public INode getParent(final IReadTransaction rtx);

  /**
   * Is there a first child?
   * 
   * @return True if there is a first child. False else.
   */
  public boolean hasFirstChild();

  /**
   * Get first child key of currently selected node.
   * 
   * @return First child key of currently selected node.
   */
  public long getFirstChildKey();

  /**
   * Get first child of currently selected node.
   * 
   * @param rtx Transaction.
   * @return First child of currently selected node.
   */
  public INode getFirstChild(final IReadTransaction rtx);

  /**
   * Is there a left sibling?
   * 
   * @return True if there is a left sibling. False else.
   */
  public boolean hasLeftSibling();

  /**
   * Get left sibling key of currently selected node.
   * 
   * @return Left sibling key of currently selected node.
   */
  public long getLeftSiblingKey();

  /**
   * Get left sibling of currently selected node.
   * 
   * @param rtx Transaction.
   * @return Left sibling of currently selected node.
   */
  public INode getLeftSibling(final IReadTransaction rtx);

  /**
   * Is there a right sibling?
   * 
   * @return True if there is a right sibling. False else.
   */
  public boolean hasRightSibling();

  /**
   * Get right sibling key of currently selected node.
   * 
   * @return Right sibling key of currently selected node.
   */
  public long getRightSiblingKey();

  /**
   * Get right sibling of currently selected node.
   * 
   * @param rtx Transaction.
   * @return Right sibling of currently selected node.
   */
  public INode getRightSibling(final IReadTransaction rtx);

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
   * Get attribute with given index.
   * 
   * @param index Index of attribute to return.
   * @return INode for given index.
   */
  public INode getAttribute(final int index);

  /**
   * Get namespace with given index.
   * 
   * @param index Index of attribute to return.
   * @return INode for given index.
   */
  public INode getNamespace(final int index);

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
   * @param rtx Transaction.
   * @return Local part of node.
   */
  public String getLocalPart(final IReadTransaction rtx);

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
   * @param rtx Transaction.
   * @return URI of node.
   */
  public String getURI(final IReadTransaction rtx);

  /**
   * Get prefix key of node.
   * 
   * @return Prefix key of node.
   */
  public int getPrefixKey();

  /**
   * Get prefix of node.
   * 
   * @param rtx Transaction.
   * @return Prefix of node.
   */
  public String getPrefix(final IReadTransaction rtx);

  /**
   * Get value of node.
   * 
   * @return Value of node.
   */
  public byte[] getValue();

}

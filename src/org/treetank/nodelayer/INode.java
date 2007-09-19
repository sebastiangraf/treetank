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

package org.treetank.nodelayer;

import org.treetank.pagelayer.Namespace;

/**
 * <h1>INode</h1>
 * 
 * Provide read-only access to node.
 */
public interface INode {

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
  public Namespace getNamespace(final int index);

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
   * Get URI key of node. Note that this actually is an IRI but the
   * W3C decided to continue using URI not to confuse anyone.
   * 
   * @return URI key of node.
   */
  public int getURIKey();

  /**
   * Get prefix key of node.
   * 
   * @return Prefix key of node.
   */
  public int getPrefixKey();

  /**
   * Get value of node.
   * 
   * @return Value of node.
   */
  public byte[] getValue();

}

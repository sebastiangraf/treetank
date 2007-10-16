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
 * $Id: IReadTransaction.java 2987 2007-10-01 12:58:09Z kramis $
 */

package org.treetank.api;

import org.treetank.pagelayer.IndirectPage;
import org.treetank.pagelayer.Node;
import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.PageWriter;

/**
 * <h1>IWriteTransactionState</h1>
 * 
 * <p>
 * Interface to access the state of a IReadTransaction.
 * </p>
 */
public interface IWriteTransactionState extends IReadTransactionState {

  /**
   * Get the page writer used within this transaction.
   * 
   * @return PageWriter instance.
   */
  public PageWriter getPageWriter();

  /**
   * COW the node page specified by its node page key.
   * 
   * @param nodePageKey Referencing the node page.
   * @return COWed node page.
   * @throws Exception of any kind.
   */
  public NodePage prepareNodePage(final long nodePageKey) throws Exception;

  /**
   * COW the node specified by its node key. This implies COWing the 
   * node page the node is stored in.
   * 
   * @param nodeKey Referencing the node.
   * @return COWed node.
   * @throws Exception of any kind.
   */
  public Node prepareNode(final long nodeKey) throws Exception;

  /**
   * Create a new node.
   * 
   * @param parentKey Key of parent node.
   * @param firstChildKey Key of first child node.
   * @param leftSiblingKey Key of left sibling node.
   * @param rightSiblingKey Key of right sibling node.
   * @param kind Kind of node.
   * @param localPartKey Local part key of node.
   * @param uriKey URI key of node.
   * @param prefixKey Prefix key of node.
   * @param value Value of node.
   * @return Freshly created node.
   * @throws Exception of any kind.
   */
  public Node createNode(
      final long parentKey,
      final long firstChildKey,
      final long leftSiblingKey,
      final long rightSiblingKey,
      final int kind,
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) throws Exception;

  /**
   * Remove node specified by its node key.
   * 
   * @param nodeKey Referencing the node.
   * @throws Exception of any kind.
   */
  public void removeNode(final long nodeKey) throws Exception;

  /**
   * Create name key given a name.
   * 
   * @param name Name to create key for.
   * @return Name key.
   * @throws Exception exception.
   */
  public int createNameKey(final String name) throws Exception;

  /**
   * COW indirect page or instantiate virgin one from given indirect page
   * reference.
   * 
   * @param reference Indirect page reference.
   * @return COWed indirect page.
   * @throws Exception of any kind.
   */
  public IndirectPage prepareIndirectPage(final PageReference reference)
      throws Exception;

  /**
   * Safely commit and serialize dereferenced dirty page.
   * 
   * @param reference Reference to dereference and serialize.
   * @throws Exception of any kind.
   */
  public void commit(final PageReference reference) throws Exception;

  /**
   * Safely commit and serialize dereferenced dirty page.
   * 
   * @param references Reference array to dereference and serialize.
   * @throws Exception of any kind.
   */
  public void commit(final PageReference[] references) throws Exception;

}

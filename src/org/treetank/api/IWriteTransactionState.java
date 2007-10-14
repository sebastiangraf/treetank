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
import org.treetank.pagelayer.NamePage;
import org.treetank.pagelayer.Node;
import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.PageWriter;
import org.treetank.pagelayer.RevisionRootPage;

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

  public NodePage prepareNodePage(final long nodePageKey) throws Exception;

  public Node prepareNode(final long nodeKey) throws Exception;

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
   * COW revision root page or instantiate virgin one from given revision
   * root page reference.
   * 
   * @param reference Revision root page reference.
   * @param revisionKey Key of revision root page in case it must be created.
   * @return COWed revision root page.
   * @throws Exception of any kind.
   */
  public RevisionRootPage prepareRevisionRootPage(
      final PageReference reference,
      final long revisionKey) throws Exception;

  /**
   * COW name page or instantiate virgin one from given name page reference.
   * 
   * @param reference Name page reference.
   * @return COWed name page.
   * @throws Exception of any kind.
   */
  public NamePage prepareNamePage(final PageReference reference)
      throws Exception;

  /**
   * COW node page or instantiate virgin one from given node page reference.
   * 
   * @param reference INode page reference.
   * @param nodePageKey Key of node page in case it must be created.
   * @return COWed node page.
   * @throws Exception of any kind.
   */
  public NodePage prepareNodePage(
      final PageReference reference,
      final long nodePageKey) throws Exception;

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
   * @param writer PageWriter to write page to.
   * @param reference Reference to dereference and serialize.
   * @throws Exception of any kind.
   */
  public void commit(final PageReference reference) throws Exception;

  /**
   * Safely commit and serialize dereferenced dirty page.
   * 
   * @param writer PageWriter to write page to.
   * @param references Reference array to dereference and serialize.
   * @throws Exception of any kind.
   */
  public void commit(final PageReference[] references) throws Exception;

}

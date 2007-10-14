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

import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageCache;
import org.treetank.pagelayer.PageReader;
import org.treetank.pagelayer.RevisionRootPage;
import org.treetank.utils.StaticTree;

/**
 * <h1>IReadTransactionState</h1>
 * 
 * <p>
 * Interface to access the state of a IReadTransaction.
 * </p>
 */
public interface IReadTransactionState {

  /**
   * Get the page cache accessed through this transaction.
   * 
   * @return PageCache instance.
   */
  public PageCache getPageCache();

  /**
   * Get the page reader used within this transaction.
   * 
   * @return PageReader instance.
   */
  public PageReader getPageReader();

  /**
   * Get the static tree used within this transaction to find a node page.
   * 
   * @return PageReader instance.
   */
  public StaticTree getStaticNodeTree();

  /**
   * Get the node page most recently accessed.
   * 
   * @param revisionRootPage to start search from.
   * @param nodePageKey Key of node page to get.
   * @return Cached node page.
   * @throws Exception of any kind.
   */
  public NodePage getNodePage(
      final RevisionRootPage revisionRootPage,
      final long nodePageKey) throws Exception;

  public NodePage prepareNodePage(final long nodePageKey) throws Exception;

}

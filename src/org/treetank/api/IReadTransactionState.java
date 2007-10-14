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
import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.PageReference;
import org.treetank.pagelayer.RevisionRootPage;
import org.treetank.pagelayer.UberPage;

/**
 * <h1>IReadTransactionState</h1>
 * 
 * <p>
 * Interface to access the state of a IReadTransaction.
 * </p>
 */
public interface IReadTransactionState {

  /**
   * Get revision root page.
   * 
   * @return Revision root page.
   */
  public RevisionRootPage getRevisionRootPage();

  /**
   * Get a node.
   * 
   * @param nodeKey Key of node to get.
   * @return Node that was requested.
   * @throws Exception of any kind.
   */
  public INode getNode(final long nodeKey) throws Exception;

  /**
   * Get the name associated with the given name key.
   * 
   * @param nameKey Key to find name.
   * @return Name associated to name key.
   * @throws Exception of any kind.
   */
  public String getName(final int nameKey) throws Exception;

  public NodePage dereferenceNodePage(
      final PageReference reference,
      final long nodePageKey) throws Exception;

  public NamePage dereferenceNamePage(final PageReference reference)
      throws Exception;

  public IndirectPage dereferenceIndirectPage(final PageReference reference)
      throws Exception;

  public RevisionRootPage dereferenceRevisionRootPage(
      final PageReference reference,
      final long revisionKey) throws Exception;

  public UberPage dereferenceUberPage(final PageReference reference)
      throws Exception;

}

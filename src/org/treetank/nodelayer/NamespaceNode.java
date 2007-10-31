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
 * $Id: Namespace.java 3030 2007-10-11 07:22:54Z kramis $
 */

package org.treetank.nodelayer;

import org.treetank.api.INode;

/**
 * <h1>NamespaceNode</h1>
 * 
 * <p>
 * Node representing a namespace.
 * </p>
 */
public final class NamespaceNode extends AbstractNode {

  /** Key of URI. */
  private int mURIKey;

  /** Key of prefix. */
  private int mPrefixKey;

  /**
   * Create namespace node.
   * 
   * @param uriKey Key of URI.
   * @param prefixKey Key of prefix.
   */
  public NamespaceNode(final long nodeKey, final int uriKey, final int prefixKey) {
    super(nodeKey);
    mURIKey = uriKey;
    mPrefixKey = prefixKey;
  }

  /**
   * Clone namespace node.
   * 
   * @param namespace Namespace node to clone.
   */
  public NamespaceNode(final INode namespace) {
    super(namespace.getNodeKey());
    mURIKey = namespace.getURIKey();
    mPrefixKey = namespace.getPrefixKey();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getPrefixKey() {
    return mPrefixKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setPrefixKey(final int prefixKey) {
    mPrefixKey = prefixKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getURIKey() {
    return mURIKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setURIKey(final int uriKey) {
    mURIKey = uriKey;
  }

}

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

package org.treetank.pagelayer;

import org.treetank.api.IConstants;
import org.treetank.api.IPage;
import org.treetank.api.IWriteTransactionState;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>NodePage</h1>
 * 
 * <p>
 * A node page stores a set of nodes.
 * </p>
 */
public final class NodePage extends AbstractPage implements IPage {

  /** Key of node page. This is the base key of all contained nodes. */
  private final long mNodePageKey;

  /** Array of nodes. This can have null nodes that were removed. */
  private final Node[] mNodes;

  private NodePage(final long nodePageKey) {
    mNodePageKey = nodePageKey;
    mNodes = new Node[IConstants.NDP_NODE_COUNT];
  }

  public static final NodePage create(final long nodePageKey) {
    final NodePage nodePage = new NodePage(nodePageKey);
    nodePage.setDirty(true);
    return nodePage;
  }

  public static final NodePage read(
      final FastByteArrayReader in,
      final long nodePageKey) throws Exception {
    final NodePage nodePage = new NodePage(nodePageKey);

    final long keyBase =
        nodePage.mNodePageKey << IConstants.NDP_NODE_COUNT_EXPONENT;
    for (int i = 0; i < IConstants.NDP_NODE_COUNT; i++) {
      if (in.readBoolean()) {
        nodePage.mNodes[i] = new Node(keyBase + i, in);
      } else {
        nodePage.mNodes[i] = null;
      }
    }

    nodePage.setDirty(false);
    return nodePage;
  }

  public static final NodePage clone(final NodePage committedNodePage) {
    final NodePage nodePage = new NodePage(committedNodePage.mNodePageKey);

    // Deep-copy all nodes.
    for (int i = 0; i < IConstants.NDP_NODE_COUNT; i++) {
      if (committedNodePage.mNodes[i] != null) {
        nodePage.mNodes[i] = new Node(committedNodePage.mNodes[i]);
      }
    }

    nodePage.setDirty(false);
    return nodePage;
  }

  /**
   * Get key of node page.
   * 
   * @return INode page key.
   */
  public final long getNodePageKey() {
    return mNodePageKey;
  }

  /**
   * Get node at a given offset.
   * 
   * @param offset Offset of node within local node page.
   * @return INode at given offset.
   */
  public final Node getNode(final int offset) {
    return mNodes[offset];
  }

  /**
   * Overwrite a single node at a given offset.
   * 
   * @param offset Offset of node to overwrite in this node page.
   * @param node INode to store at given nodeOffset.
   */
  public final void setNode(final int offset, final Node node) {
    mNodes[offset] = node;
    setDirty(true);
  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final IWriteTransactionState state) {
    setDirty(false);
  }

  /**
   * {@inheritDoc}
   */
  public final void serialize(final FastByteArrayWriter out) throws Exception {
    for (int i = 0; i < IConstants.NDP_NODE_COUNT; i++) {
      if (mNodes[i] != null) {
        out.writeBoolean(true);
        mNodes[i].serialize(out);
      } else {
        out.writeBoolean(false);
      }
    }
  }

}

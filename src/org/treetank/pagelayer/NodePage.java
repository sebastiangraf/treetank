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
import org.treetank.nodelayer.AbstractNode;
import org.treetank.nodelayer.DocumentRootNode;
import org.treetank.nodelayer.ElementNode;
import org.treetank.nodelayer.FullTextNode;
import org.treetank.nodelayer.FullTextRootNode;
import org.treetank.nodelayer.TextNode;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>NodePage</h1>
 * 
 * <p>
 * A node page stores a set of nodes.
 * </p>
 */
public final class NodePage extends AbstractPage {

  /** Key of node page. This is the base key of all contained nodes. */
  private final long mNodePageKey;

  /** Array of nodes. This can have null nodes that were removed. */
  private final AbstractNode[] mNodes;

  /**
   * Create node page.
   * 
   * @param nodePageKey Base key assigned to this node page.
   */
  public NodePage(final long nodePageKey) {
    super(0);
    mNodePageKey = nodePageKey;
    mNodes = new AbstractNode[IConstants.NDP_NODE_COUNT];
  }

  /**
   * Read node page.
   * 
   * @param in Input bytes to read page from.
   * @param nodePageKey Base key assigned to this node page.
   */
  public NodePage(final FastByteArrayReader in, final long nodePageKey) {
    super(0, in);
    mNodePageKey = nodePageKey;
    mNodes = new AbstractNode[IConstants.NDP_NODE_COUNT];

    final long keyBase = mNodePageKey << IConstants.NDP_NODE_COUNT_EXPONENT;
    for (int offset = 0; offset < IConstants.NDP_NODE_COUNT; offset++) {
      final int kind = in.readByte();
      switch (kind) {
      case IConstants.UNKNOWN:
        // Was null node, do nothing here.
        break;
      case IConstants.DOCUMENT_ROOT:
        mNodes[offset] = new DocumentRootNode(in);
        break;
      case IConstants.ELEMENT:
        mNodes[offset] = new ElementNode(keyBase + offset, in);
        break;
      case IConstants.TEXT:
        mNodes[offset] = new TextNode(keyBase + offset, in);
        break;
      case IConstants.FULLTEXT:
        mNodes[offset] = new FullTextNode(keyBase + offset, in);
        break;
      case IConstants.FULLTEXT_ROOT:
        mNodes[offset] = new FullTextRootNode(in);
        break;
      default:
        throw new IllegalStateException(
            "Unsupported node kind encountered during read: " + kind);
      }
    }
  }

  /**
   * Clone node page.
   * 
   * @param committedNodePage Node page to clone.
   */
  public NodePage(final NodePage committedNodePage) {
    super(0, committedNodePage);
    mNodePageKey = committedNodePage.mNodePageKey;
    mNodes = new AbstractNode[IConstants.NDP_NODE_COUNT];

    // Deep-copy all nodes.
    for (int offset = 0; offset < IConstants.NDP_NODE_COUNT; offset++) {
      if (committedNodePage.mNodes[offset] != null) {
        final int kind = committedNodePage.mNodes[offset].getKind();
        switch (kind) {
        case IConstants.UNKNOWN:
          // Was null node, do nothing here.
          break;
        case IConstants.DOCUMENT_ROOT:
          mNodes[offset] =
              new DocumentRootNode(committedNodePage.mNodes[offset]);
          break;
        case IConstants.ELEMENT:
          mNodes[offset] = new ElementNode(committedNodePage.mNodes[offset]);
          break;
        case IConstants.TEXT:
          mNodes[offset] = new TextNode(committedNodePage.mNodes[offset]);
          break;
        case IConstants.FULLTEXT:
          mNodes[offset] = new FullTextNode(committedNodePage.mNodes[offset]);
          break;
        case IConstants.FULLTEXT_ROOT:
          mNodes[offset] =
              new FullTextRootNode(committedNodePage.mNodes[offset]);
          break;
        default:
          throw new IllegalStateException(
              "Unsupported node kind encountered during clone: " + kind);
        }
      }
    }
  }

  /**
   * Get key of node page.
   * 
   * @return Node page key.
   */
  public final long getNodePageKey() {
    return mNodePageKey;
  }

  /**
   * Get node at a given offset.
   * 
   * @param offset Offset of node within local node page.
   * @return Node at given offset.
   */
  public final AbstractNode getNode(final int offset) {
    return mNodes[offset];
  }

  /**
   * Overwrite a single node at a given offset.
   * 
   * @param offset Offset of node to overwrite in this node page.
   * @param node Node to store at given nodeOffset.
   */
  public final void setNode(final int offset, final AbstractNode node) {
    mNodes[offset] = node;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final FastByteArrayWriter out) {
    super.serialize(out);

    for (final AbstractNode node : mNodes) {
      if (node != null) {
        out.writeByte((byte) node.getKind());
        node.serialize(out);
      } else {
        out.writeByte((byte) IConstants.UNKNOWN);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String toString() {
    return super.toString()
        + ": nodePageKey="
        + mNodePageKey
        + ", isDirty="
        + isDirty();
  }

}

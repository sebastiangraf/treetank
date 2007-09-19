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

package org.treetank.pagelayer;

import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.IConstants;

/**
 * <h1>NodePage</h1>
 * 
 * <p>
 * A node page stores a set of nodes.
 * </p>
 */
public final class NodePage implements IPage {

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
    return nodePage;
  }

  public static final NodePage read(final FastByteArrayReader in)
      throws Exception {
    final NodePage nodePage = new NodePage(in.readLong());

    final long keyBase = Node.keyBase(nodePage.mNodePageKey);
    for (int i = 0; i < IConstants.NDP_NODE_COUNT; i++) {
      if (in.readBoolean()) {
        nodePage.mNodes[i] = new Node(keyBase + (i << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT), in);
      } else {
        nodePage.mNodes[i] = null;
      }
    }

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
   * @param nodeOffset Offset of node within local node page.
   * @return INode at given offset.
   */
  public final Node getNode(final int nodeOffset) {
    return mNodes[nodeOffset];
  }

  /**
   * Overwrite a single node at a given offset.
   * 
   * @param nodeOffset Offset of node to overwrite in this node page.
   * @param node INode to store at given nodeOffset.
   */
  public final void setNode(final int nodeOffset, final Node node) {
    mNodes[nodeOffset] = node;
  }

  /**
   * {@inheritDoc}
   */
  public final void commit(final PageWriter pageWriter) {
    // Nothing to do because there are no page references.
  }

  /**
   * {@inheritDoc}
   */
  public final void serialize(final FastByteArrayWriter out) throws Exception {
    out.writeLong(mNodePageKey);
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

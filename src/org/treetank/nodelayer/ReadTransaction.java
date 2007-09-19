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

import org.treetank.pagelayer.Node;
import org.treetank.pagelayer.RevisionRootPage;
import org.treetank.utils.IConstants;

/**
 * <h1>ReadTransaction</h1>
 * 
 * <p>
 * Read-only transaction wiht single-threaded cursor semantics. Each
 * read-only transaction works on a given revision key.
 * </p>
 */
public class ReadTransaction implements IReadTransaction {

  /** Strong reference to revision root page this transaction reads from. */
  protected final RevisionRootPage mRevisionRootPage;

  /** Strong reference to currently selected node. */
  protected INode mCurrentNode;

  /**
   * Constructor.
   * 
   * @param revisionRootPage Revision root page to work with.
   */
  protected ReadTransaction(final RevisionRootPage revisionRootPage) {
    mRevisionRootPage = revisionRootPage;
    mCurrentNode = null;
  }

  /**
   * {@inheritDoc}
   */
  public final long revisionKey() {
    return mRevisionRootPage.getRevisionKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long revisionSize() {
    return mRevisionRootPage.getRevisionSize();
  }

  /**
   * {@inheritDoc}
   */
  public final boolean isSelected() {
    return (mCurrentNode != null);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToRoot() throws Exception {
    return moveTo(IConstants.ROOT_KEY);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveTo(final long nodeKey) throws Exception {
    if (nodeKey != IConstants.NULL_KEY) {

      // Calculate coordinates for given nodeKey.
      final long nodePageKey = Node.nodePageKey(nodeKey);
      final int nodePageOffset = Node.nodePageOffset(nodeKey);

      // Fetch node by offset within mCurrentNodePage.
      mCurrentNode =
          mRevisionRootPage.getNodePage(nodePageKey).getNode(nodePageOffset);

    } else {
      mCurrentNode = null;
    }
    return (mCurrentNode != null);
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToParent() throws Exception {
    return moveTo(mCurrentNode.getParentKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToFirstChild() throws Exception {
    return moveTo(mCurrentNode.getFirstChildKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToLeftSibling() throws Exception {
    return moveTo(mCurrentNode.getLeftSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToRightSibling() throws Exception {
    return moveTo(mCurrentNode.getRightSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  public final boolean moveToAttribute(final int index) throws Exception {
    mCurrentNode = mCurrentNode.getAttribute(index);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public final INode getNode() {
    assertIsSelected();
    return mCurrentNode;
  }

  /**
   * {@inheritDoc}
   */
  public final long getNodeKey() {
    assertIsSelected();
    return mCurrentNode.getNodeKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getParentKey() {
    assertIsSelected();
    return mCurrentNode.getParentKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getFirstChildKey() {
    assertIsSelected();
    return mCurrentNode.getFirstChildKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getLeftSiblingKey() {
    assertIsSelected();
    return mCurrentNode.getLeftSiblingKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getRightSiblingKey() {
    assertIsSelected();
    return mCurrentNode.getRightSiblingKey();
  }

  /**
   * {@inheritDoc}
   */
  public final long getChildCount() {
    assertIsSelected();
    return mCurrentNode.getChildCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getAttributeCount() {
    assertIsSelected();
    return mCurrentNode.getAttributeCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getNamespaceCount() {
    assertIsSelected();
    return mCurrentNode.getNamespaceCount();
  }

  /**
   * {@inheritDoc}
   */
  public final int getKind() {
    assertIsSelected();
    return mCurrentNode.getKind();
  }

  /**
   * {@inheritDoc}
   */
  public final int getLocalPartKey() {
    assertIsSelected();
    return mCurrentNode.getLocalPartKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getLocalPart() throws Exception {
    assertIsSelected();
    return mRevisionRootPage.getName(mCurrentNode.getLocalPartKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getURIKey() {
    assertIsSelected();
    return mCurrentNode.getURIKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getURI() throws Exception {
    assertIsSelected();
    return mRevisionRootPage.getName(mCurrentNode.getURIKey());
  }

  /**
   * {@inheritDoc}
   */
  public final int getPrefixKey() {
    assertIsSelected();
    return mCurrentNode.getPrefixKey();
  }

  /**
   * {@inheritDoc}
   */
  public final String getPrefix() throws Exception {
    assertIsSelected();
    return mRevisionRootPage.getName(mCurrentNode.getPrefixKey());
  }

  /**
   * {@inheritDoc}
   */
  public final byte[] getValue() {
    assertIsSelected();
    return mCurrentNode.getValue();
  }

  /**
   * Assert that a node is selected.
   */
  protected void assertIsSelected() {
    if (mCurrentNode == null) {
      throw new IllegalStateException("No node selected.");
    }
  }

  /**
   * {@inheritDoc}
   */
  public final int keyForName(final String name) {
    return name.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  public final String nameForKey(final int key) throws Exception {
    return mRevisionRootPage.getName(key);
  }

}

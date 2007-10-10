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
 * $Id:ReadTransaction.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.pagelayer.Node;
import org.treetank.pagelayer.NodePage;
import org.treetank.pagelayer.RevisionRootPage;

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

  protected NodePage mCurrentNodePage;

  /**
   * Constructor.
   * 
   * @param revisionRootPage Revision root page to work with.
   */
  protected ReadTransaction(final RevisionRootPage revisionRootPage) {
    mRevisionRootPage = revisionRootPage;
    mCurrentNode = null;
    mCurrentNodePage = null;
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

      // Fetch node page if it changed.
      if (mCurrentNodePage == null
          || mCurrentNodePage.getNodePageKey() != nodePageKey) {
        mCurrentNodePage = mRevisionRootPage.getNodePage(nodePageKey);
      }

      // Fetch node by offset within mCurrentNodePage.
      mCurrentNode = mCurrentNodePage.getNode(nodePageOffset);

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

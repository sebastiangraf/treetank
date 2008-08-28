/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.nodelayer;

import org.treetank.api.IReadTransaction;
import org.treetank.utils.IByteBuffer;

/**
 * <h1>DocumentNode</h1>
 * 
 * <p>
 * Node representing the root of a document. This node
 * is guaranteed to exist in revision 0 and can not be removed.
 * </p>
 */
public final class DocumentRootNode extends AbstractNode {

  /** Key of first child. */
  private long mFirstChildKey;

  /** Child count including element and text nodes. */
  private long mChildCount;

  /**
   * Constructor to create document node.
   */
  public DocumentRootNode() {
    super(IReadTransaction.DOCUMENT_ROOT_KEY);
    mFirstChildKey = IReadTransaction.NULL_NODE_KEY;
    mChildCount = 0L;
  }

  /**
   * Clone document node.
   * 
   * @param node Node to clone.
   */
  public DocumentRootNode(final AbstractNode node) {
    super(node.getNodeKey());
    mFirstChildKey = node.getFirstChildKey();
    mChildCount = node.getChildCount();
  }

  /**
   * Read document node.
   * 
   * @param in Byte input to read node from.
   */
  public DocumentRootNode(final IByteBuffer in) {
    super(IReadTransaction.DOCUMENT_ROOT_KEY);
    mFirstChildKey = in.get();
    mChildCount = in.get();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean isDocumentRoot() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean hasFirstChild() {
    return (mFirstChildKey != IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getFirstChildKey() {
    return mFirstChildKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setFirstChildKey(final long firstChildKey) {
    mFirstChildKey = firstChildKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getChildCount() {
    return mChildCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setChildCount(final long childCount) {
    mChildCount = childCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void incrementChildCount() {
    mChildCount += 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void decrementChildCount() {
    mChildCount -= 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getKind() {
    return IReadTransaction.DOCUMENT_ROOT_KIND;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final IByteBuffer out) {
    out.put(mFirstChildKey);
    out.put(mChildCount);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "DocumentRootNode "
        + "\n\tnodeKey: "
        + getNodeKey()
        + "\n\tchildcount: "
        + mChildCount
        + "\n\tfirstChildKey: "
        + mFirstChildKey;
  }

}

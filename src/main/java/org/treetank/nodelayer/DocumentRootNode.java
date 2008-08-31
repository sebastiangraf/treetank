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

  private static final int SIZE = 3;

  private static final int FIRST_CHILD_KEY = 1;

  private static final int CHILD_COUNT = 2;

  /**
   * Constructor to create document node.
   */
  public DocumentRootNode() {
    super(SIZE, IReadTransaction.DOCUMENT_ROOT_KEY);
    mData[FIRST_CHILD_KEY] = IReadTransaction.NULL_NODE_KEY;
    mData[CHILD_COUNT] = 0L;
  }

  /**
   * Clone document node.
   * 
   * @param node Node to clone.
   */
  public DocumentRootNode(final AbstractNode node) {
    super(node);
  }

  /**
   * Read document node.
   * 
   * @param in Byte input to read node from.
   */
  public DocumentRootNode(final IByteBuffer in) {
    super(SIZE, IReadTransaction.DOCUMENT_ROOT_KEY, in);
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
    return (mData[FIRST_CHILD_KEY] != IReadTransaction.NULL_NODE_KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getFirstChildKey() {
    return mData[FIRST_CHILD_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setFirstChildKey(final long firstChildKey) {
    mData[FIRST_CHILD_KEY] = firstChildKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getChildCount() {
    return mData[CHILD_COUNT];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setChildCount(final long childCount) {
    mData[CHILD_COUNT] = childCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void incrementChildCount() {
    mData[CHILD_COUNT] += 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void decrementChildCount() {
    mData[CHILD_COUNT] -= 1;
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
    super.serialize(out);
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
        + mData[CHILD_COUNT]
        + "\n\tfirstChildKey: "
        + mData[FIRST_CHILD_KEY];
  }

}

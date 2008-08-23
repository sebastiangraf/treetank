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

import java.nio.ByteBuffer;

import org.treetank.api.IReadTransaction;

/**
 * <h1>FullTextRootNode</h1>
 * 
 * <p>
 * Node representing a fulltext root node.
 * </p>
 */
public final class FullTextRootNode extends AbstractNode {

  /** Key of first child. */
  private long mFirstChildKey;

  /**
   * Constructor to create full text root node.
   */
  public FullTextRootNode() {
    super(IReadTransaction.FULLTEXT_ROOT_KEY);
    mFirstChildKey = IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * Clone full text root node.
   * 
   * @param node Node to clone.
   */
  public FullTextRootNode(final AbstractNode node) {
    super(node.getNodeKey());
    mFirstChildKey = node.getFirstChildKey();
  }

  /**
   * Read full text root node.
   * 
   * @param in Byte input to read node from.
   */
  public FullTextRootNode(final ByteBuffer in) {
    super(IReadTransaction.FULLTEXT_ROOT_KEY);
    mFirstChildKey = IReadTransaction.FULLTEXT_ROOT_KEY - in.getLong();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean isFullTextRoot() {
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
  public final int getKind() {
    return IReadTransaction.FULLTEXT_ROOT_KIND;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final ByteBuffer out) {
    out.putLong(IReadTransaction.FULLTEXT_ROOT_KEY - mFirstChildKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "FullTextRootNode "
        + "\n\tnodeKey: "
        + getNodeKey()
        + "\n\tfirstChildKey: "
        + mFirstChildKey;
  }
}

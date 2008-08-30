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
 * <h1>NamespaceNode</h1>
 * 
 * <p>
 * Node representing a namespace.
 * </p>
 */
public final class NamespaceNode extends AbstractNode {

  /** Key of parent. */
  private long mParentKey;

  /** Key of URI. */
  private int mURIKey;

  /** Key of prefix. */
  private int mNameKey;

  /**
   * Create namespace node.
   * 
   * @param uriKey Key of URI.
   * @param nameKey Key of prefix.
   */
  public NamespaceNode(
      final long nodeKey,
      final long parentKey,
      final int uriKey,
      final int nameKey) {
    super(nodeKey);
    mParentKey = parentKey;
    mURIKey = uriKey;
    mNameKey = nameKey;
  }

  /**
   * Clone namespace node.
   * 
   * @param namespace Namespace node to clone.
   */
  public NamespaceNode(final AbstractNode namespace) {
    super(namespace.getNodeKey());
    mParentKey = namespace.getParentKey();
    mURIKey = namespace.getURIKey();
    mNameKey = namespace.getNameKey();
  }

  public NamespaceNode(final long nodeKey, final IByteBuffer in) {
    super(nodeKey);
    long[] values = in.getAll(3);
    mParentKey = nodeKey - values[0];
    mURIKey = (int) values[1];
    mNameKey = (int) values[2];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean hasParent() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final long getParentKey() {
    return mParentKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setParentKey(final long parentKey) {
    mParentKey = parentKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getKind() {
    return IReadTransaction.NAMESPACE_KIND;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getNameKey() {
    return mNameKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setNameKey(final int nameKey) {
    mNameKey = nameKey;
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

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final IByteBuffer out) {
    out.putAll(new long[] { getNodeKey() - mParentKey, mURIKey, mNameKey });
  }

}

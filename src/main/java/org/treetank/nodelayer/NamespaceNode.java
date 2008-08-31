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

  private static final int SIZE = 4;

  private static final int PARENT_KEY = 1;

  private static final int URI_KEY = 2;

  private static final int NAME_KEY = 3;

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
    super(SIZE, nodeKey);
    mData[PARENT_KEY] = nodeKey - parentKey;
    mData[URI_KEY] = uriKey;
    mData[NAME_KEY] = nameKey;
  }

  /**
   * Clone namespace node.
   * 
   * @param namespace Namespace node to clone.
   */
  public NamespaceNode(final AbstractNode namespace) {
    super(namespace);
  }

  public NamespaceNode(final long nodeKey, final IByteBuffer in) {
    super(SIZE, nodeKey, in);
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
    return mData[NODE_KEY] - mData[PARENT_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setParentKey(final long parentKey) {
    mData[PARENT_KEY] = mData[NODE_KEY] - parentKey;
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
    return (int) mData[NAME_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setNameKey(final int nameKey) {
    mData[NAME_KEY] = nameKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getURIKey() {
    return (int) mData[URI_KEY];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setURIKey(final int uriKey) {
    mData[URI_KEY] = uriKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final IByteBuffer out) {
    super.serialize(out);
  }

}

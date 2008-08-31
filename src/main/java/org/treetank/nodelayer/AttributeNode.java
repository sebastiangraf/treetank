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
 * <h1>AttributeNode</h1>
 * 
 * <p>Node representing an attribute.</p>
 */
public final class AttributeNode extends AbstractNode {

  private static final int SIZE = 6;

  private static final int PARENT_KEY = 1;

  private static final int NAME_KEY = 2;

  private static final int URI_KEY = 3;

  private static final int TYPE = 4;

  private static final int VALUE_LENGTH = 5;

  /** Value of attribute. */
  private byte[] mValue;

  /**
   * Constructor to create attribute.
   * 
   * @param nodeKey Key of node.
   * @param nameKey Key of qualified name.
   * @param uriKey Key of URI.
   * @param type Type of attribute value.
   * @param value Value of attribute.
   */
  public AttributeNode(
      final long nodeKey,
      final long parentKey,
      final int nameKey,
      final int uriKey,
      final int type,
      final byte[] value) {
    super(SIZE, nodeKey);
    mData[PARENT_KEY] = nodeKey - parentKey;
    mData[NAME_KEY] = nameKey;
    mData[URI_KEY] = uriKey;
    mData[TYPE] = type;
    mData[VALUE_LENGTH] = value.length;
    mValue = value;
  }

  /**
   * Constructor to clone attribute.
   * 
   * @param attribute Attribute to clone.
   */
  public AttributeNode(final AbstractNode attribute) {
    super(attribute);
    mValue = attribute.getRawValue();
  }

  public AttributeNode(final long nodeKey, final IByteBuffer in) {
    super(SIZE, nodeKey, in);
    mValue = in.getArray((int) mData[VALUE_LENGTH]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final boolean isAttribute() {
    return true;
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
    this.mData[NAME_KEY] = nameKey;
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
  public final int getTypeKey() {
    return (int) mData[TYPE];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final byte[] getRawValue() {
    return mValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setValue(final int valueType, final byte[] value) {
    mData[TYPE] = valueType;
    mData[VALUE_LENGTH] = value.length;
    mValue = value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setType(final int valueType) {
    mData[TYPE] = valueType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getKind() {
    return IReadTransaction.ATTRIBUTE_KIND;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void serialize(final IByteBuffer out) {
    super.serialize(out);
    out.putArray(mValue);
  }

}

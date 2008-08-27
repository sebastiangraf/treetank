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
 * <h1>AttributeNode</h1>
 * 
 * <p>Node representing an attribute.</p>
 */
public final class AttributeNode extends AbstractNode {

  /** Key of parent node. */
  private long mParentKey;

  /** Key of qualified name. */
  private int mNameKey;

  /** Key of URI. */
  private int mURIKey;

  /** Type of node. */
  private int mType;

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
    super(nodeKey);
    mParentKey = parentKey;
    mNameKey = nameKey;
    mURIKey = uriKey;
    mType = type;
    mValue = value;
  }

  /**
   * Constructor to clone attribute.
   * 
   * @param attribute Attribute to clone.
   */
  public AttributeNode(final AbstractNode attribute) {
    super(attribute.getNodeKey());
    mParentKey = attribute.getParentKey();
    mNameKey = attribute.getNameKey();
    mURIKey = attribute.getURIKey();
    mType = attribute.getTypeKey();
    mValue = attribute.getRawValue();
  }

  public AttributeNode(final long nodeKey, final ByteBuffer in) {
    super(nodeKey);
    mParentKey = in.getLong();
    mNameKey = in.getInt();
    mURIKey = in.getInt();
    mType = in.getInt();
    mValue = new byte[in.get()];
    in.get(mValue);
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
    return mNameKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setNameKey(final int nameKey) {
    this.mNameKey = nameKey;
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
  public final int getTypeKey() {
    return mType;
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
    mType = valueType;
    mValue = value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setType(final int valueType) {
    mType = valueType;
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
  public final void serialize(final ByteBuffer out) {
    out.putLong(mParentKey);
    out.putInt(mNameKey);
    out.putInt(mURIKey);
    out.putInt(mType);
    out.put((byte) mValue.length);
    out.put(mValue);
  }

}

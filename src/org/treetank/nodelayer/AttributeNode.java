/*
 * Copyright (c) 2007, Marc Kramis
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
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>AttributeNode</h1>
 * 
 * <p>Node representing an attribute.</p>
 */
public final class AttributeNode extends AbstractNode {

  /** Key of qualified name. */
  private int mNameKey;

  /** Key of URI. */
  private int mURIKey;

  /** Type of value. */
  private int mValueType;

  /** Value of attribute. */
  private byte[] mValue;

  /**
   * Constructor to create attribute.
   * 
   * @param nodeKey Key of node.
   * @param nameKey Key of qualified name.
   * @param uriKey Key of URI.
   * @param valueType Type of attribute value.
   * @param value Value of attribute.
   */
  public AttributeNode(
      final long nodeKey,
      final int nameKey,
      final int uriKey,
      final int valueType,
      final byte[] value) {
    super(nodeKey);
    mNameKey = nameKey;
    mURIKey = uriKey;
    mValueType = valueType;
    mValue = value;
  }

  /**
   * Constructor to clone attribute.
   * 
   * @param attribute Attribute to clone.
   */
  public AttributeNode(final AbstractNode attribute) {
    super(attribute.getNodeKey());
    mNameKey = attribute.getNameKey();
    mURIKey = attribute.getURIKey();
    mValueType = attribute.getValueType();
    mValue = attribute.getValue();
  }

  public AttributeNode(final long nodeKey, final FastByteArrayReader in) {
    super(nodeKey);

    mNameKey = in.readVarInt();
    mURIKey = in.readVarInt();
    mValueType = in.readByte();
    mValue = in.readByteArray();
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
    return getNodeKey();
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
  public final int getValueType() {
    return mValueType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final byte[] getValue() {
    return mValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setValue(final int valueType, final byte[] value) {
    mValueType = valueType;
    mValue = value;
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
  public final void serialize(final FastByteArrayWriter out) {
    out.writeVarInt(mNameKey);
    out.writeVarInt(mURIKey);
    out.writeByte((byte) mValueType);
    out.writeByteArray(mValue);
  }

}

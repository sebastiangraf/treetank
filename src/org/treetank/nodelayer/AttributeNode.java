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
 * $Id: Attribute.java 3276 2007-10-25 15:28:13Z kramis $
 */

package org.treetank.nodelayer;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;

/**
 * <h1>AttributeNode</h1>
 * 
 * <p>Node representing an attribute.</p>
 */
public final class AttributeNode extends AbstractNode {

  /** Key of parent node. */
  private long mParentKey;

  /** Key of local part. */
  private int mLocalPartKey;

  /** Key of URI. */
  private int mURIKey;

  /** Key of prefix. */
  private int mPrefixKey;

  /** Value of attribute. */
  private byte[] mValue;

  /**
   * Constructor to create attribute.
   * 
   * @param nodeKey Key of node.
   * @param parentKey Key of parent node.
   * @param localPartKey Key of local part.
   * @param uriKey Key of URI.
   * @param prefixKey Key of prefix.
   * @param value Value of attribute.
   */
  public AttributeNode(
      final long nodeKey,
      final long parentKey,
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value) {
    super(nodeKey);
    mParentKey = parentKey;
    mLocalPartKey = localPartKey;
    mURIKey = uriKey;
    mPrefixKey = prefixKey;
    mValue = value;
  }

  /**
   * Constructor to clone attribute.
   * 
   * @param attribute Attribute to clone.
   */
  public AttributeNode(final INode attribute) {
    super(attribute.getNodeKey());
    mParentKey = attribute.getParentKey();
    mLocalPartKey = attribute.getLocalPartKey();
    mURIKey = attribute.getURIKey();
    mPrefixKey = attribute.getPrefixKey();
    mValue = attribute.getValue();
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
  public final int getLocalPartKey() {
    return mLocalPartKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getLocalPart(final IReadTransaction rtx) {
    return rtx.nameForKey(mLocalPartKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setLocalPartKey(final int localPartKey) {
    this.mLocalPartKey = localPartKey;
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
  public final INode getParent(final IReadTransaction rtx) {
    return rtx.moveTo(mParentKey);
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
  public final int getPrefixKey() {
    return mPrefixKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final String getPrefix(final IReadTransaction rtx) {
    return rtx.nameForKey(mPrefixKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setPrefixKey(final int prefixKey) {
    mPrefixKey = prefixKey;
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
  public final String getURI(final IReadTransaction rtx) {
    return rtx.nameForKey(mURIKey);
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
  public final byte[] getValue() {
    return mValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setValue(final byte[] value) {
    mValue = value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final int getKind() {
    return IConstants.ATTRIBUTE;
  }

}

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
 * $Id: INode.java 3268 2007-10-25 13:16:01Z kramis $
 */

package org.treetank.pagelayer;

import org.treetank.api.INode;
import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>ISerializableNode</h1>
 * 
 * Interface to serialize node.
 */
public interface InternalNode extends INode {

  /**
   * Serialize this node.
   * 
   * @param out Byte output.
   */
  public void serialize(final FastByteArrayWriter out);

  public void setNodeKey(final long nodeKey);

  public void setParentKey(final long parentKey);

  public void setFirstChildKey(final long firstChildKey);

  public void setLeftSiblingKey(final long leftSiblingKey);

  public void setRightSiblingKey(final long rightSiblingKey);

  public void setChildCount(final long childCount);

  public void incrementChildCount();

  public void decrementChildCount();

  public void setAttribute(
      final int index,
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value);

  public void insertAttribute(
      final int localPartKey,
      final int uriKey,
      final int prefixKey,
      final byte[] value);

  public void setNamespace(
      final int index,
      final int uriKey,
      final int prefixKey);

  public void insertNamespace(final int uriKey, final int prefixKey);

  public void setKind(final byte kind);

  public void setLocalPartKey(final int localPartKey);

  public void setPrefixKey(final int prefixKey);

  public void setURIKey(final int uriKey);

  public void setValue(final byte[] value);

}

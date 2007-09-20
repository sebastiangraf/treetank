/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright (C) 2007 Marc Kramis
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
 * $Id$
 */

package org.treetank.utils;

/**
 * <h1>KeyPacker</h1>
 * 
 * <p>
 * An address points to a page which consists of [1..256] blocks each of
 * 512 byte in size. The page size therefore ranges between [512..131072] bytes.
 * As such, a 16 bit offset is required to address every two-byte couple in a
 * page. A 8 bit size denotes the page size in blocks. This leaves 40 bits 
 * for the page start block address or 2^40 pages in the system.
 * </p>
 * 
 * <p>
 * Pack a 40bit address, 8bit size, and 16bit offset into a 64bit value.
 * </p>
 */
public final class KeyPacker {

  /**
   * Default constructor is hidden.
   * 
   */
  private KeyPacker() {
    // hidden
  }

  /**
   * Pack address, size, and offset into single value.
   * 
   * @param nodeKey
   *          40bit address.
   * @param attributeIndex
   *           8bit size.
   * @param offset
   *          16bit offset.
   * @return 64bit packed value.
   */
  public static final long pack(final long nodeKey, final int attributeIndex) {

    return ((nodeKey << 8) | attributeIndex);
  }

  /**
   * Unpack address from single value.
   * 
   * @param packed
   *          64bit packed value.
   * @return 40bit unpacked address.
   */
  public static final long unpackNodeKey(final long packed) {
    return packed >> 8;
  }

  /**
   * Unpack offset from single value.
   * 
   * @param packed
   *          64bit packed value.
   * @return 16bit unpacked offset.
   */
  public static final int unpackAttributeIndex(final long packed) {
    return (int) (packed & 0xFF);
  }

}

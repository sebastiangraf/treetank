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

package org.treetank.pagelayer;

import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

public class PageReference {

  private boolean mIsDirty;

  private IPage mPage;

  private long mStart;

  private int mSize;

  private long mChecksum;

  public PageReference() {
    this(null, -1L, -1, -1L);
  }

  public PageReference(final PageReference pageReference) {
    this(
        pageReference.mPage,
        pageReference.mStart,
        pageReference.mSize,
        pageReference.mChecksum);
    mIsDirty = true;
  }

  public PageReference(
      final IPage page,
      final long start,
      final int size,
      final long checksum) {
    mIsDirty = false;
    mPage = page;
    mStart = start;
    mSize = size;
    mChecksum = checksum;
  }

  public PageReference(final FastByteArrayReader in) throws Exception {
    this(null, in.readLong(), in.readInt(), in.readLong());
  }

  public final boolean isInstantiated() {
    return (mPage != null);
  }

  public final boolean isCommitted() {
    return (mStart != -1L);
  }

  public final boolean isDirty() {
    return mIsDirty;
  }

  public final long getChecksum() {
    return mChecksum;
  }

  public final void setChecksum(final long checksum) {
    mChecksum = checksum;
  }

  public final IPage getPage() {
    return mPage;
  }

  public final void setPage(final IPage page) {
    mPage = page;
    mIsDirty = true;
  }

  public final int getSize() {
    return mSize;
  }

  public final void setSize(final int size) {
    mSize = size;
  }

  public final long getStart() {
    return mStart;
  }

  public final void setStart(final long start) {
    mStart = start;
  }

  @Override
  public final boolean equals(final Object object) {
    final PageReference pageReference = (PageReference) object;
    return ((mChecksum == pageReference.mChecksum)
        && (mStart == pageReference.mStart) && (mSize == pageReference.mSize));
  }

  public final void serialize(final FastByteArrayWriter out) throws Exception {
    mIsDirty = false;
    out.writeLong(mStart);
    out.writeInt(mSize);
    out.writeLong(mChecksum);
  }

}

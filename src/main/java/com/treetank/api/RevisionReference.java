/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * NO permission to use, copy, modify, and/or distribute this software.
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

package com.treetank.api;

import com.treetank.util.SerialiserTool;

public final class RevisionReference {

  private long mOffset;

  private int mLength;

  private long mRevision;

  public RevisionReference() {
    this(0, 0, 0);
  }

  public RevisionReference(
      final long offset,
      final int length,
      final long revision) {
    mOffset = offset;
    mLength = length;
    mRevision = revision;
  }

  public RevisionReference(final byte[] buffer) {
    mOffset = SerialiserTool.readLong(0, buffer);
    mLength = SerialiserTool.readInt(8, buffer);
    mRevision = SerialiserTool.readLong(12, buffer);
  }

  public final void setOffset(final long offset) {
    mOffset = offset;
  }

  public final void setLength(final int length) {
    mLength = length;
  }

  public final void setRevision(final long revision) {
    mRevision = revision;
  }

  public final long getOffset() {
    return mOffset;
  }

  public final int getLength() {
    return mLength;
  }

  public final long getRevision() {
    return mRevision;
  }

  public final byte[] serialise() {
    final byte[] buffer = new byte[64];
    SerialiserTool.writeLong(0, buffer, mOffset);
    SerialiserTool.writeInt(8, buffer, mLength);
    SerialiserTool.writeLong(12, buffer, mRevision);
    return buffer;
  }

  public final String toString() {
    return "RevisionReference("
        + mOffset
        + ", "
        + mLength
        + ", "
        + mRevision
        + ")";
  }

}

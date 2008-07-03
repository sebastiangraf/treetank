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

package com.treetank.shared;


public final class FragmentReference {

  private long mOffset;

  private int mLength;

  public FragmentReference() {
    this(0, 0);
  }

  public FragmentReference(final long offset, final int length) {
    mOffset = offset;
    mLength = length;
  }

  public FragmentReference(final byte[] buffer) {
    mOffset = Serialiser.readLong(0, buffer);
    mLength = Serialiser.readInt(8, buffer);
  }

  public final void setOffset(final long offset) {
    mOffset = offset;
  }

  public final void setLength(final int length) {
    mLength = length;
  }

  public final long getOffset() {
    return mOffset;
  }

  public final int getLength() {
    return mLength;
  }

  public final byte[] serialise() {
    final byte[] buffer = new byte[12];
    Serialiser.writeLong(0, buffer, mOffset);
    Serialiser.writeInt(8, buffer, mLength);
    return buffer;
  }

  public final String toString() {
    return "FragmentReference(" + mOffset + ", " + mLength + ")";
  }

}

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

package org.treetank.openbsd;

import org.treetank.utils.IByteBuffer;

public class ByteBufferNativeImpl implements IByteBuffer {

  final long mAddress;

  final int mCapacity;

  int mPosition;

  public ByteBufferNativeImpl(final int capacity) {
    mAddress = allocate(capacity);
    mCapacity = capacity;
    mPosition = 0;
  }

  public final int position() {
    return mPosition;
  }

  public final void position(final int position) {
    if ((position >= mCapacity) || (position < 0)) {
      throw new IllegalArgumentException("Position out of bounds.");
    }
    mPosition = position;
  }

  public final long get() {
    return get(mAddress, mPosition);
  }

  public final byte[] get(final int length) {
    return getArray(mAddress, mPosition);
  }

  public final void put(final long value) {
    put(mAddress, mPosition, value);
  }

  public final void put(final byte[] value) {
    putArray(mAddress, mPosition, value);
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      free(mAddress);
    } finally {
      super.finalize();
    }
  }

  private final native long get(final long address, final int position);

  private final native byte[] getArray(final long address, final int position);

  private final native void put(
      final long address,
      final int position,
      final long value);

  private final native void putArray(
      final long address,
      final int position,
      final byte[] value);

  private final native long allocate(final int capacity);

  private final native void free(final long address);

}

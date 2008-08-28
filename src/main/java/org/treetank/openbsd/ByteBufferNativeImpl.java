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

public class ByteBufferNativeImpl {

  final long mAddress;

  final int mCapacity;

  public ByteBufferNativeImpl(final int capacity) {
    mAddress = allocate(capacity);
    mCapacity = capacity;
  }

  public final byte get(final int position) {
    return get(mAddress, position);
  }

  public final int getInt(final int position) {
    return getInt(mAddress, position);
  }

  public final long getLong(final int position) {
    return getLong(mAddress, position);
  }

  public final byte[] getByteArray(final int position) {
    return getByteArray(mAddress, position);
  }

  public final void put(final int position, final byte value) {
    put(mAddress, position, value);
  }

  public final void putInt(final int position, final int value) {
    putInt(mAddress, position, value);
  }

  public final void putLong(final int position, final long value) {
    putLong(mAddress, position, value);
  }

  public final void putByteArray(final int position, final byte[] value) {
    putByteArray(mAddress, position, value);
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      free(mAddress);
    } finally {
      super.finalize();
    }
  }

  private final native byte get(final long address, final int position);

  private final native int getInt(final long address, final int position);

  private final native long getLong(final long address, final int position);

  private final native byte[] getByteArray(
      final long address,
      final int position);

  private final native void put(
      final long address,
      final int position,
      final byte value);

  private final native void putInt(
      final long address,
      final int position,
      final int value);

  private final native void putLong(
      final long address,
      final int position,
      final long value);

  private final native void putByteArray(
      final long address,
      final int position,
      final byte[] value);

  private final native long allocate(final int capacity);

  private final native void free(final long address);

}

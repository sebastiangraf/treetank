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

  public final native long get();

  public final native byte[] get(final int length);

  public final native void put(final long value);

  public final native void put(final byte[] value);

  private final native long allocate(final int capacity);

  private final native void free(final long address);

  @Override
  protected void finalize() throws Throwable {
    try {
      free(mAddress);
    } finally {
      super.finalize();
    }
  }

}

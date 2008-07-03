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

package com.treetank.core;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;

import com.treetank.api.FragmentReference;
import com.treetank.api.IDevice;
import com.treetank.api.IFragmentWriteCore;
import com.treetank.device.Device;

public final class FragmentWriteCore implements IFragmentWriteCore {

  private final IDevice mDevice1;

  private final Deflater mDeflater;

  private final ByteArrayOutputStream mOut;

  private final byte[] mBuffer;

  public FragmentWriteCore(final String device) {

    if ((device == null) || (device.length() < 1)) {
      throw new IllegalArgumentException(
          "Argument 'device' must not be null and longer than zero.");
    }

    mDevice1 = new Device(device + ".tt1", "rw");
    mDeflater = new Deflater(6);
    mOut = new ByteArrayOutputStream();
    mBuffer = new byte[8192];
  }

  public final FragmentReference writeFragment(final byte[] fragment) {

    if ((fragment == null) || (fragment.length < 1)) {
      throw new IllegalArgumentException(
          "Argument 'fragment' must not be null and longer than zero.");
    }

    try {

      mDeflater.reset();
      mOut.reset();
      mDeflater.setInput(fragment, 0, fragment.length);
      mDeflater.finish();
      int count;
      while (!mDeflater.finished()) {
        count = mDeflater.deflate(mBuffer);
        mOut.write(mBuffer, 0, count);
      }

      final byte[] buffer = mOut.toByteArray();
      final long offset = mDevice1.size();
      final int length = buffer.length;

      mDevice1.write(offset, buffer);

      return new FragmentReference(offset, length);

    } catch (Exception e) {
      throw new RuntimeException("FragmentWriteCore "
          + "could not write fragment due to: "
          + e.toString());
    }
  }

}

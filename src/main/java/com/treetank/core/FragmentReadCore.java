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
import java.util.zip.Inflater;

import com.treetank.api.IDevice;
import com.treetank.api.IFragmentReadCore;
import com.treetank.device.Device;
import com.treetank.util.Fragment;
import com.treetank.util.FragmentReference;

public final class FragmentReadCore implements IFragmentReadCore {

  private final IDevice mDevice1;

  private final Inflater mInflater;

  private final ByteArrayOutputStream mOut;

  private final byte[] mBuffer;

  public FragmentReadCore(final String device) {

    if ((device == null) || (device.length() < 1)) {
      throw new IllegalArgumentException(
          "Argument 'device' must not be null and longer than zero.");
    }

    mDevice1 = new Device(device + ".tt1", "r");
    mInflater = new Inflater();
    mOut = new ByteArrayOutputStream();
    mBuffer = new byte[8192];
  }

  public final Fragment readFragment(final FragmentReference fragmentReference) {

    if ((fragmentReference == null)
        || (fragmentReference.getOffset() < 1)
        || (fragmentReference.getLength() < 1)) {
      throw new IllegalArgumentException(
          "Argument 'fragmentReference' must not be null "
              + "and offset and length must be greater than zero.");
    }

    try {

      final byte[] fragment =
          mDevice1.read(fragmentReference.getOffset(), fragmentReference
              .getLength());

      mInflater.reset();
      mOut.reset();
      mInflater.setInput(fragment);
      int count;
      while (!mInflater.finished()) {
        count = mInflater.inflate(mBuffer);
        mOut.write(mBuffer, 0, count);
      }

      return new Fragment(mOut.toByteArray());

    } catch (Exception e) {
      throw new RuntimeException("FragmentReadCore "
          + "could not read fragment due to: "
          + e.toString());
    }
  }

}

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

import com.treetank.api.Configuration;
import com.treetank.api.IDevice;
import com.treetank.api.IHeaderLoadCore;
import com.treetank.device.Device;

public final class HeaderLoadCore implements IHeaderLoadCore {

  private final String mDevice;

  private final IDevice mDevice1;

  private final IDevice mDevice2;

  public HeaderLoadCore(final String device) {

    if ((device == null) || (device.length() < 1)) {
      throw new IllegalArgumentException(
          "Argument 'device' must not be null and longer than zero.");
    }

    mDevice = device;
    mDevice1 = new Device(device + ".tt1", "r");
    mDevice2 = new Device(device + ".tt2", "r");
  }

  public final Configuration load() {

    try {

      final byte[] header1 = mDevice1.read(0, 512);
      final byte[] header2 = mDevice1.read(512, 512);
      final byte[] header3 = mDevice2.read(0, 512);
      final byte[] header4 = mDevice2.read(512, 512);

      for (int i = 0; i < 480; i++) {
        if ((header1[i] != header2[i])
            || (header1[i] != header3[i])
            || (header1[i] != header4[i])) {
          throw new IllegalStateException("Header of '"
              + mDevice
              + "' do not match.");
        }
      }

      for (int i = 480; i < 512; i++) {
        if ((header1[i] != 0)
            || (header2[i] != 0)
            || (header3[i] != 0)
            || (header4[i] != 0)) {
          throw new IllegalStateException("Header of '"
              + mDevice
              + "' does not support enabled security.");
        }
      }

      final byte[] buffer = new byte[448];
      System.arraycopy(header1, 32, buffer, 0, 448);

      return new Configuration(buffer, ((mDevice2.size() - 960) >> 6) - 1);

    } catch (Exception e) {
      throw new RuntimeException("HeaderLoadCore "
          + "could not load '"
          + mDevice
          + "' due to: "
          + e.toString());
    }

  }

}

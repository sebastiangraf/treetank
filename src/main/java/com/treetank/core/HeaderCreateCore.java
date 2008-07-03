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

import java.util.Random;

import com.treetank.api.IDevice;
import com.treetank.api.IHeaderCreateCore;
import com.treetank.device.Device;
import com.treetank.shared.ByteArrayWriter;
import com.treetank.shared.Configuration;

public final class HeaderCreateCore implements IHeaderCreateCore {

  private final String mDevice;

  private final IDevice mDevice1;

  private final IDevice mDevice2;

  public HeaderCreateCore(final String device) {

    if ((device == null) || (device.length() < 1)) {
      throw new IllegalArgumentException(
          "Argument 'device' must not be null and longer than zero.");
    }

    mDevice = device;
    mDevice1 = new Device(device + ".tt1", "rw");
    mDevice2 = new Device(device + ".tt2", "rw");
  }

  public final void create(final Configuration configuration) {

    if ((configuration == null)) {
      throw new IllegalArgumentException(
          "Argument 'configuration' must not be null.");
    }

    try {

      final ByteArrayWriter writer = new ByteArrayWriter();
      final byte[] salt = new byte[32];
      final byte[] authentication = new byte[32];
      final Random random = new Random();

      random.nextBytes(salt);
      writer.writeByteArray(salt);
      configuration.serialise(writer);
      writer.writeByteArray(authentication);

      mDevice1.write(0, writer);
      mDevice1.write(512, writer);

      mDevice2.write(0, writer);
      mDevice2.write(512, writer);

    } catch (Exception e) {
      throw new RuntimeException("HeaderCreateCore "
          + "could not create '"
          + mDevice
          + "' due to: "
          + e.toString());
    }

  }

}

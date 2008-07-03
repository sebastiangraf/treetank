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

import com.treetank.api.IDevice;
import com.treetank.api.IRevisionReadCore;
import com.treetank.device.Device;
import com.treetank.shared.RevisionReference;

public final class RevisionReadCore implements IRevisionReadCore {

  private final IDevice mDevice1;

  private final IDevice mDevice2;

  public RevisionReadCore(final String device) {

    if ((device == null) || (device.length() < 1)) {
      throw new IllegalArgumentException(
          "Argument 'device' must not be null and longer than zero.");
    }

    mDevice1 = new Device(device + ".tt1", "r");
    mDevice2 = new Device(device + ".tt2", "r");
  }

  public final RevisionReference readRevision(final long revision) {

    if ((revision < 1)) {
      throw new IllegalArgumentException(
          "Argument 'revision' must be greater than zero.");
    }

    try {

      final byte[] buffer = mDevice2.read((revision << 6) + 960, 64);
      return new RevisionReference(buffer);

    } catch (Exception e) {
      throw new RuntimeException("RevisionReadCore "
          + "could not read revision due to: "
          + e.toString());
    }
  }

}

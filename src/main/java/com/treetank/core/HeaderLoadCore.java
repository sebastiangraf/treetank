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

      final byte[] header1 = new byte[512];
      final byte[] header2 = new byte[512];
      final byte[] header3 = new byte[512];
      final byte[] header4 = new byte[512];

      mDevice1.read(0, 0, 512, header1);
      mDevice1.read(512, 0, 512, header2);

      mDevice2.read(0, 0, 512, header3);
      mDevice2.read(512, 0, 512, header4);

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

  @Override
  protected void finalize() throws Throwable {
    try {
      mDevice1.close();
      mDevice2.close();
    } finally {
      super.finalize();
    }
  }

}

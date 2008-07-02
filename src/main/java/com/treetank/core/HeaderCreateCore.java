package com.treetank.core;

import java.util.Random;

import com.treetank.api.Configuration;
import com.treetank.api.IDevice;
import com.treetank.api.IHeaderCreateCore;
import com.treetank.device.Device;

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

      final byte[] salt = new byte[32];
      final byte[] authentication = new byte[32];
      final byte[] header = new byte[512];

      final Random random = new Random();
      random.nextBytes(salt);

      System.arraycopy(salt, 0, header, 0, 32);
      System.arraycopy(configuration.serialise(), 0, header, 32, 448);
      System.arraycopy(authentication, 0, header, 480, 32);

      mDevice1.write(0, header);
      mDevice1.write(512, header);

      mDevice2.write(0, header);
      mDevice2.write(512, header);

    } catch (Exception e) {
      throw new RuntimeException("HeaderCreateCore "
          + "could not create '"
          + mDevice
          + "' due to: "
          + e.toString());
    }

  }

}

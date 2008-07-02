package com.treetank.core;

import java.io.ByteArrayOutputStream;
import java.util.zip.Inflater;

import com.treetank.api.FragmentReference;
import com.treetank.api.IDevice;
import com.treetank.api.IFragmentReadCore;
import com.treetank.device.Device;

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

  public final byte[] readFragment(final FragmentReference fragmentReference) {

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

      return mOut.toByteArray();

    } catch (Exception e) {
      throw new RuntimeException("FragmentReadCore "
          + "could not read fragment due to: "
          + e.toString());
    }
  }

}

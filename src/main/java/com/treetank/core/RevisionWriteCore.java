package com.treetank.core;

import com.treetank.api.FragmentReference;
import com.treetank.api.IDevice;
import com.treetank.api.IRevisionWriteCore;
import com.treetank.api.RevisionReference;
import com.treetank.device.Device;

public final class RevisionWriteCore implements IRevisionWriteCore {

  private final IDevice mDevice1;

  private final IDevice mDevice2;

  public RevisionWriteCore(final String device) {

    if (device == null || device.length() < 1) {
      throw new IllegalArgumentException(
          "Argument 'device' must not be null and longer than zero.");
    }

    mDevice1 = new Device(device + ".tt1", "rw");
    mDevice2 = new Device(device + ".tt2", "rw");
  }

  public final RevisionReference writeRevision(
      final long revision,
      final FragmentReference fragmentReference) {

    if ((revision < 1)) {
      throw new IllegalArgumentException(
          "Argument 'revision' must be greater than zero.");
    }

    if ((fragmentReference == null)
        || (fragmentReference.getOffset() < 1)
        || (fragmentReference.getLength() < 1)) {
      throw new IllegalArgumentException(
          "Argument 'fragmentReference' must not be null "
              + "and offset and length must be greater than zero.");
    }

    try {

      final RevisionReference revisionReference =
          new RevisionReference(
              fragmentReference.getOffset(),
              fragmentReference.getLength(),
              revision);

      final byte[] buffer = revisionReference.serialise();

      mDevice1.write(mDevice1.size(), buffer);
      mDevice2.write(mDevice2.size(), buffer);

      return revisionReference;

    } catch (Exception e) {
      throw new RuntimeException("RevisionWriteCore "
          + "could not write revision due to: "
          + e.toString());
    }
  }

}

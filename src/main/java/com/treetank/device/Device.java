package com.treetank.device;

import java.io.RandomAccessFile;

import com.treetank.api.IDevice;

public final class Device implements IDevice {

  private final RandomAccessFile mFile;

  public Device(final String device, final String mode) {
    try {
      mFile = new RandomAccessFile(device, mode);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  final public byte[] read(final long offset, final int length) {
    try {
      final byte[] buffer = new byte[length];
      mFile.seek(offset);
      mFile.readFully(buffer);
      return buffer;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  final public void write(final long offset, final byte[] buffer) {
    try {
      mFile.seek(offset);
      mFile.write(buffer);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  final public long size() {
    long size = 0L;
    try {
      size = mFile.length();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return size;
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      mFile.close();
    } finally {
      super.finalize();
    }
  }

}

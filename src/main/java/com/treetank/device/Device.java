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

  final public void read(
      final long inDeviceOffset,
      final int inDataOffset,
      final int inDataLength,
      final byte[] outData) {
    try {
      mFile.seek(inDeviceOffset);
      mFile.readFully(outData, inDataOffset, inDataLength);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  final public void write(
      final long inDeviceOffset,
      final int inDataOffset,
      final int inDataLength,
      final byte[] inData) {
    try {
      mFile.seek(inDeviceOffset);
      mFile.write(inData, inDataOffset, inDataLength);
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

  final public void close() {
    try {
      mFile.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      close();
    } finally {
      super.finalize();
    }
  }

}

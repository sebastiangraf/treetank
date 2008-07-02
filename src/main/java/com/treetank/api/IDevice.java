package com.treetank.api;

public interface IDevice {

  public void read(
      final long inDeviceOffset,
      final int inDataOffset,
      final int inDataLength,
      final byte[] outData);

  public void write(
      final long inDeviceOffset,
      final int inDataOffset,
      final int inDataLength,
      final byte[] inData);

  public long size();

  public void close();

}

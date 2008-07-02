package com.treetank.api;

public interface IDevice {

  public byte[] read(final long offset, final int length);

  public void write(final long offset, final byte[] buffer);

  public long size();

}

package com.treetank.api;

public final class Configuration {

  private long mMaxRevision;

  public Configuration() {
    this(0);
  }

  public Configuration(final long maxRevision) {
    mMaxRevision = maxRevision;
  }

  public Configuration(final byte[] buffer, final long maxRevision) {
    mMaxRevision = maxRevision;
  }

  public final void incrementMaxRevision() {
    mMaxRevision += 1;
  }

  public final long getMaxRevision() {
    return mMaxRevision;
  }

  public final byte[] serialise() {
    final byte[] buffer = new byte[448];
    return buffer;
  }

  public final String toString() {
    return "Configuration(" + mMaxRevision + ")";
  }

}

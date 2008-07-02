package com.treetank.api;

import com.treetank.util.SerialiserTool;

public final class FragmentReference {

  private long mOffset;

  private int mLength;

  public FragmentReference() {
    this(0, 0);
  }

  public FragmentReference(final long offset, final int length) {
    mOffset = offset;
    mLength = length;
  }

  public FragmentReference(final byte[] buffer) {
    mOffset = SerialiserTool.readLong(0, buffer);
    mLength = SerialiserTool.readInt(8, buffer);
  }

  public final void setOffset(final long offset) {
    mOffset = offset;
  }

  public final void setLength(final int length) {
    mLength = length;
  }

  public final long getOffset() {
    return mOffset;
  }

  public final int getLength() {
    return mLength;
  }

  public final byte[] serialise() {
    final byte[] buffer = new byte[12];
    SerialiserTool.writeLong(0, buffer, mOffset);
    SerialiserTool.writeInt(8, buffer, mLength);
    return buffer;
  }

  public final String toString() {
    return "FragmentReference(" + mOffset + ", " + mLength + ")";
  }

}

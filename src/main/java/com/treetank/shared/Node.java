/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * NO permission to use, copy, modify, and/or distribute this software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package com.treetank.shared;

import com.treetank.api.INode;

public final class Node implements INode {

  private int mType;

  private int mOffset;

  private long mRevision;

  public Node() {
    this(0, 0, 0);
  }

  public Node(final int type, final int offset, final long revision) {
    mType = type;
    mOffset = offset;
    mRevision = revision;
  }

  public final void setType(final int type) {
    mType = type;
  }

  public final void setOffset(final int offset) {
    mOffset = offset;
  }

  public final void setRevsion(final long revision) {
    mRevision = revision;
  }

  public final int getType() {
    return mType;
  }

  public final int getOffset() {
    return mOffset;
  }

  public final long getRevision() {
    return mRevision;
  }

  public final void serialise(final ByteArrayWriter buffer) {
    buffer.writeVarInt(mType);
    buffer.writeVarInt(mOffset);
    buffer.writeVarLong(mRevision);
  }

  public final void deserialise(final ByteArrayReader buffer) {
    mType = buffer.readVarInt();
    mOffset = buffer.readVarInt();
    mRevision = buffer.readVarLong();
  }

  public final String toString() {
    return "Node(" + mType + ", " + mOffset + ", " + mRevision + ")";
  }

}

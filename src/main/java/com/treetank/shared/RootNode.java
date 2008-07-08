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

public final class RootNode extends Node implements INode {

  public static final int TYPE = 1;

  private String mAuthor;

  private String mComment;

  public RootNode() {
    this(0, 0, null, null);
  }

  public RootNode(
      final int offset,
      final long revision,
      final String author,
      final String comment) {
    super(offset, revision);
    mAuthor = author;
    mComment = comment;
  }

  public final void setAuthor(final String author) {
    mAuthor = author;
  }

  public final void setComment(final String comment) {
    mComment = comment;
  }

  public final String getAuthor() {
    return mAuthor;
  }

  public final String getComment() {
    return mComment;
  }

  public final void serialise(final ByteArrayWriter buffer) {
    super.serialise(buffer);
    buffer.writeUtf(mAuthor);
    buffer.writeUtf(mComment);
  }

  public final void deserialise(final ByteArrayReader buffer) {
    super.deserialise(buffer);
    mAuthor = buffer.readUtf();
    mComment = buffer.readUtf();
  }

  public final int getType() {
    return TYPE;
  }

  public final String toString() {
    return "RootNode(" + getOffset() + ", " + getRevision() + ")";
  }

}

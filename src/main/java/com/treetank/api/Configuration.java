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

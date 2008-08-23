/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
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

package org.treetank.pagelayer;

import java.nio.ByteBuffer;

import edu.emory.mathcs.backport.java.util.concurrent.ArrayBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.BlockingQueue;

public final class NativeTreeTank implements ICompression {

  private static final BlockingQueue queue = new ArrayBlockingQueue(8);

  static {
    try {
      queue.put(0);
      queue.put(1);
      queue.put(2);
      queue.put(3);
      queue.put(4);
      queue.put(5);
      queue.put(6);
      queue.put(7);
    } catch (Exception e) {
      throw new RuntimeException("Could not allocate native compression cores.");
    }
  }

  public final int crypt(final ByteBuffer reference, final ByteBuffer buffer) {
    int error = 0;
    int core = -1;
    try {
      core = (Integer) queue.take();
      error = write(core, reference, buffer);
    } catch (Exception e) {
      return 1;
    } finally {
      try {
        if (core != -1) {
          queue.put(core);
        }
      } catch (Exception ie) {
        throw new RuntimeException(ie);
      }
    }
    return error;
  }

  public final byte[] decompress(final byte[] buffer, final int length) {
    byte[] data = null;
    int core = -1;
    try {
      core = (Integer) queue.take();
      data = read(core, buffer, length);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        if (core != -1) {
          queue.put(core);
        }
      } catch (Exception ie) {
        throw new RuntimeException(ie);
      }
    }
    return data;
  }

  private native int write(
      final int core,
      final  ByteBuffer reference,
      final ByteBuffer buffer);

  private native byte[] read(
      final int core,
      final byte[] buffer,
      final int length);

}

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
      queue.put((byte) 1);
      queue.put((byte) 2);
      queue.put((byte) 3);
      queue.put((byte) 4);
      queue.put((byte) 5);
      queue.put((byte) 6);
      queue.put((byte) 7);
      queue.put((byte) 8);
    } catch (Exception e) {
      throw new RuntimeException("Could not allocate native compression cores.");
    }
  }

  public final short crypt(final short length, final ByteBuffer buffer) {
    short resultLength = (short) 0;
    byte core = (byte) 0;
    try {
      core = (Byte) queue.take();
      resultLength = syscall((byte) 1, core, length, buffer);
    } catch (Exception e) {
      return 0;
    } finally {
      try {
        if (core != -1) {
          queue.put(core);
        }
      } catch (Exception ie) {
        throw new RuntimeException(ie);
      }
    }
    return resultLength;
  }

  public final short decrypt(final short length, final ByteBuffer buffer) {
    short resultLength = (short) 0;
    byte core = (byte) 0;
    try {
      core = (Byte) queue.take();
      resultLength = syscall((byte) 1, core, length, buffer);
    } catch (Exception e) {
      return 0;
    } finally {
      try {
        if (core != -1) {
          queue.put(core);
        }
      } catch (Exception ie) {
        throw new RuntimeException(ie);
      }
    }
    return resultLength;
  }

  /**
   * Call to sys_treetank.
   * 
   * @param tank TreeTank identifier in [1, ..., 256].
   * @param operation Operation identifier in [1, ..., 256].
   * @param length Length of input buffer in [1, ..., 32767].
   * @param buffer Direct data exchange buffer.
   * @return Length of output buffer in [1, ..., 32767] or error in [0].
   */
  private native short syscall(
      final byte tank,
      final byte operation,
      final short length,
      final ByteBuffer buffer);

}

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

package org.treetank.pagelayer;

import java.nio.ByteBuffer;

import edu.emory.mathcs.backport.java.util.concurrent.ArrayBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.BlockingQueue;

public final class CryptoNativeImpl implements ICrypto {

  private static final BlockingQueue coreQueue = new ArrayBlockingQueue(8);

  static {
    try {
      for (int i = 1; i < 15; i++) {
        coreQueue.put((byte) (i << 0x4));
      }
    } catch (Exception e) {
      coreQueue.clear();
      throw new RuntimeException("Could not allocate native compression cores.");
    }
  }

  public final short crypt(final short length, final ByteBuffer buffer) {
    short resultLength = (short) 0x0;
    byte tank = (byte) 0x1;
    byte command = 0x1;
    byte core = 0x0;
    try {
      core = (Byte) coreQueue.take();
      resultLength = syscall(tank, (byte) (command | core), length, buffer);
    } catch (Exception e) {
      return 0x0;
    } finally {
      try {
        if (core != 0x0) {
          coreQueue.put(core);
        }
      } catch (Exception ie) {
        throw new RuntimeException(ie);
      }
    }
    return resultLength;
  }

  public final short decrypt(final short length, final ByteBuffer buffer) {
    short resultLength = (short) 0x0;
    byte tank = (byte) 0x1;
    byte command = 0x2;
    byte core = 0x0;
    try {
      core = (Byte) coreQueue.take();
      resultLength = syscall(tank, (byte) (command | core), length, buffer);
    } catch (Exception e) {
      return 0x0;
    } finally {
      try {
        if (core != 0x0) {
          coreQueue.put(core);
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
   *        <code>(operation & 0xF)</code> = command in [1, ..., 16].
   *        <code>(operation >> 0x4)</code> = core in [1, ..., 16].
   * @param length Length of input buffer in [1, ..., 32767].
   * @param buffer Direct data exchange buffer != NULL.
   * @return Length of output buffer in [1, ..., 32767] or error in [0].
   */
  private native short syscall(
      final byte tank,
      final byte operation,
      final short length,
      final ByteBuffer buffer);

}

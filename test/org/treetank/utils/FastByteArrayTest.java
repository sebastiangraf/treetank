/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package org.treetank.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FastByteArrayTest {

  @Test
  public void testSerializeDeserialize() throws Exception {

    final FastByteArrayWriter out = new FastByteArrayWriter();
    out.writeBoolean(true);
    out.writeInt(4);
    out.writePseudoInt(3456345);
    out.writePseudoInt(-1);
    out.writePseudoVarSizeInt(2^28-1);
    out.writePseudoVarSizeInt(-2^28);
    out.writePseudoLong(Integer.MAX_VALUE);
    out.writePseudoLong(Integer.MIN_VALUE);
    out.writeLong(7L);
    out.writeByte((byte) 7);

    final byte[] bytes = out.getBytes();
    final FastByteArrayReader in = new FastByteArrayReader(bytes);

    assertEquals(true, in.readBoolean());
    assertEquals(4, in.readInt());
    assertEquals(3456345, in.readPseudoInt());
    assertEquals(-1, in.readPseudoInt());
    assertEquals(2^28-1, in.readPseudoVarSizeInt());
    assertEquals(-2^28, in.readPseudoVarSizeInt());
    assertEquals(Integer.MAX_VALUE, in.readPseudoLong());
    assertEquals(Integer.MIN_VALUE, in.readPseudoLong());
    assertEquals(7L, in.readLong());
    assertEquals((byte) 7, in.readByte());

  }

}

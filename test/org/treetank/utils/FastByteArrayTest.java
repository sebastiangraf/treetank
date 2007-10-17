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
    out.writeVarInt(Integer.MAX_VALUE);
    out.writeVarInt(Integer.MIN_VALUE);
    out.writeVarLong(Long.MAX_VALUE);
    out.writeVarLong(Long.MIN_VALUE);
    out.writeLong(7L);
    out.writeByte((byte) 7);

    final byte[] bytes = out.getBytes();
    final FastByteArrayReader in = new FastByteArrayReader(bytes);

    assertEquals(true, in.readBoolean());
    assertEquals(4, in.readInt());
    assertEquals(Integer.MAX_VALUE, in.readVarInt());
    assertEquals(Integer.MIN_VALUE, in.readVarInt());
    assertEquals(Long.MAX_VALUE, in.readVarLong());
    assertEquals(Long.MIN_VALUE, in.readVarLong());
    assertEquals(7L, in.readLong());
    assertEquals((byte) 7, in.readByte());

  }

}

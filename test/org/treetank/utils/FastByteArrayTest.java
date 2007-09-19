/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

public class FastByteArrayTest {

  @Test
  public void testSerializeDeserialize() throws Exception {

    final FastByteArrayWriter out = new FastByteArrayWriter();
    out.writeBoolean(true);
    out.writeInt(4);
    out.writeLong(7L);
    out.writeUTF("1");
    out.writeUTF("2");
    out.writeByte((byte) 7);
    out.writeCharArray("abc".toCharArray());

    final byte[] bytes = out.getBytes();
    final FastByteArrayReader in = new FastByteArrayReader(bytes);

    assertEquals(true, in.readBoolean());
    assertEquals(4, in.readInt());
    assertEquals(7L, in.readLong());
    assertEquals("1", in.readUTF());
    assertEquals("2", in.readUTF());
    assertEquals((byte) 7, in.readByte());
    assertEquals("abc", new String(in.readCharArray()));

  }

}

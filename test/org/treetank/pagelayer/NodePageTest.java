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

package org.treetank.pagelayer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.treetank.pagelayer.Node;
import org.treetank.pagelayer.NodePage;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;
import org.treetank.utils.IConstants;
import org.treetank.utils.UTF;


public class NodePageTest {

  @Test
  public void testSerializeDeserialize() throws Exception {
    final NodePage page1 = NodePage.create(0L);
    assertEquals(0L, page1.getNodePageKey());
    final Node node1 =
        new Node(0L, 1L, 2L, 3L, 4L, 5, 6, 7, 8, UTF.convert("foo"));
    node1.insertAttribute(22, 23, 24, new byte[0]);
    assertEquals(0L, node1.getNodeKey());
    page1.setNode(0, node1);

    final FastByteArrayWriter out = new FastByteArrayWriter();
    page1.serialize(out);

    final FastByteArrayReader in = new FastByteArrayReader(out.getBytes());

    final NodePage page2 = NodePage.read(in);
    assertEquals(0L, page2.getNode(0).getNodeKey());
    assertEquals(1L, page2.getNode(0).getParentKey());
    assertEquals(2L, page2.getNode(0).getFirstChildKey());
    assertEquals(3L, page2.getNode(0).getLeftSiblingKey());
    assertEquals(4L, page2.getNode(0).getRightSiblingKey());
    assertEquals(0L, page2.getNode(0).getChildCount());
    assertEquals(1, page2.getNode(0).getAttributeCount());
    assertEquals(1L, page2.getNode(0).getAttribute(0).getNodeKey());
    assertEquals(0L, page2.getNode(0).getAttribute(0).getParentKey());
    assertEquals(22, page2.getNode(0).getAttribute(0).getLocalPartKey());
    assertEquals(23, page2.getNode(0).getAttribute(0).getURIKey());
    assertEquals(24, page2.getNode(0).getAttribute(0).getPrefixKey());
    assertEquals(5, page2.getNode(0).getKind());
    assertEquals(6, page2.getNode(0).getLocalPartKey());
    assertEquals(7, page2.getNode(0).getURIKey());
    assertEquals(8, page2.getNode(0).getPrefixKey());
    assertEquals("foo", new String(
        page2.getNode(0).getValue(),
        IConstants.ENCODING));

  }

}

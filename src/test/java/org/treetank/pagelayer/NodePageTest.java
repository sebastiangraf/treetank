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

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.treetank.nodelayer.ElementNode;

public class NodePageTest {

  @Test
  public void testSerializeDeserialize() {
    final NodePage page1 = new NodePage(0L);
    assertEquals(0L, page1.getNodePageKey());
    final ElementNode node1 = new ElementNode(0L, 1L, 2L, 3L, 4L, 6, 7, 0);
    node1.insertAttribute(22, 23, 25, new byte[0]);
    assertEquals(0L, node1.getNodeKey());
    page1.setNode(0, node1);

    final ByteBuffer out = ByteBuffer.allocate(1000);
    page1.serialize(out);

    out.position(0);
    final NodePage page2 = new NodePage(out, 0L);
    assertEquals(0L, page2.getNode(0).getNodeKey());
    assertEquals(1L, page2.getNode(0).getParentKey());
    assertEquals(2L, page2.getNode(0).getFirstChildKey());
    assertEquals(3L, page2.getNode(0).getLeftSiblingKey());
    assertEquals(4L, page2.getNode(0).getRightSiblingKey());
    assertEquals(0L, page2.getNode(0).getChildCount());
    assertEquals(1, page2.getNode(0).getAttributeCount());
    assertEquals(0L, page2.getNode(0).getAttribute(0).getNodeKey());
    assertEquals(0L, page2.getNode(0).getAttribute(0).getParentKey());
    assertEquals(22, page2.getNode(0).getAttribute(0).getNameKey());
    assertEquals(23, page2.getNode(0).getAttribute(0).getURIKey());
    assertEquals(25, page2.getNode(0).getAttribute(0).getTypeKey());
    assertEquals(6, page2.getNode(0).getNameKey());
    assertEquals(7, page2.getNode(0).getURIKey());

  }

}

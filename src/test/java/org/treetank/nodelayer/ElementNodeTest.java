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

package org.treetank.nodelayer;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.treetank.api.IReadTransaction;

public class ElementNodeTest {

  @Test
  public void testElementNode() {

    // Create empty node.
    final AbstractNode node1 =
        new ElementNode(13L, 14L, 15L, 16L, 17L, 18, 19, 0);
    final ByteBuffer out = ByteBuffer.allocate(1000);

    // Modify it.
    node1.incrementChildCount();
    node1.decrementChildCount();
    node1.insertAttribute(21, 22, 27, new byte[] { (byte) 24, (byte) 24 });
    node1.insertNamespace(25, 26);

    // Serialize and deserialize node.
    node1.serialize(out);
    out.position(0);
    final AbstractNode node2 = new ElementNode(13L, out);

    // Clone node.
    final AbstractNode node3 = node2; //new ElementNode(node2);

    // Now compare.
    assertEquals(13L, node3.getNodeKey());
    assertEquals(14L, node3.getParentKey());
    assertEquals(15L, node3.getFirstChildKey());
    assertEquals(16L, node3.getLeftSiblingKey());
    assertEquals(17L, node3.getRightSiblingKey());
    assertEquals(0, node3.getChildCount());
    assertEquals(1, node3.getAttributeCount());
    assertEquals(1, node3.getNamespaceCount());
    assertEquals(18, node3.getNameKey());
    assertEquals(19, node3.getURIKey());
    assertEquals(null, node3.getRawValue());
    assertEquals(IReadTransaction.ELEMENT_KIND, node3.getKind());
    assertEquals(true, node3.hasFirstChild());
    assertEquals(true, node3.hasParent());
    assertEquals(true, node3.hasLeftSibling());
    assertEquals(true, node3.hasRightSibling());
    assertEquals(false, node3.isAttribute());
    assertEquals(false, node3.isDocumentRoot());
    assertEquals(true, node3.isElement());
    assertEquals(false, node3.isFullText());
    assertEquals(false, node3.isFullTextLeaf());
    assertEquals(false, node3.isFullTextRoot());
    assertEquals(false, node3.isText());

    assertEquals(13L, node3.getAttribute(0).getNodeKey());
    assertEquals(21, node3.getAttribute(0).getNameKey());
    assertEquals(22, node3.getAttribute(0).getURIKey());
    assertEquals(27, node3.getAttribute(0).getTypeKey());
    assertEquals(2, node3.getAttribute(0).getRawValue().length);

    assertEquals(13L, node3.getNamespace(0).getNodeKey());
    assertEquals(25, node3.getNamespace(0).getURIKey());
    assertEquals(26, node3.getNamespace(0).getPrefixKey());

  }

}

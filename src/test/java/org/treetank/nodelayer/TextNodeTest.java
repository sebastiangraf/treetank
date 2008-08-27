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

public class TextNodeTest {

  @Test
  public void testTextRootNode() {

    // Create empty node.
    final AbstractNode node1 =
        new TextNode(
            13L,
            14L,
            15L,
            16L,
            19,
            new byte[] { (byte) 17, (byte) 18 });
    final ByteBuffer out = ByteBuffer.allocate(1000);

    // Modify it.
    node1.incrementChildCount();
    node1.decrementChildCount();

    // Serialize and deserialize node.
    node1.serialize(out);
    out.position(0);
    final AbstractNode node2 = new TextNode(13L, out);

    // Clone node.
    final AbstractNode node3 = new TextNode(node2);

    // Now compare.
    assertEquals(13L, node3.getNodeKey());
    assertEquals(14L, node3.getParentKey());
    assertEquals(IReadTransaction.NULL_NODE_KEY, node3.getFirstChildKey());
    assertEquals(15L, node3.getLeftSiblingKey());
    assertEquals(16L, node3.getRightSiblingKey());
    assertEquals(0L, node3.getChildCount());
    assertEquals(0, node3.getAttributeCount());
    assertEquals(0, node3.getNamespaceCount());
    assertEquals(19, node3.getTypeKey());
    assertEquals(IReadTransaction.NULL_NAME_KEY, node3.getNameKey());
    assertEquals(IReadTransaction.NULL_NAME_KEY, node3.getURIKey());
    assertEquals(IReadTransaction.NULL_NAME_KEY, node3.getNameKey());
    assertEquals(2, node3.getRawValue().length);
    assertEquals(IReadTransaction.TEXT_KIND, node3.getKind());
    assertEquals(false, node3.hasFirstChild());
    assertEquals(true, node3.hasParent());
    assertEquals(true, node3.hasLeftSibling());
    assertEquals(true, node3.hasRightSibling());
    assertEquals(false, node3.isAttribute());
    assertEquals(false, node3.isDocumentRoot());
    assertEquals(false, node3.isElement());
    assertEquals(true, node3.isText());

  }

}

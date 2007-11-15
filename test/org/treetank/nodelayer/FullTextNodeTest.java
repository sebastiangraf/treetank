/*
 * Copyright (c) 2007, Marc Kramis
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

import org.junit.Test;
import org.treetank.api.IConstants;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

public class FullTextNodeTest {

  @Test
  public void testFullTextNode() {

    // Create empty node.
    final AbstractNode node1 = new FullTextNode(13L, 14L, 15L, 16L, 17L, 19);
    final FastByteArrayWriter out = new FastByteArrayWriter();

    // Modify it.
    node1.incrementChildCount();
    node1.decrementChildCount();

    // Serialize and deserialize node.
    node1.serialize(out);
    final FastByteArrayReader in = new FastByteArrayReader(out.getBytes());
    final AbstractNode node2 = new FullTextNode(13L, in);

    // Clone node.
    final AbstractNode node3 = new FullTextNode(node2);

    // Now compare.
    assertEquals(13L, node3.getNodeKey());
    assertEquals(14L, node3.getParentKey());
    assertEquals(15L, node3.getFirstChildKey());
    assertEquals(16L, node3.getLeftSiblingKey());
    assertEquals(17L, node3.getRightSiblingKey());
    assertEquals(0, node3.getChildCount());
    assertEquals(0, node3.getAttributeCount());
    assertEquals(0, node3.getNamespaceCount());
    assertEquals(19, node3.getLocalPartKey());
    assertEquals(IConstants.NULL_NAME, node3.getURIKey());
    assertEquals(IConstants.NULL_NAME, node3.getPrefixKey());
    assertEquals(null, node3.getValue());
    assertEquals(IConstants.FULLTEXT, node3.getKind());
    assertEquals(true, node3.hasFirstChild());
    assertEquals(true, node3.hasParent());
    assertEquals(true, node3.hasLeftSibling());
    assertEquals(true, node3.hasRightSibling());
    assertEquals(false, node3.isAttribute());
    assertEquals(false, node3.isDocumentRoot());
    assertEquals(false, node3.isElement());
    assertEquals(true, node3.isFullText());
    assertEquals(false, node3.isFullTextLeaf());
    assertEquals(false, node3.isFullTextRoot());
    assertEquals(false, node3.isText());

  }

}

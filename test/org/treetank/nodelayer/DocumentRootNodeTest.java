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
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

public class DocumentRootNodeTest {

  @Test
  public void testDocumentRootNode() {

    // Create empty node.
    final AbstractNode node1 = new DocumentRootNode();
    final FastByteArrayWriter out = new FastByteArrayWriter();

    // Modify it.
    node1.incrementChildCount();
    node1.decrementChildCount();

    // Serialize and deserialize node.
    node1.serialize(out);
    final FastByteArrayReader in = new FastByteArrayReader(out.getBytes());
    final AbstractNode node2 = new DocumentRootNode(in);

    // Clone node.
    final AbstractNode node3 = new DocumentRootNode(node2);

    // Now compare.
    assertEquals(IReadTransaction.DOCUMENT_ROOT_KEY, node3.getNodeKey());
    assertEquals(IReadTransaction.NULL_NODE_KEY, node3.getParentKey());
    assertEquals(IReadTransaction.NULL_NODE_KEY, node3.getFirstChildKey());
    assertEquals(IReadTransaction.NULL_NODE_KEY, node3.getLeftSiblingKey());
    assertEquals(IReadTransaction.NULL_NODE_KEY, node3.getRightSiblingKey());
    assertEquals(0L, node3.getChildCount());
    assertEquals(0, node3.getAttributeCount());
    assertEquals(0, node3.getNamespaceCount());
    assertEquals(IReadTransaction.NULL_NAME_KEY, node3.getNameKey());
    assertEquals(IReadTransaction.NULL_NAME_KEY, node3.getURIKey());
    assertEquals(IReadTransaction.NULL_NAME_KEY, node3.getPrefixKey());
    assertEquals(null, node3.getValue());
    assertEquals(IReadTransaction.DOCUMENT_ROOT_KIND, node3.getKind());
    assertEquals(false, node3.hasFirstChild());
    assertEquals(false, node3.hasParent());
    assertEquals(false, node3.hasLeftSibling());
    assertEquals(false, node3.hasRightSibling());
    assertEquals(false, node3.isAttribute());
    assertEquals(true, node3.isDocumentRoot());
    assertEquals(false, node3.isElement());
    assertEquals(false, node3.isFullText());
    assertEquals(false, node3.isFullTextLeaf());
    assertEquals(false, node3.isFullTextRoot());
    assertEquals(false, node3.isText());

  }

}

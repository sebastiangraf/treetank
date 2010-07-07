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
 * $Id: ElementNodeTest.java 4424 2008-08-28 09:15:01Z kramis $
 */

package com.treetank.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import org.junit.Test;

import com.treetank.io.file.ByteBufferSinkAndSource;

public class ElementNodeTest {

    @Test
    public void testElementNode() {

        final long[] data = ElementNode.createData(13, 14, 16, 17, 12, 1, 18,
                19, 20);

        // Create empty node.
        final ElementNode node1 = new ElementNode(data, new ArrayList<Long>(0),
                new ArrayList<Long>(0));
        node1.insertAttribute(97);
        node1.insertAttribute(98);
        node1.insertNamespace(99);
        node1.insertNamespace(100);
        check(node1);

        // Serialize and deserialize node.
        final ByteBufferSinkAndSource out = new ByteBufferSinkAndSource();
        node1.serialize(out);
        out.position(0);
        final ElementNode node2 = (ElementNode) ENodes.ELEMENT_KIND
                .createNodeFromPersistence(out);
        check(node2);

        // Clone node.
        final ElementNode node3 = (ElementNode) node2.clone();
        check(node3);
    }

    private final static void check(final ElementNode node) {
        // Now compare.
        assertEquals(13L, node.getNodeKey());
        assertEquals(14L, node.getParentKey());
        assertEquals(12L, node.getFirstChildKey());
        assertEquals(16L, node.getLeftSiblingKey());
        assertEquals(17L, node.getRightSiblingKey());
        assertEquals(1, node.getChildCount());
        assertEquals(2, node.getAttributeCount());
        assertEquals(2, node.getNamespaceCount());
        assertEquals(18, node.getNameKey());
        assertEquals(19, node.getURIKey());
        assertNull(null, node.getRawValue());
        assertEquals(20, node.getTypeKey());
        assertEquals(ENodes.ELEMENT_KIND, node.getKind());
        assertEquals(true, node.hasFirstChild());
        assertEquals(true, node.hasParent());
        assertEquals(true, node.hasLeftSibling());
        assertEquals(true, node.hasRightSibling());
        assertEquals(97L, node.getAttributeKey(0));
        assertEquals(98L, node.getAttributeKey(1));
        assertEquals(99L, node.getNamespaceKey(0));
        assertEquals(100L, node.getNamespaceKey(1));
    }

}

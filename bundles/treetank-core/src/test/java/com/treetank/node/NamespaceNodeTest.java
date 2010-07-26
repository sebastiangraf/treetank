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
 * $Id: NamespaceNodeTest.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.treetank.io.file.ByteBufferSinkAndSource;

public class NamespaceNodeTest {

    @Test
    public void testNamespaceNode() {

        final NamespaceNode node1 = (NamespaceNode)NamespaceNode.createData(99, 13, 14, 15);
        // Create empty node.

        // Serialize and deserialize node.
        final ByteBufferSinkAndSource out = new ByteBufferSinkAndSource();
        node1.serialize(out);
        out.position(0);
        final NamespaceNode node2 = (NamespaceNode)ENodes.NAMESPACE_KIND.createNodeFromPersistence(out);
        check(node2);

        // Clone node.
        final NamespaceNode node3 = (NamespaceNode)node2.clone();
        check(node3);

    }

    private final static void check(final NamespaceNode node) {
        // Now compare.
        assertEquals(99L, node.getNodeKey());
        assertEquals(13L, node.getParentKey());

        assertEquals(14, node.getURIKey());
        assertEquals(15, node.getNameKey());
        assertNull(node.getRawValue());
        assertEquals(ENodes.NAMESPACE_KIND, node.getKind());
        assertEquals(true, node.hasParent());
    }

    @Test
    public void testHashCode() {
        final NamespaceNode node = (NamespaceNode)NamespaceNode.createData(99L, 13L, 14, 15);
        final NamespaceNode node2 = (NamespaceNode)NamespaceNode.createData(99L, 13L, 14, 15);
        final NamespaceNode node3 = (NamespaceNode)NamespaceNode.createData(100L, 15L, 12, 16);

        assertEquals(node2.hashCode(), node.hashCode());
        assertTrue(node2.equals(node));
        assertFalse(node3.equals(node));
        assertFalse(node3.equals(node2));

    }

}

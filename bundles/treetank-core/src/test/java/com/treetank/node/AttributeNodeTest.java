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
 * $Id: AttributeNodeTest.java 4413 2008-08-27 16:59:32Z kramis $
 */

package com.treetank.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.treetank.io.file.ByteBufferSinkAndSource;

public class AttributeNodeTest {

    @Test
    public void testAttributeNode() {

        final long[] data = AttributeNode.createData(99, 13, 14, 15, 19);
        final byte[] value = { (byte) 17, (byte) 18 };

        // Create empty node.
        final AttributeNode node1 = new AttributeNode(data, value);
        check(node1);

        // Serialize and deserialize node.
        final ByteBufferSinkAndSource out = new ByteBufferSinkAndSource();
        node1.serialize(out);
        out.position(0);
        final AttributeNode node2 = (AttributeNode) ENodes.ATTRIBUTE_KIND
                .createNodeFromPersistence(out);
        check(node2);

        // Clone node.
        final AttributeNode node3 = (AttributeNode) node2.clone();
        check(node3);

    }

    private final static void check(final AttributeNode node) {
        // Now compare.
        assertEquals(99L, node.getNodeKey());
        assertEquals(13L, node.getParentKey());

        assertEquals(14, node.getNameKey());
        assertEquals(15, node.getURIKey());
        assertEquals(19, node.getTypeKey());
        assertEquals(2, node.getRawValue().length);
        assertEquals(ENodes.ATTRIBUTE_KIND, node.getKind());
        assertEquals(true, node.hasParent());
        assertEquals(ENodes.ATTRIBUTE_KIND, node.getKind());
    }

}

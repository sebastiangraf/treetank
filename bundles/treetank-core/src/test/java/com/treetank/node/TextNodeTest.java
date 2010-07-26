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
 * $Id: TextNodeTest.java 4424 2008-08-28 09:15:01Z kramis $
 */

package com.treetank.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.treetank.io.file.ByteBufferSinkAndSource;
import com.treetank.settings.EFixed;

public class TextNodeTest {

    @Test
    public void testTextRootNode() {

        // Create empty node.
        final byte[] value = {
            (byte)17, (byte)18
        };
        final TextNode node1 = (TextNode)TextNode.createData(13, 14, 15, 16, 19, value);
        check(node1);

        // Serialize and deserialize node.
        final ByteBufferSinkAndSource out = new ByteBufferSinkAndSource();
        node1.serialize(out);
        out.position(0);
        final TextNode node2 = (TextNode)ENodes.TEXT_KIND.createNodeFromPersistence(out);
        check(node2);

        // Clone node.
        final TextNode node3 = (TextNode)node2.clone();
        check(node3);
    }

    private final static void check(final TextNode node) {

        // Now compare.
        assertEquals(13L, node.getNodeKey());
        assertEquals(14L, node.getParentKey());
        assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(), node.getFirstChildKey());
        assertEquals(15L, node.getLeftSiblingKey());
        assertEquals(16L, node.getRightSiblingKey());
        assertEquals(19, node.getTypeKey());
        assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(), node.getNameKey());
        assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(), node.getURIKey());
        assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(), node.getNameKey());
        assertEquals(2, node.getRawValue().length);
        assertEquals(ENodes.TEXT_KIND, node.getKind());
        assertEquals(false, node.hasFirstChild());
        assertEquals(true, node.hasParent());
        assertEquals(true, node.hasLeftSibling());
        assertEquals(true, node.hasRightSibling());
    }

    @Test
    public void testHashCode() {
        final byte[] value = {
            (byte)17, (byte)18
        };
        final byte[] value2 = {
            (byte)19, (byte)20
        };
        final TextNode node = (TextNode)TextNode.createData(99, 13, 14, 15, 19, value);
        final TextNode node2 = (TextNode)TextNode.createData(99, 13, 14, 15, 19, value);
        final TextNode node3 = (TextNode)TextNode.createData(99, 13, 14, 15, 19, value2);
        final TextNode node4 = (TextNode)TextNode.createData(100, 15, 12, 16, 123, value);

        assertEquals(node2.hashCode(), node.hashCode());
        assertTrue(node2.equals(node));
        assertFalse(node3.equals(node));
        assertFalse(node4.equals(node));

    }

}

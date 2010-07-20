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
 * $Id: DocumentRootNodeTest.java 4424 2008-08-28 09:15:01Z kramis $
 */

package com.treetank.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.treetank.io.file.ByteBufferSinkAndSource;
import com.treetank.settings.EFixed;

public class DocumentRootNodeTest {

    @Test
    public void testDocumentRootNode() {

        // Create empty node.
        final DocumentRootNode node1 = DocumentRootNode.createData();
        check(node1);

        // Serialize and deserialize node.
        final ByteBufferSinkAndSource out = new ByteBufferSinkAndSource();
        node1.serialize(out);
        out.position(0);
        final DocumentRootNode node2 = (DocumentRootNode) ENodes.ROOT_KIND
                .createNodeFromPersistence(out);
        check(node2);

        // Clone node.
        final DocumentRootNode node3 = (DocumentRootNode) node2.clone();
        check(node3);

    }

    private final static void check(final DocumentRootNode node) {
        // Now compare.
        assertEquals(EFixed.ROOT_NODE_KEY.getStandardProperty(),
                node.getNodeKey());
        assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(),
                node.getParentKey());
        assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(),
                node.getFirstChildKey());
        assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(),
                node.getLeftSiblingKey());
        assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(),
                node.getRightSiblingKey());
        assertEquals(0L, node.getChildCount());
        assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(),
                node.getNameKey());
        assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(),
                node.getURIKey());
        assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(),
                node.getNameKey());
        assertEquals(null, node.getRawValue());
        assertEquals(ENodes.ROOT_KIND, node.getKind());

    }

    @Test
    public void testHashCode() {
        final long[] data = { 99, 13, 14, 15, 12, 34 };
        final long[] data2 = { 100, 15, 12, 16, 54, 63 };

        final int[] intData = { 123 };
        final int[] intData2 = { 23 };

        final DocumentRootNode node = new DocumentRootNode(data, intData);
        final DocumentRootNode node2 = new DocumentRootNode(data2, intData2);
        final DocumentRootNode node3 = new DocumentRootNode(data, intData);

        assertEquals(node3.hashCode(), node.hashCode());
        assertTrue(node3.equals(node));
        assertFalse(node3.equals(node2));

    }

}

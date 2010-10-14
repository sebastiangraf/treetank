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
 * $Id: NodePageTest.java 4424 2008-08-28 09:15:01Z kramis $
 */

package com.treetank.page;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.treetank.io.file.ByteBufferSinkAndSource;
import com.treetank.node.ElementNode;

public class NodePageTest {

    @Test
    public void testSerializeDeserialize() {
        final NodePage page1 = new NodePage(0L, 0L);
        assertEquals(0L, page1.getNodePageKey());
        final ElementNode node1 = (ElementNode)ElementNode.createData(0, 1, 3, 4, 12, 1, 6, 7, 8, 9);
        node1.insertAttribute(88L);
        node1.insertAttribute(87L);
        node1.insertNamespace(99L);
        node1.insertNamespace(98L);
        assertEquals(0L, node1.getNodeKey());
        page1.setNode(0, node1);

        final ByteBufferSinkAndSource out = new ByteBufferSinkAndSource();
        PagePersistenter.serializePage(out, page1);
        final int position = out.position();

        out.position(0);
        final NodePage page2 = (NodePage)PagePersistenter.createPage(out);
        assertEquals(position, out.position());
        assertEquals(0L, page2.getNode(0).getNodeKey());
        assertEquals(1L, page2.getNode(0).getParentKey());
        assertEquals(12L, ((ElementNode)page2.getNode(0)).getFirstChildKey());
        assertEquals(3L, ((ElementNode)page2.getNode(0)).getLeftSiblingKey());
        assertEquals(4L, ((ElementNode)page2.getNode(0)).getRightSiblingKey());
        assertEquals(1, ((ElementNode)page2.getNode(0)).getChildCount());
        assertEquals(2, ((ElementNode)page2.getNode(0)).getAttributeCount());
        assertEquals(2, ((ElementNode)page2.getNode(0)).getNamespaceCount());
        assertEquals(88L, ((ElementNode)page2.getNode(0)).getAttributeKey(0));
        assertEquals(87L, ((ElementNode)page2.getNode(0)).getAttributeKey(1));
        assertEquals(99L, ((ElementNode)page2.getNode(0)).getNamespaceKey(0));
        assertEquals(98L, ((ElementNode)page2.getNode(0)).getNamespaceKey(1));
        assertEquals(6, page2.getNode(0).getNameKey());
        assertEquals(7, page2.getNode(0).getURIKey());
        assertEquals(8, page2.getNode(0).getTypeKey());

    }
}

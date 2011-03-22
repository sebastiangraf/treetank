/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.page;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.treetank.io.file.ByteBufferSinkAndSource;
import org.treetank.node.ElementNode;
import org.treetank.page.NodePage;
import org.treetank.page.PagePersistenter;

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
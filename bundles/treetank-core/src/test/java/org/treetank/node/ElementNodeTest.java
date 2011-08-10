/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

package org.treetank.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.treetank.io.file.ByteBufferSinkAndSource;

public class ElementNodeTest {

    @Test
    public void testElementNode() {

        final ElementNode node1 = (ElementNode)ElementNode.createData(13, 14, 16, 17, 12, 1, 18, 19, 20, 21);

        // Create empty node.
        node1.insertAttribute(97);
        node1.insertAttribute(98);
        node1.insertNamespace(99);
        node1.insertNamespace(100);
        check(node1);

        // Serialize and deserialize node.
        final ByteBufferSinkAndSource out = new ByteBufferSinkAndSource();
        node1.serialize(out);
        out.position(0);
        final ElementNode node2 = (ElementNode)ENodes.ELEMENT_KIND.createNodeFromPersistence(out);
        check(node2);

        // Clone node.
        final ElementNode node3 = (ElementNode)node2.clone();
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

    @Test
    public void testHashCode() {
        final ElementNode node = (ElementNode)ElementNode.createData(0, 0, 0, 0, 0, 0, 18, 19, 20, 21);
        final ElementNode node2 = (ElementNode)ElementNode.createData(1, 1, 1, 1, 1, 1, 19, 20, 21, 22);

        final ElementNode node3 = (ElementNode)ElementNode.createData(2, 2, 2, 2, 2, 2, 18, 19, 20, 21);

        assertEquals(node3.hashCode(), node.hashCode());
        assertTrue(node3.equals(node));
        assertFalse(node2.equals(node3));
    }

}

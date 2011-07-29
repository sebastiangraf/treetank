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

import org.treetank.io.file.ByteBufferSinkAndSource;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        final NamespaceNode node = (NamespaceNode)NamespaceNode.createData(0, 0, 14, 15);
        final NamespaceNode node2 = (NamespaceNode)NamespaceNode.createData(1, 1, 14, 15);
        final NamespaceNode node3 = (NamespaceNode)NamespaceNode.createData(2, 2, 12, 16);

        assertEquals(node2.hashCode(), node.hashCode());
        assertTrue(node2.equals(node));
        assertFalse(node3.equals(node));
        assertFalse(node3.equals(node2));

    }

}

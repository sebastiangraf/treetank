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
import static org.junit.Assert.assertTrue;

public class AttributeNodeTest {

    @Test
    public void testAttributeNode() {
        final byte[] value = {
            (byte)17, (byte)18
        };
        final AttributeNode node1 = (AttributeNode)AttributeNode.createData(99, 13, 14, 15, 19, value);

        // Create empty node.
        check(node1);

        // Serialize and deserialize node.
        final ByteBufferSinkAndSource out = new ByteBufferSinkAndSource();
        node1.serialize(out);
        out.position(0);
        final AttributeNode node2 = (AttributeNode)ENodes.ATTRIBUTE_KIND.createNodeFromPersistence(out);
        check(node2);

        // Clone node.
        final AttributeNode node3 = (AttributeNode)node2.clone();
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

    @Test
    public void testHashCode() {
        final byte[] value = {
            (byte)17, (byte)18
        };
        final byte[] value2 = {
            (byte)19, (byte)20
        };

        final AttributeNode node = (AttributeNode)AttributeNode.createData(0, 0, 14, 15, 19, value);
        final AttributeNode node2 = (AttributeNode)AttributeNode.createData(1, 1, 14, 15, 19, value);
        final AttributeNode node3 = (AttributeNode)AttributeNode.createData(2, 2, 14, 15, 19, value2);
        final AttributeNode node4 = (AttributeNode)AttributeNode.createData(3, 3, 12, 16, 123, value);

        assertEquals(node2.hashCode(), node.hashCode());
        assertTrue(node2.equals(node));
        assertFalse(node3.equals(node));
        assertFalse(node4.equals(node));
    }
}

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

package org.treetank.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.treetank.io.file.ByteBufferSinkAndSource;
import org.treetank.node.ENodes;
import org.treetank.node.TextNode;
import org.treetank.settings.EFixed;

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
        final TextNode node = (TextNode)TextNode.createData(2, 2, 2, 2, 19, value);
        final TextNode node2 = (TextNode)TextNode.createData(3, 3, 3, 3, 19, value);
        final TextNode node3 = (TextNode)TextNode.createData(4, 4, 4, 4, 19, value2);
        final TextNode node4 = (TextNode)TextNode.createData(5, 5, 5, 5, 123, value);

        assertEquals(node2.hashCode(), node.hashCode());
        assertTrue(node2.equals(node));
        assertFalse(node3.equals(node));
        assertFalse(node4.equals(node));

    }

}

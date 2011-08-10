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

import org.junit.Test;
import org.treetank.io.file.ByteBufferSinkAndSource;
import org.treetank.settings.EFixed;

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
        final DocumentRootNode node2 = (DocumentRootNode)ENodes.ROOT_KIND.createNodeFromPersistence(out);
        check(node2);

        // Clone node.
        final DocumentRootNode node3 = (DocumentRootNode)node2.clone();
        check(node3);

    }

    private final static void check(final DocumentRootNode node) {
        // Now compare.
        assertEquals(EFixed.ROOT_NODE_KEY.getStandardProperty(), node.getNodeKey());
        assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(), node.getParentKey());
        assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(), node.getFirstChildKey());
        assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(), node.getLeftSiblingKey());
        assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(), node.getRightSiblingKey());
        assertEquals(0L, node.getChildCount());
        assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(), node.getNameKey());
        assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(), node.getURIKey());
        assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(), node.getNameKey());
        assertEquals(null, node.getRawValue());
        assertEquals(ENodes.ROOT_KIND, node.getKind());

    }

    // @Test
    // public void testHashCode() {
    // final long[] data = {
    // 99, 13, 14, 15, 12, 34
    // };
    // final long[] data2 = {
    // 100, 15, 12, 16, 54, 63
    // };
    //
    // final int[] intData = {
    // 123
    // };
    // final int[] intData2 = {
    // 23
    // };
    //
    // final DocumentRootNode node = new DocumentRootNode(data, intData);
    // final DocumentRootNode node2 = new DocumentRootNode(data2, intData2);
    // final DocumentRootNode node3 = new DocumentRootNode(data, intData);
    //
    // assertEquals(node3.hashCode(), node.hashCode());
    // assertTrue(node3.equals(node));
    // assertFalse(node3.equals(node2));
    //
    // }

}

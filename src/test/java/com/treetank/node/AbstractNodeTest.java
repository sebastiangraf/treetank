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
 * $Id: AbstractNodeTest.java 4258 2008-07-14 16:45:28Z kramis $
 */

package com.treetank.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AbstractNodeTest {

    @Test
    public void testDocumentRootNode() {

        final AbstractNode node1 = new TextNode(13L, 14L, 15L, 16L, 19,
                new byte[] { (byte) 17, (byte) 18 });
        final AbstractNode node2 = new TextNode(23L, 24L, 25L, 26L, 29,
                new byte[] { (byte) 27, (byte) 28 });

        // Test hash.
        assertEquals(13, node1.hashCode());
        assertEquals(23, node2.hashCode());

        // Test equals.
        assertEquals(false, node1.equals(null));
        assertEquals(true, node1.equals(node1));
        assertEquals(false, node1.equals(node2));
        assertEquals(false, node2.equals(node1));

        // Test compare.
        assertEquals(0, node1.compareTo(node1));
        assertEquals(-1, node1.compareTo(node2));
        assertEquals(1, node2.compareTo(node1));

    }

}

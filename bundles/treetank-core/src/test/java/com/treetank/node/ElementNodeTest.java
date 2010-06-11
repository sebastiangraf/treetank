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
 * $Id: ElementNodeTest.java 4424 2008-08-28 09:15:01Z kramis $
 */

package com.treetank.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.treetank.io.file.ByteBufferSinkAndSource;
import com.treetank.settings.ENodes;

public class ElementNodeTest {

	@Test
	public void testElementNode() {

		// Create empty node.
		final AbstractNode node1 = new ElementNode(13L, 14L, 15L, 16L, 17L, 18,
				19, 0);
		final ByteBufferSinkAndSource out = new ByteBufferSinkAndSource();

		// Modify it.
		node1.incrementChildCount();
		node1.decrementChildCount();
		node1.insertAttribute(98L);
		node1.insertNamespace(99L);

		// Serialize and deserialize node.
		node1.serialize(out);
		out.position(0);
		final AbstractNode node2 = new ElementNode(out);

		// Clone node.
		final AbstractNode node3 = node2; // new ElementNode(node2);

		// Now compare.
		assertEquals(13L, node3.getNodeKey());
		assertEquals(14L, node3.getParentKey());
		assertEquals(15L, ((IStructuralNode) node3).getFirstChildKey());
		assertEquals(16L, ((IStructuralNode) node3).getLeftSiblingKey());
		assertEquals(17L, ((IStructuralNode) node3).getRightSiblingKey());
		assertEquals(0, node3.getChildCount());
		assertEquals(1, ((ElementNode) node3).getAttributeCount());
		assertEquals(1, ((ElementNode) node3).getNamespaceCount());
		assertEquals(18, node3.getNameKey());
		assertEquals(19, node3.getURIKey());
		assertEquals(null, node3.getRawValue());
		assertEquals(ENodes.ELEMENT_KIND, node3.getKind());
		assertEquals(true, ((IStructuralNode) node3).hasFirstChild());
		assertEquals(true, node3.hasParent());
		assertEquals(true, ((IStructuralNode) node3).hasLeftSibling());
		assertEquals(true, ((IStructuralNode) node3).hasRightSibling());
		assertEquals(ENodes.ELEMENT_KIND, node3.getKind());
		assertEquals(98L, ((ElementNode) node3).getAttributeKey(0));
		assertEquals(99L, ((ElementNode) node3).getNamespaceKey(0));

	}

}

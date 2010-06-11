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

import org.junit.Test;

import com.treetank.io.file.ByteBufferSinkAndSource;
import com.treetank.settings.EFixed;
import com.treetank.settings.ENodes;

public class DocumentRootNodeTest {

	@Test
	public void testDocumentRootNode() {

		// Create empty node.
		final AbstractNode node1 = new DocumentRootNode();
		final ByteBufferSinkAndSource out = new ByteBufferSinkAndSource();

		// Modify it.
		node1.incrementChildCount();
		node1.decrementChildCount();

		// Serialize and deserialize node.
		node1.serialize(out);
		out.position(0);
		final AbstractNode node2 = new DocumentRootNode(out);

		// Clone node.
		final AbstractNode node3 = new DocumentRootNode(node2);

		// Now compare.
		assertEquals(EFixed.ROOT_NODE_KEY.getStandardProperty(), node3
				.getNodeKey());
		assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(), node3
				.getParentKey());
		assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(),
				((IStructuralNode) node3).getFirstChildKey());
		assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(),
				((IStructuralNode) node3).getLeftSiblingKey());
		assertEquals(EFixed.NULL_NODE_KEY.getStandardProperty(),
				((IStructuralNode) node3).getRightSiblingKey());
		assertEquals(0L, node3.getChildCount());
		assertEquals(0, node3.getAttributeCount());
		assertEquals(0, node3.getNamespaceCount());
		assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(), node3
				.getNameKey());
		assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(), node3
				.getURIKey());
		assertEquals(EFixed.NULL_INT_KEY.getStandardProperty(), node3
				.getNameKey());
		assertEquals(null, node3.getRawValue());
		assertEquals(ENodes.ROOT_KIND, node3.getKind());
		assertEquals(false, ((IStructuralNode) node3).hasFirstChild());
		assertEquals(false, node3.hasParent());
		assertEquals(false, ((IStructuralNode) node3).hasLeftSibling());
		assertEquals(false, ((IStructuralNode) node3).hasRightSibling());
		assertEquals(ENodes.ROOT_KIND, node3.getKind());

	}

}

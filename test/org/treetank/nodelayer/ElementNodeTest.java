/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id: NodePageTest.java 3317 2007-10-29 12:45:25Z kramis $
 */

package org.treetank.nodelayer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.treetank.api.IConstants;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

public class ElementNodeTest {

  @Test
  public void testElementNode() {

    // Create empty node.
    final AbstractNode node1 =
        new ElementNode(13L, 14L, 15L, 16L, 17L, 18, 19, 20);
    final FastByteArrayWriter out = new FastByteArrayWriter();

    // Modify it.
    node1.incrementChildCount();
    node1.decrementChildCount();
    node1.insertAttribute(21, 22, 23, new byte[] { (byte) 24, (byte) 24 });
    node1.insertNamespace(25, 26);

    // Serialize and deserialize node.
    node1.serialize(out);
    final FastByteArrayReader in = new FastByteArrayReader(out.getBytes());
    final AbstractNode node2 = new ElementNode(13L, in);

    // Clone node.
    final AbstractNode node3 = node2; //new ElementNode(node2);

    // Now compare.
    assertEquals(13L, node3.getNodeKey());
    assertEquals(14L, node3.getParentKey());
    assertEquals(15L, node3.getFirstChildKey());
    assertEquals(16L, node3.getLeftSiblingKey());
    assertEquals(17L, node3.getRightSiblingKey());
    assertEquals(0, node3.getChildCount());
    assertEquals(1, node3.getAttributeCount());
    assertEquals(1, node3.getNamespaceCount());
    assertEquals(18, node3.getLocalPartKey());
    assertEquals(19, node3.getURIKey());
    assertEquals(20, node3.getPrefixKey());
    assertEquals(null, node3.getValue());
    assertEquals(IConstants.ELEMENT, node3.getKind());
    assertEquals(true, node3.hasFirstChild());
    assertEquals(true, node3.hasParent());
    assertEquals(true, node3.hasLeftSibling());
    assertEquals(true, node3.hasRightSibling());
    assertEquals(false, node3.isAttribute());
    assertEquals(false, node3.isDocumentRoot());
    assertEquals(true, node3.isElement());
    assertEquals(false, node3.isFullText());
    assertEquals(false, node3.isFullTextLeaf());
    assertEquals(false, node3.isFullTextRoot());
    assertEquals(false, node3.isText());

    assertEquals(13L, node3.getAttribute(0).getNodeKey());
    assertEquals(21, node3.getAttribute(0).getLocalPartKey());
    assertEquals(22, node3.getAttribute(0).getURIKey());
    assertEquals(23, node3.getAttribute(0).getPrefixKey());
    assertEquals(2, node3.getAttribute(0).getValue().length);

    assertEquals(13L, node3.getNamespace(0).getNodeKey());
    assertEquals(25, node3.getNamespace(0).getURIKey());
    assertEquals(26, node3.getNamespace(0).getPrefixKey());

  }

}

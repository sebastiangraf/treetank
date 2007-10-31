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

public class AttributeNodeTest {

  @Test
  public void testAttributeNode() {

    // Create empty node.
    final AbstractNode node1 =
        new AttributeNode(13L, 14, 15, 16, new byte[] {
            (byte) 17,
            (byte) 18 });

    // Modify it.
    node1.incrementChildCount();
    node1.decrementChildCount();

    // Clone node.
    final AbstractNode node2 = new AttributeNode(node1);

    // Now compare.
    assertEquals(13L, node2.getNodeKey());
    assertEquals(13L, node2.getParentKey());
    assertEquals(IConstants.NULL_KEY, node2.getFirstChildKey());
    assertEquals(IConstants.NULL_KEY, node2.getLeftSiblingKey());
    assertEquals(IConstants.NULL_KEY, node2.getRightSiblingKey());
    assertEquals(0, node2.getChildCount());
    assertEquals(0, node2.getAttributeCount());
    assertEquals(0, node2.getNamespaceCount());
    assertEquals(14, node2.getLocalPartKey());
    assertEquals(15, node2.getURIKey());
    assertEquals(16, node2.getPrefixKey());
    assertEquals(2, node2.getValue().length);
    assertEquals(IConstants.ATTRIBUTE, node2.getKind());
    assertEquals(false, node2.hasFirstChild());
    assertEquals(true, node2.hasParent());
    assertEquals(false, node2.hasLeftSibling());
    assertEquals(false, node2.hasRightSibling());
    assertEquals(true, node2.isAttribute());
    assertEquals(false, node2.isDocumentRoot());
    assertEquals(false, node2.isElement());
    assertEquals(false, node2.isFullText());
    assertEquals(false, node2.isFullTextAttribute());
    assertEquals(false, node2.isFullTextRoot());
    assertEquals(false, node2.isText());

  }

}

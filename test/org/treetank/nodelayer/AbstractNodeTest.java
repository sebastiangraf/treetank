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

public class AbstractNodeTest {

  @Test
  public void testDocumentRootNode() {

    final AbstractNode node1 =
        new TextNode(13L, 14L, 15L, 16L, new byte[] { (byte) 17, (byte) 18 });
    final AbstractNode node2 =
        new TextNode(23L, 24L, 25L, 26L, new byte[] { (byte) 27, (byte) 28 });

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

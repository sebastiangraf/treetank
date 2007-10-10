/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright (C) 2007 Marc Kramis
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
 * $Id$
 */

package org.treetank.pagelayer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.treetank.api.IConstants;

public class NodeTest {

  @Test
  public void testKeys() throws Exception {

    assertEquals(0L, Node.nodePageKey(0L));
    assertEquals(0, Node.nodePageOffset(0L));
    assertEquals(0L, Node.keyBase(0L));

    assertEquals(0L, Node.nodePageKey(1L));
    assertEquals(1, Node.nodePageOffset(1L));
    assertEquals(0L, Node.keyBase(0L));

    assertEquals(0L, Node.nodePageKey(1L));
    assertEquals(1, Node.nodePageOffset(1L));
    assertEquals(0L, Node.keyBase(0L));

    assertEquals(1L, Node.nodePageKey(1L << IConstants.NDP_NODE_COUNT_EXPONENT));
    assertEquals(0, Node
        .nodePageOffset(1L << IConstants.NDP_NODE_COUNT_EXPONENT));
    assertEquals(1L << IConstants.NDP_NODE_COUNT_EXPONENT, Node.keyBase(1L));

  }

}

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
 * $Id$
 */

package org.treetank.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StaticTreeTest {

  @Test
  public void testOffsets() throws Exception {

    int[] offsets = null;

    offsets = StaticTree.calcIndirectPageOffsets(0);
    assertEquals(0, offsets[0]);
    assertEquals(0, offsets[1]);
    assertEquals(0, offsets[2]);
    assertEquals(0, offsets[3]);
    assertEquals(0, offsets[4]);

    offsets = StaticTree.calcIndirectPageOffsets(255L);
    assertEquals(0, offsets[0]);
    assertEquals(0, offsets[1]);
    assertEquals(0, offsets[2]);
    assertEquals(0, offsets[3]);
    assertEquals(255, offsets[4]);

    offsets = StaticTree.calcIndirectPageOffsets(256L);
    assertEquals(0, offsets[0]);
    assertEquals(0, offsets[1]);
    assertEquals(0, offsets[2]);
    assertEquals(1, offsets[3]);
    assertEquals(0, offsets[4]);

  }

}

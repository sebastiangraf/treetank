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

package org.treetank.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.treetank.utils.UTF;

public class UTFTest {

  @Test
  public void test() {
    assertEquals("a", UTF.convert(UTF.convert("a")));
    assertEquals("foo", UTF.convert(UTF.convert("foo")));
    assertEquals("fö§", UTF.convert(UTF.convert("fö§")));
    assertEquals("", UTF.convert(UTF.convert("")));
  }

}

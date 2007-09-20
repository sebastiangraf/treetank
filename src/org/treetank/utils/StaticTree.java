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

public final class StaticTree {

  /**
   * Default constructor is hidden.
   * 
   */
  private StaticTree() {
    // hidden
  }

  public static final int[] test() {
    return null;
  }

  public static final int[] calcNodePageOffsets(final long key) {

    // How many levels do we have to include in the search?
    int level = 0;
    while (key - IConstants.RRP_CUMULATED_NODE_PAGE_COUNT[level] >= 0) {
      level += 1;
    }
    final int[] levels = new int[level + 1];

    // Unrolled loop for best performance.
    long tmpKey = key;
    switch (level) {
    case 0:
      levels[0] = (int) key;
      break;
    case 1:
      levels[0] = 0;
      tmpKey -= IConstants.RRP_CUMULATED_NODE_PAGE_COUNT[0];
      levels[1] = (int) (tmpKey);
      break;
    case 2:
      levels[0] = 1;
      tmpKey -= IConstants.RRP_CUMULATED_NODE_PAGE_COUNT[1];
      levels[1] = (int) (tmpKey >> IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]);
      levels[2] =
          (int) (tmpKey - (levels[1] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      break;
    case 3:
      levels[0] = 2;
      tmpKey -= IConstants.RRP_CUMULATED_NODE_PAGE_COUNT[2];
      levels[1] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1]));
      tmpKey -= levels[1] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1];
      levels[2] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      levels[3] =
          (int) (tmpKey - (levels[2] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      break;
    case 4:
      levels[0] = 3;
      tmpKey -= IConstants.RRP_CUMULATED_NODE_PAGE_COUNT[3];
      levels[1] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[2]));
      tmpKey -= levels[1] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[2];
      levels[2] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1]));
      tmpKey -= levels[2] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1];
      levels[3] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      levels[4] =
          (int) (tmpKey - (levels[3] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      break;
    case 5:
      levels[0] = 4;
      tmpKey -= IConstants.RRP_CUMULATED_NODE_PAGE_COUNT[4];
      levels[1] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[3]));
      tmpKey -= levels[1] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[3];
      levels[2] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[2]));
      tmpKey -= levels[2] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[2];
      levels[3] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1]));
      tmpKey -= levels[3] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1];
      levels[4] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      levels[5] =
          (int) (tmpKey - (levels[4] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      break;
    default:
      throw new IllegalStateException("Only 5 levels supported.");
    }

    return levels;

  }

  public static final int[] calcRevisionRootPageOffsets(final long key) {

    // How many levels do we have to include in the search?
    int level = 0;
    while (key - IConstants.UP_CUMULATED_REVISION_ROOT_PAGE_COUNT[level] >= 0) {
      level += 1;
    }
    final int[] levels = new int[level + 1];

    // Unrolled loop for best performance.
    long tmpKey = key;
    switch (level) {
    case 0:
      levels[0] = (int) key;
      break;
    case 1:
      levels[0] = 0;
      tmpKey -= IConstants.UP_CUMULATED_REVISION_ROOT_PAGE_COUNT[0];
      levels[1] = (int) (tmpKey);
      break;
    case 2:
      levels[0] = 1;
      tmpKey -= IConstants.UP_CUMULATED_REVISION_ROOT_PAGE_COUNT[1];
      levels[1] = (int) (tmpKey >> IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]);
      levels[2] =
          (int) (tmpKey - (levels[1] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      break;
    case 3:
      levels[0] = 2;
      tmpKey -= IConstants.UP_CUMULATED_REVISION_ROOT_PAGE_COUNT[2];
      levels[1] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1]));
      tmpKey -= levels[1] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1];
      levels[2] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      levels[3] =
          (int) (tmpKey - (levels[2] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      break;
    case 4:
      levels[0] = 3;
      tmpKey -= IConstants.UP_CUMULATED_REVISION_ROOT_PAGE_COUNT[3];
      levels[1] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[2]));
      tmpKey -= levels[1] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[2];
      levels[2] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1]));
      tmpKey -= levels[2] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1];
      levels[3] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      levels[4] =
          (int) (tmpKey - (levels[3] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      break;
    case 5:
      levels[0] = 4;
      tmpKey -= IConstants.UP_CUMULATED_REVISION_ROOT_PAGE_COUNT[4];
      levels[1] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[3]));
      tmpKey -= levels[1] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[3];
      levels[2] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[2]));
      tmpKey -= levels[2] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[2];
      levels[3] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1]));
      tmpKey -= levels[3] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[1];
      levels[4] =
          (int) (tmpKey >> (IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      levels[5] =
          (int) (tmpKey - (levels[4] << IConstants.IP_LEVEL_PAGE_COUNT_EXPONENT[0]));
      break;
    default:
      throw new IllegalStateException("Only 5 levels supported.");
    }

    return levels;

  }

}

/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
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

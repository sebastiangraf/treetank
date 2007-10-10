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

import org.treetank.api.IConstants;
import org.treetank.api.IPage;
import org.treetank.pagelayer.IndirectPage;
import org.treetank.pagelayer.PageCache;
import org.treetank.pagelayer.PageReference;

public final class StaticTree {

  private PageReference mStartReference;

  private PageCache mCache;

  private int[] mOffsets;

  public StaticTree(final PageReference startReference, final PageCache cache) {
    mStartReference = startReference;
    mCache = cache;
  }

  public final PageReference get(final long key) throws Exception {

    // Calculate number of levels and offsets of these levels.
    mOffsets = StaticTree.calcIndirectPageOffsets(key);

    // Indirect reference.
    PageReference reference = mStartReference;
    IPage page = null;

    // Remaining levels.
    for (int i = 0; i < mOffsets.length; i++) {
      page = mCache.dereference(reference, IConstants.INDIRECT_PAGE);
      reference = ((IndirectPage) page).getPageReference(mOffsets[i]);
    }

    return reference;

  }

  public static final int[] calcIndirectPageOffsets(final long key) {

    final int[] levels = new int[6];

    // Unrolled loop for best performance.
    long tmpKey = key;

    levels[0] = 5;
    tmpKey -= IConstants.INP_LEVEL_PAGE_COUNT[4];

    levels[1] = (int) (tmpKey >> (IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3]));
    tmpKey -= levels[1] << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[3];
    levels[2] = (int) (tmpKey >> (IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[2]));
    tmpKey -= levels[2] << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[2];
    levels[3] = (int) (tmpKey >> (IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[1]));
    tmpKey -= levels[3] << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[1];
    levels[4] = (int) (tmpKey >> (IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[0]));

    levels[5] =
        (int) (tmpKey - (levels[4] << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[0]));

    return levels;

  }

}

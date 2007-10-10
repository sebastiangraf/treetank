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

  private int[] mCurrentOffsets;

  private IPage[] mCurrentPages;

  public StaticTree(final PageReference startReference, final PageCache cache) {
    mStartReference = startReference;
    mCache = cache;
    mCurrentOffsets = new int[IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length];
    mCurrentPages = new IPage[IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length];
  }

  public final PageReference get(final long key) throws Exception {

    // Indirect reference.
    PageReference reference = mStartReference;
    IPage page = null;

    // Remaining levels.
    int levelSteps = 0;
    long levelKey = key;
    for (int i = 0; i < mCurrentOffsets.length; i++) {

      // Calculate offset of current level.
      levelSteps =
          (int) (levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i]);
      levelKey -= levelSteps << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i];

      // Fetch page from current level.
      page = mCache.dereference(reference, IConstants.INDIRECT_PAGE);
      reference = ((IndirectPage) page).getPageReference(levelSteps);
    }

    return reference;

  }

  public static final int[] calcIndirectPageOffsets(final long key) {

    final int[] levels =
        new int[IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length];

    long tmpKey = key;
    for (int i = 0; i < levels.length; i++) {
      levels[i] = (int) (tmpKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i]);
      tmpKey -= levels[i] << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i];
    }

    return levels;

  }

}

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
import org.treetank.api.IReadTransactionState;
import org.treetank.api.IWriteTransactionState;
import org.treetank.pagelayer.IndirectPage;
import org.treetank.pagelayer.PageReference;

public final class StaticTree {

  private PageReference mStartReference;

  private int[] mIndirectOffsets;

  private IndirectPage[] mIndirectPages;

  public StaticTree(final PageReference startReference) {
    mStartReference = startReference;
    mIndirectOffsets = new int[IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length];
    mIndirectPages =
        new IndirectPage[IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT.length];
    for (int i = 0; i < mIndirectOffsets.length; i++) {
      mIndirectOffsets[i] = -1;
    }
  }

  public final PageReference get(
      final IReadTransactionState state,
      final long key) throws Exception {

    // Indirect reference.
    PageReference reference = mStartReference;

    // Remaining levels.
    int levelSteps = 0;
    long levelKey = key;
    for (int i = 0; i < mIndirectOffsets.length; i++) {

      // Calculate offset of current level.
      levelSteps =
          (int) (levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i]);
      levelKey -= levelSteps << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i];

      // Fetch page from current level.
      if (levelSteps != mIndirectOffsets[i]) {
        mIndirectOffsets[i] = levelSteps;
        mIndirectPages[i] = state.dereferenceIndirectPage(reference);
      }
      reference = mIndirectPages[i].getPageReference(levelSteps);
    }

    return reference;

  }

  public final PageReference prepare(
      final IWriteTransactionState state,
      final long key) throws Exception {

    // Indirect reference.
    PageReference reference = mStartReference;

    // Remaining levels.
    int levelSteps = 0;
    long levelKey = key;
    for (int i = 0; i < mIndirectOffsets.length; i++) {

      // Calculate offset of current level.
      levelSteps =
          (int) (levelKey >> IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i]);
      levelKey -= levelSteps << IConstants.INP_LEVEL_PAGE_COUNT_EXPONENT[i];

      // Fetch page from current level.
      mIndirectOffsets[i] = levelSteps;
      mIndirectPages[i] = state.prepareIndirectPage(reference);
      reference = mIndirectPages[i].getPageReference(levelSteps);
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

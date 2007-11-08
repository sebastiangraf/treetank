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
 * $Id: ParentAxisIterator.java 3174 2007-10-22 13:44:43Z kramis $
 */

package org.treetank.axislayer;

import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;

/**
 * <h1>ParentAxis</h1>
 * 
 * <p>
 * Iterate to parent node starting at a given node. Self is not included.
 * </p>
 */
public class FullTextAxis extends AbstractAxis {

  private long mFullTextLeafKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) trx to iterate with.
   * @param token Token to find.
   */
  public FullTextAxis(final IReadTransaction rtx, final String token) {
    super(rtx);

    if (token == null || token.length() == 0) {
      throw new IllegalArgumentException("Token can not be null or empty.");
    }

    rtx.moveToFullTextRoot();

    boolean contained = true;
    for (final char character : token.toCharArray()) {
      contained = contained && isContained(character);
    }

    mFullTextLeafKey = IConstants.NULL_KEY;
    if (contained && rtx.hasFirstChild()) {
      rtx.moveToFirstChild();
      while (rtx.isFullText() && rtx.hasRightSibling()) {
        rtx.moveToRightSibling();
      }
      if (rtx.isFullTextLeaf()) {
        mFullTextLeafKey = rtx.getNodeKey();
      }
    }

  }

  /**
   * Is the given character contained?
   * 
   * @param character Character to look for.
   * @return True if the character is contained. False else.
   */
  private final boolean isContained(final int character) {
    if (getTransaction().hasFirstChild()) {
      getTransaction().moveToFirstChild();
      while (getTransaction().isFullText()
          && (getTransaction().getLocalPartKey() != character)
          && getTransaction().hasRightSibling()) {
        getTransaction().moveToRightSibling();
      }
      return (getTransaction().getLocalPartKey() == character);
    } else {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    resetToLastKey();
    if (mFullTextLeafKey != IConstants.NULL_KEY) {
      getTransaction().moveTo(mFullTextLeafKey);
      mFullTextLeafKey = getTransaction().getRightSiblingKey();
      if (getTransaction().hasFirstChild()) {
        getTransaction().moveToFirstChild();
        return true;
      } else {
        resetToStartKey();
        return false;
      }
    } else {
      resetToStartKey();
      return false;
    }
  }

}

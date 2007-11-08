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

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>ParentAxis</h1>
 * 
 * <p>
 * Iterate to parent node starting at a given node. Self is not included.
 * </p>
 */
public class FullTextAxis extends AbstractAxis {

  /** Axis (either child or descendant) to search for fulltext leafs. */
  private final IAxis mAxis;

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

    IAxis innerAxis;
    boolean contained = true;
    if (token.charAt(token.length() - 1) != '*') {
      for (final char character : token.toCharArray()) {
        contained = contained && isContained(character);
      }
      innerAxis = new ChildAxis(rtx);
    } else {
      if (token.length() > 1) {
        for (final char character : token
            .substring(0, token.length() - 1)
            .toCharArray()) {
          contained = contained && isContained(character);
        }
      }
      innerAxis = new DescendantAxis(rtx);
    }
    if (!contained) {
      innerAxis = new SelfAxis(rtx);
    }

    mAxis = new FullTextLeafTestAxis(innerAxis);

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
    if (mAxis.hasNext()) {
      mAxis.next();
      getTransaction().moveToFirstChild();
      return true;
    } else {
      resetToStartKey();
      return false;
    }
  }

}

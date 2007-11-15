/*
 * Copyright (c) 2007, Marc Kramis
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
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

    mAxis = new FilterAxis(innerAxis, new FullTextLeafFilter());

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {
    super.reset(nodeKey);
    if (mAxis != null) {
      mAxis.reset(nodeKey);
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

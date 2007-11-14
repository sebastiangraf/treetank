/*
 * Copyright 2007, Marc Kramis
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

    mAxis = new FilterAxis(innerAxis, new FullTextLeafAxisFilter());

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

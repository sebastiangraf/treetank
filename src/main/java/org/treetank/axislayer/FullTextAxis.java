/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
public class FullTextAxis extends AbstractAxis implements IAxis {

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

    IAxis innerAxis = null;
    if (token.charAt(token.length() - 1) != '*') {
      // No wildcard.
      rtx.moveToToken(token);
      innerAxis = new ChildAxis(rtx);
    } else {
      // Wildcard.
      if (token.length() > 1) {
        rtx.moveToToken(token.substring(0, token.length() - 1));
        if (rtx.getNodeKey() != IReadTransaction.FULLTEXT_ROOT_KEY) {
          innerAxis = new DescendantAxis(rtx);
        }
      } else {
        rtx.moveToFullTextRoot();
        innerAxis = new DescendantAxis(rtx);
      }
    }
    if (innerAxis == null) {
      innerAxis = new SelfAxis(rtx);
    }

    mAxis = new FilterAxis(innerAxis, new FullTextLeafFilter(rtx));

    resetToStartKey();

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

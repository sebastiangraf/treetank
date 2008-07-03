/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
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
 * $Id: $
 */

package org.treetank.xpath.filter;

import org.treetank.api.IAxis;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>DocumentNodeAxis</h1>
 * <p>
 * Iterate to document node starting at a given node.
 * </p>
 */
public class DocumentNodeAxis extends AbstractAxis implements IAxis {

  /** Track number of calls of next. */
  private boolean mFirst;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) transaction to iterate with.
   */
  public DocumentNodeAxis(final IReadTransaction rtx) {

    super(rtx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mFirst = true;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {

    resetToLastKey();

    if (mFirst) {
      mFirst = false;
      getTransaction().moveToDocumentRoot();
      return true;
    } else {
      resetToStartKey();
      return false;
    }
  }

}

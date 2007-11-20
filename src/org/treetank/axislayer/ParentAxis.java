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
public class ParentAxis extends AbstractAxis implements IAxis {

  /** Track number of calls of next. */
  private boolean mFirst;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx Exclusive (immutable) trx to iterate with.
   */
  public ParentAxis(final IReadTransaction rtx) {
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
    if (!getTransaction().isDocumentRootKind()
        && mFirst
        && getTransaction().hasParent()
        && getTransaction().getParentKey() != IReadTransaction.DOCUMENT_ROOT_KEY) {
      mFirst = false;
      getTransaction().moveToParent();
      return true;
    } else {
      resetToStartKey();
      return false;
    }
  }

}

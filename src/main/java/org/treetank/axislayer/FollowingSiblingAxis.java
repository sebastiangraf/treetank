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
 * $Id$
 */

package org.treetank.axislayer;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>FollowingSiblingAxis</h1>
 * 
 * <p>
 * Iterate over all following siblings of kind ELEMENT or TEXT starting at a
 * given node. Self is not included.
 * </p>
 */
public class FollowingSiblingAxis extends AbstractAxis implements IAxis {

  private boolean mIsFirst;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   */
  public FollowingSiblingAxis(final IReadTransaction rtx) {

    super(rtx);
    mIsFirst = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mIsFirst = true;

  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {

    
    if (mIsFirst) {
      mIsFirst = false;
      //if the context node is an attribute or namespace node, 
      //the following-sibling axis is empty
      if (getTransaction().isAttributeKind() 
       //   || getTransaction().isNamespaceKind()
          ) {
        resetToStartKey();
        return false;
      }
    }
    
    resetToLastKey();
    
    
    
    if (getTransaction().hasRightSibling()) {
      getTransaction().moveToRightSibling();
      return true;
    }
    resetToStartKey();
    return false;
  }

}

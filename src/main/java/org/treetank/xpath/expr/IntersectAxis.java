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

package org.treetank.xpath.expr;

import java.util.HashSet;
import java.util.Set;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;

/**
 * <h1>IntersectAxis</h1>
 * <p>
 * Returns an intersection of two operands. This axis takes two node sequences
 * as operands and returns a sequence containing all the nodes that occur in
 * both operands.
 * </p>
 */
public class IntersectAxis extends AbstractAxis implements IAxis {

  /** First operand sequence. */
  private final IAxis mOp1;

  /** Second operand sequence. */
  private final IAxis mOp2;

  /** Set to decide, if an item is contained in both sequences. */
  private final Set<Long> mDupSet;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param operand1
   *          First operand
   * @param operand2
   *          Second operand
   */
  public IntersectAxis(final IReadTransaction rtx, final IAxis operand1,
      final IAxis operand2) {

    super(rtx);
    mOp1 = operand1;
    mOp2 = operand2;
    mDupSet = new HashSet<Long>();

  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(final long nodeKey) {

    super.reset(nodeKey);
    
    if (mDupSet != null) {
      mDupSet.clear();
    }
    
    
    if (mOp1 != null) {
      mOp1.reset(nodeKey);
    }
    if (mOp2 != null) {
      mOp2.reset(nodeKey);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {

    // store all item keys of the first sequence to the set.
    while (mOp1.hasNext()) {
      if (getTransaction().getNodeKey() < 0) {  //only nodes are allowed
        throw new XPathError(ErrorType.XPTY0004);
      } 
      
      mDupSet.add(getTransaction().getNodeKey());
    }

    while (mOp2.hasNext()) {
      
      if (getTransaction().getNodeKey() < 0) {  //only nodes are allowed
        throw new XPathError(ErrorType.XPTY0004);
      } 
      
      // return true, if item key is already in the set -> item is contained in
      // both input sequences.
      if (!mDupSet.add(getTransaction().getNodeKey())) {
        return true;
      }
    }

    return false;
  }

}

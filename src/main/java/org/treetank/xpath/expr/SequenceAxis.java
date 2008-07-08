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

import java.util.Arrays;
import java.util.List;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;

/**
 * <h1>SequenceAxis</h1>
 * <p>
 * Axis that represents a sequence of singleExpressions, normally separated by a
 * ','.
 * </p>
 * <p>
 * Calling hasNext() returns the results of the singleExpressions consecutively.
 * </p>
 *
 */
public class SequenceAxis extends AbstractAxis implements IAxis {

  private final List<IAxis> mSeq;
  private IAxis mCurrent;
  private int num;
  
  /**
   * 
   * Constructor. Initializes the internal state.
   *
   * @param rtx     Exclusive (immutable) trx to iterate with.
   * @param axis    The singleExpressions contained by the sequence
   */
  public SequenceAxis(final IReadTransaction rtx, final IAxis...axis) {

    super(rtx);
    mSeq = Arrays.asList(axis);
    num = 0;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(final long nodeKey) {
    super.reset(nodeKey);
    if (mSeq != null) {
      for (IAxis ax : mSeq) {
        ax.reset(nodeKey);
      }
    }
    mCurrent = null;
    num = 0;

    
  }
  
  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {
    
    resetToLastKey();

    if (mCurrent != null) {
      
      if (mCurrent.hasNext()) {
        return true;
      } else {
        //necessary, because previous hasNext() changes state
      resetToLastKey();
      }
    }

   
    
    while (num < mSeq.size()) {
     
      mCurrent = mSeq.get(num++);  

      //mCurrent.getTransaction().moveTo(getTransaction().getNodeKey());
      mCurrent.reset(getTransaction().getNodeKey());
      if (mCurrent.hasNext()) {
        return true;
      }
    }

    resetToStartKey();
    return false;

  }
    

}

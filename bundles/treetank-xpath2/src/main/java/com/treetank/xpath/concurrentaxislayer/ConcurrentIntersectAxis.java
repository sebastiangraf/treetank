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
 * $Id: ConcurrentIntersectAxis.java 4512 2008-11-07 15:05:40Z scherer $
 */

package com.treetank.xpath.concurrentaxislayer;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.xpath.XPathAxis;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;

/**
 * <h1>ConcurrentIntersectAxis</h1>
 * <p>
 * Computes concurrently and returns an intersection of two operands. This axis 
 * takes two node sequences as operands and returns a sequence containing all 
 * the nodes that occur in both operands. The result is in doc order and 
 * duplicate free.
 * </p>
 */
public class ConcurrentIntersectAxis extends AbstractAxis implements IAxis {

  /** First operand sequence. */
  private final ConcurrentAxis mOp1;

  /** Second operand sequence. */
  private final ConcurrentAxis mOp2;

  /** Is axis called for the first time? */
  private boolean mFirst;

  /** Current result of the 1st axis */
  private long mCurrentResult1;

  /** Current result of the 2nd axis. */
  private long mCurrentResult2;

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
  public ConcurrentIntersectAxis(
      final IReadTransaction rtx,
      final IAxis operand1,
      final IAxis operand2) {

    super(rtx);
    mOp1 = new ConcurrentAxis(rtx, operand1);
    mOp2 = new ConcurrentAxis(rtx, operand2);
    mFirst = true;
    mCurrentResult1 = IReadTransaction.NULL_NODE_KEY;
    mCurrentResult2 = IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized void reset(final long nodeKey) {

    super.reset(nodeKey);

    if (mOp1 != null) {
      mOp1.reset(nodeKey);
    }
    if (mOp2 != null) {
      mOp2.reset(nodeKey);
    }

    mFirst = true;
    mCurrentResult1 = IReadTransaction.NULL_NODE_KEY;
    mCurrentResult2 = IReadTransaction.NULL_NODE_KEY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public synchronized boolean hasNext() {

    resetToLastKey();

    if (mFirst) {
      mFirst = false;
      mCurrentResult1 = getNext(mOp1);
      mCurrentResult2 = getNext(mOp2);
    }

    final long nodeKey;

    //if 1st axis has a result left that is not contained in the 2nd it is returned
    while (!mOp1.isFinished()) {
      while (!mOp2.isFinished()) {

        // if both results are not equal get next values
        while (mCurrentResult1 != mCurrentResult2
            && !mOp1.isFinished()
            && !mOp2.isFinished()) {

          //get next result from 1st axis, if current is smaller than 2nd
          while (mCurrentResult1 < mCurrentResult2
              && !mOp1.isFinished()
              && !mOp2.isFinished()) {
            mCurrentResult1 = getNext(mOp1);

          }

          //get next result from 2nd axis if current is smaller than 1st
          while (mCurrentResult1 > mCurrentResult2
              && !mOp1.isFinished()
              && !mOp2.isFinished()) {
            mCurrentResult2 = getNext(mOp2);

          }
        }

        if (!mOp1.isFinished() && !mOp2.isFinished()) {
          //if both results are equal return it
          assert (mCurrentResult1 == mCurrentResult2);
          nodeKey = mCurrentResult1;
          if (isValid(nodeKey)) {
            mCurrentResult1 = getNext(mOp1);
            mCurrentResult2 = getNext(mOp2);
            getTransaction().moveTo(nodeKey);
            return true;
          }
          //should never come here!
          throw new IllegalStateException(nodeKey + " is not valid!");

        }
        break;

      }
      break;

    }
    //no results left
    resetToStartKey();
    return false;

  }

  /** 
   * @return the next result of the axis. If the axis has no next result, the 
   *  null node key is returned. */
  private long getNext(final IAxis axis) {
    return (axis.hasNext()) ? axis.next() : IReadTransaction.NULL_NODE_KEY;

  }

  /**
   * Checks, whether the given nodekey belongs to a node or an atomic value.
   * Returns true for a node and throws an exception for an atomic value, 
   * because these are not allowed in the except expression.
   * 
   * @param nodeKey the nodekey to validate
   * @return true, if key is a key of a node, otherwise throws an exception
   */
  private boolean isValid(final long nodeKey) {
    if (nodeKey < 0) { //only nodes are allowed
     // XPathAxis.log.error("union throws XPathError XPTY0004");
      throw new XPathError(ErrorType.XPTY0004);
    }
    return true;

  }

  @Override
  public void setTransaction(final IReadTransaction rtx) {
    super.setTransaction(rtx);
    mOp1.setTransaction(rtx);
    mOp2.setTransaction(rtx);
  }
}

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

package org.treetank.xpath.comparators;

import org.treetank.api.IAxis;
import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.XPathConstants;
import org.treetank.xpath.expr.LiteralExpr;
import org.treetank.xpath.types.Type;

/**
 * <h1>AbstractComparator</h1>
 * <p>
 * Abstract axis that evaluates a comparison.
 * </p>
 */
public abstract class AbstractComparator extends AbstractAxis implements IAxis,
    XPathConstants {

  /** Kind of comparison. */
  private final CompKind mComp;

  /** First value of the comparison. */
  private final IAxis mOperand1;

  /** Second value of the comparison. */
  private final IAxis mOperand2;

  /** Is first evaluation? */
  private boolean mIsFirst;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param operand1
   *          First value of the comparison
   * @param operand2
   *          Second value of the comparison
   * @param comp
   *          comparison kind
   */
  public AbstractComparator(final IReadTransaction rtx, final IAxis operand1,
      final IAxis operand2, final CompKind comp) {

    super(rtx);
    mComp = comp;
    mOperand1 = operand1;
    mOperand2 = operand2;
    mIsFirst = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mIsFirst = true;
    if (mOperand1 != null) {
      mOperand1.reset(nodeKey);
    }

    if (mOperand2 != null) {
      mOperand2.reset(nodeKey);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {

    resetToLastKey();

    if (mIsFirst) {
      mIsFirst = false;

      // TODO: why?
      if (!(mOperand1 instanceof LiteralExpr)) {
        mOperand1.reset(getTransaction().getNodeKey());
      }

      // TODO: why?
      if (!(mOperand2 instanceof LiteralExpr)) {
        mOperand2.reset(getTransaction().getNodeKey());
      }

      /*
       * Evaluates the comparison. First atomizes both operands and then
       * executes the comparison on them. At the end, the transaction is set to
       * the retrieved result item.
       */

      if (mOperand1.hasNext()) {
        // atomize operands
        final AtomicValue[] operand1 = atomize(mOperand1);
        if (mOperand2.hasNext()) {
          final AtomicValue[] operand2 = atomize(mOperand2);

          hook(operand1, operand2);

          // get comparison result
          final boolean resultValue = compare(operand1, operand2);
          final IItem result = new AtomicValue(resultValue);

          // add retrieved AtomicValue to item list
          final int itemKey = getTransaction().getItemList().addItem(result);
          getTransaction().moveTo(itemKey);

          return true;

        }
      }
    }

    // return empty sequence or function called more than once
    resetToStartKey();
    return false;
  }

  /**
   * Allowes the general comparisons to do some extra functionality.
   * 
   * @param operand1 first operand
   * @param operand2 second operand
   */
  protected void hook(final AtomicValue[] operand1, 
      final AtomicValue[] operand2) {

    // do nothing
  }

  /**
   * Performs the comparison of two atomic values.
   * 
   * @param operand1
   *          first comparison operand.
   * @param operand2
   *          second comparison operand.
   * @return the result of the comparison
   */
  protected abstract boolean compare(final AtomicValue[] operand1,
      final AtomicValue[] operand2);

  /**
   * Atomizes an operand according to the rules specified in the XPath
   * specification.
   * 
   * @param operand
   *          the operand that will be atomized.
   * @return the atomized operand. (always an atomic value)
   */
  protected abstract AtomicValue[] atomize(final IAxis operand);

  /**
   * Returns the common comparable type of the two operands, or an error, if the
   * two operands don't have a common type on which a comparison is allowed
   * according to the XPath 2.0 specification.
   * 
   * @param key1
   *          first comparison operand's type key
   * @param key2
   *          second comparison operand's type key
   * @return the type the comparison can be evaluated on
   */
  protected abstract Type getType(final int key1, final int key2);

  /**
   * @return the first operand
   */
  public final IAxis getOperand1() {

    return mOperand1;
  }

  /**
   * @return the second operand
   */
  public final IAxis getOperand2() {

    return mOperand2;
  }

  /**
   * @return comparison kind
   */
  public CompKind getCompKind() {

    return mComp;
  }

}

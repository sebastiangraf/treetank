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
 */

package com.treetank.service.xml.xpath.operators;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.treetank.api.IAxis;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.access.ReadTransaction;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.service.xml.xpath.XPathConstants;
import com.treetank.service.xml.xpath.functions.Function;
import com.treetank.service.xml.xpath.functions.XPathError;
import com.treetank.service.xml.xpath.functions.XPathError.ErrorType;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.settings.EFixed;

/**
 * <h1>AbstractOpAxis</h1>
 * <p>
 * Abstract axis for all operators performing an arithmetic operation.
 * </p>
 */
public abstract class ConcurrentAbstractOpAxis extends AbsAxis
    implements
    IAxis,
    XPathConstants {

  /** First arithmetic operand. */
  private final IAxis mOperand1;

  /** Second arithmetic operand. */
  private final IAxis mOperand2;

  /** True, if axis has not been evaluated yet. */
  private boolean mIsFirst;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param op1
   *          First value of the operation
   * @param op2
   *          Second value of the operation
   */
  public ConcurrentAbstractOpAxis(
      final IReadTransaction rtx,
      final IAxis op1,
      final IAxis op2) {

    super(rtx);
    mOperand1 = op1;
    mOperand2 = op2;
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

      final Future<Integer> itemF1 =
          XPathAxis.executor.submit(new Atomize(getTransaction(), mOperand1));

      final Future<Integer> itemF2 =
          XPathAxis.executor.submit(new Atomize(getTransaction(), mOperand2));

      try {
        Integer item1 = itemF1.get();
        Integer item2 = itemF2.get();
 

        if (item1 != EFixed.NULL_NODE_KEY.getStandardProperty()) {
          if (item2 != EFixed.NULL_NODE_KEY.getStandardProperty()) {
            final IItem result =
                operate((AtomicValue) getTransaction().getItemList().getItem(
                    item1), (AtomicValue) getTransaction()
                    .getItemList()
                    .getItem(item2));
            // add retrieved AtomicValue to item list
            int itemKey = getTransaction().getItemList().addItem(result);
            getTransaction().moveTo(itemKey);

            return true;
          }
        }

      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }

      if (XPATH_10_COMP) { // and empty sequence, return NaN
        final IItem result = new AtomicValue(Double.NaN, Type.DOUBLE);
        int itemKey = getTransaction().getItemList().addItem(result);
        getTransaction().moveTo(itemKey);
        return true;
      }

    }
    // either not the first call, or empty sequence
    resetToStartKey();
    return false;

  }

  /**
   * Performs the operation on the two input operands. First checks if the types
   * of the operands are a valid combination for the operation and if so
   * computed the result. Otherwise an XPathError is thrown.
   * 
   * @param operand1
   *          first input operand
   * @param operand2
   *          second input operand
   * @return result of the operation
   */
  protected abstract IItem operate(
      final AtomicValue operand1,
      final AtomicValue operand2);

  /**
   * Checks if the types of the operands are a valid combination for the
   * operation and if so returns the corresponding result type. Otherwise an
   * XPathError is thrown. 
   * This typed check is done according to the 
   * <a href="http://www.w3.org/TR/xpath20/#mapping">Operator Mapping</a>.
   * 
   * @param op1
   *          first operand's type key
   * @param op2
   *          second operand's type key
   * @return return type of the arithmetic function according to the operand
   *         type combination.
   */
  protected abstract Type getReturnType(final int op1, final int op2);

  @Override
  public void setTransaction(final IReadTransaction rtx) {
    super.setTransaction(rtx);
    mOperand1.setTransaction(rtx);
    mOperand2.setTransaction(rtx);
  }

}

class Atomize implements Callable<Integer> {

  final IAxis mAxis;

  final IReadTransaction mRtx;

  Atomize(final IReadTransaction rtx, final IAxis axis) {
    mRtx = new ReadTransaction((ReadTransaction) rtx);
    mAxis = axis;
    mAxis.setTransaction(mRtx);

  }

  public Integer call() throws XPathError{

    if (mAxis.hasNext()) {

      int type = mAxis.getTransaction().getNode().getTypeKey();
      AtomicValue atom;

      if (XPathConstants.XPATH_10_COMP) {
        if (type == mAxis.getTransaction().keyForName("xs:double")
            || type == mAxis.getTransaction().keyForName("xs:untypedAtomic")
            || type == mAxis.getTransaction().keyForName("xs:boolean")
            || type == mAxis.getTransaction().keyForName("xs:string")
            || type == mAxis.getTransaction().keyForName("xs:integer")
            || type == mAxis.getTransaction().keyForName("xs:float")
            || type == mAxis.getTransaction().keyForName("xs:decimal")) {
          Function.fnnumber(mAxis.getTransaction());
        }

        atom =
            new AtomicValue(mAxis.getTransaction().getNode().getRawValue(), mAxis
                .getTransaction()
                .getNode().getTypeKey());
      } else {
        // unatomicType is cast to double
        if (type == mAxis.getTransaction().keyForName("xs:untypedAtomic")) {
          type = mAxis.getTransaction().keyForName("xs:double");
          // TODO: throw error, of cast fails
        }

        atom = new AtomicValue(mAxis.getTransaction().getNode().getRawValue(), type);
      }

      if (!XPathConstants.XPATH_10_COMP && mAxis.hasNext()) {
        throw new XPathError(ErrorType.XPTY0004);
      }

      return mAxis.getTransaction().getItemList().addItem(atom);

    }

    return -1;
    //return IReadTransaction.NULL_NODE_KEY;

  }
}

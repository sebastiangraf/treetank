/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.service.xml.xpath.expr;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.settings.EFixed;

/**
 * <h1> VarRefExpr </h1>
 * <p>
 * Reference to the current item of the variable expression.
 * </p>
 */
public class VarRefExpr extends AbstractExpression implements IAxis, IObserver {

  /** Buffer for the following variable values. */
  private final BlockingQueue<Long> mNextResults;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param variable
   *          Reference the variable expression that computes the items the
   *          variable holds.
   */
  public VarRefExpr(final IReadTransaction rtx, final VariableAxis variable) {

    super(rtx);
    variable.addObserver(this);
    mNextResults = new ArrayBlockingQueue<Long>(10);

  }

  /**
   * {@inheritDoc}
   */
  public synchronized void update(final long varKey) {

  //  XPathAxis.logger.debug("Update VarRef with "+varKey);
    try {
      while (mNextResults.remainingCapacity() == 0) {
        wait();
      }
      mNextResults.put(varKey);
      notifyAll();
    } catch (InterruptedException e) {
    //  XPathAxis.logger.debug("VarRef throws InterruptedExeption on put");
      e.printStackTrace();
    }
  }

  /**
   * {@inheritDoc}
   */
    @Override
  public synchronized void evaluate() {
   resetToLastKey();
  // XPathAxis.log.debug("VarRef hasNext called");
   
  final long currentKey;
  try {
    while (mNextResults.isEmpty()) {
      wait();
    }
    currentKey = mNextResults.take();
    notifyAll();
    //XPathAxis.log.debug("Got new value VarRef "+currentKey);
    if (currentKey != (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
      getTransaction().moveTo(currentKey);
      return;
    }  
  } catch (InterruptedException e) {
  //  XPathAxis.log.debug("VarRef throws InterruptedExeption on take");
    e.printStackTrace();
  }
  
 // XPathAxis.log.debug("VarRef was called too often.");
  throw new IllegalStateException("Requested more variable values than available.");
   
 
  }

}

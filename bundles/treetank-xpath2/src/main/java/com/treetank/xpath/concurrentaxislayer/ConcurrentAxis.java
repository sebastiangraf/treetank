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
 * $Id: ConcurrentAxis.java 4509 2008-11-06 15:21:17Z scherer $
 */

package com.treetank.xpath.concurrentaxislayer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.service.xml.xpath.XPathAxis;

/**
 * <h1>ConcurrentAxis</h1>
 * <p>
 * Realizes in combination with the <code>ConurrentAxisHelper</code> the 
 * concurrent evaluation of pipeline steps.
 * The given axis is uncoupled from the main thread by embedding it in a 
 * Runnable that uses its one transaction and stores all the results to a queue.
 * The ConcurrentAxis gets the computed results from that queue one by one on 
 * every hasNext() call and sets the main-transaction to it. As soon as the end
 * of the computed result sequence is reached (marked by the NULL_NODE_KEY),
 * the ConcurrentAxis returns <code>false</code>.
 * </p>
 * <p>
 * This framework is working according to the producer-consumer-principle, where
 * the ConcurrentAxisHelper and its encapsulated axis is the producer and the 
 * ConcurrentAxis with its callees is the consumer This can be used by any class
 * that implements the IAxis interface. Note: Make sure that the used class is 
 * thread-safe.
 * </p>
 */
public class ConcurrentAxis extends AbsAxis implements IAxis {

  /**Axis that is running in an own thread and produces results for this axis.*/
  private final IAxis mProducer;

  /** Queue that stores result keys already computed by the producer. End of the
   *  result sequence is marked by the NULL_NODE_KEY. */
  private final BlockingQueue<Long> mResults;

  /** Capacity of the mResults queue. */
  private final int M_CAPACITY = 10;

  /** Has axis already been called? */
  private boolean mFirst;

  /** Runnable in which the producer is running. */
  private Runnable task;

  /** Is axis already finished and has no results left? */
  private boolean mFinished;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param childAxis
   *          Producer axis.
   */
  public ConcurrentAxis(final IReadTransaction rtx, final IAxis childAxis) {
    super(rtx);
    mResults = new ArrayBlockingQueue<Long>(M_CAPACITY);
    mFirst = true;

    mProducer = childAxis;
    task = new ConcurrentAxisHelper(getTransaction(), mProducer, mResults);
    mFinished = false;

  }

  /**
   * {@inheritDoc}
   */
  public synchronized void reset(final long nodeKey) {
    super.reset(nodeKey);
    mFirst = true;
    mFinished = false;

    if (mProducer != null) {
      mProducer.reset(nodeKey);
    }
    if (mResults != null) {
      mResults.clear();
    }

    if (task != null) {
      task = new ConcurrentAxisHelper(getTransaction(), mProducer, mResults);
    }
  }

  /**
   * {@inheritDoc}
   */
  public synchronized boolean hasNext() {

    resetToLastKey();

    //start producer on first call
    if (mFirst) {
      mFirst = false;
      XPathAxis.executor.submit(task);
    }

    if (mFinished) {
      resetToStartKey();
      return false;
    }

    long result = IReadTransaction.NULL_NODE_KEY;

    try {
      //get result from producer as soon as it is available
      result = mResults.take();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //NULL_NODE_KEY marks end of the sequence computed by the producer
    if (result != IReadTransaction.NULL_NODE_KEY) {
      getTransaction().moveTo(result);
      return true;
    }

    mFinished = true;
    resetToStartKey();
    return false;

  }

  /**
   * @return true, if axis still has results left
   */
  public boolean isFinished() {
    return mFinished;
  }

}

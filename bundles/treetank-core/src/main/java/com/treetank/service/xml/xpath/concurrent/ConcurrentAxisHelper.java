/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * Copyright (c) 2010, Patrick Lang (Master Project), University of Konstanz
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
 * $Id: ConcurrentAxisHelper.java 4509 2008-11-06 15:21:17Z scherer $
 */

package com.treetank.service.xml.xpath.concurrent;

import java.util.concurrent.BlockingQueue;

import com.treetank.access.ReadTransaction;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.service.xml.xpath.comparators.AbsComparator;
import com.treetank.service.xml.xpath.expr.ExceptAxis;
import com.treetank.service.xml.xpath.expr.IntersectAxis;
import com.treetank.service.xml.xpath.expr.LiteralExpr;
import com.treetank.service.xml.xpath.expr.SequenceAxis;
import com.treetank.service.xml.xpath.expr.UnionAxis;
import com.treetank.service.xml.xpath.filter.DupFilterAxis;
import com.treetank.settings.EFixed;

/**
 * <h1>ConcurrentAxisHelper</h1>
 * <p>
 * Is the helper for the ConcurrentAxis and realizes the concurrent evaluation of pipeline steps by decoupling
 * the given axis from the main thread and storing its results in a blocking queue so establish a
 * producer-consumer-relationship between the ConcurrentAxis and this one.
 * </p>
 * <p>
 * This axis should only be used and instantiated by the ConcurrentAxis. Find more information on how to use
 * this framework in the ConcurrentAxis documentation.
 * </p>
 */
public class ConcurrentAxisHelper implements Runnable {

    /**
     * Additional transaction to operate on parallel to the original
     * transaction.
     */
    private final IReadTransaction mRTX;

    /** Axis that computes the results. */
    private final AbsAxis mAxis;

    /**
     * Queue that stores result keys already computed by this axis. End of the
     * result sequence is marked by the NULL_NODE_KEY. This is used for
     * communication with the consumer.
     */
    private final BlockingQueue<Long> mResults;

    /**
     * True, if next() has to be called for the given axis after calling
     * hasNext().
     */
    private final boolean callNext;

    // private Logger logger = Logger.getLogger("ThreadedAxisHelper");

    /**
     * Bind axis step to transaction.
     * Make sure to create a new ReadTransaction instead of using the parameter
     * rtx. Because of concurrency every axis has to have it's own transaction.
     * 
     * @param rtx
     *            Transaction to operate with.
     */
    public ConcurrentAxisHelper(final IReadTransaction rtx, final AbsAxis axis,
        final BlockingQueue<Long> results) {
        mRTX = new ReadTransaction((ReadTransaction)rtx);
        mAxis = axis;
        mAxis.setTransaction(mRTX);
        mResults = results;
        callNext =
            !(mAxis instanceof UnionAxis || mAxis instanceof ExceptAxis
                || mAxis instanceof ConcurrentExceptAxis || mAxis instanceof IntersectAxis
                || mAxis instanceof LiteralExpr || mAxis instanceof AbsComparator
                || mAxis instanceof SequenceAxis || mAxis instanceof XPathAxis
                || mAxis instanceof DupFilterAxis || mAxis instanceof ConcurrentUnionAxis || mAxis instanceof ConcurrentIntersectAxis);
    }

    public void run() {

        // Compute all results of the given axis and store the results in the queue.
        while (mAxis.hasNext()) {
            // for some axis next(( has to be called here
            if (callNext) {
                mAxis.next();
            }
            try {
                // store result in queue as soon as there is space left
                mResults.put(mAxis.getTransaction().getNode().getNodeKey());
                // wait until next thread arrives and exchange blocking queue
                ConcurrentExchanger.getInstance().exchange(mResults);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }

        try {
            // Mark end of result sequence by the NULL_NODE_KEY
            mResults.put((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}

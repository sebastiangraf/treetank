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
 * $Id: ConcurrentUnionAxis.java 4512 2008-11-07 15:05:40Z scherer $
 */

package com.treetank.service.xml.xpath.concurrent;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.xpath.EXPathError;
import com.treetank.settings.EFixed;

/**
 * <h1>ConcurrentUnionAxis</h1>
 * <p>
 * Computes concurrently and returns a union of two operands. This axis takes two node sequences as operands
 * and returns a sequence containing all the items that occur in either of the operands. A union of two
 * sequences may lead to a sequence containing duplicates. These duplicates are removed by the concept of ....
 * Additionally this guarantees the document order.
 * </p>
 */
public class ConcurrentUnionAxis extends AbsAxis implements IAxis {

    /** First operand sequence. */
    private final ConcurrentAxis mOp1;

    /** Second operand sequence. */
    private final ConcurrentAxis mOp2;

    private boolean mFirst;

    private long mCurrentResult1;

    private long mCurrentResult2;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param operand1
     *            First operand
     * @param operand2
     *            Second operand
     */
    public ConcurrentUnionAxis(final IReadTransaction rtx, final IAxis operand1, final IAxis operand2) {

        super(rtx);
        mOp1 = new ConcurrentAxis(rtx, operand1);
        mOp2 = new ConcurrentAxis(rtx, operand2);
        mFirst = true;
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

        // if both operands have results left return the smallest value (doc order)
        if (!mOp1.isFinished()) {
            if (!mOp2.isFinished()) {
                if (mCurrentResult1 < mCurrentResult2) {
                    nodeKey = mCurrentResult1;
                    mCurrentResult1 = getNext(mOp1);

                } else if (mCurrentResult1 > mCurrentResult2) {
                    nodeKey = mCurrentResult2;
                    mCurrentResult2 = getNext(mOp2);
                } else {
                    // return only one of the values (prevent duplicates)
                    nodeKey = mCurrentResult2;
                    mCurrentResult1 = getNext(mOp1);
                    mCurrentResult2 = getNext(mOp2);
                }

                if (nodeKey < 0) {
                    try {
                        throw EXPathError.XPTY0004.getEncapsulatedException();
                    } catch (final TTXPathException mExp) {
                        mExp.printStackTrace();
                    }
                }
                getTransaction().moveTo(nodeKey);
                return true;
            }

            // only operand1 has results left, so return all of them
            nodeKey = mCurrentResult1;
            if (isValid(nodeKey)) {
                mCurrentResult1 = getNext(mOp1);
                getTransaction().moveTo(nodeKey);
                return true;
            }
            // should never come here!
            throw new IllegalStateException(nodeKey + " is not valid!");

        } else if (!mOp2.isFinished()) {
            // only operand1 has results left, so return all of them

            nodeKey = mCurrentResult2;
            if (isValid(nodeKey)) {
                mCurrentResult2 = getNext(mOp2);
                getTransaction().moveTo(nodeKey);
                return true;
            }
            // should never come here!
            throw new IllegalStateException(nodeKey + " is not valid!");

        }
        // no results left
        resetToStartKey();
        return false;

    }

    /**
     * @return the next result of the axis. If the axis has no next result, the
     *         null node key is returned.
     */
    private long getNext(final IAxis axis) {
        return (axis.hasNext()) ? axis.next() : (Long)EFixed.NULL_NODE_KEY.getStandardProperty();

    }

    /**
     * Checks, whether the given nodekey belongs to a node or an atomic value.
     * Returns true for a node and throws an exception for an atomic value,
     * because these are not allowed in the except expression.
     * 
     * @param nodeKey
     *            the nodekey to validate
     * @return true, if key is a key of a node, otherwise throws an exception
     */
    private boolean isValid(final long nodeKey) {
        if (nodeKey < 0) {
            // throw new XPathError(ErrorType.XPTY0004);
            try {
                throw new TTXPathException("err:XPTY0004 The type is not appropriate the expression or the "
                    + "typedoes not match a required type as specified by the matching rules.");
            } catch (TTXPathException mExp) {
                mExp.printStackTrace();
            }
        }
        return true;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTransaction(final IReadTransaction rtx) {
        super.setTransaction(rtx);
        mOp1.setTransaction(rtx);
        mOp2.setTransaction(rtx);
    }
}

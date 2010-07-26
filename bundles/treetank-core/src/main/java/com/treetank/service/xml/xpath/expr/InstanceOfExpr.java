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
 * $Id: InstanceOfExpr.java 4246 2008-07-08 08:54:09Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.SequenceType;
import com.treetank.utils.TypedValue;

/**
 * <h1>InstanceOfExpr</h1>
 * <p>
 * The boolean instance of expression returns true if the value of its first operand matches the SequenceType
 * in its second operand, according to the rules for SequenceType matching; otherwise it returns false.
 * </p>
 */
public class InstanceOfExpr extends AbstractExpression implements IAxis {

    /** The sequence to test. */
    private final IAxis mInputExpr;

    /** The sequence type that the sequence needs to have to be an instance of. */
    private final SequenceType mSequenceType;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param inputExpr
     *            input expression, to test
     * @param sequenceType
     *            sequence type to test whether the input sequence matches to.
     */
    public InstanceOfExpr(final IReadTransaction rtx, final IAxis inputExpr, final SequenceType sequenceType) {

        super(rtx);
        mInputExpr = inputExpr;
        mSequenceType = sequenceType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final long nodeKey) {

        super.reset(nodeKey);

        if (mInputExpr != null) {
            mInputExpr.reset(nodeKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void evaluate() {

        boolean isInstanceOf;

        if (mInputExpr.hasNext()) {
            if (mSequenceType.isEmptySequence()) {
                isInstanceOf = false;
            } else {

                isInstanceOf = mSequenceType.getFilter().filter();
                switch (mSequenceType.getWildcard()) {

                case '*':
                case '+':
                    // This seams to break the pipeline, but because the
                    // intermediate
                    // result are no longer used, it might be not that bad
                    while(mInputExpr.hasNext() && isInstanceOf) {
                        isInstanceOf = isInstanceOf && mSequenceType.getFilter().filter();
                    }
                    break;
                default: // no wildcard, or '?'
                    // only one result item is allowed
                    isInstanceOf = isInstanceOf && !mInputExpr.hasNext();
                }
            }

        } else { // empty sequence
            isInstanceOf =
                mSequenceType.isEmptySequence()
                || (mSequenceType.hasWildcard() && (mSequenceType.getWildcard() == '?' || mSequenceType
                    .getWildcard() == '*'));
        }

        // create result item and move transaction to it.
        int itemKey =
            getTransaction().getItemList().addItem(
                new AtomicValue(TypedValue.getBytes(Boolean.toString(isInstanceOf)), getTransaction()
                    .keyForName("xs:boolean")));
        getTransaction().moveTo(itemKey);

    }
}

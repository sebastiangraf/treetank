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

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbstractAxis;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.utils.TypedValue;

/**
 * <h1>RangeExpr</h1>
 * <p>
 * A range expression can be used to construct a sequence of consecutive integers.
 * </p>
 * <p>
 * If either operand is an empty sequence, or if the integer derived from the first operand is greater than
 * the integer derived from the second operand, the result of the range expression is an empty sequence.
 * </p>
 * <p>
 * If the two operands convert to the same integer, the result of the range expression is that integer.
 * Otherwise, the result is a sequence containing the two integer operands and every integer between the two
 * operands, in increasing order.
 * </p>
 */
public class RangeAxis extends AbstractAxis implements IAxis {

    /** The expression the range starts from. */
    private final IAxis mFrom;

    /** The expression the range ends. */
    private final IAxis mTo;

    /** Is it the first run of range axis? */
    private boolean mFirst;

    /** The integer value the expression starts from. */
    private int mStart;

    /** The integer value the expression ends. */
    private int mEnd;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mFrom
     *            start of the range
     * @param mTo
     *            the end of the range
     */
    public RangeAxis(final IReadTransaction rtx, final IAxis mFrom, final IAxis mTo) {

        super(rtx);
        this.mFrom = mFrom;
        this.mTo = mTo;
        mFirst = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {

        resetToLastKey();

        if (mFirst) {
            mFirst = false;
            if (mFrom.hasNext()
                && Type.getType(mFrom.getTransaction().getNode().getTypeKey()).derivesFrom(Type.INTEGER)) {
                mStart =
                    (int)Double.parseDouble(TypedValue.parseString(mFrom.getTransaction().getNode()
                        .getRawValue()));

                if (mTo.hasNext()
                    && Type.getType(mTo.getTransaction().getNode().getTypeKey()).derivesFrom(Type.INTEGER)) {

                    mEnd =
                        Integer
                            .parseInt(TypedValue.parseString(mTo.getTransaction().getNode().getRawValue()));

                } else {
                    // at least one operand is the empty sequence
                    resetToStartKey();
                    return false;
                }
            } else {
                // at least one operand is the empty sequence
                resetToStartKey();
                return false;
            }
        }

        if (mStart <= mEnd) {
            final int itemKey =
                getTransaction().getItemList().addItem(
                    new AtomicValue(TypedValue.getBytes(Integer.toString(mStart)), getTransaction()
                        .keyForName("xs:integer")));
            getTransaction().moveTo(itemKey);
            mStart++;
            return true;
        } else {
            resetToStartKey();
            return false;
        }
    }

}

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

package com.treetank.service.xml.xpath.operators;

import com.treetank.api.IAxis;
import com.treetank.api.IItem;
import com.treetank.api.IReadTransaction;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.XPathError;
import com.treetank.service.xml.xpath.XPathError.ErrorType;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.utils.TypedValue;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * <h1>AddOpAxis</h1>
 * <p>
 * Performs an arithmetic addition on two input operators.
 * </p>
 */
public class AddOpAxis extends AbsObAxis {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(AddOpAxis.class));

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mOp1
     *            First value of the operation
     * @param mOp2
     *            Second value of the operation
     */
    public AddOpAxis(final IReadTransaction rtx, final IAxis mOp1, final IAxis mOp2) {

        super(rtx, mOp1, mOp2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IItem operate(final AtomicValue mOperand1, final AtomicValue mOperand2) {

        final Type returnType = getReturnType(mOperand1.getTypeKey(), mOperand2.getTypeKey());
        final int typeKey = getTransaction().keyForName(returnType.getStringRepr());

        final byte[] value;

        switch (returnType) {
        case DOUBLE:
        case FLOAT:
        case DECIMAL:
        case INTEGER:
            final double dOp1 = Double.parseDouble(TypedValue.parseString(mOperand1.getRawValue()));
            final double dOp2 = Double.parseDouble(TypedValue.parseString(mOperand2.getRawValue()));
            value = TypedValue.getBytes(dOp1 + dOp2);
            break;
        case DATE:
        case TIME:
        case DATE_TIME:
        case YEAR_MONTH_DURATION:
        case DAY_TIME_DURATION:
            throw new IllegalStateException("Add operator is not implemented for the type "
                + returnType.getStringRepr() + " yet.");
        default:
            throw new XPathError(ErrorType.XPTY0004);

        }

        return new AtomicValue(value, typeKey);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Type getReturnType(final int mOp1, final int mOp2) {

        Type mType1;
        Type mType2;
        try {
            mType1 = Type.getType(mOp1).getPrimitiveBaseType();
            mType2 = Type.getType(mOp2).getPrimitiveBaseType();
        } catch (final IllegalStateException e) {
            LOGWRAPPER.error(e);
            throw new XPathError(ErrorType.XPTY0004);
        }

        if (mType1.isNumericType() && mType2.isNumericType()) {

            // if both have the same numeric type, return it
            if (mType1 == mType2) {
                return mType1;
            }

            if (mType1 == Type.DOUBLE || mType2 == Type.DOUBLE) {
                return Type.DOUBLE;
            } else if (mType1 == Type.FLOAT || mType2 == Type.FLOAT) {
                return Type.FLOAT;
            } else {
                assert (mType1 == Type.DECIMAL || mType2 == Type.DECIMAL);
                return Type.DECIMAL;
            }

        } else {

            switch (mType1) {
            case DATE:
                if (mType2 == Type.YEAR_MONTH_DURATION || mType2 == Type.DAY_TIME_DURATION) {
                    return mType1;
                }
                break;
            case TIME:
                if (mType2 == Type.DAY_TIME_DURATION) {
                    return mType1;
                }
                break;
            case DATE_TIME:
                if (mType2 == Type.YEAR_MONTH_DURATION || mType2 == Type.DAY_TIME_DURATION) {
                    return mType1;
                }
                break;
            case YEAR_MONTH_DURATION:
                if (mType2 == Type.DATE || mType2 == Type.DATE_TIME || mType2 == Type.YEAR_MONTH_DURATION) {
                    return mType2;
                }
                break;
            case DAY_TIME_DURATION:
                if (mType2 == Type.DATE || mType2 == Type.TIME || mType2 == Type.DATE_TIME
                    || mType2 == Type.DAY_TIME_DURATION) {
                    return mType2;
                }
                break;
            default:
                throw new XPathError(ErrorType.XPTY0004);
            }
            throw new XPathError(ErrorType.XPTY0004);
        }
    }

}

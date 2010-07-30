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
import com.treetank.service.xml.xpath.functions.XPathError;
import com.treetank.service.xml.xpath.functions.XPathError.ErrorType;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.utils.TypedValue;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * <h1>DivOpAxis</h1>
 * <p>
 * Performs an arithmetic division on two input operators.
 * </p>
 */
public class DivOpAxis extends AbstractOpAxis {
    
    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(DivOpAxis.class));

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
    public DivOpAxis(final IReadTransaction rtx, final IAxis mOp1, final IAxis mOp2) {

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
        case DECIMAL:
        case FLOAT:
        case DOUBLE:
            final double aD = Double.parseDouble(TypedValue.parseString(mOperand1.getRawValue()));
            final double dValue;

            if (aD == 0.0 || aD == -0.0) {
                dValue = Double.NaN;
            } else {
                dValue = aD / Double.parseDouble(TypedValue.parseString(mOperand2.getRawValue()));
            }

            value = TypedValue.getBytes(dValue);
            return new AtomicValue(value, typeKey);

        case INTEGER:
            try {
                final int iValue =
                    (int)Double.parseDouble(TypedValue.parseString(mOperand1.getRawValue()))
                    / (int)Double.parseDouble(TypedValue.parseString(mOperand2.getRawValue()));
                value = TypedValue.getBytes(iValue);
                return new AtomicValue(value, typeKey);
            } catch (final ArithmeticException e) {
                LOGWRAPPER.error(e);
                throw new XPathError(ErrorType.FOAR0001);
            }
        case YEAR_MONTH_DURATION:
        case DAY_TIME_DURATION:
            throw new IllegalStateException("Add operator is not implemented for the type "
                + returnType.getStringRepr() + " yet.");
        default:
            throw new XPathError(ErrorType.XPTY0004);

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Type getReturnType(final int mOp1, final int mOp2) {

        Type type1;
        Type type2;
        try {
            type1 = Type.getType(mOp1).getPrimitiveBaseType();
            type2 = Type.getType(mOp2).getPrimitiveBaseType();
        } catch (final IllegalStateException e) {
            LOGWRAPPER.error(e);
            throw new XPathError(ErrorType.XPTY0004);
        }

        if (type1.isNumericType() && type2.isNumericType()) {

            // if both have the same numeric type, return it
            if (type1 == type2) {
                return type1;
            }

            if (type1 == Type.DOUBLE || type2 == Type.DOUBLE) {
                return Type.DOUBLE;
            } else if (type1 == Type.FLOAT || type2 == Type.FLOAT) {
                return Type.FLOAT;
            } else {
                assert (type1 == Type.DECIMAL || type2 == Type.DECIMAL);
                return Type.DECIMAL;
            }

        } else {

            switch (type1) {

            case YEAR_MONTH_DURATION:
                if (type2 == Type.YEAR_MONTH_DURATION) {
                    return Type.DECIMAL;
                }
                if (type2.isNumericType()) {
                    return type1;
                }
                break;
            case DAY_TIME_DURATION:
                if (type2 == Type.DAY_TIME_DURATION) {
                    return Type.DECIMAL;
                }
                if (type2.isNumericType()) {
                    return type1;
                }
                break;
            default:
                throw new XPathError(ErrorType.XPTY0004);
            }
            throw new XPathError(ErrorType.XPTY0004);
        }
    }

}

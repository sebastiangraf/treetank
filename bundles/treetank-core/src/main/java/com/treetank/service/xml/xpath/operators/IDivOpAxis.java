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
 * <h1>AddOpAxis</h1>
 * <p>
 * Performs an arithmetic integer division on two input operators.
 * </p>
 */
public class IDivOpAxis extends AbstractOpAxis {
    
    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(IDivOpAxis.class));

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
    public IDivOpAxis(final IReadTransaction rtx, final IAxis mOp1, final IAxis mOp2) {

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

        try {
            final int op1 = (int)Double.parseDouble(TypedValue.parseString(mOperand1.getRawValue()));
            final int op2 = (int)Double.parseDouble(TypedValue.parseString(mOperand2.getRawValue()));
            final int iValue = op1 / op2;
            value = TypedValue.getBytes(iValue);
            return new AtomicValue(value, typeKey);
        } catch (final ArithmeticException e) {
            //LOGWRAPPER.error(e);
            throw new XPathError(ErrorType.FOAR0001);
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

            return Type.INTEGER;
        } else {

            throw new XPathError(ErrorType.XPTY0004);

        }
    }

}

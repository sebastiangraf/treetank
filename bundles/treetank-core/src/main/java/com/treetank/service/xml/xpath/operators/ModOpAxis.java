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
 * $Id: ModOpAxis.java 4246 2008-07-08 08:54:09Z scherer $
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

/**
 * <h1>AddOpAxis</h1>
 * <p>
 * Performs a modulo operation using two input operators.
 * </p>
 */
public class ModOpAxis extends AbstractOpAxis {

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param op1
     *            First value of the operation
     * @param op2
     *            Second value of the operation
     */
    public ModOpAxis(final IReadTransaction rtx, final IAxis op1,
            final IAxis op2) {

        super(rtx, op1, op2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IItem operate(final AtomicValue operand1, final AtomicValue operand2) {

        Type returnType = getReturnType(operand1.getTypeKey(),
                operand2.getTypeKey());
        int typeKey = getTransaction().keyForName(returnType.getStringRepr());

        final byte[] value;

        switch (returnType) {
        case DOUBLE:
        case FLOAT:
        case DECIMAL:
            final double dOp1 = Double.parseDouble(TypedValue
                    .parseString(operand1.getRawValue()));
            final double dOp2 = Double.parseDouble(TypedValue
                    .parseString(operand2.getRawValue()));
            value = TypedValue.getBytes(dOp1 % dOp2);
            break;
        case INTEGER:
            try {
                final int iOp1 = (int) Double.parseDouble(TypedValue
                        .parseString(operand1.getRawValue()));
                final int iOp2 = (int) Double.parseDouble(TypedValue
                        .parseString(operand2.getRawValue()));
                value = TypedValue.getBytes(iOp1 % iOp2);
            } catch (ArithmeticException e) {
                throw new XPathError(ErrorType.FOAR0001);
            }
            break;
        default:
            throw new XPathError(ErrorType.XPTY0004);

        }

        return new AtomicValue(value, typeKey);

    }

    /**
     * {@inheritDoc}
     */
    protected Type getReturnType(final int op1, final int op2) {
        Type type1;
        Type type2;
        try {
            type1 = Type.getType(op1).getPrimitiveBaseType();
            type2 = Type.getType(op2).getPrimitiveBaseType();
        } catch (IllegalStateException e) {
            throw new XPathError(ErrorType.XPTY0004);
        }

        // only numeric values are valid for the mod operator
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
            throw new XPathError(ErrorType.XPTY0004);
        }
    }

}

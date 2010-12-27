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

package com.treetank.service.xml.xpath.comparators;

import java.util.ArrayList;
import java.util.List;

import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.functions.Function;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.utils.TypedValue;

import static com.treetank.service.xml.xpath.XPathAxis.XPATH_10_COMP;

/**
 * <h1>GeneralComp</h1>
 * <p>
 * General comparisons are existentially quantified comparisons that may be applied to operand sequences of
 * any length.
 * </p>
 */
public class GeneralComp extends AbsComparator {

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mOperand1
     *            First value of the comparison
     * @param mOperand2
     *            Second value of the comparison
     * @param mCom
     *            comparison kind
     */
    public GeneralComp(final IReadTransaction rtx, final AbsAxis mOperand1, final AbsAxis mOperand2,
        final CompKind mCom) {

        super(rtx, mOperand1, mOperand2, mCom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean compare(final AtomicValue[] mOperand1, final AtomicValue[] mOperand2)
        throws TTXPathException {

        assert mOperand1.length >= 1 && mOperand2.length >= 1;

        for (AtomicValue op1 : mOperand1) {
            for (AtomicValue op2 : mOperand2) {
                String value1 = TypedValue.parseString(op1.getRawValue());
                String value2 = TypedValue.parseString(op2.getRawValue());
                if (getCompKind().compare(value1, value2, getType(op1.getTypeKey(), op2.getTypeKey()))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AtomicValue[] atomize(final AbsAxis mOperand) {

        final IReadTransaction rtx = getTransaction();
        final List<AtomicValue> op = new ArrayList<AtomicValue>();
        AtomicValue atomized;
        // cast to double, if compatible with XPath 1.0 and <, >, >=, <=
        final boolean convert =
            !(!XPATH_10_COMP || getCompKind() == CompKind.EQ || getCompKind() == CompKind.EQ);

        do {
            if (convert) { // cast to double
                Function.fnnumber(rtx);
            }
            atomized = new AtomicValue(rtx.getNode().getRawValue(), rtx.getNode().getTypeKey());
            op.add(atomized);
        } while (mOperand.hasNext());

        return op.toArray(new AtomicValue[op.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Type getType(final int mKey1, final int mKey2) throws TTXPathException {

        final Type mType1 = Type.getType(mKey1).getPrimitiveBaseType();
        final Type mType2 = Type.getType(mKey2).getPrimitiveBaseType();

        if (XPATH_10_COMP) {
            if (mType1.isNumericType() || mType2.isNumericType()) {
                return Type.DOUBLE;
            }

            if (mType1 == Type.STRING || mType2 == Type.STRING
                || (mType1 == Type.UNTYPED_ATOMIC && mType2 == Type.UNTYPED_ATOMIC)) {
                return Type.STRING;
            }

            if (mType1 == Type.UNTYPED_ATOMIC || mType2 == Type.UNTYPED_ATOMIC) {
                return Type.UNTYPED_ATOMIC;

            }

        } else {
            if (mType1 == Type.UNTYPED_ATOMIC) {
                switch (mType2) {
                case UNTYPED_ATOMIC:
                case STRING:
                    return Type.STRING;
                case INTEGER:
                case DECIMAL:
                case FLOAT:
                case DOUBLE:
                    return Type.DOUBLE;

                default:
                    return mType2;
                }
            }

            if (mType2 == Type.UNTYPED_ATOMIC) {
                switch (mType1) {
                case UNTYPED_ATOMIC:
                case STRING:
                    return Type.STRING;
                case INTEGER:
                case DECIMAL:
                case FLOAT:
                case DOUBLE:
                    return Type.DOUBLE;

                default:
                    return mType1;
                }
            }

        }

        return Type.getLeastCommonType(mType1, mType2);

    }

    // protected void hook(final AtomicValue[] operand1, final AtomicValue[]
    // operand2) {
    //
    // if (operand1.length == 1
    // && operand1[0].getTypeKey() == getTransaction()
    // .keyForName("xs:boolean")) {
    // operand2 = new AtomicValue[1];
    // getOperand2().reset(startKey);
    // operand2[0] = new AtomicValue(Function.ebv(getOperand1()));
    // } else {
    // if (operand2.length == 1
    // && operand2[0].getTypeKey() == getTransaction().keyForName(
    // "xs:boolean")) {
    // operand1 = new AtomicValue[1];
    // getOperand1().reset(startKey);
    // operand1[0] = new AtomicValue(Function.ebv(getOperand2()));
    // }
    // }
    // }

}

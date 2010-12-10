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
import com.treetank.axis.AbsAxis;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.functions.Function;
import com.treetank.service.xml.xpath.types.Type;

import static com.treetank.service.xml.xpath.XPathAxis.XPATH_10_COMP;

/**
 * <h1>AbstractOpAxis</h1>
 * <p>
 * Abstract axis for all operators performing an arithmetic operation.
 * </p>
 */
public abstract class AbsObAxis extends AbsAxis implements IAxis {

    /** First arithmetic operand. */
    private final IAxis mOperand1;

    /** Second arithmetic operand. */
    private final IAxis mOperand2;

    /** True, if axis has not been evaluated yet. */
    private boolean mIsFirst;

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
    public AbsObAxis(final IReadTransaction rtx, final IAxis mOp1, final IAxis mOp2) {

        super(rtx);
        mOperand1 = mOp1;
        mOperand2 = mOp2;
        mIsFirst = true;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void reset(final long mNodeKey) {

        super.reset(mNodeKey);
        mIsFirst = true;
        if (mOperand1 != null) {
            mOperand1.reset(mNodeKey);
        }

        if (mOperand2 != null) {
            mOperand2.reset(mNodeKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {

        resetToLastKey();

        if (mIsFirst) {
            mIsFirst = false;

            if (mOperand1.hasNext()) {
                // atomize operand
                final AtomicValue mItem1 = atomize(mOperand1);

                if (mOperand2.hasNext()) {
                    // atomize operand
                    final AtomicValue mItem2 = atomize(mOperand2);
                    final IItem result = operate(mItem1, mItem2);
                    // add retrieved AtomicValue to item list
                    final int itemKey = getTransaction().getItemList().addItem(result);
                    getTransaction().moveTo(itemKey);

                    return true;
                }
            }

            if (XPATH_10_COMP) { // and empty sequence, return NaN
                final IItem result = new AtomicValue(Double.NaN, Type.DOUBLE);
                final int itemKey = getTransaction().getItemList().addItem(result);
                getTransaction().moveTo(itemKey);
                return true;
            }

        }
        // either not the first call, or empty sequence
        resetToStartKey();
        return false;

    }

    /**
     * Atomizes an operand according to the rules specified in the XPath
     * specification.
     * 
     * @param mOperand
     *            the operand to atomize
     * @return the atomized operand. (always an atomic value)
     */
    private AtomicValue atomize(final IAxis mOperand) {

        final IReadTransaction rtx = getTransaction();
        int type = rtx.getNode().getTypeKey();
        AtomicValue atom;

        if (XPATH_10_COMP) {
            if (type == rtx.keyForName("xs:double") || type == rtx.keyForName("xs:untypedAtomic")
                || type == rtx.keyForName("xs:boolean") || type == rtx.keyForName("xs:string")
                || type == rtx.keyForName("xs:integer") || type == rtx.keyForName("xs:float")
                || type == rtx.keyForName("xs:decimal")) {
                Function.fnnumber(mOperand.getTransaction());
            }

            atom = new AtomicValue(rtx.getNode().getRawValue(), rtx.getNode().getTypeKey());
        } else {
            // unatomicType is cast to double
            if (type == rtx.keyForName("xs:untypedAtomic")) {
                type = rtx.keyForName("xs:double");
                // TODO: throw error, of cast fails
            }

            atom = new AtomicValue(rtx.getNode().getRawValue(), type);
        }

        // if (!XPATH_10_COMP && operand.hasNext()) {
        // throw new XPathError(ErrorType.XPTY0004);
        // }

        return atom;
    }

    /**
     * Performs the operation on the two input operands. First checks if the
     * types of the operands are a valid combination for the operation and if so
     * computed the result. Otherwise an XPathError is thrown.
     * 
     * @param mOperand1
     *            first input operand
     * @param mOperand2
     *            second input operand
     * @return result of the operation
     */
    protected abstract IItem operate(final AtomicValue mOperand1, final AtomicValue mOperand2);

    /**
     * Checks if the types of the operands are a valid combination for the
     * operation and if so returns the corresponding result type. Otherwise an
     * XPathError is thrown. This typed check is done according to the <a
     * href="http://www.w3.org/TR/xpath20/#mapping">Operator Mapping</a>.
     * 
     * @param mOp1
     *            first operand's type key
     * @param mOp2
     *            second operand's type key
     * @return return type of the arithmetic function according to the operand
     *         type combination.
     */
    protected abstract Type getReturnType(final int mOp1, final int mOp2);

}

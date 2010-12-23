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
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.functions.Function;
import com.treetank.utils.TypedValue;

/**
 * <h1>Logical Or Expression</h1>
 * <p>
 * The logical or expression performs a logical disjunction of the boolean values of two input sequences. If a
 * logical expression does not raise an error, its value is always one of the boolean values true or false.
 * </p>
 * <p>
 * The value of an or-expression is determined by the effective boolean values of its operands, as shown in
 * the following table:
 * <table>
 * <tr>
 * <th>OR</th>
 * <th>EBV2 = true</th>
 * <th>EBV2 = false</th>
 * <th>error in EBV2</th>
 * </tr>
 * <tr>
 * <th>EBV1 = true</th>
 * <th>true</th>
 * <th>true</th>
 * <th>true</th>
 * </tr>
 * <tr>
 * <th>EBV1 = false</th>
 * <th>true</th>
 * <th>false</th>
 * <th>error</th>
 * </tr>
 * <tr>
 * <th>error in EBV1</th>
 * <th>error</th>
 * <th>error</th>
 * <th>error</th>
 * </tr>
 * </table>
 */
public class OrExpr extends AbsExpression implements IAxis {

    /** First operand of the logical expression. */
    private final IAxis mOp1;

    /** Second operand of the logical expression. */
    private final IAxis mOp2;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) transaction to iterate with.
     * @param mOperand1
     *            First operand
     * @param mOperand2
     *            Second operand
     */
    public OrExpr(final IReadTransaction rtx, final IAxis mOperand1, final IAxis mOperand2) {

        super(rtx);
        mOp1 = mOperand1;
        mOp2 = mOperand2;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final long mNodeKey) {

        super.reset(mNodeKey);
        if (mOp1 != null) {
            mOp1.reset(mNodeKey);
        }
        if (mOp2 != null) {
            mOp2.reset(mNodeKey);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTXPathException
     */
    @Override
    protected void evaluate() throws TTXPathException {

        // first find the effective boolean values of the two operands, then
        // determine value of the and-expression and store it in am item
        final boolean result = Function.ebv(mOp1) || Function.ebv(mOp2);
        // note: the error handling is implicitly done by the fnBoolean()
        // function.

        // add result item to list and set the item as the current item
        final int itemKey =
            getTransaction().getItemList().addItem(
                new AtomicValue(TypedValue.getBytes(Boolean.toString(result)), getTransaction().keyForName(
                    "xs:boolean")));
        getTransaction().moveTo(itemKey);

    }

}

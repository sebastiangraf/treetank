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

import java.util.List;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.utils.TypedValue;

/**
 * <h1>Some expression</h1>
 * <p>
 * IAxis that represents the quantified expression "some".
 * </p>
 * <p>
 * The quantified expression is true if at least one evaluation of the test expression has the effective
 * boolean value true; otherwise the quantified expression is false. This rule implies that, if the in-clauses
 * generate zero binding tuples, the value of the quantified expression is false.
 * </p>
 */
public class SomeExpr extends AbstractExpression implements IAxis {

    private final List<IAxis> mVars;

    private final IAxis mSatisfy;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mVars
     *            Variables for which the condition must be satisfied
     * @param mSatisfy
     *            condition that must be satisfied by at least one item of the
     *            variable results in order to evaluate expression to true
     */
    public SomeExpr(final IReadTransaction rtx, final List<IAxis> mVars, final IAxis mSatisfy) {

        super(rtx);
        this.mVars = mVars;
        this.mSatisfy = mSatisfy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void reset(final long mNodeKey) {

        super.reset(mNodeKey);

        if (mVars != null) {
            for (IAxis var : mVars) {
                var.reset(mNodeKey);
            }
        }

        if (mSatisfy != null) {
            mSatisfy.reset(mNodeKey);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void evaluate() {

        boolean satisfiesCond = false;

        for (IAxis axis : mVars) {
            while (axis.hasNext()) {
                if (mSatisfy.hasNext()) {
                    // condition is satisfied for this item -> expression is
                    // true
                    satisfiesCond = true;
                    break;
                }
            }
        }

        final int itemKey =
            getTransaction().getItemList().addItem(
                new AtomicValue(TypedValue.getBytes(Boolean.toString(satisfiesCond)), getTransaction()
                    .keyForName("xs:boolean")));
        getTransaction().moveTo(itemKey);

    }
    
    @Override
    public void setTransaction(final IReadTransaction rtx) {
      super.setTransaction(rtx);
      for (IAxis axis : mVars) {
        axis.setTransaction(rtx);
      }
      
      mSatisfy.setTransaction(rtx);
    }

}

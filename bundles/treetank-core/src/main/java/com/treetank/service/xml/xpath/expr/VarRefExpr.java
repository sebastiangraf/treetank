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

/**
 * <h1>VarRefExpr</h1>
 * <p>
 * Reference to the current item of the variable expression.
 * </p>
 */
public class VarRefExpr extends AbsExpression implements IAxis, IObserver {

    /** Key of the item the variable is set to at the moment. */
    private long mVarKey;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mVariable
     *            Reference the variable expression that computes the items the
     *            variable holds.
     */
    public VarRefExpr(final IReadTransaction rtx, final VariableAxis mVariable) {

        super(rtx);
        mVariable.addObserver(this);
        mVarKey = -1;

    }

    /**
     * {@inheritDoc}
     */
    public void update(final long mVarKey) {

        this.mVarKey = mVarKey;
        reset(this.mVarKey);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evaluate() {

        // assure that the transaction is set to the current context item of the
        // variable's binding sequence.
        getTransaction().moveTo(mVarKey);

    }

}

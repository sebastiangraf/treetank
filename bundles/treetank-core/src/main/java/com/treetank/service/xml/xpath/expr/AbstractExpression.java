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
 * $Id: AbstractExpression.java 4246 2008-07-08 08:54:09Z scherer $
 */

package com.treetank.service.xml.xpath.expr;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbstractAxis;

/**
 * <h1>AbstractExpression</h1>
 * <p>
 * Template for all expressions.
 * </p>
 * <p>
 * This class is a template for most complex expressions of the XPath 2.0 language. These expressions work
 * like an axis, as all other XPath 2.0 expressions in this implementation, but the expression is only
 * evaluated once. Therefore the axis returns true only for the first call and false for all others.
 * </p>
 */
public abstract class AbstractExpression extends AbstractAxis implements IAxis {

    /** Defines, whether hasNext() has already been called. */
    private boolean mIsFirst;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     */
    public AbstractExpression(final IReadTransaction rtx) {

        super(rtx);
        mIsFirst = true;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final long nodeKey) {

        super.reset(nodeKey);
        mIsFirst = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {

        resetToLastKey();

        if (mIsFirst) {
            mIsFirst = false;

            // evaluate expression
            evaluate();

            return true;
        } else {

            // only the first call yields to true, all further calls will yield
            // to
            // false. Calling hasNext() makes no sense, since evaluating the
            // expression on the same input would always return the same result.
            resetToStartKey();
            return false;
        }

    }

    /**
     * Performs the expression dependent evaluation of the expression. (Template
     * method)
     */
    protected abstract void evaluate();

}

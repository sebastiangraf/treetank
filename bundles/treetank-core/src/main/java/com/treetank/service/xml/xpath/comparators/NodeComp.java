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

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.EXPathError;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.utils.TypedValue;

/**
 * <h1>NodeComp</h1>
 * <p>
 * Node comparisons are used to compare two nodes, by their identity or by their document order.
 * </p>
 */
public class NodeComp extends AbsComparator implements IAxis {

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mOperand1
     *            First value of the comparison
     * @param mOperand2
     *            Second value of the comparison
     * @param mComp
     *            comparison kind
     */
    public NodeComp(final IReadTransaction rtx, final IAxis mOperand1, final IAxis mOperand2,
        final CompKind mComp) {

        super(rtx, mOperand1, mOperand2, mComp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AtomicValue[] atomize(final IAxis mOperand) throws TTXPathException {

        final IReadTransaction rtx = getTransaction();
        // store item key as atomic value
        final AtomicValue mAtomized =
            new AtomicValue(TypedValue.getBytes(((Long)rtx.getNode().getNodeKey()).toString()), rtx
                .keyForName("xs:integer"));
        final AtomicValue[] op = {
            mAtomized
        };

        // the operands must be singletons in case of a node comparison
        if (mOperand.hasNext()) {
            throw EXPathError.XPTY0004.getEncapsulatedException();
        } else {
            return op;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Type getType(final int mKey1, final int mKey2) {

        return Type.INTEGER;

    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    protected boolean compare(final AtomicValue[] mOperand1, final AtomicValue[] mOperand2)
        throws TTXPathException {

        final String op1 = TypedValue.parseString(mOperand1[0].getRawValue());
        final String op2 = TypedValue.parseString(mOperand2[0].getRawValue());

        return getCompKind().compare(op1, op2, getType(mOperand1[0].getTypeKey(), mOperand2[0].getTypeKey()));
    }

}

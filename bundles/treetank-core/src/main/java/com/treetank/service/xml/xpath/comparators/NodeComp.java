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
 * $Id: NodeComp.java 4246 2008-07-08 08:54:09Z scherer $
 */

package com.treetank.service.xml.xpath.comparators;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.functions.XPathError;
import com.treetank.service.xml.xpath.functions.XPathError.ErrorType;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.utils.TypedValue;

/**
 * <h1>NodeComp</h1>
 * <p>
 * Node comparisons are used to compare two nodes, by their identity or by their document order.
 * </p>
 */
public class NodeComp extends AbstractComparator implements IAxis {

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param operand1
     *            First value of the comparison
     * @param operand2
     *            Second value of the comparison
     * @param comp
     *            comparison kind
     */
    public NodeComp(final IReadTransaction rtx, final IAxis operand1, final IAxis operand2,
        final CompKind comp) {

        super(rtx, operand1, operand2, comp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AtomicValue[] atomize(final IAxis operand) {

        final IReadTransaction rtx = getTransaction();
        // store item key as atomic value
        AtomicValue atomized =
            new AtomicValue(TypedValue.getBytes(((Long)rtx.getNode().getNodeKey()).toString()), rtx
                .keyForName("xs:integer"));
        final AtomicValue[] op = {
            atomized
        };

        // the operands must be singletons in case of a node comparison
        if (operand.hasNext()) {
            throw new XPathError(ErrorType.XPTY0004);
        } else {
            return op;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Type getType(final int key1, final int key2) {

        return Type.INTEGER;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean compare(final AtomicValue[] operand1, final AtomicValue[] operand2) {

        final String op1 = TypedValue.parseString(operand1[0].getRawValue());
        final String op2 = TypedValue.parseString(operand2[0].getRawValue());

        return getCompKind().compare(op1, op2, getType(operand1[0].getTypeKey(), operand2[0].getTypeKey()));
    }

}

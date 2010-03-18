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
 * $Id: ValueComp.java 4246 2008-07-08 08:54:09Z scherer $
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
 * <h1>ValueComp</h1>
 * <p>
 * Value comparisons are used for comparing single values.
 * </p>
 * 
 */
public class ValueComp extends AbstractComparator {

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
    public ValueComp(final IReadTransaction rtx, final IAxis operand1,
            final IAxis operand2, final CompKind comp) {

        super(rtx, operand1, operand2, comp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean compare(final AtomicValue[] operand1,
            final AtomicValue[] operand2) {
        final Type type = getType(operand1[0].getTypeKey(), operand2[0]
                .getTypeKey());
        final String op1 = TypedValue.parseString(operand1[0].getRawValue());
        final String op2 = TypedValue.parseString(operand2[0].getRawValue());

        return getCompKind().compare(op1, op2, type);
    }

    /**
     * {@inheritDoc}
     */
    protected AtomicValue[] atomize(final IAxis operand) {

        final IReadTransaction trx = getTransaction();

        int type = trx.getNode().getTypeKey();

        // (3.) if type is untypedAtomic, cast to string
        if (type == trx.keyForName("xs:unytpedAtomic")) {
            type = trx.keyForName("xs:string");
        }

        final AtomicValue atomized = new AtomicValue(operand.getTransaction()
                .getNode().getRawValue(), type);
        AtomicValue[] op = { atomized };

        // (4.) the operands must be singletons in case of a value comparison
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

        Type type1 = Type.getType(key1).getPrimitiveBaseType();
        Type type2 = Type.getType(key2).getPrimitiveBaseType();
        return Type.getLeastCommonType(type1, type2);

    }

}

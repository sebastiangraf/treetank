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
import com.treetank.service.xml.xpath.SingleType;
import com.treetank.service.xml.xpath.XPathError;
import com.treetank.service.xml.xpath.XPathError.ErrorType;
import com.treetank.service.xml.xpath.types.Type;
import com.treetank.utils.TypedValue;

/**
 * <h1>CastExpr</h1>
 * <p>
 * The cast expression cast a given value into a given target type.
 * </p>
 * <p>
 * Occasionally it is necessary to convert a value to a specific datatype. For this purpose, XPath provides a
 * cast expression that creates a new value of a specific type based on an existing value. A cast expression
 * takes two operands: an input expression and a target type. The type of the input expression is called the
 * input or source type. The target type must be an atomic type that is in the in-scope schema types
 * [err:XPST0051]. In addition, the target type cannot be xs:NOTATION or xs:anyAtomicType [err:XPST0080]. The
 * optional occurrence indicator "?" denotes that an empty sequence is permitted.
 * </p>
 */
public class CastExpr extends AbsExpression implements IAxis {

    /** The input expression to cast to a specified target expression. */
    private final IAxis mSourceExpr;

    /** The type, to which the input expression will be casted to. */
    private final Type mTargetType;

    /** Defines, whether an empty sequence will be casted to any target type. */
    private final boolean mPermitEmptySeq;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param rtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mInputExpr
     *            input expression, that will be casted.
     * @param mTarget
     *            Type the input expression will be casted to.
     */
    public CastExpr(final IReadTransaction rtx, final IAxis mInputExpr, final SingleType mTarget) {

        super(rtx);
        mSourceExpr = mInputExpr;
        mTargetType = mTarget.getAtomic();
        mPermitEmptySeq = mTarget.hasInterogation();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final long mNodeKey) {

        super.reset(mNodeKey);
        if (mSourceExpr != null) {
            mSourceExpr.reset(mNodeKey);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evaluate() throws TTXPathException {

        // atomic type must not be xs:anyAtomicType or xs:NOTATION
        if (mTargetType == Type.ANY_ATOMIC_TYPE || mTargetType == Type.NOTATION) {
            throw new XPathError(ErrorType.XPST0080);
        }

        if (mSourceExpr.hasNext()) {

            final Type sourceType = Type.getType(getTransaction().getNode().getTypeKey());
            final String sourceValue = TypedValue.parseString(getTransaction().getNode().getRawValue());

            // cast source to target type, if possible
            if (sourceType.isCastableTo(mTargetType, sourceValue)) {
                throw new IllegalStateException("casts not implemented yet.");
                // ((XPathReadTransaction)
                // getTransaction()).castTo(mTargetType);
            }

            // 2. if the result sequence of the input expression has more than
            // one
            // items, a type error is raised.
            if (mSourceExpr.hasNext()) {
                throw new XPathError(ErrorType.XPTY0004);
            }

        } else {
            // 3. if is empty sequence:
            if (!mPermitEmptySeq) {
                // if '?' is specified after the target type, the result is an
                // empty sequence (which means, nothing is changed),
                // otherwise an error is raised.
                throw new XPathError(ErrorType.XPTY0004);

            }
        }

    }

}

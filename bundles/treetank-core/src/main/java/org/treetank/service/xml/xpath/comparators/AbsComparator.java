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

package org.treetank.service.xml.xpath.comparators;

import org.treetank.api.IItem;
import org.treetank.api.IReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.exception.TTXPathException;
import org.treetank.service.xml.xpath.AtomicValue;
import org.treetank.service.xml.xpath.expr.LiteralExpr;
import org.treetank.service.xml.xpath.types.Type;

/**
 * <h1>AbstractComparator</h1>
 * <p>
 * Abstract axis that evaluates a comparison.
 * </p>
 */
public abstract class AbsComparator extends AbsAxis  {

    /** Kind of comparison. */
    private final CompKind mComp;

    /** First value of the comparison. */
    private final AbsAxis mOperand1;

    /** Second value of the comparison. */
    private final AbsAxis mOperand2;

    /** Is first evaluation? */
    private boolean mIsFirst;

    /**
     * Constructor. Initializes the internal state.
     * 
     * @param mRtx
     *            Exclusive (immutable) trx to iterate with.
     * @param mOperand1
     *            First value of the comparison
     * @param mOperand2
     *            Second value of the comparison
     * @param mComp
     *            comparison kind
     */
    public AbsComparator(final IReadTransaction mRtx, final AbsAxis mOperand1, final AbsAxis mOperand2,
        final CompKind mComp) {

        super(mRtx);
        this.mComp = mComp;
        this.mOperand1 = mOperand1;
        this.mOperand2 = mOperand2;
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
    public final boolean hasNext() {

        resetToLastKey();

        if (mIsFirst) {
            mIsFirst = false;

            // TODO: why?
            if (!(mOperand1 instanceof LiteralExpr)) {
                mOperand1.reset(getTransaction().getNode().getNodeKey());
            }

            // TODO: why?
            if (!(mOperand2 instanceof LiteralExpr)) {
                mOperand2.reset(getTransaction().getNode().getNodeKey());
            }

            /*
             * Evaluates the comparison. First atomizes both operands and then
             * executes the comparison on them. At the end, the transaction is
             * set to the retrieved result item.
             */
            if (mOperand1.hasNext()) {
                try {
                    // atomize operands
                    final AtomicValue[] operandOne = atomize(mOperand1);
                    if (mOperand2.hasNext()) {
                        final AtomicValue[] operandTwo = atomize(mOperand2);

                        hook(operandOne, operandTwo);
                        try {
                            // get comparison result
                            final boolean resultValue = compare(operandOne, operandTwo);
                            final IItem result = new AtomicValue(resultValue);

                            // add retrieved AtomicValue to item list
                            final int itemKey = getTransaction().getItemList().addItem(result);
                            getTransaction().moveTo(itemKey);
                        } catch (TTXPathException e) {
                            throw new RuntimeException(e);
                        }
                        return true;

                    }
                } catch (final TTXPathException exc) {
                    throw new RuntimeException(exc);
                }
            }
        }
        // return empty sequence or function called more than once
        resetToStartKey();
        return false;
    }

    /**
     * Allowes the general comparisons to do some extra functionality.
     * 
     * @param paramOperandOne
     *            first operand
     * @param paramOperandTwo
     *            second operand
     */
    protected void hook(final AtomicValue[] paramOperandOne, final AtomicValue[] paramOperandTwo) {

        // do nothing
    }

    /**
     * Performs the comparison of two atomic values.
     * 
     * @param paramOperandOne
     *            first comparison operand.
     * @param paramOperandTwo
     *            second comparison operand.
     * @return the result of the comparison
     */
    protected abstract boolean compare(final AtomicValue[] paramOperandOne,
        final AtomicValue[] paramOperandTwo) throws TTXPathException;

    /**
     * Atomizes an operand according to the rules specified in the XPath
     * specification.
     * 
     * @param paramOperand
     *            the operand that will be atomized.
     * @return the atomized operand. (always an atomic value)
     * @throws TTXPathException
     *             if any goes wrong.
     */
    protected abstract AtomicValue[] atomize(final AbsAxis paramOperand) throws TTXPathException;

    /**
     * Returns the common comparable type of the two operands, or an error, if
     * the two operands don't have a common type on which a comparison is
     * allowed according to the XPath 2.0 specification.
     * 
     * @param mKey1
     *            first comparison operand's type key
     * @param mKey2
     *            second comparison operand's type key
     * @return the type the comparison can be evaluated on
     */
    protected abstract Type getType(final int mKey1, final int mKey2) throws TTXPathException;

    /**
     * Getting CompKind for this Comparator.
     * 
     * @return comparison kind
     */
    public final CompKind getCompKind() {
        return mComp;
    }

    /**
     * Factory method to implement the comparator.
     * 
     * @param paramRtx
     *            rtx for accessing data
     * @param paramOperandOne
     *            operand one to be compared
     * @param paramOperandTwo
     *            operand two to be compared
     * @param paramKind
     *            kind of comparison
     * @param paramVal
     *            string value to estimate
     * @return
     */
    public static final AbsComparator getComparator(final IReadTransaction paramRtx,
        final AbsAxis paramOperandOne, final AbsAxis paramOperandTwo, final CompKind paramKind,
        final String paramVal) {
        if ("eq".equals(paramVal) || "lt".equals(paramVal) || "le".equals(paramVal) || "gt".equals(paramVal)
            || "ge".equals(paramVal)) {
            return new ValueComp(paramRtx, paramOperandOne, paramOperandTwo, paramKind);
        } else if ("=".equals(paramVal) || "!=".equals(paramVal) || "<".equals(paramVal)
            || "<=".equals(paramVal) || ">".equals(paramVal) || ">=".equals(paramVal)) {
            return new GeneralComp(paramRtx, paramOperandOne, paramOperandTwo, paramKind);
        } else if ("is".equals(paramVal) || "<<".equals(paramVal) || ">>".equals(paramVal)) {
            new NodeComp(paramRtx, paramOperandOne, paramOperandTwo, paramKind);
        }
        throw new IllegalStateException(paramVal + " is not a valid comparison.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setTransaction(final IReadTransaction rtx) {
      super.setTransaction(rtx);
      mOperand1.setTransaction(rtx);
      mOperand2.setTransaction(rtx);
    }

}

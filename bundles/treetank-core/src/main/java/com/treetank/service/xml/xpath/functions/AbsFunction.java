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

package com.treetank.service.xml.xpath.functions;

import java.util.List;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.xpath.AtomicValue;
import com.treetank.service.xml.xpath.EXPathError;
import com.treetank.service.xml.xpath.expr.AbsExpression;

/**
 * <h1>AbstractFunction</h1>
 * <p>
 * Abstract super class for all function classes.
 * </p>
 * <p>
 * All functions that extend the abstract class only need take care of the result computation. Everything
 * else, like checking if arguments are valid and adding the result with the corresponding type to the
 * transaction list is done by the abstract super class.
 * </p>
 * <h2>Developer Example</h2>
 * 
 * <p>
 * 
 * <pre>
 *   Must extend &lt;code&gt;AbstractFunction&lt;/code&gt; and implement &lt;code&gt;IAxis&lt;/code&gt;.
 *   And the implement the abstract method computeResult(), that returns the 
 *   computed value as a bye-array
 *   
 *   
 *   
 *   public class ExampleFunctionAxis extends AbstractFunction implements IAxis {
 *  
 *     public ExampleAxis(final IReadTransaction rtx, final List&lt;IAxis&gt; args,
 *       final int min, final int max, final int returnType) {
 *       // Must be called as first.
 *       super(rtx, args, min, max, returnType);
 *     }
 *     protected byte[] computeResult() {
 *       .... compute value and return as byte array
 *     }
 *   
 *   }
 * </pre>
 * 
 * </p>
 */
public abstract class AbsFunction extends AbsExpression {

    /** The function's arguments. */
    private final List<IAxis> mArgs;

    /** Minimum number of possible function arguments. */
    private final int mMin;

    /** Maximum number of possible function arguments. */
    private final int mMax;

    /** The function's return type. */
    private final int mReturnType;

    /**
     * Constructor. Initializes internal state and do a statical analysis
     * concerning the function's arguments.
     * 
     * @param rtx
     *            Transaction to operate on
     * @param args
     *            List of function arguments
     * @param min
     *            min number of allowed function arguments
     * @param max
     *            max number of allowed function arguments
     * @param returnType
     *            the type that the function's result will have
     * @throws TTXPathException
     *             if the verify process is failing.
     */
    public AbsFunction(final IReadTransaction rtx, final List<IAxis> args, final int min, final int max,
        final int returnType) throws TTXPathException {

        super(rtx);
        mArgs = args;
        mMin = min;
        mMax = max;
        mReturnType = returnType;
        varifyParam(args.size());
    }

    /**
     * Checks if the number of input arguments of this function is a valid
     * according to the function specification in <a
     * href="http://www.w3.org/TR/xquery-operators/"> XQuery 1.0 and XPath 2.0
     * Functions and Operators</a>. Throws an XPath error in case of a non-valid
     * number.
     * 
     * @param mNumber
     *            number of given function arguments
     * @throws TTXPathException
     *             if function call fails.
     */
    public final void varifyParam(final int mNumber) throws TTXPathException {

        if (mNumber < mMin || mNumber > mMax) {
            throw EXPathError.XPST0017.getEncapsulatedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(final long mNodeKey) {

        super.reset(mNodeKey);
        if (mArgs != null) {
            for (IAxis ax : mArgs) {
                ax.reset(mNodeKey);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evaluate() throws TTXPathException {

        // compute the function's result
        final byte[] value = computeResult();

        // create an atomic value, add it to the list and move the cursor to it.
        final int itemKey = getTransaction().getItemList().addItem(new AtomicValue(value, mReturnType));
        getTransaction().moveTo(itemKey);

    }

    /**
     * Computes the result value of the function. This implementation is acts as
     * a hook operation and needs to be overridden by the concrete function
     * classes, otherwise an exception is thrown.
     * 
     * @return value of the result
     * @throws TTXPathException
     *             if anythin odd happens while execution
     */
    protected abstract byte[] computeResult() throws TTXPathException;

    /**
     * @return the list of function arguments
     */
    protected List<IAxis> getArgs() {

        return mArgs;
    }

}

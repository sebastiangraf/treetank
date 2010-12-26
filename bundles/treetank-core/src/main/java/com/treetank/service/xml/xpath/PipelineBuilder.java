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

package com.treetank.service.xml.xpath;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.treetank.api.IAxis;
import com.treetank.api.IFilter;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.FilterAxis;
import com.treetank.exception.TTXPathException;
import com.treetank.service.xml.xpath.comparators.AbsComparator;
import com.treetank.service.xml.xpath.comparators.CompKind;
import com.treetank.service.xml.xpath.expr.AndExpr;
import com.treetank.service.xml.xpath.expr.CastExpr;
import com.treetank.service.xml.xpath.expr.CastableExpr;
import com.treetank.service.xml.xpath.expr.EveryExpr;
import com.treetank.service.xml.xpath.expr.ExceptAxis;
import com.treetank.service.xml.xpath.expr.ForAxis;
import com.treetank.service.xml.xpath.expr.IfAxis;
import com.treetank.service.xml.xpath.expr.InstanceOfExpr;
import com.treetank.service.xml.xpath.expr.IntersectAxis;
import com.treetank.service.xml.xpath.expr.LiteralExpr;
import com.treetank.service.xml.xpath.expr.OrExpr;
import com.treetank.service.xml.xpath.expr.RangeAxis;
import com.treetank.service.xml.xpath.expr.SequenceAxis;
import com.treetank.service.xml.xpath.expr.SomeExpr;
import com.treetank.service.xml.xpath.expr.UnionAxis;
import com.treetank.service.xml.xpath.expr.VarRefExpr;
import com.treetank.service.xml.xpath.expr.VariableAxis;
import com.treetank.service.xml.xpath.filter.DupFilterAxis;
import com.treetank.service.xml.xpath.filter.PredicateFilterAxis;
import com.treetank.service.xml.xpath.functions.AbsFunction;
import com.treetank.service.xml.xpath.functions.FuncDef;
import com.treetank.service.xml.xpath.operators.AddOpAxis;
import com.treetank.service.xml.xpath.operators.DivOpAxis;
import com.treetank.service.xml.xpath.operators.IDivOpAxis;
import com.treetank.service.xml.xpath.operators.ModOpAxis;
import com.treetank.service.xml.xpath.operators.MulOpAxis;
import com.treetank.service.xml.xpath.operators.SubOpAxis;
import com.treetank.utils.FastStack;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * <h1>PipeBuilder</h1>
 * <p>
 * Builder of a query execution plan in the pipeline manner.
 * </p>
 */
public final class PipelineBuilder {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(PipelineBuilder.class));

    /** Stack of pipeline builder which stores expressions. */
    private final FastStack<FastStack<ExpressionSingle>> mExprStack;

    /** Maps a variable name to the item that the variable holds. */
    private final Map<String, IAxis> mVarRefMap;

    /**
     * Constructor.
     */
    public PipelineBuilder() {

        mExprStack = new FastStack<FastStack<ExpressionSingle>>();

        mVarRefMap = new HashMap<String, IAxis>();

    }

    /**
     * @return the current pipeline stack
     */
    public FastStack<ExpressionSingle> getPipeStack() {

        if (mExprStack.size() == 0) {
            throw new IllegalStateException("No pipe on the stack");
        }
        return mExprStack.peek();
    }

    /**
     * Adds a new pipeline stack to the stack holding all expressions.
     */
    public void addExpr() {

        mExprStack.push(new FastStack<ExpressionSingle>());
    }

    /**
     * Ends an expression. This means that the currently used pipeline stack
     * will be emptied and the singleExpressions that were on the stack are
     * combined by a sequence expression, which is lated added to the next
     * pipeline stack.
     * 
     * @param mTransaction
     *            transaction to operate on
     * @param mNum
     *            number of singleExpressions that will be added to the sequence
     */
    public void finishExpr(final IReadTransaction mTransaction, final int mNum) {

        // all singleExpression that are on the stack will be combined in the
        // sequence, so the number of singleExpressions in the sequence and the
        // size
        // of the stack containing these SingleExpressions have to be the same.
        if (getPipeStack().size() != mNum) {
            // this should never happen
            throw new IllegalStateException("The query has not been processed correctly");
        }
        int no = mNum;

        IAxis[] axis;
        if (no > 1) {

            axis = new IAxis[no];

            // add all SingleExpression to a list
            while (no-- > 0) {
                axis[no] = getPipeStack().pop().getExpr();
            }

            if (mExprStack.size() > 1) {
                assert mExprStack.peek().empty();
                mExprStack.pop();
            }

            if (getPipeStack().empty() || getExpression().getSize() != 0) {
                addExpressionSingle();
            }
            getExpression().add(new SequenceAxis(mTransaction, axis));

        } else if (no == 1) {
            // only one expression does not need to be capsled by a seq
            axis = new IAxis[1];
            axis[0] = getPipeStack().pop().getExpr();

            if (mExprStack.size() > 1) {
                assert mExprStack.peek().empty();
                mExprStack.pop();
            }

            if (getPipeStack().empty() || getExpression().getSize() != 0) {
                addExpressionSingle();

            }

            final IAxis iAxis;
            if (mExprStack.size() == 1 && getPipeStack().size() == 1 && getExpression().getSize() == 0) {
                iAxis = new SequenceAxis(mTransaction, axis);
            } else {
                iAxis = axis[0];
            }

            getExpression().add(iAxis);
        } else {
            mExprStack.pop();
        }

    }

    /**
     * Adds a new single expression to the pipeline. This is done by adding a
     * complete new chain to the stack because the new single expression has
     * nothing in common with the previous expressions.
     */
    public void addExpressionSingle() {

        // A new single expression is completely independent from the previous
        // expression, therefore a new expression chain is build and added to
        // the
        // stack.
        getPipeStack().push(new ExpressionSingle());
    }

    /**
     * Returns the current pipeline. If there is no existing pipeline, a new one
     * is generated and returned.
     * 
     * @return a reference to the currently used pipeline.
     */
    public ExpressionSingle getExpression() {

        return getPipeStack().peek();
    }

    /**
     * Adds a for expression to the pipeline. In case the for expression has
     * more then one for condition, the for expression is converted to a nested
     * for expression with only one for condition each, see the following
     * example: for $a in /a, $b in /b, $c in /c return /d is converted to for
     * $a in /a return for $b in /b return for $c in /c return /d
     * 
     * @param mForConditionNum
     *            Number of all for conditions of the expression
     */
    public void addForExpression(final int mForConditionNum) {

        assert getPipeStack().size() >= (mForConditionNum + 1);

        AbsAxis forAxis = (AbsAxis)(getPipeStack().pop().getExpr());
        final IReadTransaction rtx = forAxis.getTransaction();
        int num = mForConditionNum;

        while (num-- > 0) {
            forAxis = new ForAxis(rtx, getPipeStack().pop().getExpr(), forAxis);
        }

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(forAxis);
    }

    /**
     * Adds a if expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     */
    public void addIfExpression(final IReadTransaction mTransaction) {

        assert getPipeStack().size() >= 3;

        final IReadTransaction rtx = mTransaction;

        final IAxis elseExpr = getPipeStack().pop().getExpr();
        final IAxis thenExpr = getPipeStack().pop().getExpr();
        final IAxis ifExpr = getPipeStack().pop().getExpr();

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(new IfAxis(rtx, ifExpr, thenExpr, elseExpr));

    }

    /**
     * Adds a comparison expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mComp
     *            Comparator type.
     */
    public void addCompExpression(final IReadTransaction mTransaction, final String mComp) {

        assert getPipeStack().size() >= 2;

        final IReadTransaction rtx = mTransaction;

        final IAxis paramOperandTwo = getPipeStack().pop().getExpr();
        final IAxis paramOperandOne = getPipeStack().pop().getExpr();

        final CompKind kind = CompKind.fromString(mComp);
        final IAxis axis = AbsComparator.getComparator(rtx, paramOperandOne, paramOperandTwo, kind, mComp);

        // // TODO: use typeswitch of JAVA 7
        // if (mComp.equals("eq")) {
        //
        // axis = new ValueComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals("ne")) {
        //
        // axis = new ValueComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals("lt")) {
        //
        // axis = new ValueComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals("le")) {
        //
        // axis = new ValueComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals("gt")) {
        //
        // axis = new ValueComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals("ge")) {
        //
        // axis = new ValueComp(rtx, paramOperandOne, paramOperandTwo, kind);
        //
        // } else if (mComp.equals("=")) {
        //
        // axis = new GeneralComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals("!=")) {
        //
        // axis = new GeneralComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals("<")) {
        //
        // axis = new GeneralComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals("<=")) {
        //
        // axis = new GeneralComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals(">")) {
        //
        // axis = new GeneralComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals(">=")) {
        //
        // axis = new GeneralComp(rtx, paramOperandOne, paramOperandTwo, kind);
        //
        // } else if (mComp.equals("is")) {
        //
        // axis = new NodeComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals("<<")) {
        //
        // axis = new NodeComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else if (mComp.equals(">>")) {
        //
        // axis = new NodeComp(rtx, paramOperandOne, paramOperandTwo, kind);
        // } else {
        // throw new IllegalStateException(mComp + " is not a valid comparison.");
        //
        // }

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);

    }

    /**
     * Adds an operator expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mOperator
     *            Operator type.
     */
    public void addOperatorExpression(final IReadTransaction mTransaction, final String mOperator) {

        assert getPipeStack().size() >= 1;

        final IReadTransaction rtx = mTransaction;

        final IAxis mOperand2 = getPipeStack().pop().getExpr();

        // the unary operation only has one operator
        final IAxis mOperand1 = getPipeStack().pop().getExpr();
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }

        final IAxis axis;

        // TODO: use typeswitch of JAVA 7
        if (mOperator.equals("+")) {
            axis = new AddOpAxis(rtx, mOperand1, mOperand2);
        } else if (mOperator.equals("-")) {

            axis = new SubOpAxis(rtx, mOperand1, mOperand2);
        } else if (mOperator.equals("*")) {
            axis = new MulOpAxis(rtx, mOperand1, mOperand2);
        } else if (mOperator.equals("div")) {
            axis = new DivOpAxis(rtx, mOperand1, mOperand2);
        } else if (mOperator.equals("idiv")) {
            axis = new IDivOpAxis(rtx, mOperand1, mOperand2);
        } else if (mOperator.equals("mod")) {
            axis = new ModOpAxis(rtx, mOperand1, mOperand2);
        } else {
            // TODO: unary operator
            throw new IllegalStateException(mOperator + " is not a valid operator.");

        }

        getExpression().add(axis);
    }

    /**
     * Adds a union expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     */
    public void addUnionExpression(final IReadTransaction mTransaction) {

        assert getPipeStack().size() >= 2;

        final IAxis mOperand2 = getPipeStack().pop().getExpr();
        final IAxis mOperand1 = getPipeStack().pop().getExpr();
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(
            new DupFilterAxis(mTransaction, new UnionAxis(mTransaction, mOperand1, mOperand2)));
    }

    /**
     * Adds a and expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     */
    public void addAndExpression(final IReadTransaction mTransaction) {
        assert getPipeStack().size() >= 2;

        final IAxis mOperand2 = getPipeStack().pop().getExpr();
        final IAxis operand1 = getPipeStack().pop().getExpr();
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(new AndExpr(mTransaction, operand1, mOperand2));
    }

    /**
     * Adds a or expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     */
    public void addOrExpression(final IReadTransaction mTransaction) {

        assert getPipeStack().size() >= 2;

        final IAxis mOperand2 = getPipeStack().pop().getExpr();
        final IAxis mOperand1 = getPipeStack().pop().getExpr();

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(new OrExpr(mTransaction, mOperand1, mOperand2));
    }

    /**
     * Adds a intersect or a exception expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mIsIntersect
     *            true, if expression is an intersection
     */
    public void addIntExcExpression(final IReadTransaction mTransaction, final boolean mIsIntersect) {

        assert getPipeStack().size() >= 2;

        final IReadTransaction rtx = mTransaction;

        final IAxis mOperand2 = getPipeStack().pop().getExpr();
        final IAxis mOperand1 = getPipeStack().pop().getExpr();

        final IAxis axis =
            mIsIntersect ? new IntersectAxis(rtx, mOperand1, mOperand2) : new ExceptAxis(rtx, mOperand1,
                mOperand2);

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);
    }

    /**
     * Adds a literal expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mItemKey
     *            key of the literal expression.
     */
    public void addLiteral(final IReadTransaction mTransaction, final int mItemKey) {

        // addExpressionSingle();
        getExpression().add(new LiteralExpr(mTransaction, mItemKey));
    }

    /**
     * Adds a step to the pipeline.
     * 
     * @param axis
     *            the axis step to add to the pipeline.
     */
    public void addStep(final IAxis axis) {

        getExpression().add(axis);
    }

    /**
     * Adds a step to the pipeline.
     * 
     * @param axis
     *            the axis step to add to the pipeline.
     * @param mFilter
     *            the node test to add to the pipeline.
     */
    public void addStep(final AbsAxis axis, final IFilter mFilter) {

        getExpression().add(new FilterAxis(axis, mFilter));
    }

    /**
     * Returns a queue of all pipelines build so far and empties the pipeline
     * stack.
     * 
     * @return all build pipelines
     */
    public IAxis getPipeline() {

        assert getPipeStack().size() <= 1;

        if (getPipeStack().size() == 1 && mExprStack.size() == 1) {
            return getPipeStack().pop().getExpr();
        } else {
            throw new IllegalStateException("Query was not build correctly.");
        }

    }

    /**
     * Adds a predicate to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     */
    public void addPredicate(final IReadTransaction mTransaction) {

        assert getPipeStack().size() >= 2;

        final IAxis mPredicate = getPipeStack().pop().getExpr();

        if (mPredicate instanceof LiteralExpr) {

            mPredicate.hasNext();
            // if is numeric literal -> abbrev for position()
            final int type = mTransaction.getNode().getTypeKey();
            if (type == mTransaction.keyForName("xs:integer") || type == mTransaction.keyForName("xs:double")
                || type == mTransaction.keyForName("xs:float")
                || type == mTransaction.keyForName("xs:decimal")) {

                throw new IllegalStateException("function fn:position() is not implemented yet.");

                // getExpression().add(
                // new PosFilter(transaction, (int)
                // Double.parseDouble(transaction
                // .getValue())));
                // return; // TODO: YES! it is dirty!

                // AtomicValue pos =
                // new AtomicValue(mTransaction.getNode().getRawValue(), mTransaction
                // .keyForName("xs:integer"));
                // long position = mTransaction.getItemList().addItem(pos);
                // mPredicate.reset(mTransaction.getNode().getNodeKey());
                // IAxis function =
                // new FNPosition(mTransaction, new ArrayList<IAxis>(), FuncDef.POS.getMin(), FuncDef.POS
                // .getMax(), mTransaction.keyForName(FuncDef.POS.getReturnType()));
                // IAxis expectedPos = new LiteralExpr(mTransaction, position);
                //
                // mPredicate = new ValueComp(mTransaction, function, expectedPos, CompKind.EQ);

            }
        }

        getExpression().add(new PredicateFilterAxis(mTransaction, mPredicate));

    }

    /**
     * Adds a SomeExpression or an EveryExpression to the pipeline, depending on
     * the parameter isSome.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mIsSome
     *            defines whether a some- or an EveryExpression is used.
     * @param mVarNum
     *            number of binding variables
     */
    public void addQuantifierExpr(final IReadTransaction mTransaction, final boolean mIsSome,
        final int mVarNum) {

        assert getPipeStack().size() >= (mVarNum + 1);

        final IAxis satisfy = getPipeStack().pop().getExpr();
        final List<IAxis> vars = new ArrayList<IAxis>();
        int num = mVarNum;

        while (num-- > 0) {
            // invert current order of variables to get original variable order
            vars.add(num, getPipeStack().pop().getExpr());
        }

        final IAxis mAxis =
            mIsSome ? new SomeExpr(mTransaction, vars, satisfy) : new EveryExpr(mTransaction, vars, satisfy);

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(mAxis);
    }

    /**
     * Adds a castable expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mSingleType
     *            single type the context item will be casted to.
     */
    public void addCastableExpr(final IReadTransaction mTransaction, final SingleType mSingleType) {

        assert getPipeStack().size() >= 1;

        final IAxis candidate = getPipeStack().pop().getExpr();

        final IAxis axis = new CastableExpr(mTransaction, candidate, mSingleType);
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);

    }

    /**
     * Adds a range expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     */
    public void addRangeExpr(final IReadTransaction mTransaction) {

        assert getPipeStack().size() >= 2;

        final IAxis mOperand2 = getPipeStack().pop().getExpr();
        final IAxis mOperand1 = getPipeStack().pop().getExpr();

        final IAxis axis = new RangeAxis(mTransaction, mOperand1, mOperand2);
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);

    }

    /**
     * Adds a cast expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mSingleType
     *            single type the context item will be casted to.
     */
    public void addCastExpr(final IReadTransaction mTransaction, final SingleType mSingleType) {

        assert getPipeStack().size() >= 1;

        final IAxis candidate = getPipeStack().pop().getExpr();

        final IAxis axis = new CastExpr(mTransaction, candidate, mSingleType);
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);

    }

    /**
     * Adds a instance of expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mSequenceType
     *            sequence type the context item should match.
     */
    public void addInstanceOfExpr(final IReadTransaction mTransaction, final SequenceType mSequenceType) {

        assert getPipeStack().size() >= 1;

        final IAxis candidate = getPipeStack().pop().getExpr();

        final IAxis axis = new InstanceOfExpr(mTransaction, candidate, mSequenceType);
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);

    }

    /**
     * Adds a treat as expression to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mSequenceType
     *            sequence type the context item will be treated as.
     */
    public void addTreatExpr(final IReadTransaction mTransaction, final SequenceType mSequenceType) {

        throw new IllegalStateException("the Treat expression is not supported yet");

    }

    /**
     * Adds a variable expression to the pipeline. Adds the expression that will
     * evaluate the results the variable holds.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mVarName
     *            name of the variable
     */
    public void addVariableExpr(final IReadTransaction mTransaction, final String mVarName) {

        assert getPipeStack().size() >= 1;

        final IAxis bindingSeq = getPipeStack().pop().getExpr();

        final IAxis axis = new VariableAxis(mTransaction, bindingSeq);
        mVarRefMap.put(mVarName, axis);

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);
    }

    /**
     * Adds a function to the pipeline.
     * 
     * @param mTransaction
     *            Transaction to operate with.
     * @param mFuncName
     *            The name of the function
     * @param mNum
     *            The number of arguments that are passed to the function
     * @throws TTXPathException
     *             if function can't be added
     */
    public void addFunction(final IReadTransaction mTransaction, final String mFuncName, final int mNum)
        throws TTXPathException {

        assert getPipeStack().size() >= mNum;

        final List<IAxis> args = new ArrayList<IAxis>(mNum);
        // arguments are stored on the stack in reverse order -> invert arg
        // order
        for (int i = 0; i < mNum; i++) {
            args.add(getPipeStack().pop().getExpr());
        }

        // get right function type
        final FuncDef func;
        try {
            func = FuncDef.fromString(mFuncName);
        } catch (final NullPointerException e) {
            LOGWRAPPER.error(e);
            throw EXPathError.XPST0017.getEncapsulatedException();
        }

        // get function class
        final Class<? extends AbsFunction> function = func.getFunc();
        final Integer min = func.getMin();
        final Integer max = func.getMax();
        final Integer returnType = mTransaction.keyForName(func.getReturnType());

        // parameter types of the function's constructor
        final Class<?>[] paramTypes = {
            IReadTransaction.class, List.class, Integer.TYPE, Integer.TYPE, Integer.TYPE
        };

        try {
            // instantiate function class with right constructor
            final Constructor<?> cons = function.getConstructor(paramTypes);
            final IAxis axis = (IAxis)cons.newInstance(mTransaction, args, min, max, returnType);

            if (getPipeStack().empty() || getExpression().getSize() != 0) {
                addExpressionSingle();
            }
            getExpression().add(axis);

        } catch (final NoSuchMethodException e) {
            LOGWRAPPER.error(e);
            throw EXPathError.XPST0017.getEncapsulatedException();
        } catch (final IllegalArgumentException e) {
            LOGWRAPPER.error(e);
            throw EXPathError.XPST0017.getEncapsulatedException();
        } catch (final InstantiationException e) {
            LOGWRAPPER.error(e);
            throw new IllegalStateException("Function not implemented yet.");
        } catch (final IllegalAccessException e) {
            LOGWRAPPER.error(e);
            throw EXPathError.XPST0017.getEncapsulatedException();
        } catch (final InvocationTargetException e) {
            LOGWRAPPER.error(e);
            throw EXPathError.XPST0017.getEncapsulatedException();
        }

    }

    /**
     * Adds a VarRefExpr to the pipeline. This Expression holds a reference to
     * the current context item of the specified variable.
     * 
     * @param mTransaction
     *            the transaction to operate on.
     * @param mVarName
     *            the name of the variable
     */
    public void addVarRefExpr(final IReadTransaction mTransaction, final String mVarName) {

        final VariableAxis axis = (VariableAxis)mVarRefMap.get(mVarName);
        if (axis != null) {
            getExpression().add(new VarRefExpr(mTransaction, axis));
        } else {
            throw new IllegalStateException("Variable " + mVarName + " unkown.");
        }

    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return new StringBuilder("Expression Stack: ").append(this.mExprStack).append("\nHashMap: ").append(
            this.mVarRefMap).toString();
    }

}

/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.service.xml.xpath;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.treetank.api.INodeReadTransaction;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.FilterAxis;
import org.treetank.axis.filter.AbsFilter;
import org.treetank.exception.TTXPathException;
import org.treetank.service.xml.xpath.comparators.AbsComparator;
import org.treetank.service.xml.xpath.comparators.CompKind;
import org.treetank.service.xml.xpath.expr.AndExpr;
import org.treetank.service.xml.xpath.expr.CastExpr;
import org.treetank.service.xml.xpath.expr.CastableExpr;
import org.treetank.service.xml.xpath.expr.EveryExpr;
import org.treetank.service.xml.xpath.expr.ExceptAxis;
import org.treetank.service.xml.xpath.expr.ForAxis;
import org.treetank.service.xml.xpath.expr.IfAxis;
import org.treetank.service.xml.xpath.expr.InstanceOfExpr;
import org.treetank.service.xml.xpath.expr.IntersectAxis;
import org.treetank.service.xml.xpath.expr.LiteralExpr;
import org.treetank.service.xml.xpath.expr.OrExpr;
import org.treetank.service.xml.xpath.expr.RangeAxis;
import org.treetank.service.xml.xpath.expr.SequenceAxis;
import org.treetank.service.xml.xpath.expr.SomeExpr;
import org.treetank.service.xml.xpath.expr.UnionAxis;
import org.treetank.service.xml.xpath.expr.VarRefExpr;
import org.treetank.service.xml.xpath.expr.VariableAxis;
import org.treetank.service.xml.xpath.filter.DupFilterAxis;
import org.treetank.service.xml.xpath.filter.PredicateFilterAxis;
import org.treetank.service.xml.xpath.functions.AbsFunction;
import org.treetank.service.xml.xpath.functions.FuncDef;
import org.treetank.service.xml.xpath.operators.AddOpAxis;
import org.treetank.service.xml.xpath.operators.DivOpAxis;
import org.treetank.service.xml.xpath.operators.IDivOpAxis;
import org.treetank.service.xml.xpath.operators.ModOpAxis;
import org.treetank.service.xml.xpath.operators.MulOpAxis;
import org.treetank.service.xml.xpath.operators.SubOpAxis;

/**
 * <h1>PipeBuilder</h1>
 * <p>
 * Builder of a query execution plan in the pipeline manner.
 * </p>
 */
public final class PipelineBuilder {

    /** Stack of pipeline builder which stores expressions. */
    private final Stack<Stack<ExpressionSingle>> mExprStack;

    /** Maps a variable name to the item that the variable holds. */
    private final Map<String, AbsAxis> mVarRefMap;

    /** Storing atomic results. */
    private final List<AtomicValue> mToStore;

    /**
     * Constructor.
     */
    public PipelineBuilder(final List<AtomicValue> pToStore) {

        mExprStack = new Stack<Stack<ExpressionSingle>>();

        mVarRefMap = new HashMap<String, AbsAxis>();
        mToStore = pToStore;
    }

    /**
     * @return the current pipeline stack
     */
    public Stack<ExpressionSingle> getPipeStack() {

        if (mExprStack.size() == 0) {
            throw new IllegalStateException("No pipe on the stack");
        }
        return mExprStack.peek();
    }

    /**
     * Adds a new pipeline stack to the stack holding all expressions.
     */
    public void addExpr() {

        mExprStack.push(new Stack<ExpressionSingle>());
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
    public void finishExpr(final INodeReadTransaction mTransaction, final int mNum) {

        // all singleExpression that are on the stack will be combined in the
        // sequence, so the number of singleExpressions in the sequence and the
        // size
        // of the stack containing these SingleExpressions have to be the same.
        if (getPipeStack().size() != mNum) {
            // this should never happen
            throw new IllegalStateException("The query has not been processed correctly");
        }
        int no = mNum;

        AbsAxis[] axis;
        if (no > 1) {

            axis = new AbsAxis[no];

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
            axis = new AbsAxis[1];
            axis[0] = getPipeStack().pop().getExpr();

            if (mExprStack.size() > 1) {
                assert mExprStack.peek().empty();
                mExprStack.pop();
            }

            if (getPipeStack().empty() || getExpression().getSize() != 0) {
                addExpressionSingle();

            }

            final AbsAxis iAxis;
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
        int num = mForConditionNum;

        while (num-- > 0) {
            forAxis = new ForAxis(getPipeStack().pop().getExpr(), forAxis);
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
    public void addIfExpression(final INodeReadTransaction mTransaction) {

        assert getPipeStack().size() >= 3;

        final INodeReadTransaction rtx = mTransaction;

        final AbsAxis elseExpr = getPipeStack().pop().getExpr();
        final AbsAxis thenExpr = getPipeStack().pop().getExpr();
        final AbsAxis ifExpr = getPipeStack().pop().getExpr();

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
    public void addCompExpression(final INodeReadTransaction mTransaction, final String mComp) {

        assert getPipeStack().size() >= 2;

        final INodeReadTransaction rtx = mTransaction;

        final AbsAxis paramOperandTwo = getPipeStack().pop().getExpr();
        final AbsAxis paramOperandOne = getPipeStack().pop().getExpr();

        final CompKind kind = CompKind.fromString(mComp);
        final AbsAxis axis =
            AbsComparator.getComparator(rtx, paramOperandOne, paramOperandTwo, kind, mComp, mToStore);

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
        // throw new IllegalStateException(mComp +
        // " is not a valid comparison.");
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
    public void addOperatorExpression(final INodeReadTransaction mTransaction, final String mOperator) {

        assert getPipeStack().size() >= 1;

        final INodeReadTransaction rtx = mTransaction;

        final AbsAxis mOperand2 = getPipeStack().pop().getExpr();

        // the unary operation only has one operator
        final AbsAxis mOperand1 = getPipeStack().pop().getExpr();
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }

        final AbsAxis axis;

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
    public void addUnionExpression(final INodeReadTransaction mTransaction) {

        assert getPipeStack().size() >= 2;

        final AbsAxis mOperand2 = getPipeStack().pop().getExpr();
        final AbsAxis mOperand1 = getPipeStack().pop().getExpr();
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
    public void addAndExpression(final INodeReadTransaction mTransaction) {
        assert getPipeStack().size() >= 2;

        final AbsAxis mOperand2 = getPipeStack().pop().getExpr();
        final AbsAxis operand1 = getPipeStack().pop().getExpr();
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
    public void addOrExpression(final INodeReadTransaction mTransaction) {

        assert getPipeStack().size() >= 2;

        final AbsAxis mOperand2 = getPipeStack().pop().getExpr();
        final AbsAxis mOperand1 = getPipeStack().pop().getExpr();

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
    public void addIntExcExpression(final INodeReadTransaction mTransaction, final boolean mIsIntersect) {

        assert getPipeStack().size() >= 2;

        final INodeReadTransaction rtx = mTransaction;

        final AbsAxis mOperand2 = getPipeStack().pop().getExpr();
        final AbsAxis mOperand1 = getPipeStack().pop().getExpr();

        final AbsAxis axis =
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
    public void addLiteral(final INodeReadTransaction mTransaction, final int mItemKey) {

        // addExpressionSingle();
        getExpression().add(new LiteralExpr(mTransaction, mItemKey));
    }

    /**
     * Adds a step to the pipeline.
     * 
     * @param axis
     *            the axis step to add to the pipeline.
     */
    public void addStep(final AbsAxis axis) {

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
    public void addStep(final AbsAxis axis, final AbsFilter mFilter) {

        getExpression().add(new FilterAxis(axis, mFilter));
    }

    /**
     * Returns a queue of all pipelines build so far and empties the pipeline
     * stack.
     * 
     * @return all build pipelines
     */
    public AbsAxis getPipeline() {

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
    public void addPredicate(final INodeReadTransaction mTransaction) {

        assert getPipeStack().size() >= 2;

        final AbsAxis mPredicate = getPipeStack().pop().getExpr();

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
                // new AtomicValue(mTransaction.getNode().getRawValue(),
                // mTransaction
                // .keyForName("xs:integer"));
                // long position = mTransaction.getItemList().addItem(pos);
                // mPredicate.reset(mTransaction.getNode().getNodeKey());
                // IAxis function =
                // new FNPosition(mTransaction, new ArrayList<IAxis>(),
                // FuncDef.POS.getMin(), FuncDef.POS
                // .getMax(),
                // mTransaction.keyForName(FuncDef.POS.getReturnType()));
                // IAxis expectedPos = new LiteralExpr(mTransaction, position);
                //
                // mPredicate = new ValueComp(mTransaction, function,
                // expectedPos, CompKind.EQ);

            }
        }

        getExpression().add(new PredicateFilterAxis(mTransaction, mPredicate, mToStore));

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
    public void addQuantifierExpr(final INodeReadTransaction mTransaction, final boolean mIsSome,
        final int mVarNum) {

        assert getPipeStack().size() >= (mVarNum + 1);

        final AbsAxis satisfy = getPipeStack().pop().getExpr();
        final List<AbsAxis> vars = new ArrayList<AbsAxis>();
        int num = mVarNum;

        while (num-- > 0) {
            // invert current order of variables to get original variable order
            vars.add(num, getPipeStack().pop().getExpr());
        }

        final AbsAxis mAxis =
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
    public void addCastableExpr(final INodeReadTransaction mTransaction, final SingleType mSingleType) {

        assert getPipeStack().size() >= 1;

        final AbsAxis candidate = getPipeStack().pop().getExpr();

        final AbsAxis axis = new CastableExpr(mTransaction, candidate, mSingleType);
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
    public void addRangeExpr(final INodeReadTransaction mTransaction) {

        assert getPipeStack().size() >= 2;

        final AbsAxis mOperand2 = getPipeStack().pop().getExpr();
        final AbsAxis mOperand1 = getPipeStack().pop().getExpr();

        final AbsAxis axis = new RangeAxis(mTransaction, mOperand1, mOperand2);
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
    public void addCastExpr(final INodeReadTransaction mTransaction, final SingleType mSingleType) {

        assert getPipeStack().size() >= 1;

        final AbsAxis candidate = getPipeStack().pop().getExpr();

        final AbsAxis axis = new CastExpr(mTransaction, candidate, mSingleType);
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
    public void addInstanceOfExpr(final INodeReadTransaction mTransaction, final SequenceType mSequenceType) {

        assert getPipeStack().size() >= 1;

        final AbsAxis candidate = getPipeStack().pop().getExpr();

        final AbsAxis axis = new InstanceOfExpr(mTransaction, candidate, mSequenceType);
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
    public void addTreatExpr(final INodeReadTransaction mTransaction, final SequenceType mSequenceType) {

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
    public void addVariableExpr(final INodeReadTransaction mTransaction, final String mVarName) {

        assert getPipeStack().size() >= 1;

        final AbsAxis bindingSeq = getPipeStack().pop().getExpr();

        final AbsAxis axis = new VariableAxis(mTransaction, bindingSeq);
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
    public void addFunction(final INodeReadTransaction mTransaction, final String mFuncName, final int mNum)
        throws TTXPathException {

        assert getPipeStack().size() >= mNum;

        final List<AbsAxis> args = new ArrayList<AbsAxis>(mNum);
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
            throw EXPathError.XPST0017.getEncapsulatedException();
        }

        // get function class
        final Class<? extends AbsFunction> function = func.getFunc();
        final Integer min = func.getMin();
        final Integer max = func.getMax();
        final Integer returnType = mTransaction.keyForName(func.getReturnType());

        // parameter types of the function's constructor
        final Class<?>[] paramTypes = {
            INodeReadTransaction.class, List.class, Integer.TYPE, Integer.TYPE, Integer.TYPE
        };

        try {
            // instantiate function class with right constructor
            final Constructor<?> cons = function.getConstructor(paramTypes);
            final AbsAxis axis = (AbsAxis)cons.newInstance(mTransaction, args, min, max, returnType);

            if (getPipeStack().empty() || getExpression().getSize() != 0) {
                addExpressionSingle();
            }
            getExpression().add(axis);

        } catch (final NoSuchMethodException e) {
            throw EXPathError.XPST0017.getEncapsulatedException();
        } catch (final IllegalArgumentException e) {
            throw EXPathError.XPST0017.getEncapsulatedException();
        } catch (final InstantiationException e) {
            throw new IllegalStateException("Function not implemented yet.");
        } catch (final IllegalAccessException e) {
            throw EXPathError.XPST0017.getEncapsulatedException();
        } catch (final InvocationTargetException e) {
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
    public void addVarRefExpr(final INodeReadTransaction mTransaction, final String mVarName) {

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

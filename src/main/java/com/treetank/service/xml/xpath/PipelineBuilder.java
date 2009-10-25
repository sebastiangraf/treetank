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
 * $Id: PipelineBuilder.java 4245 2008-07-08 08:44:34Z scherer $
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
import com.treetank.axis.AbstractAxis;
import com.treetank.axis.FilterAxis;
import com.treetank.service.xml.xpath.comparators.CompKind;
import com.treetank.service.xml.xpath.comparators.GeneralComp;
import com.treetank.service.xml.xpath.comparators.NodeComp;
import com.treetank.service.xml.xpath.comparators.ValueComp;
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
import com.treetank.service.xml.xpath.functions.AbstractFunction;
import com.treetank.service.xml.xpath.functions.FuncDef;
import com.treetank.service.xml.xpath.functions.XPathError;
import com.treetank.service.xml.xpath.functions.XPathError.ErrorType;
import com.treetank.service.xml.xpath.operators.AddOpAxis;
import com.treetank.service.xml.xpath.operators.DivOpAxis;
import com.treetank.service.xml.xpath.operators.IDivOpAxis;
import com.treetank.service.xml.xpath.operators.ModOpAxis;
import com.treetank.service.xml.xpath.operators.MulOpAxis;
import com.treetank.service.xml.xpath.operators.SubOpAxis;
import com.treetank.utils.FastStack;

/**
 * <h1>PipeBuilder</h1>
 * <p>
 * Builder of a query execution plan in the pipeline manner.
 * </p>
 */
public final class PipelineBuilder {

    private final FastStack<FastStack<ExpressionSingle>> mExprStack;

    /** Maps a variable name to the item that the variable holds. */
    private final Map<String, IAxis> mVarRefMap;

    /**
     * Maps a function name to the ordinal value of the function in the enum
     * Func.
     */
    private Map<String, Integer> mFuncMap;

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

        mExprStack.push((new FastStack<ExpressionSingle>()));
    }

    /**
     * Ends an expression. This means that the currently used pipeline stack
     * will be emptied and the singleExpressions that were on the stack are
     * combined by a sequence expression, which is lated added to the next
     * pipeline stack.
     * 
     * @param transaction
     *            transaction to operate on
     * @param num
     *            number of singleExpressions that will be added to the sequence
     */
    public void finishExpr(final IReadTransaction transaction, final int num) {

        // all singleExpression that are on the stack will be combined in the
        // sequence, so the number of singleExpressions in the sequence and the
        // size
        // of the stack containing these SingleExpressions have to be the same.
        if (getPipeStack().size() != num) {
            // this should never happen
            throw new IllegalStateException(
                    "The query has not been porcessed correctly");
        }
        int no = num;

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
            getExpression().add(new SequenceAxis(transaction, axis));

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
            if (mExprStack.size() == 1 && getPipeStack().size() == 1
                    && getExpression().getSize() == 0) {
                iAxis = new SequenceAxis(transaction, axis);
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
     * @param forConditionNum
     *            Number of all for conditions of the expression
     */
    public void addForExpression(final int forConditionNum) {

        assert getPipeStack().size() >= (forConditionNum + 1);

        AbstractAxis forAxis = ((AbstractAxis) (getPipeStack().pop().getExpr()));
        final IReadTransaction rtx = forAxis.getTransaction();
        int num = forConditionNum;

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
     * @param transaction
     *            Transaction to operate with.
     */
    public void addIfExpression(final IReadTransaction transaction) {

        assert getPipeStack().size() >= 3;

        final IReadTransaction rtx = transaction;

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
     * @param transaction
     *            Transaction to operate with.
     * @param comp
     *            Comparator type.
     */
    public void addCompExpression(final IReadTransaction transaction,
            final String comp) {

        assert getPipeStack().size() >= 2;

        final IReadTransaction rtx = transaction;

        final IAxis operand2 = getPipeStack().pop().getExpr();
        final IAxis operand1 = getPipeStack().pop().getExpr();

        final IAxis axis;
        final CompKind kind;

        // TODO: use typeswitch of JAVA 7
        if (comp.equals("eq")) {
            kind = CompKind.EQ;
            axis = new ValueComp(rtx, operand1, operand2, kind);
        } else if (comp.equals("ne")) {
            kind = CompKind.NE;
            axis = new ValueComp(rtx, operand1, operand2, kind);
        } else if (comp.equals("lt")) {
            kind = CompKind.LT;
            axis = new ValueComp(rtx, operand1, operand2, kind);
        } else if (comp.equals("le")) {
            kind = CompKind.LE;
            axis = new ValueComp(rtx, operand1, operand2, kind);
        } else if (comp.equals("gt")) {
            kind = CompKind.GT;
            axis = new ValueComp(rtx, operand1, operand2, kind);
        } else if (comp.equals("ge")) {
            kind = CompKind.GE;
            axis = new ValueComp(rtx, operand1, operand2, kind);

        } else if (comp.equals("=")) {
            kind = CompKind.EQ;
            axis = new GeneralComp(rtx, operand1, operand2, kind);
        } else if (comp.equals("!=")) {
            kind = CompKind.NE;
            axis = new GeneralComp(rtx, operand1, operand2, kind);
        } else if (comp.equals("<")) {
            kind = CompKind.LT;
            axis = new GeneralComp(rtx, operand1, operand2, kind);
        } else if (comp.equals("<=")) {
            kind = CompKind.LE;
            axis = new GeneralComp(rtx, operand1, operand2, kind);
        } else if (comp.equals(">")) {
            kind = CompKind.GT;
            axis = new GeneralComp(rtx, operand1, operand2, kind);
        } else if (comp.equals(">=")) {
            kind = CompKind.GE;
            axis = new GeneralComp(rtx, operand1, operand2, kind);

        } else if (comp.equals("is")) {
            kind = CompKind.IS;
            axis = new NodeComp(rtx, operand1, operand2, kind);
        } else if (comp.equals("<<")) {
            kind = CompKind.PRE;
            axis = new NodeComp(rtx, operand1, operand2, kind);
        } else if (comp.equals(">>")) {
            kind = CompKind.FO;
            axis = new NodeComp(rtx, operand1, operand2, kind);
        } else {
            throw new IllegalStateException(comp
                    + " is not a valid comparison.");

        }

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);

    }

    /**
     * Adds an operator expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     * @param operator
     *            Operator type.
     */
    public void addOperatorExpression(final IReadTransaction transaction,
            final String operator) {

        assert getPipeStack().size() >= 1;

        final IReadTransaction rtx = transaction;

        final IAxis operand2 = getPipeStack().pop().getExpr();

        // the unary operation only has one operator
        final IAxis operand1 = getPipeStack().pop().getExpr();
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }

        final IAxis axis;

        // TODO: use typeswitch of JAVA 7
        if (operator.equals("+")) {
            axis = new AddOpAxis(rtx, operand1, operand2);
        } else if (operator.equals("-")) {

            axis = new SubOpAxis(rtx, operand1, operand2);
        } else if (operator.equals("*")) {
            axis = new MulOpAxis(rtx, operand1, operand2);
        } else if (operator.equals("div")) {
            axis = new DivOpAxis(rtx, operand1, operand2);
        } else if (operator.equals("idiv")) {
            axis = new IDivOpAxis(rtx, operand1, operand2);
        } else if (operator.equals("mod")) {
            axis = new ModOpAxis(rtx, operand1, operand2);
        } else {
            // TODO: unary operator
            throw new IllegalStateException(operator
                    + " is not a valid operator.");

        }

        getExpression().add(axis);
    }

    /**
     * Adds a union expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     */
    public void addUnionExpression(final IReadTransaction transaction) {

        assert getPipeStack().size() >= 2;

        final IAxis operand2 = getPipeStack().pop().getExpr();
        final IAxis operand1 = getPipeStack().pop().getExpr();
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(
                new DupFilterAxis(transaction, new UnionAxis(transaction,
                        operand1, operand2)));
    }

    /**
     * Adds a and expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     */
    public void addAndExpression(final IReadTransaction transaction) {

        assert getPipeStack().size() >= 2;

        final IAxis operand2 = getPipeStack().pop().getExpr();
        final IAxis operand1 = getPipeStack().pop().getExpr();
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(new AndExpr(transaction, operand1, operand2));
    }

    /**
     * Adds a or expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     */
    public void addOrExpression(final IReadTransaction transaction) {

        assert getPipeStack().size() >= 2;

        final IAxis operand2 = getPipeStack().pop().getExpr();
        final IAxis operand1 = getPipeStack().pop().getExpr();

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(new OrExpr(transaction, operand1, operand2));
    }

    /**
     * Adds a intersect or a exception expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     * @param isIntersect
     *            true, if expression is an intersection
     */
    public void addIntExcExpression(final IReadTransaction transaction,
            final boolean isIntersect) {

        assert getPipeStack().size() >= 2;

        final IReadTransaction rtx = transaction;

        final IAxis operand2 = getPipeStack().pop().getExpr();
        final IAxis operand1 = getPipeStack().pop().getExpr();

        final IAxis axis = isIntersect ? new IntersectAxis(rtx, operand1,
                operand2) : new ExceptAxis(rtx, operand1, operand2);

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);
    }

    /**
     * Adds a literal expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     * @param itemKey
     *            key of the literal expression.
     */
    public void addLiteral(final IReadTransaction transaction, final int itemKey) {

        // addExpressionSingle();
        getExpression().add(new LiteralExpr(transaction, itemKey));
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
     * @param filter
     *            the node test to add to the pipeline.
     */
    public void addStep(final IAxis axis, final IFilter filter) {

        getExpression().add(new FilterAxis(axis, filter));
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
     * @param transaction
     *            Transaction to operate with.
     */
    public void addPredicate(final IReadTransaction transaction) {

        assert getPipeStack().size() >= 2;

        IAxis predicate = getPipeStack().pop().getExpr();

        if (predicate instanceof LiteralExpr) {

            predicate.hasNext();
            // if is numeric literal -> abbrev for position()
            int type = transaction.getNode().getTypeKey();
            if (type == transaction.keyForName("xs:integer")
                    || type == transaction.keyForName("xs:double")
                    || type == transaction.keyForName("xs:float")
                    || type == transaction.keyForName("xs:decimal")) {

                throw new IllegalStateException(
                        "function fn:position() is not implemented yet.");

                // getExpression().add(
                // new PosFilter(transaction, (int)
                // Double.parseDouble(transaction
                // .getValue())));
                // return; // TODO: YES! it is dirty!

                // AtomicValue pos = new AtomicValue(transaction.getRawValue(),
                // transaction.keyForName("xs:integer"));
                // long position = transaction.getItemList().addItem(pos);
                // predicate.reset(transaction.getNodeKey());
                // IAxis function = new FNPosition(transaction, new
                // ArrayList<IAxis>(),
                // FuncDef.POS.getMin(), FuncDef.POS.getMax(), transaction
                // .keyForName(FuncDef.POS.getReturnType()));
                // IAxis expectedPos = new LiteralExpr(transaction, position);
                //        
                // predicate = new ValueComp(transaction, function, expectedPos,
                // CompKind.EQ);

            }
        }

        getExpression().add(new PredicateFilterAxis(transaction, predicate));

    }

    /**
     * Adds a SomeExpression or an EveryExpression to the pipeline, depending on
     * the parameter isSome.
     * 
     * @param transaction
     *            Transaction to operate with.
     * @param isSome
     *            defines whether a some- or an EveryExpression is used.
     * @param varNum
     *            number of binding variables
     */
    public void addQuantifierExpr(final IReadTransaction transaction,
            final boolean isSome, final int varNum) {

        assert getPipeStack().size() >= (varNum + 1);

        final IAxis satisfy = getPipeStack().pop().getExpr();
        final List<IAxis> vars = new ArrayList<IAxis>();
        int num = varNum;

        while (num-- > 0) {
            // invert current order of variables to get original variable order
            vars.add(num, getPipeStack().pop().getExpr());
        }

        IAxis axis = isSome ? new SomeExpr(transaction, vars, satisfy)
                : new EveryExpr(transaction, vars, satisfy);

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);
    }

    /**
     * Adds a castable expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     * @param singleType
     *            single type the context item will be casted to.
     */
    public void addCastableExpr(final IReadTransaction transaction,
            final SingleType singleType) {

        assert getPipeStack().size() >= 1;

        final IAxis candidate = getPipeStack().pop().getExpr();

        final IAxis axis = new CastableExpr(transaction, candidate, singleType);
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);

    }

    /**
     * Adds a range expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     */
    public void addRangeExpr(final IReadTransaction transaction) {

        assert getPipeStack().size() >= 2;

        final IAxis operand2 = getPipeStack().pop().getExpr();
        final IAxis operand1 = getPipeStack().pop().getExpr();

        final IAxis axis = new RangeAxis(transaction, operand1, operand2);
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);

    }

    /**
     * Adds a cast expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     * @param singleType
     *            single type the context item will be casted to.
     */
    public void addCastExpr(final IReadTransaction transaction,
            final SingleType singleType) {

        assert getPipeStack().size() >= 1;

        final IAxis candidate = getPipeStack().pop().getExpr();

        final IAxis axis = new CastExpr(transaction, candidate, singleType);
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);

    }

    /**
     * Adds a instance of expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     * @param sequenceType
     *            sequence type the context item should match.
     */
    public void addInstanceOfExpr(final IReadTransaction transaction,
            final SequenceType sequenceType) {

        assert getPipeStack().size() >= 1;

        final IAxis candidate = getPipeStack().pop().getExpr();

        final IAxis axis = new InstanceOfExpr(transaction, candidate,
                sequenceType);
        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);

    }

    /**
     * Adds a treat as expression to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     * @param sequenceType
     *            sequence type the context item will be treated as.
     */
    public void addTreatExpr(final IReadTransaction transaction,
            final SequenceType sequenceType) {

        throw new IllegalStateException(
                "the Treat expression is not supported yet");

    }

    /**
     * Adds a variable expression to the pipeline. Adds the expression that will
     * evaluate the results the variable holds.
     * 
     * @param transaction
     *            Transaction to operate with.
     * @param varName
     *            name of the variable
     */
    public void addVariableExpr(final IReadTransaction transaction,
            final String varName) {

        assert getPipeStack().size() >= 1;

        final IAxis bindingSeq = getPipeStack().pop().getExpr();

        final IAxis axis = new VariableAxis(transaction, bindingSeq);
        mVarRefMap.put(varName, axis);

        if (getPipeStack().empty() || getExpression().getSize() != 0) {
            addExpressionSingle();
        }
        getExpression().add(axis);
    }

    /**
     * Adds a function to the pipeline.
     * 
     * @param transaction
     *            Transaction to operate with.
     * @param funcName
     *            The name of the function
     * @param num
     *            The number of arguments that are passed to the function
     */
    public void addFunction(final IReadTransaction transaction,
            final String funcName, final int num) {

        assert getPipeStack().size() >= num;

        final List<IAxis> args = new ArrayList<IAxis>(num);
        // arguments are stored on the stack in reverse order -> invert arg
        // order
        for (int i = 0; i < num; i++) {
            args.add(getPipeStack().pop().getExpr());
        }

        // initializer function mapping only, when needed
        if (mFuncMap == null) {
            mFuncMap = new HashMap<String, Integer>();

            for (FuncDef func : FuncDef.values()) {
                mFuncMap.put(func.getName(), func.ordinal());
            }
        }

        // get right function type
        final FuncDef func;
        try {
            func = FuncDef.values()[mFuncMap.get(funcName)];
        } catch (NullPointerException e) {
            throw new XPathError(ErrorType.XPST0017);
        }

        // get function class
        final Class<? extends AbstractFunction> function = func.getFunc();
        final Integer min = func.getMin();
        final Integer max = func.getMax();
        final Integer returnType = transaction.keyForName(func.getReturnType());

        // parameter types of the function's constructor
        final Class[] paramTypes = { IReadTransaction.class, List.class,
                Integer.TYPE, Integer.TYPE, Integer.TYPE };

        try {
            // instantiate function class with right constructor
            final Constructor cons = function.getConstructor(paramTypes);
            final IAxis axis = (IAxis) cons.newInstance(transaction, args, min,
                    max, returnType);

            if (getPipeStack().empty() || getExpression().getSize() != 0) {
                addExpressionSingle();
            }
            getExpression().add(axis);

        } catch (NoSuchMethodException e) {
            throw new XPathError(ErrorType.XPST0017);
        } catch (IllegalArgumentException e) {
            throw new XPathError(ErrorType.XPST0017);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Function not implemented yet.");
        } catch (IllegalAccessException e) {
            throw new XPathError(ErrorType.XPST0017);
        } catch (InvocationTargetException e) {
            throw new XPathError(ErrorType.XPST0017);
        }

    }

    /**
     * Adds a VarRefExpr to the pipeline. This Expression holds a reference to
     * the current context item of the specified variable.
     * 
     * @param transaction
     *            the transaction to operate on.
     * @param varName
     *            the name of the variable
     */
    public void addVarRefExpr(final IReadTransaction transaction,
            final String varName) {

        final VariableAxis axis = (VariableAxis) mVarRefMap.get(varName);
        if (axis != null) {
            getExpression().add(new VarRefExpr(transaction, axis));
        } else {
            throw new IllegalStateException("Variable " + varName + " unkown.");
        }

    }

}

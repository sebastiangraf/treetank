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
 * $Id: ExpressionSingle.java 4245 2008-07-08 08:44:34Z scherer $
 */

package com.treetank.service.xml.xpath;

import com.treetank.api.IAxis;
import com.treetank.axis.AncestorAxis;
import com.treetank.axis.ChildAxis;
import com.treetank.axis.DescendantAxis;
import com.treetank.axis.FilterAxis;
import com.treetank.axis.FollowingAxis;
import com.treetank.axis.FollowingSiblingAxis;
import com.treetank.axis.NestedAxis;
import com.treetank.axis.ParentAxis;
import com.treetank.axis.PrecedingAxis;
import com.treetank.axis.PrecedingSiblingAxis;
import com.treetank.service.xml.xpath.expr.UnionAxis;
import com.treetank.service.xml.xpath.filter.DupFilterAxis;

/**
 * <h1>ExpresseionSingle</h1>
 * <p>
 * This class builds an execution chain to execute a XPath query. All added axis
 * are build together by using NestedAxis.
 */
public class ExpressionSingle {

    /**
     * Counts the number of added axis. This is used to handle the special
     * behavior for the first and second axis that are added.
     */
    private int mNumber;

    /** The first added axis has to be stored till a second one is added. */
    private IAxis mFirstAxis;

    /** Contains the execution chain consisting of nested NestedAxis. */
    private IAxis mExpr;

    /** Current ordering state. */
    private OrdState ord;

    /** Current duplicate state. */
    private DupState dup;

    /**
     * Constructor. Initializes the internal state.
     */
    public ExpressionSingle() {

        mNumber = 0;

        ord = OrdState.MAX1;
        ord.init();
        dup = DupState.MAX1;

    }

    /**
     * Adds a new Axis to the expression chain. The first axis that is added has
     * to be stored till a second axis is added. When the second axis is added,
     * it is nested with the first one and builds the execution chain.
     * 
     * @param ax
     *            The axis to add.
     */
    public void add(final IAxis ax) {
        IAxis axis = ax;

        if (isDupOrd(axis)) {
            axis = new DupFilterAxis(axis.getTransaction(), axis);
            DupState.nodup = true;
        }

        switch (mNumber) {
        case 0:
            mFirstAxis = axis;
            mNumber++;
            break;
        case 1:
            mExpr = new NestedAxis(mFirstAxis, axis);
            mNumber++;
            break;
        default:
            final IAxis cache = mExpr;
            mExpr = new NestedAxis(cache, axis);
        }

    }

    /**
     * Returns a chain to execute the query. If there is only one axis added,
     * the chain was not build yet, so only this axis is returned.
     * 
     * @return The query execution chain
     */
    public IAxis getExpr() {

        return (mNumber == 1) ? mFirstAxis : mExpr;
    }

    /**
     * Returns the number of axis in this expression.
     * 
     * @return size of the expression
     */
    public int getSize() {

        return mNumber;
    }

    /**
     * Determines for a given string representation of an axis, whether this
     * axis leads to duplicates in the result sequence or not. Furthermore it
     * determines the new state for the order state that specifies, if the
     * result sequence is in document order. This method is implemented
     * according to the automata in [Hidders, J., Michiels, P., "Avoiding
     * Unnecessary Ordering Operations in XPath", 2003]
     * 
     * @param ax
     *            name of the current axis
     * @return true, if expression is still duplicate free
     */
    public boolean isDupOrd(final IAxis ax) {

        IAxis axis = ax;

        while (axis instanceof FilterAxis) {
            axis = ((FilterAxis) axis).getAxis();
        }

        if (axis instanceof UnionAxis) {
            ord = ord.updateOrdUnion();
            dup = dup.updateUnion();

        } else if (axis instanceof ChildAxis) {
            ord = ord.updateOrdChild();
            dup = dup.updateDupChild();

        } else if (axis instanceof ParentAxis) {

            ord = ord.updateOrdParent();
            dup = dup.updateDupParent();

        } else if (axis instanceof DescendantAxis) {

            ord = ord.updateOrdDesc();
            dup = dup.updateDupDesc();

        } else if (axis instanceof AncestorAxis) {

            ord = ord.updateOrdAncestor();
            dup = dup.updateDupAncestor();

        } else if (axis instanceof FollowingAxis
                || axis instanceof PrecedingAxis) {

            ord = ord.updateOrdFollPre();
            dup = dup.updateDupFollPre();

        } else if (axis instanceof FollowingSiblingAxis
                || axis instanceof PrecedingSiblingAxis) {

            ord = ord.updateOrdFollPreSib();
            dup = dup.updateDupFollPreSib();
        }

        return !DupState.nodup;
    }

    /**
     * @return true, if the result is in document order
     */
    public boolean isOrdered() {

        // the result sequence is unordered, if the order rank is greater than
        // zero
        // or the order state is in state UNORD
        // return (ord != OrdState.UNORD && ord.mOrdRank == 0);
        return (ord != OrdState.UNORD && OrdState.mOrdRank == 0);
    }
}

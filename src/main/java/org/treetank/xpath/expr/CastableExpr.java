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
 * $Id$
 */

package org.treetank.xpath.expr;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.SingleType;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;
import org.treetank.xpath.types.Type;

/**
 * <h1>CastableExpression</h1>
 * <p>
 * The castable expression tests whether a given value is castable into a given
 * target type. The target type must be an atomic type that is in the in-scope
 * schema types [err:XPST0051]. In addition, the target type cannot be
 * xs:NOTATION or xs:anyAtomicType [err:XPST0080]. The optional occurrence
 * indicator "?" denotes that an empty sequence is permitted.
 * </p>
 * <p>
 * The expression V castable as T returns true if the value V can be
 * successfully cast into the target type T by using a cast expression;
 * otherwise it returns false. The castable expression can be used as a
 * predicate to avoid errors at evaluation time. It can also be used to select
 * an appropriate type for processing of a given value.
 * </p>
 */
public class CastableExpr extends AbstractExpression implements IAxis {

  /** The input expression to cast to a specified target expression. */
  private final IAxis mSourceExpr;

  /** The type, to which the input expression should be cast to. */
  private final Type mTargetType;

  /** Defines, whether an empty sequence can be casted to any target type. */
  private final boolean mPermitEmptySeq;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param inputExpr
   *          input expression, that's castablity will be tested.
   * @param target
   *          Type to test, whether the input expression can be casted to.
   */
  public CastableExpr(final IReadTransaction rtx, final IAxis inputExpr,
      final SingleType target) {

    super(rtx);
    mSourceExpr = inputExpr;
    mTargetType = target.getAtomic();
    mPermitEmptySeq = target.hasInterogation();

  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(final long nodeKey) {

    super.reset(nodeKey);
    if (mSourceExpr != null) {
      mSourceExpr.reset(nodeKey);
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected void evaluate() {

    // defines if current item is castable to the target type, or not
    boolean isCastable;

    // atomic type must not be xs:anyAtomicType or xs:NOTATION
    if (mTargetType == Type.ANY_ATOMIC_TYPE || mTargetType == Type.NOTATION) {
      throw new XPathError(ErrorType.XPST0080);
    }

    if (mSourceExpr.hasNext()) { // result sequence > 0

      final Type sourceType = Type.getType(getTransaction().getTypeKey());
      final String sourceValue = getTransaction().getValue();

      // determine castability
      isCastable = sourceType.isCastableTo(mTargetType, sourceValue);

      // if the result sequence of the input expression has more than one
      // item, a type error is raised.
      if (mSourceExpr.hasNext()) { // result sequence > 1
        throw new XPathError(ErrorType.XPTY0004);
      }

    } else { // result sequence = 0 (empty sequence)

      // empty sequence is allowed.
      isCastable = mPermitEmptySeq;

    }

    // create result item and move transaction to it.
    int itemKey = getTransaction().getItemList().addItem(new AtomicValue(
        TypedValue.getBytes(Boolean.toString(isCastable)),
        getTransaction().keyForName("xs:boolean")));
    getTransaction().moveTo(itemKey);

  }

}

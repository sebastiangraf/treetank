
package org.treetank.xpath.expr;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;

/**
 * <h1>AbstractExpression</h1>
 * <p>
 * Template for all expressions.
 * </p>
 * <p>
 * This class is a template for most complex expressions of the XPath 2.0
 * language. These expressions work like an axis, as all other XPath 2.0
 * expressions in this implementation, but the expression is only evaluated
 * once. Therefore the axis returns true only for the first call and false for
 * all others.
 * </p>
 * 
 * @author Tina Scherer
 */
public abstract class AbstractExpression extends AbstractAxis implements IAxis {

  /** Defines, whether hasNext() has already been called. */
  private boolean mIsFirst;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   */
  public AbstractExpression(final IReadTransaction rtx) {

    super(rtx);
    mIsFirst = true;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(final long nodeKey) {

    super.reset(nodeKey);
    mIsFirst = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {

    resetToLastKey();

    if (mIsFirst) {
      mIsFirst = false;

      // evaluate expression
      evaluate();

      return true;
    } else {

      // only the first call yields to true, all further calls will yield to
      // false. Calling hasNext() makes no sense, since evaluating the
      // expression on the same input would always return the same result.
      resetToStartKey();
      return false;
    }

  }

  /**
   * Performs the expression dependent evaluation of the expression. (Template
   * method)
   */
  protected abstract void evaluate();

}

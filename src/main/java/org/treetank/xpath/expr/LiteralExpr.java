
package org.treetank.xpath.expr;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>LiteralExpr</h1>
 * Expression that holds a literal.
 * 
 * @author Tina Scherer
 */
public class LiteralExpr extends AbstractExpression implements IAxis {

  private final long mLiteralKey;


  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param itemKey itemKey of the literal
   */
  public LiteralExpr(final IReadTransaction rtx, final long itemKey) {
    super(rtx);

    mLiteralKey = itemKey;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void evaluate() {

    // set transaction to literal
    getTransaction().moveTo(mLiteralKey);

  }

}

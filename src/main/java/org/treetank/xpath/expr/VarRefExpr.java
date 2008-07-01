
package org.treetank.xpath.expr;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1> VarRefExpr </h1>
 * <p>
 * Reference to the current item of the variable expression.
 * </p>
 * 
 * @author Tina Scherer
 */
public class VarRefExpr extends AbstractExpression implements IAxis, IObserver {

  /** Key of the item the variable is set to at the moment. */
  private long mVarKey;
  

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param variable
   *          Reference the variable expression that computes the items the
   *          variable holds.
   */
  public VarRefExpr(final IReadTransaction rtx, final VariableAxis variable) {

    super(rtx);
    variable.addObserver(this);
    mVarKey = -1;

  }

  /**
   * {@inheritDoc}
   */
  public void update(final long varKey) {

    mVarKey = varKey;
    reset(mVarKey);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void evaluate() {
   

    // assure that the transaction is set to the current context item of the
    // variable's binding sequence.
    getTransaction().moveTo(mVarKey);

  }

}

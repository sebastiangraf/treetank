
package org.treetank.xpath.expr;

import java.util.List;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;

/**
 * <h1>EveryExpr</h1>
 * <p>
 * IAxis that represents the quantified expression "every".
 * </p>
 * <p>
 * The quantified expression is true if every evaluation of the test expression
 * has the effective boolean value true; otherwise the quantified expression is
 * false. This rule implies that, if the in-clauses generate zero binding
 * tuples, the value of the quantified expression is true.
 * </p>
 * 
 * @author Tina Scherer
 */
public class EveryExpr extends AbstractExpression implements IAxis {

  private final List<IAxis> mVars;

  private final IAxis mSatisfy;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param vars
   *          Variables for which the condition must be satisfied
   * @param satisfy
   *          condition every item of the variable results must satisfy in order
   *          to evaluate expression to true
   */
  public EveryExpr(final IReadTransaction rtx, final List<IAxis> vars,
      final IAxis satisfy) {

    super(rtx);
    mVars = vars;
    mSatisfy = satisfy;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(final long nodeKey) {

    super.reset(nodeKey);
    if (mVars != null) {
      for (IAxis axis : mVars) {
        axis.reset(nodeKey);
      }
    }

    if (mSatisfy != null) {
      mSatisfy.reset(nodeKey);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void evaluate() {

    boolean satisfiesCond = true;

    for (IAxis axis : mVars) {
      while (axis.hasNext()) {
        axis.next();
        if (!mSatisfy.hasNext()) {
          // condition is not satisfied for this item -> expression is false
          satisfiesCond = false;
          break;
        }
      }
    }
    int itemKey = getTransaction().getItemList().addItem(new AtomicValue(
        TypedValue.getBytes(Boolean.toString(satisfiesCond)), 
        getTransaction().keyForName("xs:boolean")));
    getTransaction().moveTo(itemKey);

  }

}

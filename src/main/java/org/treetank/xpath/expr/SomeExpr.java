
package org.treetank.xpath.expr;

import java.util.List;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;

/**
 * <h1> Some expression </h1>
 * <p>
 * IAxis that represents the quantified expression "some".
 * </p>
 * <p>
 * The quantified expression is true if at least one evaluation of the test
 * expression has the effective boolean value true; otherwise the quantified
 * expression is false. This rule implies that, if the in-clauses generate zero
 * binding tuples, the value of the quantified expression is false.
 * </p>
 * 
 * @author Tina Scherer
 */
public class SomeExpr extends AbstractExpression implements IAxis {

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
   *          condition that must be satisfied by at least one item of the
   *          variable results in order to evaluate expression to true
   */
  public SomeExpr(final IReadTransaction rtx, final List<IAxis> vars,
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
      for (IAxis var : mVars) {
        var.reset(nodeKey);
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

    boolean satisfiesCond = false;

    for (IAxis axis : mVars) {
      while (axis.hasNext()) {
        if (mSatisfy.hasNext()) {
          // condition is satisfied for this item -> expression is true
          satisfiesCond = true;
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

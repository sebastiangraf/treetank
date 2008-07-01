package org.treetank.xpath.expr;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.xpath.functions.Function;

/**
 * <h1>IfAxis</h1>
 * <p>
 * IAxis that represents the conditional expression based on the keywords 
 * if, then, and else.</p>
 * <p>
 * The first step in processing a conditional expression is to find the 
 * effective boolean value of the test expression.
 * If the effective boolean value of the test expression is true, the value of 
 * the then-expression is returned. If the effective boolean value of the test 
 * expression is false, the value of the else-expression is returned.
 * </p>
 * 
 * @author Tina Scherer
 *
 */
public class IfAxis extends AbstractAxis implements IAxis {

  private final IAxis mIf;
  private final IAxis mThen;
  private final IAxis mElse;
  private boolean mFirst;
  private IAxis mResult;
  /**
   * 
   * Constructor. Initializes the internal state.
   *
   * @param rtx     Exclusive (immutable) trx to iterate with.
   * @param ifAxis  Test expression
   * @param thenAxis Will be evaluated if test expression evaluates to true.
   * @param elseAxis Will be evaluated if test expression evaluates to false.
   */
  public IfAxis(final IReadTransaction rtx, final IAxis ifAxis, 
      final IAxis thenAxis, final IAxis elseAxis) {

    super(rtx);
    mIf = ifAxis;
    mThen = thenAxis;
    mElse = elseAxis;
    mFirst = true;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(final long nodeKey) {

    super.reset(nodeKey);
    mFirst = true;
    
    if (mIf != null) {
       mIf.reset(nodeKey);
    }
    
    if (mThen != null) {
      mThen.reset(nodeKey);
   }
    
    if (mElse != null) {
      mElse.reset(nodeKey);
   }
    
  }

  
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {
    
    resetToLastKey();
    
    if (mFirst) {
      mFirst = false;
      mResult =  (Function.ebv(mIf)) ? mThen : mElse;
    }
    
    if (mResult.hasNext()) {
      return true;
    } else {
      resetToStartKey();
      return false;
    }
  }
    

}

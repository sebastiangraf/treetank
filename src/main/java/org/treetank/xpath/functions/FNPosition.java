
package org.treetank.xpath.functions;

import java.util.List;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.TypedValue;

/**
 * <h1>FNPosition</h1>
 * <p>
 * IAxis that represents the function fn:position specified in <a
 * href="http://www.w3.org/TR/xquery-operators/"> XQuery 1.0 and XPath 2.0
 * Functions and Operators</a>.
 * </p>
 * <p>
 * The function returns position of the item in the expression result set.
 * </p>
 * 
 * @author Tina Scherer
 */
public class FNPosition extends AbstractFunction {

  private Integer posCount;
  

  /**
   * Constructor.
   * 
   * Initializes internal state and do a statical analysis concerning the 
   * function's arguments. 
   * 
   * @param rtx   Transaction to operate on
   * @param args  List of function arguments
   * @param min   min number of allowed function arguments
   * @param max   max number of allowed function arguments
   * @param returnType  the type that the function's result will have
   */
  public FNPosition(final IReadTransaction rtx, final List<IAxis> args,
      final int min, final int max, final int returnType) {

    super(rtx, args, min, max, returnType);
    posCount = 0;
    if (getArgs().size() != 0) {
      throw new IllegalStateException("This function is not supported yet.");
    }
  }

  
  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(final long nodeKey) {
    super.reset(nodeKey);
  }
  
  /**
   * Resets the position counter.
   * This is necessary, because the position of the current item is not the 
   * position in the final result sequence, but an intermediate result sequence.
   */
  public void resetCounter() {
    posCount = 0;
  }
    

  /**
   * {@inheritDoc}
   */
  @Override
  protected byte[] computeResult() {
    

    posCount++;

    return TypedValue.getBytes(posCount.toString());
  }

}

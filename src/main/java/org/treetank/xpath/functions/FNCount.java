
package org.treetank.xpath.functions;

import java.util.List;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.TypedValue;

/**
 * <h1>FNCount</h1>
 * <p>
 * IAxis that represents the function fn:count specified in <a
 * href="http://www.w3.org/TR/xquery-operators/"> XQuery 1.0 and XPath 2.0
 * Functions and Operators</a>.
 * </p>
 * <p>
 * The function returns the number of given arguments.
 * </p>
 * 
 * @author Tina Scherer
 */
public class FNCount extends AbstractFunction {


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
  public FNCount(final IReadTransaction rtx, final List<IAxis> args,
      final int min, final int max, final int returnType) {

    super(rtx, args, min, max, returnType);
  }

    

  /**
   * {@inheritDoc}
   */
  @Override
  protected byte[] computeResult() {
    
    final IAxis axis = getArgs().get(0);

    Integer count = 0;
    while (axis.hasNext()) {
      axis.next();
      count++;
    }

   return TypedValue.getBytes(count.toString());

  }

}

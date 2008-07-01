
package org.treetank.xpath.functions;

import java.util.List;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.functions.XPathError.ErrorType;
import org.treetank.xpath.types.Type;

/**
 * <h1>FNBooleean</h1>
 * <p>
 * IAxis that represents the function fn:boolean specified in <a
 * href="http://www.w3.org/TR/xquery-operators/"> XQuery 1.0 and XPath 2.0
 * Functions and Operators</a>.
 * </p>
 * <p>
 * The function returns the effective boolean value of given arguments.
 * </p>
 * 
 * @author Tina Scherer
 */
public class FNBoolean extends AbstractFunction {

  /**
   * Constructor. Initializes internal state and do a statical analysis
   * concerning the function's arguments.
   * 
   * @param rtx
   *          Transaction to operate on
   * @param args
   *          List of function arguments
   * @param min
   *          min number of allowed function arguments
   * @param max
   *          max number of allowed function arguments
   * @param returnType
   *          the type that the function's result will have
   */
  public FNBoolean(final IReadTransaction rtx, final List<IAxis> args,
      final int min, final int max, final int returnType) {

    super(rtx, args, min, max, returnType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected byte[] computeResult() {

    final IAxis axis = getArgs().get(0);
    boolean value = false;

    if (axis.hasNext()) {

      final IReadTransaction rtx = axis.getTransaction();

      if (rtx.getNodeKey() >= 0) { // first item is a node -> true
        value = true;
      } else {

        final Type type = Type.getType(rtx.getTypeKey());

        if (type.derivesFrom(Type.BOOLEAN)) {
          value = Boolean.parseBoolean(rtx.getValue());
          // value = TypedValue.parseBoolean(rtx.getRawValue()); //TODO
        } else if (type.derivesFrom(Type.STRING)
            || type.derivesFrom(Type.ANY_URI)
            || type.derivesFrom(Type.UNTYPED_ATOMIC)) {
          // if length = 0 -> false
          value = (rtx.getValue().length() > 0);
        } else if (type.isNumericType()) {
          final double dValue = TypedValue.parseDouble(rtx.getRawValue());
          value = !(dValue == Double.NaN || dValue == 0.0d);
        } else {
          // for all other types throw error FORG0006
          throw new XPathError(ErrorType.FORG0006);
        }

        // if is not a singleton
        if (axis.hasNext()) {
          throw new XPathError(ErrorType.FORG0006);
        }
      }

    } else {
      // expression is an empty sequence -> false
      value = false;
    }

    return TypedValue.getBytes(Boolean.toString(value));

  }

}

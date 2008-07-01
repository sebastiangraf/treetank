
package org.treetank.xpath.expr;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.functions.Function;

/**
 * <h1> Logical Or Expression </h1>
 * <p>
 * The logical or expression performs a logical disjunction of the boolean
 * values of two input sequences. If a logical expression does not raise an
 * error, its value is always one of the boolean values true or false.
 * </p>
 * <p>
 * The value of an or-expression is determined by the effective boolean values
 * of its operands, as shown in the following table: <table>
 * <tr>
 * <th>OR</th>
 * <th>EBV2 = true</th>
 * <th>EBV2 = false</th>
 * <th>error in EBV2</th>
 * </tr>
 * <tr>
 * <th>EBV1 = true</th>
 * <th>true</th>
 * <th>true</th>
 * <th>true</th>
 * </tr>
 * <tr>
 * <th>EBV1 = false</th>
 * <th>true</th>
 * <th>false</th>
 * <th>error</th>
 * </tr>
 * <tr>
 * <th>error in EBV1</th>
 * <th>error</th>
 * <th>error</th>
 * <th>error</th>
 * </tr>
 * </table>
 * 
 * @author Tina Scherer
 */
public class OrExpr extends AbstractExpression implements IAxis {

  /** First operand of the logical expression. */
  private final IAxis mOp1;

  /** Second operand of the logical expression. */
  private final IAxis mOp2;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) transaction to iterate with.
   * @param operand1
   *          First operand
   * @param operand2
   *          Second operand
   */
  public OrExpr(final IReadTransaction rtx, final IAxis operand1,
      final IAxis operand2) {

    super(rtx);
    mOp1 = operand1;
    mOp2 = operand2;

  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(final long nodeKey) {

    super.reset(nodeKey);
    if (mOp1 != null) {
      mOp1.reset(nodeKey);
    }
    if (mOp2 != null) {
      mOp2.reset(nodeKey);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void evaluate() {

    // first find the effective boolean values of the two operands, then
    // determine value of the and-expression and store it in am item
    final boolean result = Function.ebv(mOp1) || Function.ebv(mOp2);
    // note: the error handling is implicitly done by the fnBoolean() function.

    // add result item to list and set the item as the current item
    int itemKey = getTransaction().getItemList().addItem(new AtomicValue(
            TypedValue.getBytes(Boolean.toString(result)), 
            getTransaction().keyForName("xs:boolean")));
    getTransaction().moveTo(itemKey);

  }

}

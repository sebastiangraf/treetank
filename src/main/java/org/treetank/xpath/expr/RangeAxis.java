
package org.treetank.xpath.expr;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.types.Type;

/**
 * <h1>RangeExpr</h1>
 * <p>
 * A range expression can be used to construct a sequence of consecutive
 * integers.
 * </p>
 * <p>
 * If either operand is an empty sequence, or if the integer derived from the
 * first operand is greater than the integer derived from the second operand,
 * the result of the range expression is an empty sequence.
 * </p>
 * <p>
 * If the two operands convert to the same integer, the result of the range
 * expression is that integer. Otherwise, the result is a sequence containing
 * the two integer operands and every integer between the two operands, in
 * increasing order.
 * </p>
 * 
 * @author Tina Scherer
 */
public class RangeAxis extends AbstractAxis implements IAxis {

  /** The expression the range starts from. */
  private final IAxis mFrom;

  /** The expression the range ends. */
  private final IAxis mTo;

  /** Is it the first run of range axis? */
  private boolean mFirst;

  /** The integer value the expression starts from. */
  private int mStart;

  /** The integer value the expression ends. */
  private int mEnd;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param from
   *          start of the range
   * @param to
   *          the end of the range
   */
  public RangeAxis(final IReadTransaction rtx, final IAxis from, 
      final IAxis to) {

    super(rtx);
    mFrom = from;
    mTo = to;
    mFirst = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {

    resetToLastKey();

    if (mFirst) {
      mFirst = false;
      if (mFrom.hasNext()
        && Type.getType(mFrom.getTransaction().getTypeKey())
            .derivesFrom(Type.INTEGER)) {
          mStart = (int) Double.parseDouble(mFrom.getTransaction().getValue());
          
         if (mTo.hasNext()
          && Type.getType(mTo.getTransaction().getTypeKey())
              .derivesFrom(Type.INTEGER)) {
        
          mEnd = Integer.parseInt(mTo.getTransaction().getValue());
       
        } else {
          // at least one operand is the empty sequence
          resetToStartKey();
          return false;
        }
      } else {
     // at least one operand is the empty sequence
        resetToStartKey();
        return false;
        }
      }

    if (mStart <= mEnd) {
      int itemKey = getTransaction().getItemList().addItem(
          new AtomicValue(TypedValue.getBytes(Integer.toString(mStart)), 
              getTransaction().keyForName("xs:integer")));
      getTransaction().moveTo(itemKey);
      mStart++;
      return true;
    } else {
      resetToStartKey();
      return false;
    }
  }

}

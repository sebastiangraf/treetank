
package org.treetank.xpath.expr;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.SequenceType;

/**
 * <h1>InstanceOfExpr</h1>
 * <p>
 * The boolean instance of expression returns true if the value of its first
 * operand matches the SequenceType in its second operand, according to the
 * rules for SequenceType matching; otherwise it returns false.
 * </p>
 * 
 * @author Tina Scherer
 */
public class InstanceOfExpr extends AbstractExpression implements IAxis {

  /** The sequence to test. */
  private final IAxis mInputExpr;

  /** The sequence type that the sequence needs to have to be an instance of. */
  private final SequenceType mSequenceType;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param inputExpr
   *          input expression, to test
   * @param sequenceType
   *          sequence type to test whether the input sequence matches to.
   */
  public InstanceOfExpr(final IReadTransaction rtx, final IAxis inputExpr,
      final SequenceType sequenceType) {

    super(rtx);
    mInputExpr = inputExpr;
    mSequenceType = sequenceType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(final long nodeKey) {

    super.reset(nodeKey);

    if (mInputExpr != null) {
      mInputExpr.reset(nodeKey);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void evaluate() {


    boolean isInstanceOf;

    if (mInputExpr.hasNext()) {
      if (mSequenceType.isEmptySequence()) {
        isInstanceOf = false;
      } else {

        isInstanceOf = mSequenceType.getFilter().filter();
        switch (mSequenceType.getWildcard()) {

          case '*':
          case '+':
            // This seams to break the pipeline, but because the intermediate
            // result are no longer used, it might be not that bad
            while (mInputExpr.hasNext() && isInstanceOf) {
              isInstanceOf = isInstanceOf && mSequenceType.getFilter().filter();
            }
            break;
          default: // no wildcard, or '?'
            // only one result item is allowed
            isInstanceOf = isInstanceOf && !mInputExpr.hasNext();
        }
      }

    } else { // empty sequence
      isInstanceOf = mSequenceType.isEmptySequence()
          || (mSequenceType.hasWildcard() && (mSequenceType.getWildcard() == '?'
            || mSequenceType
              .getWildcard() == '*'));
    }

    // create result item and move transaction to it.
    int itemKey = getTransaction().getItemList().addItem(new AtomicValue(
        TypedValue.getBytes(Boolean.toString(isInstanceOf)),
        getTransaction().keyForName("xs:boolean")));
    getTransaction().moveTo(itemKey);

  }
}


package org.treetank.xpath.expr;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;

/**
 * <h1>UnionAxis</h1>
 * <p>
 * Returns an union of two operands. This axis takes two node sequences as
 * operands and returns a sequence containing all the items that occur in either
 * of the operands. A union of two sequences may lead to a sequence containing
 * duplicates. These duplicates can be removed by wrapping the UnionAxis with a
 * DupFilterAxis. The resulting sequence may also be out of document order.
 * </p>
 * 
 * @author Tina Scherer
 */
public class UnionAxis extends AbstractAxis implements IAxis {

  /** First operand sequence. */
  private final IAxis mOp1;

  /** Second operand sequence. */
  private final IAxis mOp2;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param operand1
   *          First operand
   * @param operand2
   *          Second operand
   */
  public UnionAxis(final IReadTransaction rtx, final IAxis operand1,
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
  public boolean hasNext() {

    // first return all values of the first operand
    while (mOp1.hasNext()) {
      mOp1.next();
      
      if (getTransaction().getNodeKey() < 0) {  //only nodes are allowed
        throw new XPathError(ErrorType.XPTY0004);
      } 
      return true;
    }

    // then all values of the second operand.
    while (mOp2.hasNext()) {
      mOp2.next();
      
      if (getTransaction().getNodeKey() < 0) {  //only nodes are allowed
        throw new XPathError(ErrorType.XPTY0004);
      } 
      return true;
    }

    return false;
  }

}

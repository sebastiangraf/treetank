
package org.treetank.xpath.expr;

import java.util.HashSet;
import java.util.Set;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.functions.XPathError.ErrorType;

/**
 * <h1>ExceptAxis</h1>
 * <p>
 * Returns the nodes of the first operand except those of the second operand.
 * This axis takes two node sequences as operands and returns a sequence
 * containing all the nodes that occur in the first, but not in the second
 * operand.
 * </p>
 * 
 * @author Tina Scherer
 */
public class ExceptAxis extends AbstractAxis implements IAxis {

  /** First operand sequence. */
  private final IAxis mOp1;

  /** Second operand sequence. */
  private final IAxis mOp2;

  /**
   * Set that is used to determine, whether an item of the first operand is also
   * contained in the result set of the second operand.
   */
  private final Set<Long> mDupSet;

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
  public ExceptAxis(final IReadTransaction rtx, final IAxis operand1,
      final IAxis operand2) {

    super(rtx);
    mOp1 = operand1;
    mOp2 = operand2;
    mDupSet = new HashSet<Long>();

  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void reset(final long nodeKey) {

    super.reset(nodeKey);
    if (mDupSet != null) {
      mDupSet.clear();
    }
    
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

    // first all items of the second operand are stored in the set.
    while (mOp2.hasNext()) {
      if (getTransaction().getNodeKey() < 0) {  //only nodes are allowed
        throw new XPathError(ErrorType.XPTY0004);
      } 
      mDupSet.add(getTransaction().getNodeKey());
    }

    while (mOp1.hasNext()) {
      if (getTransaction().getNodeKey() < 0) {  //only nodes are allowed
        throw new XPathError(ErrorType.XPTY0004);
      } 
      
      // return true, if node is not already in the set, which means, that it is
      // not also an item of the result set of the second operand sequence.
      if (mDupSet.add(getTransaction().getNodeKey())) {
        return true;
      }
    }

    return false;
  }

}

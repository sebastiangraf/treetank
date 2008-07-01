package org.treetank.xpath.expr;
//
//package org.treetank.extensions.xpath.expr;
//
//import org.treetank.api.IAxis;
//import org.treetank.api.IReadTransaction;
//import org.treetank.axislayer.AbstractAxis;
//import org.treetank.extensions.xpath.XPathConstants;
//import org.treetank.extensions.xpath.functions.Operators;
//
///**
// * <h1>OperatorAxis</h1>
// * <p>
// * Axis to perform an arithmetic operation.
// * </p>
// * 
// * @author Tina Scherer
// */
//public class OperatorAxis extends AbstractAxis implements IAxis, XPathConstants {
//
//  private final IAxis mOperand1;
//
//  private final IAxis mOperand2;
//
//  private final Operators mOperator;
//
//  private boolean mIsFirst;
//
//  /**
//   * Constructor. Initializes the internal state.
//   * 
//   * @param rtx
//   *          Exclusive (immutable) trx to iterate with.
//   * @param op1
//   *          First value of the operation
//   * @param op2
//   *          Second value of the operation
//   * @param operator
//   *          The operation type
//   */
//  public OperatorAxis(final IReadTransaction rtx, final IAxis op1,
//      final IAxis op2, final Operators operator) {
//
//    super(rtx);
//    mOperand1 = op1;
//    mOperand2 = op2;
//    mOperator = operator;
//    mIsFirst = true;
//
//  }
//
//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  public final void reset(final long nodeKey) {
//
//    super.reset(nodeKey);
//    mIsFirst = true;
//    if (mOperand1 != null) {
//      mOperand1.reset(nodeKey);
//    }
//    
//    if (mOperand2 != null) {
//      mOperand2.reset(nodeKey);
//    }
//  }
//
//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  public boolean hasNext() {
//
//    resetToLastKey();
//    
//
//    if (mIsFirst) {
//      mIsFirst = false;
//
//
//      if (mOperator.eval(getTransaction(), mOperand1, mOperand2)) {
//        return true;
//      }
//
//    } 
//    // either not the first call,
//    resetToStartKey();
//    return false;
//
//  }
//
//}

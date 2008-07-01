
package org.treetank.xpath.filter;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;

/**
 * @author Tina Scherer
 */
public class PosFilter extends AbstractAxis implements IAxis {

  private final int mExpectedPos;

  /** The position of the current item. */
  private int mPosCount;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param expectedPos
   *          he expected position
   */
  public PosFilter(final IReadTransaction rtx, final int expectedPos) {

    super(rtx);
    mExpectedPos = expectedPos;
    mPosCount = 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mPosCount = 0;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {

    resetToLastKey();

    // a predicate has to evaluate to true only once.
    if (mExpectedPos == ++mPosCount) {
      return true;
    }

    resetToStartKey();
    return false;

  }

}

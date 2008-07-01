
package org.treetank.xpath.filter;

import java.util.HashSet;
import java.util.Set;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.axislayer.FilterAxis;
import org.treetank.axislayer.NestedAxis;
import org.treetank.xpath.expr.UnionAxis;

/**
 * <h1>DupFilterAxis</h1>
 * <p>
 * Duplicate Filter. Assures that the resulting node set contains no duplicates.
 * </p>
 * <p>
 * Encapsulates a given XPath axis and only passes on those items that have not
 * already been passed. This does not break the pipeline since every
 * intermediary result is immediately passed on, as long as it is not already in
 * the set (which indicates that it was already returned). </p>
 * 
 * @author Tina Scherer
 */
public class DupFilterAxis extends AbstractAxis {

  /** Sequence that may contain duplicates. */
  private final IAxis mAxis;

  /** Set that stores all already returned item keys. */
  private final Set<Long> mDupSet;

  /**
   * Defines whether next() has to be called for the dupAxis after calling
   * hasNext(). In some cases next() has already been called by another axis.
   */
  private final boolean callNext;

  /**
   * Constructor. Initializes the internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   * @param dupAxis
   *          Sequence that may return duplicates.
   */
  public DupFilterAxis(final IReadTransaction rtx, final IAxis dupAxis) {

    super(rtx);
    mAxis = dupAxis;
    mDupSet = new HashSet<Long>();
    // if the dupAxis is not one of the specified axis, 'next()' has explicitly
    // be called for those axis after calling 'hasNext()'. For all other axis
    // next() has already been called by another axis.
    callNext = !(
        mAxis instanceof FilterAxis 
        || mAxis instanceof NestedAxis 
        || mAxis instanceof UnionAxis);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    if (mAxis != null) {
      mAxis.reset(nodeKey);
    }
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {

    resetToLastKey();

    while (mAxis.hasNext()) {

      // call next(), if it was not already called for that axis.
      if (callNext) {
        mAxis.next();
      }

      // add current item key to the set. If true is returned the item is no
      // duplicate and can be returned by the duplicate filter.
      if (mDupSet.add(getTransaction().getNodeKey())) {
        return true;
      }
    }

    resetToStartKey();
    return false;
  }
}

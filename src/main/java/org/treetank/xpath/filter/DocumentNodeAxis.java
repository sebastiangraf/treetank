
package org.treetank.xpath.filter;

import org.treetank.api.IAxis;
import org.treetank.axislayer.AbstractAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>DocumentNodeAxis</h1>
 * <p>
 * Iterate to document node starting at a given node.
 * </p>
 */
public class DocumentNodeAxis extends AbstractAxis implements IAxis {

  /** Track number of calls of next. */
  private boolean mFirst;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) transaction to iterate with.
   */
  public DocumentNodeAxis(final IReadTransaction rtx) {

    super(rtx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mFirst = true;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {

    resetToLastKey();

    if (mFirst) {
      mFirst = false;
      getTransaction().moveToDocumentRoot();
      return true;
    } else {
      resetToStartKey();
      return false;
    }
  }

}

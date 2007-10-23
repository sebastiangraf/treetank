package org.treetank.xmllayer;

import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastStack;

/**
 * <h1>PostOrderIterator</h1>
 * 
 * <p>
 * Iterate over the whole tree starting with the last node.
 * </p>
 */
public class PostOrderAxis extends AbstractAxis {

  /** For remembering last parent. */
  private final FastStack<Long> lastParent;

  /** The nodeKey of the next node to visit. */
  private long nextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *            Exclusive (immutable) trx to iterate with.
   * @param startAtBeginning
   *            Starting at the beginning of the tree and though just
   *            traversing the whole tree..No, the root is not the start!
   */
  public PostOrderAxis(
      final IReadTransaction rtx,
      final boolean startAtBeginning) {
    super(rtx);
    lastParent = new FastStack<Long>();
    lastParent.push(IConstants.NULL_KEY);
    nextKey = rtx.getNodeKey();
    if (startAtBeginning) {
      startAtBeginning();
    }

  }

  /**
   * Method to start at the beginning of the tree.
   */
  private final void startAtBeginning() {
    mRTX.moveToRoot();
    while (mRTX.getFirstChildKey() != IConstants.NULL_KEY) {
      lastParent.push(mRTX.getNodeKey());
      mRTX.moveToFirstChild();

      nextKey = mRTX.getNodeKey();
    }
    next();
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasNext() {

    if (mRTX.moveTo(nextKey)) {
      while (mRTX.getFirstChildKey() != IConstants.NULL_KEY
          && mRTX.getNodeKey() != this.lastParent.peek()) {
        this.lastParent.push(mRTX.getNodeKey());
        mRTX.moveToFirstChild();
      }
      if (mRTX.getNodeKey() == this.lastParent.peek()) {
        this.lastParent.pop();
      }

      if (mRTX.getRightSiblingKey() != IConstants.NULL_KEY) {
        nextKey = mRTX.getRightSiblingKey();

      } else {
        nextKey = this.lastParent.peek();
      }

      mCurrentNode = mRTX.getNode();
      return true;

    } else {
      mCurrentNode = null;
      return false;
    }
  }

}

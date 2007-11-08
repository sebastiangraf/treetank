package org.treetank.axislayer;

import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastStack;

/**
 * <h1>PostOrder</h1>
 * 
 * <p>
 * Iterate over the whole tree starting with the last node.
 * </p>
 */
public class PostOrderAxis extends AbstractAxis {

  /** For remembering last parent. */
  private final FastStack<Long> mLastParent;

  /** The nodeKey of the next node to visit. */
  private long mNextKey;

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
    mLastParent = new FastStack<Long>();
    mLastParent.push(IConstants.NULL_KEY);
    mNextKey = rtx.getNodeKey();
    if (startAtBeginning) {
      startAtBeginning();
    }

  }

  /**
   * Method to start at the beginning of the tree.
   */
  private final void startAtBeginning() {
    mRTX.moveToDocumentRoot();
    while (mRTX.hasFirstChild()) {
      mLastParent.push(mRTX.getNodeKey());
      mRTX.moveToFirstChild();

      mNextKey = mRTX.getNodeKey();
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasNext() {
    resetToLastKey();
    long key = mRTX.moveTo(mNextKey);
    if (mRTX.isSelected()) {
      while (mRTX.hasFirstChild() && key != mLastParent.peek()) {
        mLastParent.push(key);
        key = mRTX.moveToFirstChild();
      }
      if (key == mLastParent.peek()) {
        mLastParent.pop();
      }

      if (mRTX.hasRightSibling()) {
        mNextKey = mRTX.getRightSiblingKey();

      } else {
        mNextKey = mLastParent.peek();
      }

      return true;

    } else {
      resetToStartKey();
      return false;
    }
  }

}

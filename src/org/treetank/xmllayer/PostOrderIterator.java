package org.treetank.xmllayer;

import org.treetank.api.IAxisIterator;
import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastLongStack;

/**
 * <h1>PostOrderIterator</h1>
 * 
 * <p>
 * Iterate over the whole tree starting with the last node.
 * </p>
 */
public class PostOrderIterator implements IAxisIterator {

  /** Exclusive (immutable) trx to iterate with. */
  private final IReadTransaction trx;

  /** For remembering last parent */
  private final FastLongStack lastParent;

  /** The nodeKey of the next node to visit. */
  private long nextKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param initTrx
   *            Exclusive (immutable) trx to iterate with.
   * @param startAtBeginning
   *            Starting at the beginning of the tree and though just
   *            traversing the whole tree..No, the root is not the start!
   * @throws Exception
   *             of any kind.
   */
  public PostOrderIterator(
      final IReadTransaction initTrx,
      final boolean startAtBeginning) throws Exception {

    // Init members.
    trx = initTrx;
    lastParent = new FastLongStack();
    lastParent.push(IConstants.NULL_KEY);
    nextKey = trx.getNodeKey();
    if (startAtBeginning) {
      startAtBeginning();
    }

  }

  /**
   * Method to start at the beginning of the tree
   * 
   * @return
   * @throws Exception
   */
  private void startAtBeginning() throws Exception {
    trx.moveToRoot();
    while (trx.getFirstChildKey() != IConstants.NULL_KEY) {
      lastParent.push(trx.getNodeKey());
      trx.moveToFirstChild();

      nextKey = trx.getNodeKey();
    }
    next();
  }

  /**
   * {@inheritDoc}
   */
  public boolean next() throws Exception {

    if (trx.moveTo(nextKey)) {
      while (trx.getFirstChildKey() != IConstants.NULL_KEY
          && trx.getNodeKey() != this.lastParent.peek()) {
        this.lastParent.push(trx.getNodeKey());
        trx.moveToFirstChild();
      }
      if (trx.getNodeKey() == this.lastParent.peek()) {
        this.lastParent.pop();
      }

      if (trx.getRightSiblingKey() != IConstants.NULL_KEY) {
        nextKey = trx.getRightSiblingKey();

      } else {
        nextKey = this.lastParent.peek();
      }

      return true;

    } else {
      return false;
    }
  }

}

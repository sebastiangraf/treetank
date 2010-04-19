package com.treetank.saxon.axis;

import com.treetank.axis.AbstractAxis;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.utils.FastStack;

/**
 * <h1>Preceding-or-Ancestor Axis</h1>
 * 
 * <p>
 * Iterate over all preceding nodes of kind ELEMENT or TEXT starting at a given
 * node. Self is not included.
 * </p>
 */
public class PrecedingOrAncestorAxis extends AbstractAxis implements IAxis {

  private boolean mIsFirst;

  private FastStack<Long> mStack;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *            Exclusive (immutable) trx to iterate with.
   */
  public PrecedingOrAncestorAxis(final IReadTransaction rtx) {

    super(rtx);
    mIsFirst = true;
    mStack = new FastStack<Long>();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mIsFirst = true;
    mStack = new FastStack<Long>();

  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {

    // assure, that preceding is not evaluated on an attribute or a
    // namespace
    if (mIsFirst) {
      mIsFirst = false;
      if (getTransaction().getNode().isAttribute()
      // || getTransaction().isNamespaceKind()
      ) {
        resetToStartKey();
        return false;
      }
    }

    resetToLastKey();

    if (!mStack.empty()) {
      // return all nodes of the current subtree in reverse document order
      getTransaction().moveTo(mStack.pop());
      return true;
    }

    if (getTransaction().getNode().hasLeftSibling()) {
      getTransaction().moveToLeftSibling();
      // because this axis return the precedings in reverse document
      // order, we
      // need to travel to the node in the subtree, that comes last in
      // document
      // order.
      getLastChild();
      return true;
    }

    while (getTransaction().getNode().hasParent()) {
      getTransaction().moveToParent();
      return true;
    }
    
    if (getTransaction().getNode().hasLeftSibling()) {
      getTransaction().moveToLeftSibling();
      // move to last node in the subtree
      getLastChild();
      return true;
    }

    resetToStartKey();
    return false;

  }

  /**
   * Moves the transaction to the node in the current subtree, that is last in
   * document order and pushes all other node key on a stack. At the end the
   * stack contains all node keys except for the last one in reverse document
   * order.
   */
  private void getLastChild() {

    // nodekey of the root of the current subtree
    final long parent = getTransaction().getNode().getNodeKey();

    // traverse tree in pre order to the leftmost leaf of the subtree and
    // push
    // all nodes to the stack
    if (getTransaction().getNode().hasFirstChild()) {
      while (getTransaction().getNode().hasFirstChild()) {
        mStack.push(getTransaction().getNode().getNodeKey());
        getTransaction().moveToFirstChild();
      }

      // traverse all the siblings of the leftmost leave and all their
      // descendants and push all of them to the stack
      while (getTransaction().getNode().hasRightSibling()) {
        mStack.push(getTransaction().getNode().getNodeKey());
        getTransaction().moveToRightSibling();
        getLastChild();
      }

      // step up the path till the root of the current subtree and process
      // all
      // right siblings and their descendants on each step
      if (getTransaction().getNode().hasParent()
          && (getTransaction().getNode().getParentKey() != parent)) {

        mStack.push(getTransaction().getNode().getNodeKey());
        while (getTransaction().getNode().hasParent()
            && (getTransaction().getNode().getParentKey() != parent)) {

          getTransaction().moveToParent();

          // traverse all the siblings of the leftmost leave and all
          // their
          // descendants and push all of them to the stack
          while (getTransaction().getNode().hasRightSibling()) {

            getTransaction().moveToRightSibling();
            getLastChild();
            mStack.push(getTransaction().getNode().getNodeKey());
          }
        }

        // set transaction to the node in the subtree that is last in
        // document
        // order
        getTransaction().moveTo(mStack.pop());
      }
    }
  }
}
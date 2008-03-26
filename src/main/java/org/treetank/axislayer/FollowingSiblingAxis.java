
package org.treetank.axislayer;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>FollowingSiblingAxis</h1>
 * 
 * <p>
 * Iterate over all following siblings of kind ELEMENT or TEXT starting at a
 * given node. Self is not included.
 * </p>
 */
public class FollowingSiblingAxis extends AbstractAxis implements IAxis {

  private boolean mIsFirst;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   */
  public FollowingSiblingAxis(final IReadTransaction rtx) {

    super(rtx);
    mIsFirst = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mIsFirst = true;

  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {

    
    if (mIsFirst) {
      mIsFirst = false;
      //if the context node is an attribute or namespace node, 
      //the following-sibling axis is empty
      if (getTransaction().isAttributeKind() 
       //   || getTransaction().isNamespaceKind()
          ) {
        resetToStartKey();
        return false;
      }
    }
    
    resetToLastKey();
    
    
    
    if (getTransaction().hasRightSibling()) {
      getTransaction().moveToRightSibling();
      return true;
    } else {
      resetToStartKey();
      return false;
    }
  }

}

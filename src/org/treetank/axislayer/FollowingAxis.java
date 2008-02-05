
package org.treetank.axislayer;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>FollowingAxis</h1>
 * 
 * <p>
 * Iterate over all following nodes of kind ELEMENT or TEXT starting at a given
 * node. Self is not included.
 * </p>
 */
public class FollowingAxis extends AbstractAxis implements IAxis {

  private boolean mIsFirst;

  private long lastKey;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   */
  public FollowingAxis(final IReadTransaction rtx) {

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
    lastKey = getTransaction().getNodeKey();

  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {

    if (mIsFirst) {
    //TODO: do attributes have followings?
      if (getTransaction().isAttributeKind() 
          //   || getTransaction().isNamespaceKind()
             ) {
        resetToStartKey();
        return false;
      }
    }
    
    resetToLastKey();

    // first following is either the following sibling of the input node,
    // or the following sibling of an ancestor.
    if (mIsFirst) {
      mIsFirst = false;
      
      // try to get following sibling
      if (getTransaction().hasRightSibling()) {
        getTransaction().moveToRightSibling();
        lastKey = getTransaction().getNodeKey();
        return true;
      } else {
        // if there is not right sibling of the context node, try to find
        // the first right sibling of one of the ancestors.
        while (getTransaction().hasParent()) {
          getTransaction().moveToParent();
          if (getTransaction().hasRightSibling()) {
            getTransaction().moveToRightSibling();
            lastKey = getTransaction().getNodeKey();
            return true;
          }
        }
        
        assert getTransaction().isDocumentRootKind();
        // context node has no following nodes
        resetToStartKey();
        return false;
      }

    } else {
      //TODO: This is not save in case of an update
      // iterate in pre-order 
      if (getTransaction().moveTo(++lastKey)) {
        return true;
      } else {
        resetToStartKey();
        return false;
      }
    }
  }

}


package org.treetank.axislayer;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>PrecedingAxis</h1>
 * 
 * <p>
 * Iterate over all preceding nodes of kind ELEMENT or TEXT starting at a
 * given node. Self is not included.
 * </p>
 */
public class PrecedingAxis extends AbstractAxis implements IAxis {

  private long mContextNode;
  private long mLastKey;
  private boolean mIsFirst;
  
  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   */
  public PrecedingAxis(final IReadTransaction rtx) {

    super(rtx);
    mContextNode = getTransaction().getNodeKey();
    
    
    
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mContextNode = getTransaction().getNodeKey();
    mIsFirst = true;

  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {

    if (mIsFirst) {
      if (getTransaction().isAttributeKind() 
      //   || getTransaction().isNamespaceKind()
      ) {
          resetToStartKey();
          return false;
        }
      }
    
    resetToLastKey();
    
    if (mIsFirst) {
      mIsFirst = false;
      
      
      getTransaction().moveToDocumentRoot();
      mLastKey = getTransaction().getNodeKey();
      
    } else {
      //TODO: incrementing lastKey is not save in case of updates
      if (!getTransaction().moveTo(++mLastKey)) {
        resetToStartKey();
        return false;
      }
    }
    
    if (getTransaction().getNodeKey() != mContextNode) {
      if (!getTransaction().isElementKind() 
          && !getTransaction().isDocumentRootKind()
          && !getTransaction().isTextKind()) {
        return hasNext();
      } else {
        return true;
      }
    } else {
      resetToStartKey();
      return false;
    }    
  }

}


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
    mIsFirst = true;
    mLastKey = getTransaction().getNodeKey();
    
    
    
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mIsFirst = true;
    mLastKey = getTransaction().getNodeKey();

  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {

    if (mIsFirst) {
      mIsFirst = false;
      if (getTransaction().isAttributeKind() 
      //   || getTransaction().isNamespaceKind()
      ) {
          resetToStartKey();
          return false;
        }
      }
    
    resetToLastKey();
    
    //TODO: This is not save in case of an update
    // iterate in pre-order 
    while (getTransaction().moveTo(--mLastKey)) {
      if (getTransaction().isElementKind() 
          || getTransaction().isDocumentRootKind()
          || getTransaction().isTextKind()) {
            return true;
      }
    } 
    
    resetToStartKey();
    return false;
    
    
        
  }

}

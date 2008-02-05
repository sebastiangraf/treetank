
package org.treetank.axislayer;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;

/**
 * <h1>PrecedingSiblingAxis</h1>
 * 
 * <p>
 * Iterate over all preceding siblings of kind ELEMENT or TEXT starting at a
 * given node. Self is not included.
 * </p>
 */
public class PrecedingSiblingAxis extends AbstractAxis implements IAxis {

  
  private boolean mIsFirst;
  private int mNumSibs;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   */
  public PrecedingSiblingAxis(final IReadTransaction rtx) {

    super(rtx);
    mIsFirst = true;
    mNumSibs = -1;
   }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mIsFirst = true;
    mNumSibs = -1;
    
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    
    if (mIsFirst) {
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
    
    if (mIsFirst) {
      mIsFirst = false;
      
      //if the context node is an attribute or namespace node, 
      //the following-sibling axis is empty
      
      
      if (getTransaction().hasLeftSibling()) {
       
        //go to first sibling in document order
        while (getTransaction().hasLeftSibling()) {
          getTransaction().moveToLeftSibling();
          mNumSibs++;
        }
        return true;
      }
      
    } else {
      if (mNumSibs-- > 0) {
        getTransaction().moveToRightSibling();
        return true;
      }
    }
    resetToStartKey();
    return false;
    
  }


}

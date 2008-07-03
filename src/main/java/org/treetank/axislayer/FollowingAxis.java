/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: $
 */

package org.treetank.axislayer;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastStack;

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
  
  private FastStack<Long> rightSiblingStack;

  /**
   * Constructor initializing internal state.
   * 
   * @param rtx
   *          Exclusive (immutable) trx to iterate with.
   */
  public FollowingAxis(final IReadTransaction rtx) {

    super(rtx);
    mIsFirst = true;
    rightSiblingStack = new FastStack<Long>();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void reset(final long nodeKey) {

    super.reset(nodeKey);
    mIsFirst = true;
    rightSiblingStack = new FastStack<Long>();

  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    
  //assure, that preceding is not evaluated on an attribute or a namespace
    if (mIsFirst && getTransaction().isAttributeKind() 
            //   || getTransaction().isNamespaceKind()
               ) {
          resetToStartKey();
          return false;
        
      }
      
     resetToLastKey();
    
    
    if (mIsFirst) {
      mIsFirst = false;
      //the first following is either a right sibling, or the right sibling of 
      //the first ancestor that has a right sibling.
      //note: ancestors and descendants are no following node!
      if (getTransaction().hasRightSibling()) {
        getTransaction().moveToRightSibling();
        
        if (getTransaction().hasRightSibling()) {
          //push right sibling on a stack to reduce path traversal
          rightSiblingStack.push(getTransaction().getRightSiblingKey());
        }
        return true;
      }
      //Try to find the right sibling of one of the ancestors.
      while (getTransaction().hasParent()) {
        getTransaction().moveToParent();
        if (getTransaction().hasRightSibling()) {
          getTransaction().moveToRightSibling();
          if (getTransaction().hasRightSibling()) {
            rightSiblingStack.push(getTransaction().getRightSiblingKey());
          }
          return true;
        }
      }
      //currentNode is last key in the document order
      resetToStartKey();
      return false;
      
    }
    //step down the tree in document order
    if (getTransaction().hasFirstChild()) {
      getTransaction().moveToFirstChild();
      if (getTransaction().hasRightSibling()) {
      //push right sibling on a stack to reduce path traversal
        rightSiblingStack.push(getTransaction().getRightSiblingKey());
      }
      
      return true;
    }
    if (rightSiblingStack.empty()) {
      
    //Try to find the right sibling of one of the ancestors.
      while (getTransaction().hasParent()) {
        getTransaction().moveToParent();
        if (getTransaction().hasRightSibling()) {
          getTransaction().moveToRightSibling();
          if (getTransaction().hasRightSibling()) {
          //push right sibling on a stack to reduce path traversal
            rightSiblingStack.push(getTransaction().getRightSiblingKey());
          }
          return true;
        }
      }
      
    } else {
      
    //get root key of sibling subtree
      getTransaction().moveTo(rightSiblingStack.pop());
      if (getTransaction().hasRightSibling()) {
      //push right sibling on a stack to reduce path traversal
        rightSiblingStack.push(getTransaction().getRightSiblingKey());
      }
      return true;
    
    }
    resetToStartKey();
    return false;
  }

}

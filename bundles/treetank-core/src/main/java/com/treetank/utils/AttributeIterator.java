package com.treetank.utils;

import java.util.Iterator;

import com.treetank.access.ReadTransaction;
import com.treetank.api.IReadTransaction;
import com.treetank.node.AttributeNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;

public class AttributeIterator implements Iterator<AttributeNode> {

  /** 
   * Treetank reading transaction.
   *  
   * @see ReadTransaction
   */
  private final IReadTransaction mRTX;

  /** Number of attribute nodes. */
  private final int attCount;
  
  /** Index of attribute node. */
  private static int index;

  /** Node key. */
  private final long nodeKey;
  
  /**
   * Constructor.
   * 
   * @param rtx
   *             Treetank reading transaction.
   */
  public AttributeIterator(final IReadTransaction rtx) {
    mRTX = rtx;
    nodeKey = mRTX.getNode().getNodeKey();
    index = 0;
    
    if (mRTX.getNode().getKind() == ENodes.ELEMENT_KIND) {
      attCount = ((ElementNode) mRTX.getNode()).getAttributeCount();
    } else {
      attCount = 0;
    }
  }

  @Override
  public boolean hasNext() {
    boolean retVal = false;
    
    if (index < attCount) {
      retVal = true;
    }
    
    return retVal;
  }

  @Override
  public AttributeNode next() {
    mRTX.moveTo(nodeKey);
    mRTX.moveToAttribute(index);
    assert mRTX.getNode().getKind() == ENodes.ATTRIBUTE_KIND;
    return (AttributeNode) mRTX.getNode();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Not supported!");
  }

}

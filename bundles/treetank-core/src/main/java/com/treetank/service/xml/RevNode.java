package com.treetank.service.xml;

import javax.xml.namespace.QName;

/**
 * <h1>RevNode</h1>
 * 
 * <p>Container which holds the full qualified name of the "timestamp" node.
 * Therefore </p>
 * 
 * @author johannes
 *
 */
public final class RevNode {
  /** QName of the node, which has the timestamp text child. */
  private transient final QName mQName;
  
  /**
   * Constructor.
   * 
   * @param qName 
   *              Full qualified name of the timestamp node.
   */
  public RevNode(final QName qName) {
    mQName = qName;
  }
  
  /**
   * Get mQName.
   * 
   * @return the full qualified name.
   */
  public QName getmQName() {
    return mQName;
  }
}

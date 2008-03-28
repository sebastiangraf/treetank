package org.treetank.api;

import org.treetank.nodelayer.AbstractNode;

public interface IItem {

  long getNodeKey();

  long getParentKey();

  long getFirstChildKey();

  long getLeftSiblingKey();

  long getRightSiblingKey();

  AbstractNode getAttribute(int index);

  boolean hasParent();
  
  boolean hasFirstChild();
  
  boolean hasLeftSibling();
  
  boolean hasRightSibling();


  byte[] getValue();

  long getChildCount();

  int getAttributeCount();

  int getNamespaceCount();

  AbstractNode getNamespace(int index);

  int getKind();

  boolean isDocumentRoot();

  boolean isElement();

  boolean isAttribute();

  boolean isText();

  boolean isFullText();

  boolean isFullTextLeaf();

  boolean isFullTextRoot();

  int getNameKey();

  int getURIKey();

  int getValueType();

}

package com.treetank.utils;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import com.treetank.access.ReadTransaction;
import com.treetank.api.IReadTransaction;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;

/**
 * Implements a namespace iterator, which is needed for the StAX implementation.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class NamespaceIterator implements Iterator<Namespace> {

  /**
   * Treetank reading transaction.
   * 
   * @see ReadTransaction
   */
  private final IReadTransaction mRTX;

  /** Number of namespace nodes. */
  private final int namespCount;

  /** Index of namespace node. */
  private static int index;

  /** Node key. */
  private final long nodeKey;

  /** Factory to create nodes {@link XMLEventFactory}. */
  private transient XMLEventFactory fac = XMLEventFactory.newFactory();

  /**
   * Constructor.
   * 
   * @param rtx
   *             Treetank reading transaction.
   */
  public NamespaceIterator(
      final IReadTransaction rtx) {
    mRTX = rtx;
    nodeKey = mRTX.getNode().getNodeKey();
    index = 0;

    if (mRTX.getNode().getKind() == ENodes.ELEMENT_KIND) {
      namespCount = ((ElementNode) mRTX.getNode()).getNamespaceCount();
    } else {
      namespCount = 0;
    }
  }

  @Override
  public boolean hasNext() {
    boolean retVal = false;

    if (index < namespCount) {
      retVal = true;
    }

    return retVal;
  }

  @Override
  public Namespace next() {
    mRTX.moveTo(nodeKey);
    mRTX.moveToNamespace(index++);
    assert mRTX.getNode().getKind() == ENodes.NAMESPACE_KIND;
    final QName qName = mRTX.getQNameOfCurrentNode();
    mRTX.moveTo(nodeKey);
    return fac.createNamespace(qName.getLocalPart(), qName.getNamespaceURI());
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Not supported!");
  }
}

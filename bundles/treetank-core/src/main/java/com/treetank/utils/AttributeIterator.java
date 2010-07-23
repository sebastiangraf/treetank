package com.treetank.utils;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;

import com.treetank.access.ReadTransaction;
import com.treetank.api.IReadTransaction;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;

public class AttributeIterator implements Iterator<Attribute> {

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

  /** Factory to create nodes {@link XMLEventFactory}. */
  private transient XMLEventFactory fac = XMLEventFactory.newFactory();

  /**
   * Constructor.
   * 
   * @param rtx
   *             Treetank reading transaction.
   */
  public AttributeIterator(
      final IReadTransaction rtx) {
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
  public Attribute next() {
    mRTX.moveTo(nodeKey);
    mRTX.moveToAttribute(index++);
    assert mRTX.getNode().getKind() == ENodes.ATTRIBUTE_KIND;
    final QName qName = mRTX.getQNameOfCurrentNode();
    final String value = mRTX.getValueOfCurrentNode();
    mRTX.moveTo(nodeKey);
    return fac.createAttribute(qName, value);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Not supported!");
  }

  //  /**
  //   * {@inheritDoc}
  //   */
  //  @Override
  //  public int getIndex(final String qName) {
  //    int index = -1;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //
  //    for (int i = 0, count = ((ElementNode) mRTX.getNode()).getAttributeCount(); i < count; i++) {
  //      mRTX.moveToAttribute(i);
  //      if (mRTX.getQNameOfCurrentNode().equals(qName)) {
  //        index = i;
  //        break;
  //      }
  //      mRTX.moveTo(nodeKey);
  //    }
  //
  //    return index;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getIndex(java.lang.String, java.lang.String)
  //   */
  //  @Override
  //  public int getIndex(final String uri, final String localName) {
  //    int index = -1;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //
  //    for (int i = 0, count = ((ElementNode) mRTX.getNode()).getAttributeCount(); i < count; i++) {
  //      mRTX.moveToAttribute(i);
  //      final QName qName = mRTX.getQNameOfCurrentNode();
  //      if (qName.getNamespaceURI().equals(uri)
  //          && qName.getLocalPart().equals(localName)) {
  //        index = i;
  //        break;
  //      }
  //      mRTX.moveTo(nodeKey);
  //    }
  //
  //    return index;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getLength()
  //   */
  //  @Override
  //  public int getLength() {
  //    return ((ElementNode) mRTX.getNode()).getAttributeCount();
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getLocalName(int)
  //   */
  //  @Override
  //  public String getLocalName(final int index) {
  //    String retVal = null;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //    if (mRTX.moveToAttribute(index)) {
  //      retVal = mRTX.getQNameOfCurrentNode().getLocalPart();
  //    }
  //    mRTX.moveTo(nodeKey);
  //    return retVal;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getQName(int)
  //   */
  //  @Override
  //  public String getQName(final int index) {
  //    String retVal = null;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //    if (mRTX.moveToAttribute(index)) {
  //      retVal = mRTX.getQNameOfCurrentNode().toString();
  //    }
  //    mRTX.moveTo(nodeKey);
  //    return retVal;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getType(int)
  //   */
  //  @Override
  //  public String getType(final int index) {
  //    String retVal = null;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //    if (mRTX.moveToAttribute(index)) {
  //      retVal = mRTX.getTypeOfCurrentNode();
  //    }
  //    mRTX.moveTo(nodeKey);
  //    return retVal;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getType(java.lang.String)
  //   */
  //  @Override
  //  public String getType(final String qName) {
  //    String retVal = null;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //
  //    for (int i = 0, count = ((ElementNode) mRTX.getNode()).getAttributeCount(); i < count; i++) {
  //      mRTX.moveToAttribute(i);
  //      if (mRTX.getQNameOfCurrentNode().equals(qName)) {
  //        retVal = mRTX.getTypeOfCurrentNode();
  //        break;
  //      }
  //      mRTX.moveTo(nodeKey);
  //    }
  //
  //    return retVal;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getType(java.lang.String, java.lang.String)
  //   */
  //  @Override
  //  public String getType(final String uri, final String localName) {
  //    String retVal = null;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //
  //    for (int i = 0, count = ((ElementNode) mRTX.getNode()).getAttributeCount(); i < count; i++) {
  //      mRTX.moveToAttribute(i);
  //      final QName qName = mRTX.getQNameOfCurrentNode();
  //      if (qName.getNamespaceURI().equals(uri)
  //          && qName.getLocalPart().equals(localName)) {
  //        retVal = mRTX.getTypeOfCurrentNode();
  //        break;
  //      }
  //      mRTX.moveTo(nodeKey);
  //    }
  //
  //    return retVal;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getURI(int)
  //   */
  //  @Override
  //  public String getURI(final int index) {
  //    String retVal = null;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //    if (mRTX.moveToAttribute(index)) {
  //      retVal = mRTX.getQNameOfCurrentNode().getNamespaceURI();
  //    }
  //    mRTX.moveTo(nodeKey);
  //    return retVal;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getValue(int)
  //   */
  //  @Override
  //  public String getValue(final int index) {
  //    String retVal = null;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //    if (mRTX.moveToAttribute(index)) {
  //      retVal = mRTX.getValueOfCurrentNode();
  //    }
  //    mRTX.moveTo(nodeKey);
  //    return retVal;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getValue(java.lang.String)
  //   */
  //  @Override
  //  public String getValue(final String qName) {
  //    String retVal = null;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //
  //    for (int i = 0, count = ((ElementNode) mRTX.getNode()).getAttributeCount(); i < count; i++) {
  //      mRTX.moveToAttribute(i);
  //      if (mRTX.getQNameOfCurrentNode().equals(qName)) {
  //        retVal = mRTX.getValueOfCurrentNode();
  //        break;
  //      }
  //      mRTX.moveTo(nodeKey);
  //    }
  //
  //    return retVal;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.xml.sax.Attributes#getValue(java.lang.String, java.lang.String)
  //   */
  //  @Override
  //  public String getValue(final String uri, final String localName) {
  //    String retVal = null;
  //    final long nodeKey = mRTX.getNode().getNodeKey();
  //
  //    for (int i = 0, count = ((ElementNode) mRTX.getNode()).getAttributeCount(); i < count; i++) {
  //      mRTX.moveToAttribute(i);
  //      final QName qName = mRTX.getQNameOfCurrentNode();
  //      if (qName.getNamespaceURI().equals(uri)
  //          && qName.getLocalPart().equals(localName)) {
  //        retVal = mRTX.getValueOfCurrentNode();
  //        break;
  //      }
  //      mRTX.moveTo(nodeKey);
  //    }
  //
  //    return retVal;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.Attribute#getDTDType()
  //   */
  //  @Override
  //  public String getDTDType() {
  //    throw new UnsupportedOperationException(
  //        "Currently not supported by Treetank!");
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.Attribute#getName()
  //   */
  //  @Override
  //  public QName getName() {
  //    return mRTX.getQNameOfCurrentNode();
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.Attribute#getValue()
  //   */
  //  @Override
  //  public String getValue() {
  //    return mRTX.getValueOfCurrentNode();
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.Attribute#isSpecified()
  //   */
  //  @Override
  //  public boolean isSpecified() {
  //    // TODO: change once Treetank supports schemas.
  //    return true;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#asCharacters()
  //   */
  //  @Override
  //  public Characters asCharacters() {
  //    throw new ClassCastException();
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#asEndElement()
  //   */
  //  @Override
  //  public EndElement asEndElement() {
  //    throw new ClassCastException();
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#asStartElement()
  //   */
  //  @Override
  //  public StartElement asStartElement() {
  //    throw new ClassCastException();
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#getEventType()
  //   */
  //  @Override
  //  public int getEventType() {
  //    return XMLStreamConstants.ATTRIBUTE;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#getLocation()
  //   */
  //  @Override
  //  public Location getLocation() {
  //    throw new UnsupportedOperationException("Not supported by Treetank!");
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#getSchemaType()
  //   */
  //  @Override
  //  public QName getSchemaType() {
  //    throw new UnsupportedOperationException(
  //        "Currently not supported by Treetank!");
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#isAttribute()
  //   */
  //  @Override
  //  public boolean isAttribute() {
  //    return true;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#isCharacters()
  //   */
  //  @Override
  //  public boolean isCharacters() {
  //    return false;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#isEndDocument()
  //   */
  //  @Override
  //  public boolean isEndDocument() {
  //    return false;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#isEndElement()
  //   */
  //  @Override
  //  public boolean isEndElement() {
  //    return false;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#isEntityReference()
  //   */
  //  @Override
  //  public boolean isEntityReference() {
  //    // TODO Auto-generated method stub
  //    return false;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#isNamespace()
  //   */
  //  @Override
  //  public boolean isNamespace() {
  //    return false;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#isProcessingInstruction()
  //   */
  //  @Override
  //  public boolean isProcessingInstruction() {
  //    return false;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#isStartDocument()
  //   */
  //  @Override
  //  public boolean isStartDocument() {
  //    return false;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#isStartElement()
  //   */
  //  @Override
  //  public boolean isStartElement() {
  //    return false;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see javax.xml.stream.events.XMLEvent#writeAsEncodedUnicode(java.io.Writer)
  //   */
  //  @Override
  //  public void writeAsEncodedUnicode(final Writer writer)
  //      throws XMLStreamException {
  //  }

}

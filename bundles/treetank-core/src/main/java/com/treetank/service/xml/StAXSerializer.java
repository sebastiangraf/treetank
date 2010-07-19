package com.treetank.service.xml;

import static com.treetank.service.xml.SerializerProperties.S_ID;
import static com.treetank.service.xml.SerializerProperties.S_REST;
import static com.treetank.service.xml.SerializerProperties.S_XMLDECL;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.api.IReadTransaction;
import com.treetank.axis.ElementFilter;
import com.treetank.axis.FilterAxis;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.utils.NamespaceIterator;

/**
 * <h1>StAXSerializer</h1>
 * 
 * <p>
 * Provides a StAX implementation (event API) for retrieving a Treetank database. 
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public class StAXSerializer extends AbsSerializeStorage
    implements
    XMLEventReader {

  /** Logger. */
  private static final Log LOGGER = LogFactory.getLog(StAXSerializer.class);

  /** 
   * Element filter axis.
   * 
   * @see ElementFilterAxis 
   */
  private final FilterAxis filterAxis;

  /** 
   * Determines if start tags have to be closed, thus if end tags have to be 
   * emitted. 
   */
  private boolean closeElements;

  /**
   * {@inheritDoc}
   */
  private XMLEvent event;

  /** 
   * XMLEventFactory to create events.
   * 
   * @see XMLEventFactory
   */
  private final XMLEventFactory fac = XMLEventFactory.newFactory();

  /**
   * Initialize XMLStreamReader implementation with transaction. The cursor
   * points to the node the XMLStreamReader starts to read. Do not serialize
   * the tank ids.
   * 
   * @param rtx
   *            Transaction with cursor pointing to start node.
   * @param map
   *            Properties map.
   */
  public StAXSerializer(
      final IReadTransaction rtx,
      final ConcurrentMap<String, Object> map) {
    this(
        rtx,
        (Boolean) map.get(S_XMLDECL),
        (Boolean) map.get(S_REST),
        (Boolean) map.get(S_ID));
  }

  /**
   * {@inheritDoc} 
   */
  public StAXSerializer(
      IReadTransaction rtx,
      boolean serializeXMLDeclaration,
      boolean serializeRest,
      boolean serializeId) {
    super(rtx, serializeXMLDeclaration, serializeRest, serializeId);
    filterAxis = new FilterAxis(mAxis, new ElementFilter(mRTX));
  }

  @Override
  public void emitEndElement() throws IOException {
    event =
        fac.createEndElement(
            mRTX.getQNameOfCurrentNode(),
            new NamespaceIterator(mRTX));
  }

  @Override
  public void emitNode() throws IOException {
    switch (mRTX.getNode().getKind()) {
    case ROOT_KIND:
      event = fac.createStartDocument();
      break;
    case ELEMENT_KIND:
//      final long key = mRTX.getNode().getNodeKey();
      // TODO
      break;
    case TEXT_KIND:
      break;
    }
  }

  @Override
  public void close() throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  @Override
  public String getElementText() throws XMLStreamException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getProperty(String arg0) throws IllegalArgumentException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasNext() {
    return mAxis.hasNext();
  }

  @Override
  public XMLEvent nextEvent() throws XMLStreamException {
    long key = mRTX.getNode().getNodeKey();

    try {
      if (mAxis.hasNext()) {
        key = mAxis.next();
        emit(key);
      }
    } catch (final IOException e) {
      LOGGER.error(e.getMessage(), e);
    }

    return event;
  }

  @Override
  public XMLEvent nextTag() throws XMLStreamException {
    long key = mRTX.getNode().getNodeKey();

    try {
      if (filterAxis.hasNext()) {
        key = filterAxis.next();
        emit(key);
      }
    } catch (final IOException e) {
      LOGGER.error(e.getMessage(), e);
    }

    return event;
  }

  @Override
  public XMLEvent peek() throws XMLStreamException {
    final long currNodeKey = mRTX.getNode().getNodeKey();
    long key = currNodeKey;
    
    try {
      if (mAxis.hasNext()) {
        key = mAxis.next();
        emit(key);
      }
    } catch (final IOException e) {
      LOGGER.error(e.getMessage(), e);
    }
    
    mRTX.moveTo(currNodeKey);
    return event;
  }

  @Override
  public Object next() {
    try {
      event = nextEvent();
    } catch (final XMLStreamException e) {
      LOGGER.error(e.getMessage(), e);
    }
    
    return event;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Not supported!");
  }
  
  /**
   * Move to node and emit it.
   * 
   * @param key 
   *              Current node key.
   * @throws IOException
   *              In case of any I/O error.
   */
  private void emit(final long key) throws IOException {
    // Emit pending end elements.
    if (closeElements) {
      if (!mStack.empty()
          && mStack.peek() != ((AbsStructNode) mRTX.getNode())
              .getLeftSiblingKey()) {
        mRTX.moveTo(mStack.pop());
        emitEndElement();
        mRTX.moveTo(key);
      } else if (!mStack.empty()) {
        mRTX.moveTo(mStack.pop());
        emitEndElement();
      } else {
        mRTX.moveTo(key);
        closeElements = false;
      }
    } else {
      // Emit node.
      emitNode();

      // Push end element to stack if we are a start element with
      // children.
      if (mRTX.getNode().getKind() == ENodes.ELEMENT_KIND
          && ((AbsStructNode) mRTX.getNode()).hasFirstChild()) {
        mStack.push(mRTX.getNode().getNodeKey());
      }

      // Remember to emit all pending end elements from stack if
      // required.
      if (!((AbsStructNode) mRTX.getNode()).hasFirstChild()
          && !((AbsStructNode) mRTX.getNode()).hasRightSibling()) {
        closeElements = true;
      }
    }
  }
}

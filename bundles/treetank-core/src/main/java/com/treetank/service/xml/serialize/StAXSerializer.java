package com.treetank.service.xml.serialize;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.access.ReadTransaction;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.DescendantAxis;
import com.treetank.axis.FilterAxis;
import com.treetank.axis.TextFilter;
import com.treetank.exception.TreetankException;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.utils.FastStack;

/**
 * <h1>StAXSerializer</h1>
 * 
 * <p>
 * Provides a StAX implementation (event API) for retrieving a Treetank
 * database.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class StAXSerializer implements XMLEventReader {

  /** Logger. */
  private static final Logger LOGGER =
      LoggerFactory.getLogger(StAXSerializer.class);

  /**
   * Determines if start tags have to be closed, thus if end tags have to be
   * emitted.
   */
  private transient boolean closeElements;

  /**
   * {@inheritDoc}
   */
  private transient XMLEvent event;

  /**
   * XMLEventFactory to create events.
   * 
   * @see XMLEventFactory
   */
  private transient final XMLEventFactory fac = XMLEventFactory.newFactory();

  /** Current node key. */
  private transient long key;

  /**
   * Determines if all end tags have been emitted.
   */
  private transient boolean closeElementsEmitted = false;

  /** Determines if nextTag() method has been called. */
  private transient boolean nextTag = false;

  /** axis for iteration */
  private transient final IAxis mAxis;

  /** Stack for reading end element. */
  private transient final FastStack<Long> mStack;
  
  /** 
   * Determines if the cursor has to move back after empty elements (used in
   * getElementText().
   */
  private transient boolean goBack;
  
  /** 
   * Determines if the cursor has moved up and therefore has to move back
   * after to the right node (used in getElementText()). */
  private transient boolean goUp;
  
  /** 
   * Last emitted key (start tags, text... except end tags; used in 
   * getElementText()). 
   */
  private transient long lastKey;

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
  public StAXSerializer(final IAxis axis) {
    nextTag = false;
    mAxis = axis;
    mStack = new FastStack<Long>();
  }

  private void emitEndElement(final IReadTransaction rtx) {
    final long nodeKey = rtx.getNode().getNodeKey();
    event =
        fac.createEndElement(
            rtx.getQNameOfCurrentNode(),
            new NamespaceIterator(rtx));
    rtx.moveTo(nodeKey);
  }

  private void emitNode(final IReadTransaction rtx) {
    switch (rtx.getNode().getKind()) {
    case ROOT_KIND:
      event = fac.createStartDocument();
      break;
    case ELEMENT_KIND:
      final long key = rtx.getNode().getNodeKey();
      final QName qName = rtx.getQNameOfCurrentNode();
      event =
          fac.createStartElement(
              qName,
              new AttributeIterator(rtx),
              new NamespaceIterator(rtx));
      rtx.moveTo(key);
      break;
    case TEXT_KIND:
      event = fac.createCharacters(rtx.getValueOfCurrentNode());
      break;
    default:
      throw new IllegalStateException("Kind not known!");
    }
  }

  @Override
  public void close() throws XMLStreamException {
    try {
      mAxis.getTransaction().close();
    } catch (final TreetankException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  @Override
  public String getElementText() throws XMLStreamException {
    final IReadTransaction rtx = mAxis.getTransaction();
    final long nodeKey = rtx.getNode().getNodeKey();
    
    /*
     * The cursor has to move back (once) after determining, that a closing tag 
     * would be the next event (precond: closeElement and either goBack or goUp 
     * is true).
     */
    if (closeElements && (goBack || goUp)) {
      if (goUp) {
        rtx.moveTo(lastKey);
        goUp = false;
      } else if (goBack) {
        rtx.moveTo(mStack.peek());
        goBack = false;
      }
    }

    if (event.getEventType() != XMLStreamConstants.START_ELEMENT) {
      rtx.moveTo(nodeKey);
      throw new XMLStreamException(
          "getElementText() only can be called on a start element");
    }
    final FilterAxis textFilterAxis =
        new FilterAxis(new DescendantAxis(rtx), new TextFilter(rtx));
    final StringBuilder strBuilder = new StringBuilder();

    while (textFilterAxis.hasNext()) {
      textFilterAxis.next();
      strBuilder.append(mAxis.getTransaction().getValueOfCurrentNode());
    }

    rtx.moveTo(nodeKey);
    return strBuilder.toString();
  }

  @Override
  public Object getProperty(final String name) throws IllegalArgumentException {
    throw new UnsupportedOperationException("Not supported by Treetank!");
  }

  @Override
  public boolean hasNext() {
    boolean retVal = false;
    
    if (!mStack.empty() && (closeElements || closeElementsEmitted)) {
      /*
       * mAxis.hasNext() can't be used in this case, because it would iterate 
       * to the next node but at first all end-tags have to be emitted.
       */     
      retVal = true;
    } else {
      retVal = mAxis.hasNext();
    }
    
    return retVal;
  }

  @Override
  public XMLEvent nextEvent() throws XMLStreamException {
    try {
      if (!closeElements && !closeElementsEmitted) {
        key = mAxis.next();

        if (nextTag) {
          if (mAxis.getTransaction().getNode().getKind() != ENodes.ELEMENT_KIND) {
            throw new XMLStreamException(
                "The next tag isn't a start- or end-tag!");
          }
          nextTag = false;
        }
      }
      emit(mAxis.getTransaction());
    } catch (final IOException e) {
      LOGGER.error(e.getMessage(), e);
    }

    return event;
  }

  @Override
  public XMLEvent nextTag() throws XMLStreamException {
    nextTag = true;
    return nextEvent();
  }

  @Override
  public XMLEvent peek() throws XMLStreamException {
    final long currNodeKey = mAxis.getTransaction().getNode().getNodeKey();
    final IReadTransaction rtx = mAxis.getTransaction();
    try {
      if (closeElements) {
        rtx.moveTo(mStack.peek());
        emitEndElement(rtx);
      } else {
        final ENodes nodeKind = rtx.getNode().getKind();
        if (((AbsStructNode) rtx.getNode()).hasFirstChild()) {
          rtx.moveToFirstChild();
          emitNode(rtx);
        } else if (((AbsStructNode) rtx.getNode()).hasRightSibling()) {
          rtx.moveToRightSibling();
          processNode(nodeKind);
        } else if (((AbsStructNode) rtx.getNode()).hasParent()) {
          rtx.moveToParent();
          emitEndElement(rtx);
        }
      }
    } catch (final IOException e) {
      LOGGER.error(e.getMessage(), e);
    }

    rtx.moveTo(currNodeKey);
    return event;
  }

  /**
   * Just calls nextEvent().
   */
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
   * Determines if a node or an end element has to be emitted.
   * 
   * @param nodeKind
   *            The node kind.
   * @throws IOException
   *             In case of any I/O error.
   */
  private void processNode(final ENodes nodeKind) throws IOException {
    switch (nodeKind) {
    case ELEMENT_KIND:
      emitEndElement(mAxis.getTransaction());
      break;
    case TEXT_KIND:
      emitNode(mAxis.getTransaction());
      break;
    default:
      // Do nothing.
    }
  }

  /**
   * Move to node and emit it.
   * 
   * @param key
   *            Current node key.
   * @throws IOException
   *             In case of any I/O error.
   */
  private void emit(final IReadTransaction rtx) throws IOException {
    // Emit pending end elements.
    if (closeElements) {
      if (!mStack.empty()
          && mStack.peek() != ((AbsStructNode) rtx.getNode())
              .getLeftSiblingKey()) {
        rtx.moveTo(mStack.pop());
        emitEndElement(rtx);
        rtx.moveTo(key);
      } else if (!mStack.empty()) {
        rtx.moveTo(mStack.pop());
        emitEndElement(rtx);
        rtx.moveTo(key);
        closeElements = false;
        closeElementsEmitted = true;
      }
    } else {
      closeElementsEmitted = false;

      // Emit node.
      emitNode(rtx);

      final long nodeKey = rtx.getNode().getNodeKey();
      lastKey = nodeKey;

      // Push end element to stack if we are a start element.
      if (rtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
        mStack.push(nodeKey);
      }

      // Remember to emit all pending end elements from stack if
      // required.
      if (!((AbsStructNode) rtx.getNode()).hasFirstChild()
          && !((AbsStructNode) rtx.getNode()).hasRightSibling()) {
        goUp = true;
        moveToNextNode();
      } else if (rtx.getNode().getKind() == ENodes.ELEMENT_KIND
          && !((ElementNode) rtx.getNode()).hasFirstChild()) {
        // Case: Empty elements with right siblings.
        goBack = true;
        moveToNextNode();
      }
    }
  }

  /**
   * Move to next node in tree either in case of a right sibling of an empty
   * element or if no further child and no right sibling can be found, so that
   * the next node is in the following axis.
   */
  private void moveToNextNode() {
    closeElements = true;
    if (mAxis.hasNext()) {
      key = mAxis.next();
    }
  }

  // /**
  // * Main method.
  // *
  // * @param args
  // * args[0] specifies the path to the TT-storage from which to
  // * generate SAX events.
  // * @throws Exception
  // */
  // public static void main(final String... args) throws Exception {
  // if (args.length != 1) {
  // LOGGER.error("Usage: StAXSerializer input-TT");
  // }
  //
  // final IDatabase database = Database.openDatabase(new File(args[0]));
  // final ISession session = database.getSession();
  // final IReadTransaction rtx = session.beginReadTransaction();
  //
  // final XMLEventReader reader = new
  // // new StAXSerializer(rtx, new SerializerProperties(null).getmProps());
  //
  // rtx.close();
  // session.close();
  // database.close();
  // }

  class AttributeIterator implements Iterator<Attribute> {

    /** 
     * Treetank reading transaction.
     *  
     * @see ReadTransaction
     */
    private final IReadTransaction mRTX;

    /** Number of attribute nodes. */
    private final int attCount;

    /** Index of attribute node. */
    private int index;

    /** Node key. */
    private final long nodeKey;

    /** Factory to create nodes {@link XMLEventFactory}. */
    private final transient XMLEventFactory fac = XMLEventFactory.newFactory();

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
  }

  /**
   * Implements a namespace iterator, which is needed for the StAX implementation.
   * 
   * @author Johannes Lichtenberger, University of Konstanz
   * 
   */
  final class NamespaceIterator implements Iterator<Namespace> {

    /**
     * Treetank reading transaction.
     * 
     * @see ReadTransaction
     */
    private final IReadTransaction mRTX;

    /** Number of namespace nodes. */
    private final int namespCount;

    /** Index of namespace node. */
    private int index;

    /** Node key. */
    private final long nodeKey;

    /** Factory to create nodes {@link XMLEventFactory}. */
    private final transient XMLEventFactory fac = XMLEventFactory.newFactory();

    /**
     * Constructor.
     * 
     * @param rtx
     *             Treetank reading transaction.
     */
    public NamespaceIterator(final IReadTransaction rtx) {
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
}

package com.treetank.service.xml;

import static com.treetank.service.xml.SerializerProperties.S_ID;
import static com.treetank.service.xml.SerializerProperties.S_REST;
import static com.treetank.service.xml.SerializerProperties.S_XMLDECL;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.treetank.api.IReadTransaction;

/**
 * <h1>StAXSerializer</h1>
 * 
 * <p>
 * Provides a StAX implementation for retrieving a Treetank database. 
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public class StAXSerializer extends AbsSerializeStorage
    implements
    XMLEventReader, Callable<Void> {
  
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
  }

  @Override
  public Void call() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void emitEndElement() throws IOException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void emitNode() throws IOException {
    // TODO Auto-generated method stub
    
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
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public XMLEvent nextEvent() throws XMLStreamException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public XMLEvent nextTag() throws XMLStreamException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public XMLEvent peek() throws XMLStreamException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object next() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void remove() {
    // TODO Auto-generated method stub
    
  }
}

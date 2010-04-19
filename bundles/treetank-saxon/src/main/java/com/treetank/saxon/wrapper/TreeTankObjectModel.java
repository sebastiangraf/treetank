package com.treetank.saxon.wrapper;

import java.io.Serializable;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.om.ExternalObjectModel;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;

/**
 * This class is the TreeTank implementation of Saxon's ExternalObjectModel 
 * interface. It supports the wrapping of TreeTank documents as instances of the
 * Saxon NodeInfo interface.
 * 
 * @author johannes
 *
 */
public final class TreeTankObjectModel implements ExternalObjectModel, Serializable {

  /**
   * {@inheritDoc}
   */
  public Receiver getDocumentBuilder(final Result result) throws XPathException {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getIdentifyingURI() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JPConverter getJPConverter(Class arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PJConverter getNodeListCreator(Object arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PJConverter getPJConverter(final Class targetClass) {
//    if (isRecognizedNodeClass(targetClass)) {
//      return new PJConverter() {
//          public Object convert(ValueRepresentation value, Class targetClass, XPathContext context) throws XPathException {
//              return convertXPathValueToObject(Value.asValue(value), targetClass);
//          }
//      };
//  } else {
      return null;
//  }
  }

  public boolean sendSource(
      Source arg0,
      Receiver arg1,
      PipelineConfiguration arg2) throws XPathException {
    // TODO Auto-generated method stub
    return false;
  }

  public NodeInfo unravel(Source arg0, Configuration arg1) {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * Test whether this object model recognizes a given class as representing a
   * node in that object model. This method will generally be called at compile time.
   *
   * @param nodeClass A class that possibly represents nodes
   * @return true if the class is used to represent nodes in this object model
   */

  private boolean isRecognizedNodeClass(final Class nodeClass) {
//    TODO
    return true;
  }

}

/*
 * GPL
 * 
 * Copyright (c) 2005.
 * 
 * University of Konstanz.
 * 
 * Distributed Systems Group.
 * 
 * Database and Information Systems Group.
 * 
 * All rights reserved.
 * 
 * $Id$
 * 
 */

package org.treetank.xmllayer;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastLongStack;
import org.treetank.utils.FastObjectStack;
import org.treetank.utils.IConstants;
import org.treetank.utils.UTF;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.org.apache.xml.internal.serializer.Method;
import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;
import com.sun.org.apache.xml.internal.serializer.Serializer;
import com.sun.org.apache.xml.internal.serializer.SerializerFactory;


/**
 * Reconstructs an XML document from XPathAccelerator encoding.
 */
public final class SAXGenerator extends Thread {

  private final IReadTransaction trx;

  private ContentHandler handler;

  private Writer writer;

  private boolean isSerialize = false;

  private boolean asInputStream = false;

  private PipedOutputStream pipedOut;

  private final FastLongStack rightSiblingKeyStack;

  /** The nodeKey of the next node to visit. */
  private long nextKey;

  private final FastObjectStack nodeStack = new FastObjectStack();

  /**
   * Constructor for printing the reconstructed XML of global storage to stdout.
   */
  public SAXGenerator(final IReadTransaction initTrx) throws Exception {
    this(initTrx, new PrintWriter(System.out));
  }

  /**
   * Constructor to write reconstructed XML to a specified Writer.
   * 
   * @param aWriter
   * @see java.io.Writer
   */
  public SAXGenerator(final IReadTransaction initTrx, final Writer initWriter)
      throws Exception {

    trx = initTrx;
    writer = initWriter;
    isSerialize = true;

    // Prepare full descendant iteration.
    rightSiblingKeyStack = new FastLongStack();
    trx.moveToRoot();
    rightSiblingKeyStack.push(IConstants.NULL_KEY);
    nextKey = trx.getFirstChildKey();

  }

  /**
   * Constructor to get reconstructed XML as InputSource.
   * 
   * @param aWriter
   * @see java.io.Writer
   */
  public SAXGenerator(
      final IReadTransaction initTrx,
      final PipedInputStream pipedIn) throws Exception {

    trx = initTrx;
    pipedOut = new PipedOutputStream(pipedIn);
    asInputStream = true;
    isSerialize = true;

    // Prepare full descendant iteration.
    rightSiblingKeyStack = new FastLongStack();
    trx.moveToRoot();
    rightSiblingKeyStack.push(IConstants.NULL_KEY);
    nextKey = trx.getFirstChildKey();

  }

  /**
   * 'Callback' Constructor.
   * <p>
   * You'll get the SAX events emited during the roconstruction process. You can
   * use these as input for your application.
   * </p>
   */
  public SAXGenerator(
      final IReadTransaction initTrx,
      final ContentHandler contentHandler) throws Exception {

    trx = initTrx;
    handler = contentHandler;

    // Prepare full descendant iteration.
    rightSiblingKeyStack = new FastLongStack();
    rightSiblingKeyStack.push(IConstants.NULL_KEY);
    trx.moveToRoot();
    nextKey = trx.getFirstChildKey();

  }

  private final String qName(final String prefix, final String localPart) {
    return (prefix.length() > 0 ? prefix + ":" + localPart : localPart);
  }

  private final AttributesImpl visitAttributes() throws Exception {

    final AttributesImpl attributes = new AttributesImpl();

    for (int i = 0, l = trx.getAttributeCount(); i < l; i++) {
      final INode attribute = trx.getNode().getAttribute(i);
      attributes.addAttribute(trx.nameForKey(attribute.getURIKey()), trx
          .nameForKey(attribute.getLocalPartKey()), qName(trx
          .nameForKey(attribute.getPrefixKey()), trx.nameForKey(attribute
          .getLocalPartKey())), "", UTF.convert(attribute.getValue()));
    }

    return attributes;
  }

  private final void setNextKey() throws Exception {
    // Where to go?
    if (trx.getFirstChildKey() != IConstants.NULL_KEY) {
      nextKey = trx.getFirstChildKey();
      if (trx.getRightSiblingKey() == IConstants.NULL_KEY) {
        rightSiblingKeyStack.push(rightSiblingKeyStack.peek());
      } else {
        rightSiblingKeyStack.push(trx.getRightSiblingKey());
      }
    } else if (trx.getRightSiblingKey() != IConstants.NULL_KEY) {
      nextKey = trx.getRightSiblingKey();
      rightSiblingKeyStack.push(trx.getRightSiblingKey());
    } else {
      nextKey = rightSiblingKeyStack.peek();
      rightSiblingKeyStack.push(rightSiblingKeyStack.peek());
    }
    // Remember node infos.
    nodeStack.push(trx.getNode());
  }

  private final void visitDocument() throws Exception {

    // Iterate over all descendants.
    while (trx.moveTo(nextKey)) {

      //debug();

      // --- Clean up all pending closing tags. --------------------------------
      while (rightSiblingKeyStack.size() > 0
          && trx.getNodeKey() == rightSiblingKeyStack.peek()) {
        rightSiblingKeyStack.pop();
        final INode node = (INode) nodeStack.pop();
        final String localPart = trx.nameForKey(node.getLocalPartKey());
        final String prefix = trx.nameForKey(node.getPrefixKey());
        final String uri = trx.nameForKey(node.getURIKey());
        if (localPart.length() > 0) {
          handler.endElement(uri, localPart, qName(prefix, localPart));
        }
      }

      setNextKey();

      // --- Emit events based on current node. --------------------------------
      switch (trx.getKind()) {
      case IConstants.ELEMENT:
        final INode node = (INode) nodeStack.peek();
        final String localPart = trx.nameForKey(node.getLocalPartKey());
        final String prefix = trx.nameForKey(node.getPrefixKey());
        final String uri = trx.nameForKey(node.getURIKey());
        handler.startElement(
            uri,
            localPart,
            qName(prefix, localPart),
            visitAttributes());
        break;
      case IConstants.TEXT:
        final char[] text = UTF.convert(trx.getValue()).toCharArray();
        handler.characters(text, 0, text.length);
        break;
      case IConstants.PROCESSING_INSTRUCTION:
        handler.processingInstruction(trx.getLocalPart(), UTF.convert(trx
            .getValue()));
        break;
      default:
        throw new IllegalStateException("Unknown kind: " + trx.getKind());

      }

    }

    // Clean up all pending closing tags.
    while (nodeStack.size() > 0) {
      rightSiblingKeyStack.pop();
      final INode node = (INode) nodeStack.pop();
      final String localPart = trx.nameForKey(node.getLocalPartKey());
      final String prefix = trx.nameForKey(node.getPrefixKey());
      final String uri = trx.nameForKey(node.getURIKey());
      if (localPart.length() > 0) {
        handler.endElement(uri, localPart, qName(prefix, localPart));
      }
    }

  }

  public final void run() {
    try {

      // Start document.
      if (isSerialize) {
        // Set up serializer, why here? XML Declaration.
        java.util.Properties props =
            OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
        props.setProperty("indent", "no");
        props.setProperty("{http://xml.apache.org/xalan}indent-amount", "4");

        // Process XML declaration.
        props.setProperty("version", "1.0");
        props.setProperty("encoding", "UTF-8");
        props.setProperty("standalone", "yes");

        Serializer serializer = SerializerFactory.getSerializer(props);

        if (asInputStream) {
          serializer.setOutputStream(pipedOut);
          serializer.setWriter(new PrintWriter(System.err));
        } else {
          serializer.setWriter(writer);
        }
        handler = serializer.asContentHandler();
      }
      handler.startDocument();

      // Traverse all descendants in document order.
      visitDocument();

      // End document.
      handler.endDocument();

      if (asInputStream) {
        pipedOut.close();
      }

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private final void debug() throws Exception {
    System.out.println(">>> DEBUG >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    System.out.println("nodeKey = " + trx.getNodeKey());
    System.out.println("nextKey = " + nextKey);
    System.out.print("rightSiblingKeyStack = { ");
    for (int i = 0; i < rightSiblingKeyStack.size(); i++) {
      System.out.print(rightSiblingKeyStack.get(i) + "; ");
    }
    System.out.println("}");
    System.out.println("}");
    System.out.println("attributeCount = " + trx.getAttributeCount());
    System.out.println("<<< DEBUG <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
  }

}

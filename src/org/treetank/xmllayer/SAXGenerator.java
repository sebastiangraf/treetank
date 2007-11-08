/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
 */

package org.treetank.xmllayer;

import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.treetank.api.IAxis;
import org.treetank.api.IConstants;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastStack;
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
public final class SAXGenerator implements Runnable {

  protected ContentHandler mHandler;

  private Writer mWriter;

  private boolean mIsSerialize = false;

  private final boolean mAsInputStream = false;

  private PipedOutputStream mPipedOut;

  private final boolean mPrettyPrint;

  /** The nodeKey of the next node to visit. */
  private final IAxis mAxis;

  private final FastStack<Long> stack;

  /**
   * 'Callback' Constructor.
   * <p>
   * You'll get the SAX events emited during the roconstruction process. You can
   * use these as input for your application.
   * </p>
   */
  public SAXGenerator(
      final IAxis axis,
      final ContentHandler contentHandler,
      final boolean prettyPrint) throws Exception {
    mAxis = axis;
    mHandler = contentHandler;
    mPrettyPrint = prettyPrint;
    stack = new FastStack<Long>();

  }

  /**
   * Constructor to write reconstructed XML to a specified Writer.
   * 
   * @param aWriter
   * @see java.io.Writer
   */
  public SAXGenerator(
      final IAxis axis,
      final Writer writer,
      final boolean prettyPrint) throws Exception {
    mWriter = writer;
    mIsSerialize = true;
    mAxis = axis;
    mPrettyPrint = prettyPrint;
    stack = new FastStack<Long>();
  }

  /**
   * Constructor for printing the reconstructed XML of global storage to stdout.
   */
  public SAXGenerator(final IAxis axis, final boolean prettyPrint)
      throws Exception {
    this(axis, new PrintWriter(System.out), prettyPrint);
  }

  private final String qName(final String prefix, final String localPart) {
    return (prefix.length() > 0 ? prefix + ":" + localPart : localPart);
  }

  private final AttributesImpl visitAttributes(final IReadTransaction rtx) {

    final AttributesImpl attributes = new AttributesImpl();

    for (int index = 0, length = rtx.getAttributeCount(); index < length; index++) {
      attributes.addAttribute(rtx.getAttributeURI(index), rtx
          .getAttributeLocalPart(index), qName(
          rtx.getAttributePrefix(index),
          rtx.getAttributeLocalPart(index)), "", UTF.parseString(rtx
          .getAttributeValue(index)));
    }

    return attributes;
  }

  private final void emitNode(final IReadTransaction rtx) throws Exception {
    // Emit events of current node.
    switch (rtx.getKind()) {
    case IConstants.ELEMENT:
      // Emit start element.
      mHandler.startElement(rtx.getURI(), rtx.getLocalPart(), qName(rtx
          .getPrefix(), rtx.getLocalPart()), visitAttributes(rtx));
      break;
    case IConstants.TEXT:
      final char[] text = UTF.parseString(rtx.getValue()).toCharArray();
      mHandler.characters(text, 0, text.length);
      break;
    default:
      throw new IllegalStateException("Unknown kind: " + rtx.getKind());
    }
  }

  private final void emitEndElement(final IReadTransaction rtx)
      throws Exception {
    mHandler.endElement(rtx.getURI(), rtx.getLocalPart(), qName(
        rtx.getPrefix(),
        rtx.getLocalPart()));
  }

  private final void visitDocument() throws Exception {
    final IReadTransaction rtx = mAxis.getTransaction();
    boolean closeElements = false;

    for (final long key : mAxis) {

      // Emit all pending end elements.
      if (closeElements) {
        while (!stack.empty() && stack.peek() != rtx.getLeftSiblingKey()) {
          rtx.moveTo(stack.pop());
          emitEndElement(rtx);
        }
        if (!stack.empty()) {
          rtx.moveTo(stack.pop());
          emitEndElement(rtx);
        }
        rtx.moveTo(key);
        closeElements = false;
      }

      emitNode(rtx);

      // Emit corresponding end element or push it to stack.
      if (rtx.isElement()) {
        if (!rtx.hasFirstChild()) {
          emitEndElement(rtx);
        } else {
          stack.push(rtx.getNodeKey());
        }
      }

      // Remember to emit all pending end elements from stack if required.
      if (!rtx.hasFirstChild() && !rtx.hasRightSibling()) {
        closeElements = true;
      }
    }

    // Finally emit all pending end elements.
    while (!stack.empty()) {
      rtx.moveTo(stack.pop());
      emitEndElement(rtx);
    }

  }

  public void run() {
    try {

      // Start document.
      if (mIsSerialize) {
        // Set up serializer, why here? XML Declaration.
        java.util.Properties props =
            OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
        props.setProperty("indent", mPrettyPrint ? "yes" : "no");
        props.setProperty("{http://xml.apache.org/xalan}indent-amount", "2");

        // Process XML declaration.
        props.setProperty("version", "1.0");
        props.setProperty("encoding", "UTF-8");
        props.setProperty("standalone", "yes");

        Serializer serializer = SerializerFactory.getSerializer(props);

        if (mAsInputStream) {
          serializer.setOutputStream(mPipedOut);
          serializer.setWriter(new PrintWriter(System.err));
        } else {
          serializer.setWriter(mWriter);
        }
        mHandler = serializer.asContentHandler();
      }
      mHandler.startDocument();

      // Traverse all descendants in document order.
      visitDocument();

      // End document.
      mHandler.endDocument();

      if (mAsInputStream) {
        mPipedOut.close();
      }

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  //  private final void debug() throws Exception {
  //    System.out.println(">>> DEBUG >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
  //    System.out.println("nodeKey = " + rtx.getNodeKey());
  //    System.out.println("nextKey = " + mNextKey);
  //    System.out.print("rightSiblingKeyStack = { ");
  //    for (int i = 0; i < mRightSiblingKeyStack.size(); i++) {
  //      System.out.print(mRightSiblingKeyStack.get(i) + "; ");
  //    }
  //    System.out.println("}");
  //    System.out.println("}");
  //    System.out.println("attributeCount = " + rtx.getAttributeCount());
  //    System.out.println("<<< DEBUG <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
  //  }

}

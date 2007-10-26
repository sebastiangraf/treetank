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
import org.treetank.api.INode;
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
public class SAXGenerator extends Thread {

  protected ContentHandler mHandler;

  private Writer mWriter;

  private boolean mIsSerialize = false;

  private final boolean mAsInputStream = false;

  private PipedOutputStream mPipedOut;

  private final boolean mPrettyPrint;

  /** The nodeKey of the next node to visit. */
  protected final IAxis mAxis;

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
  }

  /**
   * Constructor for printing the reconstructed XML of global storage to stdout.
   */
  public SAXGenerator(final IAxis axis, final boolean prettyPrint)
      throws Exception {
    this(axis, new PrintWriter(System.out), prettyPrint);
  }

  protected final String qName(final String prefix, final String localPart) {
    return (prefix.length() > 0 ? prefix + ":" + localPart : localPart);
  }

  protected final AttributesImpl visitAttributes(final IReadTransaction rtx)
      throws Exception {

    final AttributesImpl attributes = new AttributesImpl();

    for (final INode attribute : new AttributeAxis(rtx)) {
      attributes.addAttribute(attribute.getURI(rtx), attribute
          .getLocalPart(rtx), qName(attribute.getPrefix(rtx), attribute
          .getLocalPart(rtx)), "", UTF.convert(attribute.getValue()));
    }

    return attributes;
  }

  protected final void emitEndElement(
      final INode node,
      final IReadTransaction rtx) throws Exception {
    mHandler.endElement(node.getURI(rtx), node.getLocalPart(rtx), qName(node
        .getPrefix(rtx), node.getLocalPart(rtx)));
  }

  private final void visitDocument() throws Exception {
    final IReadTransaction rtx = mAxis.getTransaction();
    final FastStack<INode> stack = new FastStack<INode>();

    for (final INode node : mAxis) {
      // Emit events of current node.
      switch (node.getKind()) {
      case IConstants.ELEMENT:

        // Emit start element.
        mHandler.startElement(node.getURI(rtx), node.getLocalPart(rtx), qName(
            node.getPrefix(rtx),
            node.getLocalPart(rtx)), visitAttributes(rtx));

        // Emit corresponding end element or push it to stack.
        if (!node.hasFirstChild()) {
          emitEndElement(node, rtx);
        } else {
          stack.push(node);
        }

        break;
      case IConstants.TEXT:
        final char[] text = UTF.convert(node.getValue()).toCharArray();
        mHandler.characters(text, 0, text.length);
        break;
      default:
        throw new IllegalStateException("Unknown kind: " + node.getKind());
      }

      // Emit pending end element from stack if required.
      if (!node.hasFirstChild() && !node.hasRightSibling()) {
        emitEndElement(stack.pop(), rtx);
      }
    }

  }

  @Override
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

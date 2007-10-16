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

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.treetank.api.IConstants;
import org.treetank.api.INode;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.FastLongStack;
import org.treetank.utils.FastObjectStack;
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

  protected final IReadTransaction mRTX;

  protected ContentHandler mHandler;

  private Writer mWriter;

  private boolean mIsSerialize = false;

  private boolean mAsInputStream = false;

  private PipedOutputStream mPipedOut;

  protected final FastLongStack mRightSiblingKeyStack;

  /** The nodeKey of the next node to visit. */
  protected long mNextKey;

  protected final FastObjectStack mNodeStack = new FastObjectStack();

  /**
   * Constructor for printing the reconstructed XML of global storage to stdout.
   */
  public SAXGenerator(final IReadTransaction rtx) throws Exception {
    this(rtx, new PrintWriter(System.out));
  }

  /**
   * Constructor to write reconstructed XML to a specified Writer.
   * 
   * @param aWriter
   * @see java.io.Writer
   */
  public SAXGenerator(final IReadTransaction rtx, final Writer writer)
      throws Exception {

    mRTX = rtx;
    mWriter = writer;
    mIsSerialize = true;

    // Prepare full descendant iteration.
    mRightSiblingKeyStack = new FastLongStack();
    mRTX.moveToRoot();
    mRightSiblingKeyStack.push(IConstants.NULL_KEY);
    mNextKey = mRTX.getFirstChildKey();

  }

  /**
   * Constructor to get reconstructed XML as InputSource.
   * 
   * @param aWriter
   * @see java.io.Writer
   */
  public SAXGenerator(final IReadTransaction rtx, final PipedInputStream pipedIn)
      throws Exception {

    mRTX = rtx;
    mPipedOut = new PipedOutputStream(pipedIn);
    mAsInputStream = true;
    mIsSerialize = true;

    // Prepare full descendant iteration.
    mRightSiblingKeyStack = new FastLongStack();
    mRTX.moveToRoot();
    mRightSiblingKeyStack.push(IConstants.NULL_KEY);
    mNextKey = mRTX.getFirstChildKey();

  }

  /**
   * 'Callback' Constructor.
   * <p>
   * You'll get the SAX events emited during the roconstruction process. You can
   * use these as input for your application.
   * </p>
   */
  public SAXGenerator(
      final IReadTransaction rtx,
      final ContentHandler contentHandler) throws Exception {

    mRTX = rtx;
    mHandler = contentHandler;

    // Prepare full descendant iteration.
    mRightSiblingKeyStack = new FastLongStack();
    mRightSiblingKeyStack.push(IConstants.NULL_KEY);
    mRTX.moveToRoot();
    mNextKey = mRTX.getFirstChildKey();

  }

  protected final String qName(final String prefix, final String localPart) {
    return (prefix.length() > 0 ? prefix + ":" + localPart : localPart);
  }

  protected final AttributesImpl visitAttributes() throws Exception {

    final AttributesImpl attributes = new AttributesImpl();

    for (int i = 0, l = mRTX.getAttributeCount(); i < l; i++) {
      final INode attribute = mRTX.getNode().getAttribute(i);
      attributes.addAttribute(mRTX.nameForKey(attribute.getURIKey()), mRTX
          .nameForKey(attribute.getLocalPartKey()), qName(mRTX
          .nameForKey(attribute.getPrefixKey()), mRTX.nameForKey(attribute
          .getLocalPartKey())), "", UTF.convert(attribute.getValue()));
    }

    return attributes;
  }

  protected final void setNextKey() throws Exception {
    // Where to go?
    if (mRTX.getFirstChildKey() != IConstants.NULL_KEY) {
      mNextKey = mRTX.getFirstChildKey();
      if (mRTX.getRightSiblingKey() == IConstants.NULL_KEY) {
        mRightSiblingKeyStack.push(mRightSiblingKeyStack.peek());
      } else {
        mRightSiblingKeyStack.push(mRTX.getRightSiblingKey());
      }
    } else if (mRTX.getRightSiblingKey() != IConstants.NULL_KEY) {
      mNextKey = mRTX.getRightSiblingKey();
      mRightSiblingKeyStack.push(mRTX.getRightSiblingKey());
    } else {
      mNextKey = mRightSiblingKeyStack.peek();
      mRightSiblingKeyStack.push(mRightSiblingKeyStack.peek());
    }
    // Remember node infos.
    mNodeStack.push(mRTX.getNode());
  }

  private final void visitDocument() throws Exception {

    // Iterate over all descendants.
    while (mRTX.moveTo(mNextKey)) {

      //debug();

      // --- Clean up all pending closing tags. --------------------------------
      while (mRightSiblingKeyStack.size() > 0
          && mRTX.getNodeKey() == mRightSiblingKeyStack.peek()) {
        mRightSiblingKeyStack.pop();
        final INode node = (INode) mNodeStack.pop();
        final String localPart = mRTX.nameForKey(node.getLocalPartKey());
        final String prefix = mRTX.nameForKey(node.getPrefixKey());
        final String uri = mRTX.nameForKey(node.getURIKey());
        if (localPart.length() > 0) {
          mHandler.endElement(uri, localPart, qName(prefix, localPart));
        }
      }

      setNextKey();

      // --- Emit events based on current node. --------------------------------
      switch (mRTX.getKind()) {
      case IConstants.ELEMENT:
        final INode node = (INode) mNodeStack.peek();
        final String localPart = mRTX.nameForKey(node.getLocalPartKey());
        final String prefix = mRTX.nameForKey(node.getPrefixKey());
        final String uri = mRTX.nameForKey(node.getURIKey());
        mHandler.startElement(
            uri,
            localPart,
            qName(prefix, localPart),
            visitAttributes());
        break;
      case IConstants.TEXT:
        final char[] text = UTF.convert(mRTX.getValue()).toCharArray();
        mHandler.characters(text, 0, text.length);
        break;
      case IConstants.PROCESSING_INSTRUCTION:
        mHandler.processingInstruction(mRTX.getLocalPart(), UTF.convert(mRTX
            .getValue()));
        break;
      default:
        throw new IllegalStateException("Unknown kind: " + mRTX.getKind());

      }

    }

    // Clean up all pending closing tags.
    while (mNodeStack.size() > 0) {
      mRightSiblingKeyStack.pop();
      final INode node = (INode) mNodeStack.pop();
      final String localPart = mRTX.nameForKey(node.getLocalPartKey());
      final String prefix = mRTX.nameForKey(node.getPrefixKey());
      final String uri = mRTX.nameForKey(node.getURIKey());
      if (localPart.length() > 0) {
        mHandler.endElement(uri, localPart, qName(prefix, localPart));
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
        props.setProperty("indent", "no");
        props.setProperty("{http://xml.apache.org/xalan}indent-amount", "4");

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
  //    System.out.println("nodeKey = " + mRTX.getNodeKey());
  //    System.out.println("nextKey = " + mNextKey);
  //    System.out.print("rightSiblingKeyStack = { ");
  //    for (int i = 0; i < mRightSiblingKeyStack.size(); i++) {
  //      System.out.print(mRightSiblingKeyStack.get(i) + "; ");
  //    }
  //    System.out.println("}");
  //    System.out.println("}");
  //    System.out.println("attributeCount = " + mRTX.getAttributeCount());
  //    System.out.println("<<< DEBUG <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
  //  }

}

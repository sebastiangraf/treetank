/*
 * Copyright (c) 2007, Marc Kramis
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.xmllayer;

import java.io.PrintWriter;
import java.io.Writer;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.utils.FastStack;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.org.apache.xml.internal.serializer.Method;
import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;
import com.sun.org.apache.xml.internal.serializer.Serializer;
import com.sun.org.apache.xml.internal.serializer.SerializerFactory;

/**
 * Reconstructs an XML document from XPathAccelerator encoding.
 */
public final class SAXGenerator implements Runnable {

  /** Content handler to fire SAX events to. */
  private ContentHandler mHandler;

  /** Writer if serialization is active. */
  private final Writer mWriter;

  /** Is output serialized? */
  private final boolean mIsSerialize;

  /** Is pretty print active? */
  private final boolean mPrettyPrint;

  /** The nodeKey of the next node to visit. */
  private final IAxis mAxis;

  /** Stack for end nodes. */
  private final FastStack<Long> stack;

  /**
   * Constructor to bind to SAX content handler.
   * 
   * @param rtx Transaction to perform descendant axis on.
   * @param contentHandler SAX content handler to fire SAX events to.
   * @param prettyPrint Is pretty print enabled?
   */
  public SAXGenerator(
      final IReadTransaction rtx,
      final ContentHandler contentHandler,
      final boolean prettyPrint) {
    mAxis = new DescendantAxis(rtx, true);
    mHandler = contentHandler;
    mPrettyPrint = prettyPrint;
    stack = new FastStack<Long>();
    mIsSerialize = false;
    mWriter = null;

  }

  /**
   * Constructor to write reconstructed XML to a specified Writer.
   * 
   * @param rtx Transaction to perform descendant axis on.
   * @param writer Output writer to write to.
   * @param prettyPrint Is pretty print enabled?
   */
  public SAXGenerator(
      final IReadTransaction rtx,
      final Writer writer,
      final boolean prettyPrint) {
    mHandler = null;
    mWriter = writer;
    mIsSerialize = true;
    mAxis = new DescendantAxis(rtx, true);
    mPrettyPrint = prettyPrint;
    stack = new FastStack<Long>();
  }

  /**
   * Constructor for printing the reconstructed XML to System.out.
   * 
   * @param rtx Transaction to perform descendant step on.
   * @param prettyPrint Is pretty print required?
   */
  public SAXGenerator(final IReadTransaction rtx, final boolean prettyPrint) {
    this(rtx, new PrintWriter(System.out), prettyPrint);
  }

  /**
   * Visit attributes of given node.
   * 
   * @param rtx Transaction to read attributes from.
   * @return SAX attributes implementation.
   */
  private final AttributesImpl visitAttributes(final IReadTransaction rtx) {

    final AttributesImpl attributes = new AttributesImpl();

    for (int index = 0, length = rtx.getAttributeCount(); index < length; index++) {
      attributes.addAttribute(rtx.getAttributeURI(index), "", rtx
          .getAttributeName(index), "", rtx.getAttributeValueAsAtom(index));
    }

    return attributes;
  }

  /**
   * Visit node.
   * 
   * @param rtx Transaction to read node from.
   * @throws SAXException in case something went wrong.
   */
  private final void visitNode(final IReadTransaction rtx) throws SAXException {
    switch (rtx.getKind()) {
    case IReadTransaction.DOCUMENT_ROOT_KIND:
      // Ignore since startDocument was already emitted.
      break;
    case IReadTransaction.ELEMENT_KIND:
      // Emit start element.
      mHandler.startElement(
          rtx.getURI(),
          "",
          rtx.getName(),
          visitAttributes(rtx));
      break;
    case IReadTransaction.TEXT_KIND:
      final char[] text = rtx.getValueAsString().toCharArray();
      mHandler.characters(text, 0, text.length);
      break;
    default:
      throw new IllegalStateException("Unknown kind: " + rtx.getKind());
    }
  }

  /**
   * Emit pending end tag.
   * 
   * @param rtx Transaction to emit tag from.
   * @throws SAXException if something went wrong.
   */
  private final void emitEndElement(final IReadTransaction rtx)
      throws SAXException {
    mHandler.endElement(rtx.getURI(), "", rtx.getName());
  }

  /**
   * Visit document by traversing over all given nodes found by the IAxis.
   * 
   * @throws SAXException if something went wrong.
   */
  private final void visitDocument() throws SAXException {
    final IReadTransaction rtx = mAxis.getTransaction();
    boolean closeElements = false;

    for (final long key : mAxis) {

      // Emit all pending end elements.
      if (closeElements) {
        while (!stack.empty() && stack.peek() != rtx.getLeftSiblingKey()) {
          rtx.moveTo(stack.pop());
          emitEndElement(rtx);
          rtx.moveTo(key);
        }
        if (!stack.empty()) {
          rtx.moveTo(stack.pop());
          emitEndElement(rtx);
        }
        rtx.moveTo(key);
        closeElements = false;
      }

      visitNode(rtx);

      // Emit corresponding end element or push it to stack.
      if (rtx.isElementKind()) {
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

  /**
   * {@inheritDoc}
   */
  public void run() {
    try {

      // Start document.
      if (mIsSerialize) {
        // Immediately serialize XML.
        java.util.Properties props =
            OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
        props.setProperty("indent", mPrettyPrint ? "yes" : "no");
        props.setProperty("{http://xml.apache.org/xalan}indent-amount", "2");

        // Process XML declaration.
        props.setProperty("version", "1.0");
        props.setProperty("encoding", "UTF-8");
        props.setProperty("standalone", "yes");

        Serializer serializer = SerializerFactory.getSerializer(props);
        serializer.setWriter(mWriter);
        mHandler = serializer.asContentHandler();
      }

      // Emit SAX document.
      mHandler.startDocument();
      visitDocument();
      mHandler.endDocument();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}

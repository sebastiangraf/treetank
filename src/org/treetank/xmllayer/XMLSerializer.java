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

import java.io.OutputStream;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.axislayer.DescendantAxis;
import org.treetank.utils.FastStack;
import org.treetank.utils.IConstants;

/**
 * <h1>XMLSerializer</h1>
 * 
 * <p>
 * Most efficient way to serialize a subtree into an OutputStream. The encoding
 * always is UTF-8. THe XML declaration is not printed. The namespace 'tnk' 
 * must be declared before.
 * </p>
 */
public final class XMLSerializer implements Runnable {

  /** Maximum size of internal builder buffer. */
  private static final int MAX_BUILDER_LENGTH = 1;

  /** Transaction to read from (is the same as the mAxis). */
  private final IReadTransaction mRTX;

  /** Descendant-or-self axis used to traverse subtree. */
  private final IAxis mAxis;

  /** Stack for reading end element. */
  private final FastStack<Long> mStack;

  /** OutputStream to write to. */
  private final OutputStream mOut;

  /** StringBuilder as intermediate cache. */
  private StringBuilder mBuilder;

  /**
   * Initialize XMLStreamReader implementation with transaction. The cursor
   * points to the node the XMLStreamReader starts to read.
   * 
   * @param rtx Transaction with cursor pointing to start node.
   * @param out OutputStream to serialize UTF-8 XML to.
   */
  public XMLSerializer(final IReadTransaction rtx, final OutputStream out) {
    mRTX = rtx;
    mAxis = new DescendantAxis(rtx, true);
    mStack = new FastStack<Long>();
    mOut = out;
    mBuilder = new StringBuilder();
  }

  /**
   * {@inheritDoc}
   */
  public final void run() {

    try {
      boolean closeElements = false;

      // Iterate over all nodes of the subtree including self.
      for (final long key : mAxis) {

        // Emit all pending end elements.
        if (closeElements) {
          while (!mStack.empty() && mStack.peek() != mRTX.getLeftSiblingKey()) {
            mRTX.moveTo(mStack.pop());
            emitEndElement();
            mRTX.moveTo(key);
          }
          if (!mStack.empty()) {
            mRTX.moveTo(mStack.pop());
            emitEndElement();
          }
          mRTX.moveTo(key);
          closeElements = false;
        }

        // Emit node.
        emitNode();

        // Push end element to stack if we are a start element with children.
        if (mRTX.isElementKind() && mRTX.hasFirstChild()) {
          mStack.push(mRTX.getNodeKey());
        }

        // Remember to emit all pending end elements from stack if required.
        if (!mRTX.hasFirstChild() && !mRTX.hasRightSibling()) {
          closeElements = true;
        }
      }

      // Finally emit all pending end elements.
      while (!mStack.empty()) {
        mRTX.moveTo(mStack.pop());
        emitEndElement();
      }

      // Flush remaining stuff in builder.
      mOut.write(mBuilder.toString().getBytes(IConstants.DEFAULT_ENCODING));

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Emit node (start element or characters).
   */
  private final void emitNode() throws Exception {
    switch (mRTX.getKind()) {
    case IReadTransaction.ELEMENT_KIND:
      // Emit start element.
      mBuilder.append("<");
      mBuilder.append(mRTX.getName());
      emitNamespaces();
      emitAttributes();
      if (mRTX.hasFirstChild()) {
        mBuilder.append(">");
      } else {
        mBuilder.append("/>");
      }
      break;
    case IReadTransaction.TEXT_KIND:
      if (mRTX.getValueType() == IReadTransaction.STRING_TYPE) {
        
      }
      mBuilder.append(mRTX.getValueAsAtom());
      break;
    }
    if (mBuilder.length() > MAX_BUILDER_LENGTH) {
      mOut.write(mBuilder.toString().getBytes(IConstants.DEFAULT_ENCODING));
      mBuilder = new StringBuilder();
    }
  }

  /**
   * Emit end element.
   */
  private final void emitEndElement() {
    mBuilder.append("</");
    mBuilder.append(mRTX.getName());
    mBuilder.append(">");
  }

  /**
   * Emit attributes of start element.
   */
  private final void emitNamespaces() {
    // Iterate over all persistent namespaces.
    for (int index = 0, length = mRTX.getNamespaceCount(); index < length; index++) {
      mBuilder.append(" xmlns:");
      mBuilder.append(mRTX.getNamespacePrefix(index));
      mBuilder.append("=\"");
      mBuilder.append(mRTX.getNamespaceURI(index));
      mBuilder.append("\"");
    }
  }

  /**
   * Emit attributes of start element.
   */
  private final void emitAttributes() {
    // Add virtual tnk:id attribute.
    //    mBuilder.append(" tnk:id=\"");
    //    mBuilder.append(mRTX.getNodeKey());
    //    mBuilder.append("\"");

    // Iterate over all persistent attributes.
    for (int index = 0, length = mRTX.getAttributeCount(); index < length; index++) {
      mBuilder.append(" ");
      mBuilder.append(mRTX.getAttributeName(index));
      mBuilder.append("=\"");
      mBuilder.append(mRTX.getAttributeValueAsAtom(index));
      mBuilder.append("\"");
    }
  }

}

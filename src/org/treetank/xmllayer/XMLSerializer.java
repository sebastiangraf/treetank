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
 * always is UTF-8. The namespace 'tnk' 
 * must be declared before.
 * </p>
 */
public final class XMLSerializer implements Runnable {

  /** Offset that must be added to digit to make it ASCII. */
  private static final int ASCII_OFFSET = 48;

  /** Precalculated powers of each available long digit. */
  private static final long[] LONG_POWERS =
      {
          1L,
          10L,
          100L,
          1000L,
          10000L,
          100000L,
          1000000L,
          10000000L,
          100000000L,
          1000000000L,
          10000000000L,
          100000000000L,
          1000000000000L,
          10000000000000L,
          100000000000000L,
          1000000000000000L,
          10000000000000000L,
          100000000000000000L,
          1000000000000000000L };

  /** " ". */
  private static final int SPACE = 32;

  /** "&lt;". */
  private static final int OPEN = 60;

  /** "&gt;". */
  private static final int CLOSE = 62;

  /** "/". */
  private static final int SLASH = 47;

  /** "=". */
  private static final int EQUAL = 61;

  /** "\"". */
  private static final int QUOTE = 34;

  /** "=\"". */
  private static final byte[] EQUAL_QUOTE = new byte[] { EQUAL, QUOTE };

  /** "&lt;/". */
  private static final byte[] OPEN_SLASH = new byte[] { OPEN, SLASH };

  /** "/&gt;". */
  private static final byte[] SLASH_CLOSE = new byte[] { SLASH, CLOSE };

  /** " tnk:id=\"". */
  private static final byte[] TNK_ID =
      new byte[] { SPACE, 116, 110, 107, 58, 105, 100, EQUAL, QUOTE };

  /** " xmlns:". */
  private static final byte[] XMLNS =
      new byte[] { SPACE, 120, 109, 108, 110, 115, 58 };

  /** Transaction to read from (is the same as the mAxis). */
  private final IReadTransaction mRTX;

  /** Descendant-or-self axis used to traverse subtree. */
  private final IAxis mAxis;

  /** Stack for reading end element. */
  private final FastStack<Long> mStack;

  /** OutputStream to write to. */
  private final OutputStream mOut;

  /** Serialize XML declaration. */
  private final boolean mSerializeXMLDeclaration;

  /** Serialize tnk:id. */
  private final boolean mSerializeId;

  /**
   * Initialize XMLStreamReader implementation with transaction. The cursor
   * points to the node the XMLStreamReader starts to read. Do not serialize
   * the tank ids.
   * 
   * @param rtx Transaction with cursor pointing to start node.
   * @param out OutputStream to serialize UTF-8 XML to.
   */
  public XMLSerializer(final IReadTransaction rtx, final OutputStream out) {
    this(rtx, out, true, false);
  }

  /**
   * Initialize XMLStreamReader implementation with transaction. The cursor
   * points to the node the XMLStreamReader starts to read.
   * 
   * @param rtx Transaction with cursor pointing to start node.
   * @param out OutputStream to serialize UTF-8 XML to.
   * @param serializeXMLDeclaration Serialize XML declaration if true.
   * @param serializeId Serialize tank id if true.
   */
  public XMLSerializer(
      final IReadTransaction rtx,
      final OutputStream out,
      final boolean serializeXMLDeclaration,
      final boolean serializeId) {
    mRTX = rtx;
    mAxis = new DescendantAxis(rtx, true);
    mStack = new FastStack<Long>();
    mOut = out;
    mSerializeXMLDeclaration = serializeXMLDeclaration;
    mSerializeId = serializeId;
  }

  /**
   * {@inheritDoc}
   */
  public final void run() {

    try {

      if (mSerializeXMLDeclaration) {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<tnk:sequence xmlns:tnk=\"http://treetank.org\"><tnk:item>");
      }

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

      if (mSerializeXMLDeclaration) {
        write("</tnk:item></tnk:sequence>");
      }

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
      mOut.write(OPEN);
      mOut.write(mRTX.getRawName());
      // Emit namespace declarations.
      for (int index = 0, length = mRTX.getNamespaceCount(); index < length; index++) {
        mOut.write(XMLNS);
        write(mRTX.getNamespacePrefix(index));
        mOut.write(EQUAL_QUOTE);
        write(mRTX.getNamespaceURI(index));
        mOut.write(QUOTE);
      }
      // Emit attributes.
      // Add virtual tnk:id attribute.
      if (mSerializeId) {
        mOut.write(TNK_ID);
        write(mRTX.getNodeKey());
        mOut.write(QUOTE);
      }

      // Iterate over all persistent attributes.
      for (int index = 0, length = mRTX.getAttributeCount(); index < length; index++) {
        mOut.write(SPACE);
        mOut.write(mRTX.getAttributeRawName(index));
        mOut.write(EQUAL_QUOTE);
        if (mRTX.getAttributeValueType(index) == IReadTransaction.STRING_TYPE) {
          mOut.write(mRTX.getAttributeValueAsByteArray(index));
        } else {
          write(mRTX.getAttributeValueAsAtom(index));
        }
        mOut.write(QUOTE);
      }
      if (mRTX.hasFirstChild()) {
        mOut.write(CLOSE);
      } else {
        mOut.write(SLASH_CLOSE);
      }
      break;
    case IReadTransaction.TEXT_KIND:
      if (mRTX.getValueType() == IReadTransaction.STRING_TYPE) {
        mOut.write(mRTX.getValueAsByteArray());
      } else {
        write(mRTX.getValueAsAtom());
      }
      break;
    }
  }

  /**
   * Emit end element.
   */
  private final void emitEndElement() throws Exception {
    mOut.write(OPEN_SLASH);
    mOut.write(mRTX.getRawName());
    mOut.write(CLOSE);
  }

  /**
   * Write characters of string.
   */
  private final void write(final String string) throws Exception {
    mOut.write(string.getBytes(IConstants.DEFAULT_ENCODING));
  }

  /**
   * Write non-negative non-zero long as UTF-8 bytes.
   */
  private final void write(final long value) throws Exception {
    final int length = (int) Math.log10((double) value);
    int digit = 0;
    long remainder = value;
    for (int i = length; i >= 0; i--) {
      digit = (byte) (remainder / LONG_POWERS[i]);
      mOut.write((byte) (digit + ASCII_OFFSET));
      remainder -= digit * LONG_POWERS[i];
    }
  }

}

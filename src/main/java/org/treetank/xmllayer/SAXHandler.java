/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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

import java.util.ArrayList;

import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.FastStack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <h1>SAXHandler</h1>
 * 
 * <p>
 * SAX handler shredding a complete XML document into an TreeTank storage.
 * </p>
 */
public class SAXHandler extends DefaultHandler {

  /** TreeTank write transaction. */
  private final IWriteTransaction mWTX;

  /** Stack containing left sibling nodeKey of each level. */
  private final FastStack<Long> mLeftSiblingKeyStack;

  /** Aggregated pending text node. */
  private final StringBuilder mCharacters;

  /** List of prefixes bound to last element node. */
  private final ArrayList<String> mPrefixList;

  /** List of URIs bound to last element node. */
  private final ArrayList<String> mURIList;

  /**
   * Constructor.
   * 
   * @param target File to write to.
   */
  public SAXHandler(final IWriteTransaction target) {
    mWTX = target;
    mLeftSiblingKeyStack = new FastStack<Long>();
    mCharacters = new StringBuilder();
    mPrefixList = new ArrayList<String>();
    mURIList = new ArrayList<String>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startDocument() throws SAXException {
    mLeftSiblingKeyStack.push(mWTX.getLeftSiblingKey());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startElement(
      final String uri,
      final String localName,
      final String qName,
      final Attributes attr) throws SAXException {

    // Insert text node.
    insertPendingText();

    // Insert element node and maintain stack.      
    long key;
    if (mLeftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
      key = mWTX.insertElementAsFirstChild(qName, uri);

    } else {
      key = mWTX.insertElementAsRightSibling(qName, uri);

    }
    mLeftSiblingKeyStack.pop();
    mLeftSiblingKeyStack.push(key);
    mLeftSiblingKeyStack.push(IReadTransaction.NULL_NODE_KEY);

    // Insert uriKey nodes.
    for (int i = 0, n = mPrefixList.size(); i < n; i++) {
      //insert(IConstants.NAMESPACE, "", uriList.get(i), prefixList.get(i), "");
    }
    mPrefixList.clear();
    mURIList.clear();

    // Insert attribute nodes.
    for (int i = 0, l = attr.getLength(); i < l; i++) {
      mWTX.insertAttribute(attr.getQName(i), attr.getURI(i), attr.getValue(i));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endElement(
      final String uri,
      final String localName,
      final String qName) throws SAXException {
    insertPendingText();
    mLeftSiblingKeyStack.pop();
    mWTX.moveTo(mLeftSiblingKeyStack.peek());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void characters(final char[] ch, final int start, final int length)
      throws SAXException {
    mCharacters.append(ch, start, length);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processingInstruction(final String target, final String data)
      throws SAXException {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    mPrefixList.add(prefix);
    mURIList.add(uri);
  }

  /**
   * Insert aggregated pending text node.
   * 
   * @throws Exception of any kind.
   */
  private final void insertPendingText() {

    final String text = mCharacters.toString().trim();
    mCharacters.setLength(0);

    if (text.length() > 0) {

      // Insert text node and maintain stacks.
      long key;
      if (mLeftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
        key = mWTX.insertTextAsFirstChild(text);
      } else {
        key = mWTX.insertTextAsRightSibling(text);
      }
      mLeftSiblingKeyStack.pop();
      mLeftSiblingKeyStack.push(key);

    }

  }

}

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

import java.util.ArrayList;

import org.treetank.api.IWriteTransaction;
import org.treetank.utils.FastStack;
import org.treetank.utils.UTF;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <h1>SAXHandler</h1>
 * 
 * <p>
 * SAX handler shredding a complete XML document into an TreeTank storage.
 * </p>
 */
public class SAXHandler extends DefaultHandler implements LexicalHandler {

  /** TreeTank write transaction. */
  protected IWriteTransaction mWTX;

  /** Stack containing left sibling nodeKey of each level. */
  protected FastStack<Long> mLeftSiblingKeyStack;

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

    try {

      mLeftSiblingKeyStack.push(mWTX.getNullNodeKey());

    } catch (Exception e) {
      throw new SAXException(e);
    }

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

    try {

      // Insert text node.
      insertPendingText();

      // Insert element node and maintain stack.      
      long key;
      if (mWTX.isNullNodeKey(mLeftSiblingKeyStack.peek())) {
        key =
            mWTX
                .insertElementAsFirstChild(localName, uri, qNameToPrefix(qName));

      } else {
        key =
            mWTX.insertElementAsRightSibling(
                localName,
                uri,
                qNameToPrefix(qName));

      }
      mLeftSiblingKeyStack.pop();
      mLeftSiblingKeyStack.push(key);

      mLeftSiblingKeyStack.push(mWTX.getNullNodeKey());

      // Insert uriKey nodes.
      for (int i = 0, n = mPrefixList.size(); i < n; i++) {
        //insert(IConstants.NAMESPACE, "", uriList.get(i), prefixList.get(i), "");
      }
      mPrefixList.clear();
      mURIList.clear();

      // Insert attribute nodes.
      for (int i = 0, l = attr.getLength(); i < l; i++) {
        mWTX.insertAttribute(
            attr.getLocalName(i),
            attr.getURI(i),
            qNameToPrefix(attr.getQName(i)),
            UTF.getBytes(attr.getValue(i)));
      }

    } catch (Exception e) {
      throw new SAXException(e);
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

    try {

      insertPendingText();

      mLeftSiblingKeyStack.pop();

      mWTX.moveTo(mLeftSiblingKeyStack.peek());

    } catch (Exception e) {
      throw new SAXException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void characters(final char[] ch, final int start, final int length)
      throws SAXException {

    mCharacters.append(ch, start, length);

  }

  @Override
  public void processingInstruction(final String target, final String data)
      throws SAXException {
    // Ignore it for now. If activated, some axis iterators must be adapted!
    //    try {
    //      insert(IConstants.PROCESSING_INSTRUCTION, target, "", "", data);
    //      
    //    } catch (Exception e) {
    //      throw new SAXException(e);
    //    }
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
  private final void insertPendingText() throws Exception {

    String text = mCharacters.toString().trim();
    mCharacters.setLength(0);

    if (text.length() > 0) {

      // Insert text node and maintain stacks.
      long key;
      if (mWTX.isNullNodeKey(mLeftSiblingKeyStack.peek())) {
        key = mWTX.insertTextAsFirstChild(UTF.getBytes(text));
      } else {
        key = mWTX.insertTextAsRightSibling(UTF.getBytes(text));
      }
      mLeftSiblingKeyStack.pop();
      mLeftSiblingKeyStack.push(key);

    }

  }

  /**
   * Extract prefixKey out of qName if there is any.
   * 
   * @param qName Fully qualified name.
   * @return Prefix or empty string.
   */
  private final String qNameToPrefix(final String qName) {

    int delimiter = qName.indexOf(":");
    if (delimiter > -1) {
      return qName.substring(0, delimiter);
    } else {
      return "";
    }
  }

  public final void comment(final char[] ch, final int start, final int length)
      throws SAXException {
    // TODO Auto-generated method stub

  }

  public final void endCDATA() throws SAXException {
    // TODO Auto-generated method stub

  }

  public final void endDTD() throws SAXException {
    // TODO Auto-generated method stub

  }

  public final void endEntity(final String name) throws SAXException {
    // TODO Auto-generated method stub

  }

  public final void startCDATA() throws SAXException {
    // TODO Auto-generated method stub

  }

  public final void startDTD(
      final String name,
      final String publicId,
      final String systemId) throws SAXException {
    //    System.out.print(name + "; ");
    //    System.out.print(publicId + "; ");
    //    System.out.println(systemId);
  }

  public final void startEntity(String name) throws SAXException {
    //    System.out.println(name);
  }

}

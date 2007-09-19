/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.xmllayer;

import java.util.ArrayList;

import org.treetank.nodelayer.IWriteTransaction;
import org.treetank.utils.FastLongStack;
import org.treetank.utils.IConstants;
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

  /** System ID of document. */
  private final String document;

  /** Idefix write transaction. */
  private final IWriteTransaction trx;

  /** Stack containing left sibling nodeKey of each level. */
  private final FastLongStack leftSiblingKeyStack;

  /** Aggregated pending text node. */
  private final StringBuilder characters;

  /** List of prefixes bound to last element node. */
  private final ArrayList<String> prefixList;

  /** List of URIs bound to last element node. */
  private final ArrayList<String> uriList;

  /**
   * Constructor.
   * 
   * @param initDocument Name of document.
   * @param initTrx Writing transaction to write to.
   */
  public SAXHandler(final String initDocument, final IWriteTransaction initTrx) {
    document = initDocument;
    trx = initTrx;
    leftSiblingKeyStack = new FastLongStack();
    characters = new StringBuilder();
    prefixList = new ArrayList<String>();
    uriList = new ArrayList<String>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void startDocument() throws SAXException {

    try {

      trx.insertRoot(document);

      leftSiblingKeyStack.push(IConstants.NULL_KEY);

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
      if (leftSiblingKeyStack.peek() == IConstants.NULL_KEY) {
        key =
            trx.insertFirstChild(
                IConstants.ELEMENT,
                localName,
                uri,
                qNameToPrefix(qName),
                UTF.EMPTY);
      } else {
        key =
            trx.insertRightSibling(
                IConstants.ELEMENT,
                localName,
                uri,
                qNameToPrefix(qName),
                UTF.EMPTY);
      }
      leftSiblingKeyStack.pop();
      leftSiblingKeyStack.push(key);

      leftSiblingKeyStack.push(IConstants.NULL_KEY);

      // Insert uriKey nodes.
      for (int i = 0, n = prefixList.size(); i < n; i++) {
        //insert(IConstants.NAMESPACE, "", uriList.get(i), prefixList.get(i), "");
      }
      prefixList.clear();
      uriList.clear();

      // Insert attribute nodes.
      for (int i = 0, l = attr.getLength(); i < l; i++) {
        trx.insertAttribute(
            attr.getLocalName(i),
            attr.getURI(i),
            qNameToPrefix(attr.getQName(i)),
            UTF.convert(attr.getValue(i)));
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

      leftSiblingKeyStack.pop();

      trx.moveTo(leftSiblingKeyStack.peek());

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

    characters.append(ch, start, length);

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

    prefixList.add(prefix);
    uriList.add(uri);
  }

  /**
   * Insert aggregated pending text node.
   * 
   * @throws Exception of any kind.
   */
  private final void insertPendingText() throws Exception {

    String text = characters.toString().trim();
    characters.setLength(0);

    if (text.length() > 0) {

      // Insert text node and maintain stacks.
      long key;
      if (leftSiblingKeyStack.peek() == IConstants.NULL_KEY) {
        key =
            trx
                .insertFirstChild(IConstants.TEXT, "", "", "", UTF
                    .convert(text));
      } else {
        key =
            trx.insertRightSibling(IConstants.TEXT, "", "", "", UTF
                .convert(text));
      }
      leftSiblingKeyStack.pop();
      leftSiblingKeyStack.push(key);

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

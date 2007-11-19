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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.utils.FastStack;
import org.treetank.utils.IConstants;
import org.treetank.utils.UTF;

public final class XMLShredder {

  public final static void shred(
      final String xmlPath,
      final SessionConfiguration sessionConfiguration) {
    shred(xmlPath, sessionConfiguration, true);
  }

  public final static void shred(
      final String xmlPath,
      final SessionConfiguration sessionConfiguration,
      final boolean isValidating) {
    try {
      final InputStream in = new FileInputStream(xmlPath);
      final XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.IS_VALIDATING, isValidating);
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      final XMLStreamReader parser = factory.createXMLStreamReader(in);
      shred(parser, sessionConfiguration);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static final void shred(
      final XMLStreamReader parser,
      final SessionConfiguration sessionConfiguration) {

    try {
      final ISession session = Session.beginSession(sessionConfiguration);
      final IWriteTransaction wtx = session.beginWriteTransaction();
      final FastStack<Long> leftSiblingKeyStack = new FastStack<Long>();

      // Make sure that we do not shred into an existing TreeTank.
      if (wtx.hasFirstChild()) {
        throw new IllegalStateException(
            "XMLShredder can not shred into an existing TreeTank.");
      }

      long key;
      String text;
      leftSiblingKeyStack.push(IConstants.NULL_KEY);

      // Iterate over all nodes.
      while (parser.hasNext()) {

        switch (parser.next()) {

        case XMLStreamConstants.START_ELEMENT:

          if (leftSiblingKeyStack.peek() == IConstants.NULL_KEY) {
            key =
                wtx.insertElementAsFirstChild(parser.getLocalName(), parser
                    .getNamespaceURI(), parser.getPrefix());
          } else {
            key =
                wtx.insertElementAsRightSibling(parser.getLocalName(), parser
                    .getNamespaceURI(), parser.getPrefix());
          }
          leftSiblingKeyStack.pop();
          leftSiblingKeyStack.push(key);
          leftSiblingKeyStack.push(IConstants.NULL_KEY);

          // Parse namespaces.
          for (int i = 0, l = parser.getNamespaceCount(); i < l; i++) {
            wtx.insertNamespace(parser.getNamespaceURI(i), parser
                .getNamespacePrefix(i));
          }

          // Parse attributes.
          for (int i = 0, l = parser.getAttributeCount(); i < l; i++) {
            wtx.insertAttribute(parser.getAttributeLocalName(i), parser
                .getAttributeNamespace(i), parser.getAttributePrefix(i), UTF
                .getBytes(parser.getAttributeValue(i)));
          }
          break;

        case XMLStreamConstants.END_ELEMENT:
          leftSiblingKeyStack.pop();
          wtx.moveTo(leftSiblingKeyStack.peek());
          break;

        case XMLStreamConstants.CHARACTERS:
          text = parser.getText().trim();
          if (text.length() > 0) {
            if (leftSiblingKeyStack.peek() == IConstants.NULL_KEY) {
              key = wtx.insertTextAsFirstChild(UTF.getBytes(text));
            } else {
              key = wtx.insertTextAsRightSibling(UTF.getBytes(text));
            }
            leftSiblingKeyStack.pop();
            leftSiblingKeyStack.push(key);
          }
          break;

        }
      }
      wtx.close();
      session.close();
      parser.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static final void main(String[] args) {
    if (args.length < 2 || args.length > 3) {
      System.out.println("Usage: XMLShredder input.xml output.tnk [key]");
      System.exit(1);
    }

    try {
      System.out.print("Shredding '" + args[0] + "' to '" + args[1] + "' ... ");
      long time = System.currentTimeMillis();
      new File(args[1]).delete();
      XMLShredder.shred(
          args[0],
          args.length == 2
              ? new SessionConfiguration(args[1])
              : new SessionConfiguration(args[1], args[2].getBytes()),
          false);
      System.out.println(" done ["
          + (System.currentTimeMillis() - time)
          + "ms].");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

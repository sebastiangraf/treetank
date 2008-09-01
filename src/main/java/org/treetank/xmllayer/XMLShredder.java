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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.utils.FastStack;
import org.treetank.utils.IConstants;

public final class XMLShredder {

  public final static void shred(
      final long id,
      final String content,
      final ISession session) {
    try {
      final XMLInputFactory factory = XMLInputFactory.newInstance();
      factory.setProperty(XMLInputFactory.IS_VALIDATING, true);
      factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      final XMLStreamReader parser =
          factory.createXMLStreamReader(new StringReader(content));
      shred(id, parser, session);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

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
      shred(0, parser, sessionConfiguration);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static final void shred(
      final long id,
      final XMLStreamReader parser,
      final SessionConfiguration sessionConfiguration) {
    final ISession session = Session.beginSession(sessionConfiguration);
    shred(id, parser, session);
    session.close();
  }

  public static final void shred(
      final long id,
      final XMLStreamReader parser,
      final ISession session) {

    try {
      final IWriteTransaction wtx =
          session.beginWriteTransaction(IConstants.COMMIT_THRESHOLD, 0);
      final FastStack<Long> leftSiblingKeyStack = new FastStack<Long>();

      // Make sure that we do not shred into an existing TreeTank.
      //      if (wtx.hasFirstChild()) {
      //        throw new IllegalStateException(
      //            "XMLShredder can not shred into an existing TreeTank.");
      //      }
      wtx.moveTo(id);

      long key;
      String text;
      leftSiblingKeyStack.push(IReadTransaction.NULL_NODE_KEY);
      // leftSiblingKeyStack.push(wtx.getLeftSiblingKey());

      // Iterate over all nodes.
      while (parser.hasNext()) {

        switch (parser.next()) {

        case XMLStreamConstants.START_ELEMENT:

          final String name =
              (parser.getPrefix() == null ? parser.getLocalName() : parser
                  .getPrefix()
                  + ":"
                  + parser.getLocalName());

          if (leftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
            key = wtx.insertElementAsFirstChild(name, parser.getNamespaceURI());
          } else {
            key =
                wtx.insertElementAsRightSibling(name, parser.getNamespaceURI());
          }
          leftSiblingKeyStack.pop();
          leftSiblingKeyStack.push(key);
          leftSiblingKeyStack.push(IReadTransaction.NULL_NODE_KEY);

          // Parse namespaces.
          for (int i = 0, l = parser.getNamespaceCount(); i < l; i++) {
            wtx.insertNamespace(parser.getNamespaceURI(i), parser
                .getNamespacePrefix(i));
            wtx.moveTo(key);
          }

          // Parse attributes.
          for (int i = 0, l = parser.getAttributeCount(); i < l; i++) {
            wtx.insertAttribute(parser.getAttributePrefix(i) == null ? parser
                .getAttributeLocalName(i) : parser.getAttributePrefix(i)
                + ":"
                + parser.getAttributeLocalName(i), parser
                .getAttributeNamespace(i), parser.getAttributeValue(i));
            wtx.moveTo(key);
          }
          break;

        case XMLStreamConstants.END_ELEMENT:
          leftSiblingKeyStack.pop();
          wtx.moveTo(leftSiblingKeyStack.peek());
          break;

        case XMLStreamConstants.CHARACTERS:
          text = parser.getText().trim();
          if (text.length() > 0) {
            if (leftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
              key = wtx.insertTextAsFirstChild(text);
            } else {
              key = wtx.insertTextAsRightSibling(text);
            }
            leftSiblingKeyStack.pop();
            leftSiblingKeyStack.push(key);
          }
          break;

        }
      }
      wtx.close();
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

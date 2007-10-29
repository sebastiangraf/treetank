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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.treetank.api.IConstants;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.utils.FastStack;
import org.treetank.utils.UTF;

public final class XMLShredder {

  public final static void shred(
      final String xmlPath,
      final SessionConfiguration sessionConfiguration)
      throws IOException,
      XMLStreamException {
    shred(xmlPath, sessionConfiguration, true);
  }

  public final static void shred(
      final String xmlPath,
      final SessionConfiguration sessionConfiguration,
      final boolean isValidating) throws IOException, XMLStreamException {
    final InputStream in = new FileInputStream(xmlPath);
    final XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.IS_VALIDATING, isValidating);
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    final XMLStreamReader parser = factory.createXMLStreamReader(in);

    shred(parser, sessionConfiguration);
  }

  public static final void shred(
      final XMLStreamReader parser,
      final SessionConfiguration sessionConfiguration)
      throws IOException,
      XMLStreamException {
    ISession session = Session.beginSession(sessionConfiguration);
    IWriteTransaction wtx = session.beginWriteTransaction();
    // Prepare variables.
    final FastStack<Long> leftSiblingKeyStack = new FastStack<Long>();
    long key;
    String text;
    leftSiblingKeyStack.push(IConstants.NULL_KEY);
    int nodeCounter = 0;

    // Iterate over all nodes.
    while (parser.hasNext()) {

      if (nodeCounter > IConstants.COMMIT_TRESHOLD) {
        final long tempkey = wtx.getNodeKey();
        wtx.commit();
        wtx = session.beginWriteTransaction();
        // System.gc();
        wtx.moveTo(tempkey);
        nodeCounter = 0;

      }
      nodeCounter++;
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
              .convert(parser.getAttributeValue(i)));
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
            key = wtx.insertTextAsFirstChild(UTF.convert(text));
          } else {
            key = wtx.insertTextAsRightSibling(UTF.convert(text));
          }
          leftSiblingKeyStack.pop();
          leftSiblingKeyStack.push(key);
        }
        break;

      }
    }
    wtx.commit();
    session.close();
    parser.close();
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

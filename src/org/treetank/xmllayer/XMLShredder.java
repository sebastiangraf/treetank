/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright (C) 2007 Marc Kramis
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
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.treetank.api.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.utils.FastLongStack;
import org.treetank.utils.IConstants;
import org.treetank.utils.UTF;


public final class XMLShredder {

  public final static void shred(final String xmlPath, final String ic3Path)
      throws Exception {
    shred(xmlPath, ic3Path, true);
  }

  public final static void shred(
      final String xmlPath,
      final String ic3,
      final boolean isValidating) throws Exception {
    final InputStream in = new FileInputStream(xmlPath);
    final XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.IS_VALIDATING, isValidating);
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    final XMLStreamReader parser = factory.createXMLStreamReader(in);

    shred(parser, ic3);
  }

  public static final void shred(final XMLStreamReader parser, final String path)
      throws Exception {
    Session session = new Session(path);
    IWriteTransaction trx = session.beginWriteTransaction();
    // Prepare variables.
    final FastLongStack leftSiblingKeyStack = new FastLongStack();
    long key;
    String text;
    leftSiblingKeyStack.push(IConstants.NULL_KEY);
    trx.insertRoot("");
    int nodeCounter = 0;

    // Iterate over all nodes.
    while (parser.hasNext()) {

      if (nodeCounter > IConstants.COMMIT_TRESHOLD) {
        final long tempkey = trx.getNodeKey();
        session.commit();
        trx = session.beginWriteTransaction();
        // System.gc();
        trx.moveTo(tempkey);
        nodeCounter = 0;

      }
      nodeCounter++;
      switch (parser.next()) {

      case XMLStreamConstants.START_ELEMENT:

        if (leftSiblingKeyStack.peek() == IConstants.NULL_KEY) {
          key =
              trx.insertFirstChild(
                  IConstants.ELEMENT,
                  parser.getLocalName(),
                  parser.getNamespaceURI(),
                  parser.getPrefix(),
                  UTF.EMPTY);
        } else {
          key =
              trx.insertRightSibling(
                  IConstants.ELEMENT,
                  parser.getLocalName(),
                  parser.getNamespaceURI(),
                  parser.getPrefix(),
                  UTF.EMPTY);
        }
        leftSiblingKeyStack.pop();
        leftSiblingKeyStack.push(key);
        leftSiblingKeyStack.push(IConstants.NULL_KEY);

        // Parse namespaces.
        for (int i = 0, l = parser.getNamespaceCount(); i < l; i++) {
          trx.insertNamespace(parser.getNamespaceURI(i), parser
              .getNamespacePrefix(i));
        }

        // Parse attributes.
        for (int i = 0, l = parser.getAttributeCount(); i < l; i++) {
          trx.insertAttribute(parser.getAttributeLocalName(i), parser
              .getAttributeNamespace(i), parser.getAttributePrefix(i), UTF
              .convert(parser.getAttributeValue(i)));
        }
        break;

      case XMLStreamConstants.END_ELEMENT:
        leftSiblingKeyStack.pop();
        trx.moveTo(leftSiblingKeyStack.peek());
        break;

      case XMLStreamConstants.CHARACTERS:
        text = parser.getText().trim();
        if (text.length() > 0) {
          if (leftSiblingKeyStack.peek() == IConstants.NULL_KEY) {
            key =
                trx.insertFirstChild(IConstants.TEXT, "", "", "", UTF
                    .convert(text));
          } else {
            key =
                trx.insertRightSibling(IConstants.TEXT, "", "", "", UTF
                    .convert(text));
          }
          leftSiblingKeyStack.pop();
          leftSiblingKeyStack.push(key);
        }
        break;

      }
    }
    session.commit();
    session.close();
    parser.close();
  }

  public static final void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage: XMLShredder input.xml output.ic3");
      System.exit(1);
    }

    try {
      System.out.print("Shredding '" + args[0] + "' to '" + args[1] + "' ... ");
      long time = System.currentTimeMillis();
      new File(args[1]).delete();
      XMLShredder.shred(args[0], args[1], false);
      System.out.println(" done ["
          + (System.currentTimeMillis() - time)
          + "ms].");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

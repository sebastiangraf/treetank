/*
 * Copyright (c) 2008, Johannes Lichtenberger (HiWi), University of Konstanz
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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.FastStack;
import org.treetank.utils.IConstants;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class StAXInput extends DefaultHandler {

  /** TreeTankStreamWriter. */
  private TreeTankStreamWriter writer;
  
  /** Namespace context. */
  private NamespaceContextImpl nspContext;

  /** Constructor. */
  public StAXInput(TreeTankStreamWriter w) {
    nspContext = new NamespaceContextImpl();
    writer = w;
  }

  /**
   * @{inheritDoc}
   */
  @Override
  public void startDocument() throws SAXException {
    writer.writeStartDocument();
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
      if (uri != null && qName != null) {
        String prefix = null;

        final int offset = qName.indexOf(":");
        
        if (offset > 0) {
          prefix = qName.substring(0, offset - 1);
          writer.writeStartElement(prefix, localName, uri);
        } else {
          writer.writeStartElement(uri, localName);
        }
      } else if (uri != null && localName != null) {
        writer.writeStartElement(uri, localName);
      } else if (uri != null) {
        writer.writeStartElement(localName);
      }

      for (int i = 0, l = attr.getLength(); i < l; i++) {
        writer.writeAttribute(attr.getURI(i), attr.getLocalName(i), attr
            .getValue(i));
      }
    } catch (XMLStreamException e) {
      e.printStackTrace();
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
    writer.writeEndElement();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void characters(final char[] ch, final int start, final int length)
      throws SAXException {
    try {
      writer.writeCharacters(ch, start, length);
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
  }
 
  /**
   * {@inheritDoc}
   */
  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    ((NamespaceContextImpl) writer.getNamespaceContext()).setNamespace(prefix, uri);
  }
  
  public static void main(final String[] args) {
    if (args.length < 2 || args.length > 3) {
      System.out.println("Usage: XMLInput input.xml output.tnk [key]");
      System.exit(1);
    }

    try {
      System.out.print("Shredding '" + args[0] + "' to '" + args[1] + "' ... ");
      long time = System.currentTimeMillis();
      new File(args[1]).delete();

      // Setup parsed session.
      final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      saxParserFactory.setValidating(false);
      saxParserFactory.setNamespaceAware(true);
      final SAXParser parser = saxParserFactory.newSAXParser();
      final InputSource inputSource = new InputSource(args[0]);
      final ISession session = Session.beginSession(new File(args[1]));
      final IWriteTransaction wrtx = session.beginWriteTransaction();
      parser.parse(inputSource, new StAXInput(new TreeTankStreamWriter(wrtx)));
      wrtx.close();

      System.out.println(" done ["
          + (System.currentTimeMillis() - time)
          + "ms].");

      session.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

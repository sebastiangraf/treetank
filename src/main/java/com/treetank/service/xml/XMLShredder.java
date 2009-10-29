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
 * $Id: XMLShredder.java 4455 2008-09-01 14:46:46Z kramis $
 */

package com.treetank.service.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;
import com.treetank.utils.FastStack;
import com.treetank.utils.IConstants;
import com.treetank.utils.TypedValue;

public final class XMLShredder {

    public final static long shred(final long id, final String content,
            final ISession session) throws TreetankException {
        try {
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            final XMLStreamReader parser = factory
                    .createXMLStreamReader(new StringReader(content));
            final long revision = shred(id, parser, session);
            return revision;
        } catch (final XMLStreamException exc) {
            throw new TreetankIOException(exc);
        }

    }

    public final static long shred(final String xmlPath,
            final SessionConfiguration sessionConfiguration)
            throws TreetankException {
        try {
            final InputStream in = new FileInputStream(xmlPath);
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            final XMLStreamReader parser = factory.createXMLStreamReader(in);
            final long revision = shred(0, parser, sessionConfiguration);
            return revision;
        } catch (final FileNotFoundException exc) {
            throw new TreetankIOException(exc);
        } catch (final XMLStreamException exc) {
            throw new TreetankIOException(exc);
        }
    }

    public static final long shred(final long id, final XMLStreamReader parser,
            final SessionConfiguration sessionConfiguration)
            throws TreetankException {
        final ISession session = Session.beginSession(sessionConfiguration);
        final long revision = shred(id, parser, session);
        session.close();
        return revision;
    }

    public static final long shred(final long id, final XMLStreamReader parser,
            final ISession session) throws TreetankException {
        try {
            final IWriteTransaction wtx = session.beginWriteTransaction();
            final long revision = wtx.getRevisionNumber();
            final FastStack<Long> leftSiblingKeyStack = new FastStack<Long>();

            // Make sure that we do not shred into an existing TreeTank.
            // if (wtx.hasFirstChild()) {
            // throw new IllegalStateException(
            // "XMLShredder can not shred into an existing TreeTank.");
            // }
            wtx.moveTo(id);

            long key;
            leftSiblingKeyStack.push(IReadTransaction.NULL_NODE_KEY);
            // leftSiblingKeyStack.push(wtx.getLeftSiblingKey());

            // Iterate over all nodes.
            while (parser.hasNext()) {

                switch (parser.next()) {

                case XMLStreamConstants.START_ELEMENT:

                    final String name = ((parser.getPrefix() == null || parser
                            .getPrefix().length() == 0) ? parser.getLocalName()
                            : parser.getPrefix() + ":" + parser.getLocalName());

                    if (leftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
                        key = wtx.insertElementAsFirstChild(name, parser
                                .getNamespaceURI());
                    } else {
                        key = wtx.insertElementAsRightSibling(name, parser
                                .getNamespaceURI());
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
                        wtx
                                .insertAttribute(
                                        (parser.getAttributePrefix(i) == null || parser
                                                .getAttributePrefix(i).length() == 0) ? parser
                                                .getAttributeLocalName(i)
                                                : parser.getAttributePrefix(i)
                                                        + ":"
                                                        + parser
                                                                .getAttributeLocalName(i),
                                        parser.getAttributeNamespace(i), parser
                                                .getAttributeValue(i));
                        wtx.moveTo(key);
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    leftSiblingKeyStack.pop();
                    wtx.moveTo(leftSiblingKeyStack.peek());
                    break;

                case XMLStreamConstants.CHARACTERS:
                    final String text = parser.getText().trim();
                    final ByteBuffer textByteBuffer = ByteBuffer
                            .wrap(TypedValue.getBytes(text));
                    int length = textByteBuffer.array().length;
                    if (textByteBuffer.array().length > 0) {
                        int beginIndex = 0;
                        do {
                            byte[] toWrite = null;
                            if (length >= IConstants.MAX_TEXTNODE_LENGTH) {
                                toWrite = new byte[IConstants.MAX_TEXTNODE_LENGTH];
                                // toWrite = text.substring(beginIndex,
                                // beginIndex
                                // + IConstants.MAX_TEXTNODE_LENGTH);
                            } else {
                                toWrite = new byte[length];
                                // toWrite = text.substring(beginIndex, text
                                // .length());
                            }
                            for (int i = 0; i < toWrite.length; i++) {
                                toWrite[i] = textByteBuffer.get();
                            }

                            if (leftSiblingKeyStack.peek() == IReadTransaction.NULL_NODE_KEY) {
                                key = wtx.insertTextAsFirstChild(wtx
                                        .keyForName("xs:untyped"), toWrite);
                            } else {
                                key = wtx.insertTextAsRightSibling(wtx
                                        .keyForName("xs:untyped"), toWrite);
                            }

                            leftSiblingKeyStack.pop();
                            leftSiblingKeyStack.push(key);

                            beginIndex = beginIndex
                                    + IConstants.MAX_TEXTNODE_LENGTH;
                            length = length - IConstants.MAX_TEXTNODE_LENGTH;
                        } while (length > 0);
                    }
                    break;

                }
            }
            wtx.commit();
            wtx.close();

            parser.close();
            return revision;
        } catch (final XMLStreamException exc1) {
            throw new TreetankIOException(exc1);
        }
    }

    public static final void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: XMLShredder input.xml output.tnk [key]");
            System.exit(1);
        }

        try {
            System.out.print("Shredding '" + args[0] + "' to '" + args[1]
                    + "' ... ");
            long time = System.currentTimeMillis();
            new File(args[1]).delete();
            XMLShredder.shred(args[0],
                    args.length == 2 ? new SessionConfiguration(args[1])
                            : new SessionConfiguration(args[1], args[2]
                                    .getBytes()));
            System.out.println(" done [" + (System.currentTimeMillis() - time)
                    + "ms].");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

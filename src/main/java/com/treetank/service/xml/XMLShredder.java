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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.settings.EFixed;
import com.treetank.utils.FastStack;
import com.treetank.utils.TypedValue;

/**
 * This class appends a given {@link XMLStreamReader} to a writetransaction. The
 * content of the stream is added as a subtree. Based on a boolean which
 * indentifies the point of insertion, the subtree is either added as subtree or
 * as rightsibling
 * 
 * @author Marc Kramis, Seabix
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class XMLShredder implements Callable<Long> {

    private final IWriteTransaction mWtx;
    private final XMLStreamReader mReader;
    private final boolean mFirstChildAppend;
    private final boolean mInsertOnlyModified;

    public XMLShredder(final IWriteTransaction wtx,
            final XMLStreamReader reader, final boolean addAsFirstChild) {
        this(wtx, reader, addAsFirstChild, false);
    }

    public XMLShredder(final IWriteTransaction wtx,
            final XMLStreamReader reader, final boolean addAsFirstChild,
            final boolean insertOnlyModified) {
        mWtx = wtx;
        mReader = reader;
        mFirstChildAppend = addAsFirstChild;
        mInsertOnlyModified = insertOnlyModified;
    }

    public Long call() throws Exception {
        try {
            final long revision = mWtx.getRevisionNumber();
            final FastStack<Long> leftSiblingKeyStack = new FastStack<Long>();

            long key;
            leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty());
            boolean firstElement = true;

            // Iterate over all nodes.
            while (mReader.hasNext()) {

                switch (mReader.next()) {

                case XMLStreamConstants.START_ELEMENT:

                    final String name = ((mReader.getPrefix() == null || mReader
                            .getPrefix().length() == 0) ? mReader
                            .getLocalName() : mReader.getPrefix() + ":"
                            + mReader.getLocalName());

                    if (firstElement && !mFirstChildAppend) {
                        if (mWtx.getNode().isDocumentRoot()) {
                            throw new TreetankUsageException(
                                    "Subtree can not be inserted as sibling of Root");
                        }
                        key = mWtx.insertElementAsRightSibling(name, mReader
                                .getNamespaceURI());
                    } else {

                        if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                                .getStandardProperty()) {
                            key = mWtx.insertElementAsFirstChild(name, mReader
                                    .getNamespaceURI());
                        } else {
                            key = mWtx.insertElementAsRightSibling(name,
                                    mReader.getNamespaceURI());
                        }
                    }

                    firstElement = false;

                    leftSiblingKeyStack.pop();
                    leftSiblingKeyStack.push(key);
                    leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                            .getStandardProperty());

                    // Parse namespaces.
                    for (int i = 0, l = mReader.getNamespaceCount(); i < l; i++) {
                        mWtx.insertNamespace(mReader.getNamespaceURI(i),
                                mReader.getNamespacePrefix(i));
                        mWtx.moveTo(key);
                    }

                    // Parse attributes.
                    for (int i = 0, l = mReader.getAttributeCount(); i < l; i++) {
                        mWtx
                                .insertAttribute(
                                        (mReader.getAttributePrefix(i) == null || mReader
                                                .getAttributePrefix(i).length() == 0) ? mReader
                                                .getAttributeLocalName(i)
                                                : mReader.getAttributePrefix(i)
                                                        + ":"
                                                        + mReader
                                                                .getAttributeLocalName(i),
                                        mReader.getAttributeNamespace(i),
                                        mReader.getAttributeValue(i));
                        mWtx.moveTo(key);
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    leftSiblingKeyStack.pop();
                    mWtx.moveTo(leftSiblingKeyStack.peek());
                    break;

                case XMLStreamConstants.CHARACTERS:
                    final String text = mReader.getText().trim();
                    final ByteBuffer textByteBuffer = ByteBuffer
                            .wrap(TypedValue.getBytes(text));
                    if (textByteBuffer.array().length > 0) {

                        if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                                .getStandardProperty()) {
                            key = mWtx.insertTextAsFirstChild(mWtx
                                    .keyForName("xs:untyped"), textByteBuffer
                                    .array());
                        } else {
                            key = mWtx.insertTextAsRightSibling(mWtx
                                    .keyForName("xs:untyped"), textByteBuffer
                                    .array());
                        }

                        leftSiblingKeyStack.pop();
                        leftSiblingKeyStack.push(key);

                    }
                    break;

                }
            }
            mWtx.commit();

            return revision;
        } catch (final XMLStreamException exc1) {
            throw new TreetankIOException(exc1);
        }
    }

    public static void main(String... args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: XMLShredder input.xml output.tnk");
            System.exit(1);
        }

        System.out.print("Shredding '" + args[0] + "' to '" + args[1]
                + "' ... ");
        long time = System.currentTimeMillis();
        final File target = new File(args[1]);
        Database.truncateDatabase(target);
        Database.createDatabase(new DatabaseConfiguration(target));
        final IDatabase db = Database.openDatabase(target);
        final ISession session = db.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final XMLStreamReader reader = createReader(new File(args[0]));
        final XMLShredder shredder = new XMLShredder(wtx, reader, true);
        shredder.call();

        wtx.close();
        session.close();
        db.close();

        System.out.println(" done [" + (System.currentTimeMillis() - time)
                + "ms].");
    }

    public static XMLStreamReader createReader(final File file)
            throws IOException, XMLStreamException {
        final InputStream in = new FileInputStream(file);
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        final XMLStreamReader parser = factory.createXMLStreamReader(in);
        return parser;
    }

}

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
 * $Id: XMLSerializer.java 4414 2008-08-27 20:01:07Z kramis $
 */

package com.treetank.service.xml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;

import com.treetank.access.Database;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.DescendantAxis;
import com.treetank.settings.EXMLSerializing;
import com.treetank.utils.FastStack;
import com.treetank.utils.IConstants;

/**
 * <h1>XMLSerializer</h1>
 * 
 * <p>
 * Most efficient way to serialize a subtree into an OutputStream. The encoding
 * always is UTF-8. Note that the OutputStream internally is wrapped by a
 * BufferedOutputStream. There is no need to buffer it again outside of this
 * class.
 * </p>
 */
public final class XMLSerializer implements Callable<Void> {

    /** Offset that must be added to digit to make it ASCII. */
    private static final int ASCII_OFFSET = 48;

    /** Precalculated powers of each available long digit. */
    private static final long[] LONG_POWERS = { 1L, 10L, 100L, 1000L, 10000L,
            100000L, 1000000L, 10000000L, 100000000L, 1000000000L,
            10000000000L, 100000000000L, 1000000000000L, 10000000000000L,
            100000000000000L, 1000000000000000L, 10000000000000000L,
            100000000000000000L, 1000000000000000000L };

    /** Transaction to read from (is the same as the mAxis). */
    private final IReadTransaction mRTX;

    /** Descendant-or-self axis used to traverse subtree. */
    private final IAxis mAxis;

    /** Stack for reading end element. */
    private final FastStack<Long> mStack;

    /** OutputStream to write to. */
    private final OutputStream mOut;

    /** Serialize XML declaration. */
    private final boolean mSerializeXMLDeclaration;

    /** Serialize rest header and closer and rest:id */
    private final boolean mSerializeRest;

    /** Serialize id */
    private final boolean mSerializeId;

    /**
     * Initialize XMLStreamReader implementation with transaction. The cursor
     * points to the node the XMLStreamReader starts to read. Do not serialize
     * the tank ids.
     * 
     * @param rtx
     *            Transaction with cursor pointing to start node.
     * @param out
     *            OutputStream to serialize UTF-8 XML to.
     */
    public XMLSerializer(final IReadTransaction rtx, final OutputStream out) {
        this(rtx, out, true, false, false);
    }

    /**
     * Initialize XMLStreamReader implementation with transaction. The cursor
     * points to the node the XMLStreamReader starts to read.
     * 
     * @param rtx
     *            Transaction with cursor pointing to start node.
     * @param out
     *            OutputStream to serialize UTF-8 XML to.
     * @param serializeXMLDeclaration
     *            Serialize XML declaration if true.
     * @param serializeRest
     *            Serialize rest if true.
     * @param serializeId
     *            Serialize id if true.
     */
    public XMLSerializer(final IReadTransaction rtx, final OutputStream out,
            final boolean serializeXMLDeclaration, final boolean serializeRest,
            final boolean serializeId) {
        mRTX = rtx;
        mAxis = new DescendantAxis(rtx, true);
        mStack = new FastStack<Long>();
        mOut = new BufferedOutputStream(out, 4096);
        mSerializeXMLDeclaration = serializeXMLDeclaration;
        mSerializeRest = serializeRest;
        mSerializeId = serializeId;
    }

    /**
     * Initialize XMLStreamReader implementation with transaction. The cursor
     * points to the node the XMLStreamReader starts to read.
     * 
     * @param rtx
     *            Transaction with cursor pointing to start node.
     * @param out
     *            OutputStream to serialize UTF-8 XML to.
     * @param serializeXMLDeclaration
     *            Serialize XML declaration if true.
     * @param serializeRest
     *            Serialize tank id if true.
     */
    public XMLSerializer(final IReadTransaction rtx, final OutputStream out,
            final boolean serializeXMLDeclaration, final boolean serializeRest) {
        this(rtx, out, serializeXMLDeclaration, serializeRest, serializeRest);
    }

    /**
     * {@inheritDoc}
     */
    public Void call() throws Exception {
        if (mSerializeXMLDeclaration) {
            write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        }
        if (mSerializeRest) {
            write("<rest:sequence xmlns:rest=\"REST\"><rest:item>");
        }

        boolean closeElements = false;

        // Iterate over all nodes of the subtree including self.
        for (final long key : mAxis) {

            // Emit all pending end elements.
            if (closeElements) {
                while (!mStack.empty()
                        && mStack.peek() != mRTX.getNode().getLeftSiblingKey()) {
                    mRTX.moveTo(mStack.pop());
                    emitEndElement();
                    mRTX.moveTo(key);
                }
                if (!mStack.empty()) {
                    mRTX.moveTo(mStack.pop());
                    emitEndElement();
                }
                mRTX.moveTo(key);
                closeElements = false;
            }

            // Emit node.
            emitNode();

            // Push end element to stack if we are a start element with
            // children.
            if (mRTX.getNode().isElement() && mRTX.getNode().hasFirstChild()) {
                mStack.push(mRTX.getNode().getNodeKey());
            }

            // Remember to emit all pending end elements from stack if
            // required.
            if (!mRTX.getNode().hasFirstChild()
                    && !mRTX.getNode().hasRightSibling()) {
                closeElements = true;
            }
        }

        // Finally emit all pending end elements.
        while (!mStack.empty()) {
            mRTX.moveTo(mStack.pop());
            emitEndElement();
        }

        if (mSerializeRest) {
            write("</rest:item></rest:sequence>");
        }

        mOut.flush();
        return null;
    }

    /**
     * Emit node (start element or characters).
     * 
     * @throws IOException
     */
    private void emitNode() throws IOException {
        switch (mRTX.getNode().getKind()) {
        case ELEMENT_KIND:
            // Emit start element.
            mOut.write(EXMLSerializing.OPEN.getBytes());
            mOut.write(mRTX.rawNameForKey(mRTX.getNode().getNameKey()));
            final long key = mRTX.getNode().getNodeKey();
            // Emit namespace declarations.
            for (int index = 0, length = mRTX.getNode().getNamespaceCount(); index < length; index++) {
                mRTX.moveToNamespace(index);
                if (mRTX.nameForKey(mRTX.getNode().getNameKey()).length() == 0) {
                    mOut.write(EXMLSerializing.XMLNS.getBytes());
                    write(mRTX.nameForKey(mRTX.getNode().getURIKey()));
                    mOut.write(EXMLSerializing.QUOTE.getBytes());
                } else {
                    mOut.write(EXMLSerializing.XMLNS_COLON.getBytes());
                    write(mRTX.nameForKey(mRTX.getNode().getNameKey()));
                    mOut.write(EXMLSerializing.EQUAL_QUOTE.getBytes());
                    write(mRTX.nameForKey(mRTX.getNode().getURIKey()));
                    mOut.write(EXMLSerializing.QUOTE.getBytes());
                }
                mRTX.moveTo(key);
            }
            // Emit attributes.
            // Add virtual rest:id attribute.
            if (mSerializeId) {
                if (mSerializeRest) {
                    mOut.write(EXMLSerializing.REST_PREFIX.getBytes());
                } else {
                    mOut.write(EXMLSerializing.SPACE.getBytes());
                }
                mOut.write(EXMLSerializing.ID.getBytes());
                mOut.write(EXMLSerializing.EQUAL_QUOTE.getBytes());
                write(mRTX.getNode().getNodeKey());
                mOut.write(EXMLSerializing.QUOTE.getBytes());
            }

            // Iterate over all persistent attributes.
            for (int index = 0; index < mRTX.getNode().getAttributeCount(); index++) {
                long nodeKey = mRTX.getNode().getNodeKey();
                mRTX.moveToAttribute(index);
                if (mRTX.getNode().isElement()) {
                    System.out.println(nodeKey);
                    System.out.println(mRTX.getNode().getNodeKey());
                }
                mOut.write(EXMLSerializing.SPACE.getBytes());
                mOut.write(mRTX.rawNameForKey(mRTX.getNode().getNameKey()));
                mOut.write(EXMLSerializing.EQUAL_QUOTE.getBytes());
                mOut.write(mRTX.getNode().getRawValue());
                mOut.write(EXMLSerializing.QUOTE.getBytes());
                mRTX.moveTo(key);
            }
            if (mRTX.getNode().hasFirstChild()) {
                mOut.write(EXMLSerializing.CLOSE.getBytes());
            } else {
                mOut.write(EXMLSerializing.SLASH_CLOSE.getBytes());
            }
            break;
        case TEXT_KIND:
            mOut.write(mRTX.getNode().getRawValue());
            break;
        }
    }

    /**
     * Emit end element.
     * 
     * @throws IOException
     */
    private void emitEndElement() throws IOException {
        mOut.write(EXMLSerializing.OPEN_SLASH.getBytes());
        mOut.write(mRTX.rawNameForKey(mRTX.getNode().getNameKey()));
        mOut.write(EXMLSerializing.CLOSE.getBytes());
    }

    /**
     * Write characters of string.
     * 
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private void write(final String string)
            throws UnsupportedEncodingException, IOException {
        mOut.write(string.getBytes(IConstants.DEFAULT_ENCODING));
    }

    /**
     * Write non-negative non-zero long as UTF-8 bytes.
     * 
     * @throws IOException
     */
    private void write(final long value) throws IOException {
        final int length = (int) Math.log10((double) value);
        int digit = 0;
        long remainder = value;
        for (int i = length; i >= 0; i--) {
            digit = (byte) (remainder / LONG_POWERS[i]);
            mOut.write((byte) (digit + ASCII_OFFSET));
            remainder -= digit * LONG_POWERS[i];
        }
    }

    public static void main(String... args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: XMLSerializer input.tnk output.xml");
            System.exit(1);
        }

        System.out.print("Serializing '" + args[0] + "' to '" + args[1]
                + "' ... ");
        long time = System.currentTimeMillis();
        final File target = new File(args[1]);
        target.delete();
        final FileOutputStream outputStream = new FileOutputStream(target);

        final IDatabase db = Database.openDatabase(new File(args[0]));
        final ISession session = db.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();
        final XMLSerializer serializer = new XMLSerializer(rtx, outputStream,
                true, false, true);
        serializer.call();

        rtx.close();
        session.close();
        db.close();
        outputStream.close();

        System.out.println(" done [" + (System.currentTimeMillis() - time)
                + "ms].");
    }

}

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

import com.treetank.access.Session;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.DescendantAxis;
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

    /** " ". */
    private static final int SPACE = 32;

    /** "&lt;". */
    private static final int OPEN = 60;

    /** "&gt;". */
    private static final int CLOSE = 62;

    /** "/". */
    private static final int SLASH = 47;

    /** "=". */
    private static final int EQUAL = 61;

    /** "\"". */
    private static final int QUOTE = 34;

    /** "=\"". */
    private static final byte[] EQUAL_QUOTE = new byte[] { EQUAL, QUOTE };

    /** "&lt;/". */
    private static final byte[] OPEN_SLASH = new byte[] { OPEN, SLASH };

    /** "/&gt;". */
    private static final byte[] SLASH_CLOSE = new byte[] { SLASH, CLOSE };

    /** " rest:id=\"". */
    private static final byte[] REST_ID = new byte[] { SPACE, 114, 101, 115,
            116, 58, 105, 100, EQUAL, QUOTE };

    /** " xmlns=\"". */
    private static final byte[] XMLNS = new byte[] { SPACE, 120, 109, 108, 110,
            115, EQUAL, QUOTE };

    /** " xmlns:". */
    private static final byte[] XMLNS_COLON = new byte[] { SPACE, 120, 109,
            108, 110, 115, 58 };

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

    /** Serialize rest:id. */
    private final boolean mSerializeRest;

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
        this(rtx, out, true, false);
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
        mRTX = rtx;
        mAxis = new DescendantAxis(rtx, true);
        mStack = new FastStack<Long>();
        mOut = new BufferedOutputStream(out, 4096);
        mSerializeXMLDeclaration = serializeXMLDeclaration;
        mSerializeRest = serializeRest;
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
            mOut.write(OPEN);
            mOut.write(mRTX.rawNameForKey(mRTX.getNode().getNameKey()));
            final long key = mRTX.getNode().getNodeKey();
            // Emit namespace declarations.
            for (int index = 0, length = mRTX.getNode().getNamespaceCount(); index < length; index++) {
                mRTX.moveToNamespace(index);
                if (mRTX.nameForKey(mRTX.getNode().getNameKey()).length() == 0) {
                    mOut.write(XMLNS);
                    write(mRTX.nameForKey(mRTX.getNode().getURIKey()));
                    mOut.write(QUOTE);
                } else {
                    mOut.write(XMLNS_COLON);
                    write(mRTX.nameForKey(mRTX.getNode().getNameKey()));
                    mOut.write(EQUAL_QUOTE);
                    write(mRTX.nameForKey(mRTX.getNode().getURIKey()));
                    mOut.write(QUOTE);
                }
                mRTX.moveTo(key);
            }
            // Emit attributes.
            // Add virtual rest:id attribute.
            if (mSerializeRest) {
                mOut.write(REST_ID);
                write(mRTX.getNode().getNodeKey());
                mOut.write(QUOTE);
            }

            // Iterate over all persistent attributes.
            for (int index = 0; index < mRTX.getNode().getAttributeCount(); index++) {
                long nodeKey = mRTX.getNode().getNodeKey();
                mRTX.moveToAttribute(index);
                if (mRTX.getNode().isElement()) {
                    System.out.println(nodeKey);
                    System.out.println(mRTX.getNode().getNodeKey());
                }
                mOut.write(SPACE);
                mOut.write(mRTX.rawNameForKey(mRTX.getNode().getNameKey()));
                mOut.write(EQUAL_QUOTE);
                mOut.write(mRTX.getNode().getRawValue());
                mOut.write(QUOTE);
                mRTX.moveTo(key);
            }
            if (mRTX.getNode().hasFirstChild()) {
                mOut.write(CLOSE);
            } else {
                mOut.write(SLASH_CLOSE);
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
        mOut.write(OPEN_SLASH);
        mOut.write(mRTX.rawNameForKey(mRTX.getNode().getNameKey()));
        mOut.write(CLOSE);
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

    /**
     * Invocation of this class from external.
     * 
     * @param args
     *            first arg is mandatory: tnk-file; second arg is optional:
     *            xml-output, system.out otherwise
     * @throws Exception
     *             of any kind
     */
    public static void main(final String[] args) throws Exception {
        OutputStream output = null;
        switch (args.length) {
        case 1:
            output = System.out;
            break;
        case 2:
            output = new FileOutputStream(new File(args[1]));
            break;
        default:
            System.out
                    .println("Usage: java XMLSerializer \"tnk-file\" [\"xml-file\"]");
            System.exit(1);
        }

        final ISession session = Session.beginSession(new File(args[0]));
        final IReadTransaction rtx = session.beginReadTransaction();

        final XMLSerializer serializer = new XMLSerializer(rtx, output);
        serializer.call();
        rtx.close();
        session.close();

        output.flush();
        output.close();

    }

}

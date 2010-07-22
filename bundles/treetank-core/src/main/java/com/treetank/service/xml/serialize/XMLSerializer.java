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

package com.treetank.service.xml.serialize;

import static com.treetank.service.xml.serialize.SerializerProperties.NL;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.settings.ECharsForSerializing;
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
public final class XMLSerializer extends AbsSerializeStorage implements
        Callable<Void> {

    /** Offset that must be added to digit to make it ASCII. */
    private static final int ASCII_OFFSET = 48;

    /** Precalculated powers of each available long digit. */
    private static final long[] LONG_POWERS = { 1L, 10L, 100L, 1000L, 10000L,
            100000L, 1000000L, 10000000L, 100000000L, 1000000000L,
            10000000000L, 100000000000L, 1000000000000L, 10000000000000L,
            100000000000000L, 1000000000000000L, 10000000000000000L,
            100000000000000000L, 1000000000000000000L };

    /** OutputStream to write to. */
    private final OutputStream mOut;

    /** Indent output. */
    private final boolean mIndent;

    /** Line sparator. */
    private final byte[] mNL = NL.getBytes();

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
    public XMLSerializer(final XMLSerializerBuilder builder) {
        super(builder.mIntermediateRtx, builder.mIntermediateDeclaration,
                builder.mIntermediateSerializeRest, builder.mIntermediateId);
        mOut = new BufferedOutputStream(builder.mIntermediateStream, 4096);
        mIndent = builder.mIntermediateIntend;
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

        serialize();

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
    @Override
    public void emitNode() throws IOException {
        switch (mRTX.getNode().getKind()) {
        case ROOT_KIND:
            if (mIndent) {
                mOut.write(mNL);
            }
            break;
        case ELEMENT_KIND:
            // Emit start element.
            indent();
            mOut.write(ECharsForSerializing.OPEN.getBytes());
            mOut.write(mRTX.rawNameForKey(mRTX.getNode().getNameKey()));
            final long key = mRTX.getNode().getNodeKey();
            // Emit namespace declarations.
            for (int index = 0, length = ((ElementNode) mRTX.getNode())
                    .getNamespaceCount(); index < length; index++) {
                mRTX.moveToNamespace(index);
                if (mRTX.nameForKey(mRTX.getNode().getNameKey()).length() == 0) {
                    mOut.write(ECharsForSerializing.XMLNS.getBytes());
                    write(mRTX.nameForKey(mRTX.getNode().getURIKey()));
                    mOut.write(ECharsForSerializing.QUOTE.getBytes());
                } else {
                    mOut.write(ECharsForSerializing.XMLNS_COLON.getBytes());
                    write(mRTX.nameForKey(mRTX.getNode().getNameKey()));
                    mOut.write(ECharsForSerializing.EQUAL_QUOTE.getBytes());
                    write(mRTX.nameForKey(mRTX.getNode().getURIKey()));
                    mOut.write(ECharsForSerializing.QUOTE.getBytes());
                }
                mRTX.moveTo(key);
            }
            // Emit attributes.
            // Add virtual rest:id attribute.
            if (mSerializeId) {
                if (mSerializeRest) {
                    mOut.write(ECharsForSerializing.REST_PREFIX.getBytes());
                } else {
                    mOut.write(ECharsForSerializing.SPACE.getBytes());
                }
                mOut.write(ECharsForSerializing.ID.getBytes());
                mOut.write(ECharsForSerializing.EQUAL_QUOTE.getBytes());
                write(mRTX.getNode().getNodeKey());
                mOut.write(ECharsForSerializing.QUOTE.getBytes());
            }

            // Iterate over all persistent attributes.
            for (int index = 0; index < ((ElementNode) mRTX.getNode())
                    .getAttributeCount(); index++) {
                long nodeKey = mRTX.getNode().getNodeKey();
                mRTX.moveToAttribute(index);
                if (mRTX.getNode().getKind() == ENodes.ELEMENT_KIND) {
                    System.out.println(nodeKey);
                    System.out.println(mRTX.getNode().getNodeKey());
                }
                mOut.write(ECharsForSerializing.SPACE.getBytes());
                mOut.write(mRTX.rawNameForKey(mRTX.getNode().getNameKey()));
                mOut.write(ECharsForSerializing.EQUAL_QUOTE.getBytes());
                mOut.write(mRTX.getNode().getRawValue());
                mOut.write(ECharsForSerializing.QUOTE.getBytes());
                mRTX.moveTo(key);
            }
            if (((AbsStructNode) mRTX.getNode()).hasFirstChild()) {
                mOut.write(ECharsForSerializing.CLOSE.getBytes());
            } else {
                mOut.write(ECharsForSerializing.SLASH_CLOSE.getBytes());
            }
            if (mIndent) {
                mOut.write(mNL);
            }
            break;
        case TEXT_KIND:
            indent();
            mOut.write(mRTX.getNode().getRawValue());
            if (mIndent) {
                mOut.write('\n');
            }
            break;
        }
    }

    /**
     * Emit end element.
     * 
     * @throws IOException
     */
    @Override
    public void emitEndElement() throws IOException {
        indent();
        mOut.write(ECharsForSerializing.OPEN_SLASH.getBytes());
        mOut.write(mRTX.rawNameForKey(mRTX.getNode().getNameKey()));
        mOut.write(ECharsForSerializing.CLOSE.getBytes());
        if (mIndent) {
            mOut.write(mNL);
        }
    }

    /**
     * Indentation of output.
     * 
     * @throws IOException
     */
    private void indent() throws IOException {
        if (mIndent) {
            for (int i = 0; i < mStack.size(); i++) {
                mOut.write("  ".getBytes());
            }
        }
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
     * Main method.
     * 
     * @param args
     *            args[0] specifies the input-TT file/folder; args[1] specifies
     *            the output XML file.
     * @throws Exception
     *             Any exception.
     */
    public static void main(String... args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: XMLSerializer input-TT output.xml");
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

        final XMLSerializer serializer = new XMLSerializerBuilder(rtx,
                outputStream).build();
        serializer.call();

        rtx.close();
        session.close();
        db.close();
        outputStream.close();

        System.out.println(" done [" + (System.currentTimeMillis() - time)
                + "ms].");
    }

    public static class XMLSerializerBuilder {

        /**
         * Intermediate {@link IReadTransaction} for builder, necessary
         */
        private final IReadTransaction mIntermediateRtx;

        /**
         * Intermediate {@link OutputStream} for stream, necessary
         */
        private final OutputStream mIntermediateStream;

        /**
         * Intermediate boolean for intendtion, not necessary
         */
        private boolean mIntermediateIntend = false;

        /**
         * Intermediate boolean for rest serialization, not necessary
         */
        private boolean mIntermediateSerializeRest = false;

        /**
         * Intermediate boolean for rest serialization, not necessary
         */
        private boolean mIntermediateDeclaration = true;

        /**
         * Intermediate boolean for ids, not necessary
         */
        private boolean mIntermediateId = false;

        /**
         * Constructor for the builder;
         * 
         * @param rtx
         * @param stream
         */
        public XMLSerializerBuilder(final IReadTransaction rtx,
                final OutputStream stream) {
            mIntermediateRtx = rtx;
            mIntermediateStream = stream;
        }

        public void setIntermediateIntend(boolean mIntermediateIntend) {
            this.mIntermediateIntend = mIntermediateIntend;
        }

        public void setIntermediateSerializeRest(
                boolean mIntermediateSerializeRest) {
            this.mIntermediateSerializeRest = mIntermediateSerializeRest;
        }

        public void setIntermediateDeclaration(boolean mIntermediateDeclaration) {
            this.mIntermediateDeclaration = mIntermediateDeclaration;
        }

        public void setIntermediateId(boolean mIntermediateId) {
            this.mIntermediateId = mIntermediateId;
        }

        public XMLSerializer build() {
            return new XMLSerializer(this);
        }
    }

}

/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.service.xml.serialize;

import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_ID;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_INDENT;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_INDENT_SPACES;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_REST;
import static com.treetank.service.xml.serialize.XMLSerializerProperties.S_XMLDECL;

import java.io.*;
import java.util.concurrent.ConcurrentMap;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ElementNode;
import com.treetank.settings.ECharsForSerializing;
import com.treetank.utils.IConstants;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * <h1>XMLSerializer</h1>
 * 
 * <p>
 * Most efficient way to serialize a subtree into an OutputStream. The encoding always is UTF-8. Note that the
 * OutputStream internally is wrapped by a BufferedOutputStream. There is no need to buffer it again outside
 * of this class.
 * </p>
 */
public final class XMLSerializer extends AbsSerializer {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(XMLSerializer.class));

    /** Offset that must be added to digit to make it ASCII. */
    private static final int ASCII_OFFSET = 48;

    /** Precalculated powers of each available long digit. */
    private static final long[] LONG_POWERS = {
        1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L, 100000000L, 1000000000L, 10000000000L,
        100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L,
        10000000000000000L, 100000000000000000L, 1000000000000000000L
    };

    /** OutputStream to write to. */
    private final OutputStream mOut;

    /** Indent output. */
    private final boolean mIndent;

    /** Serialize XML declaration. */
    private final boolean mSerializeXMLDeclaration;

    /** Serialize rest header and closer and rest:id. */
    private final boolean mSerializeRest;

    /** Serialize id. */
    private final boolean mSerializeId;

    /** Number of spaces to indent. */
    private final int mIndentSpaces;

    /**
     * Initialize XMLStreamReader implementation with transaction. The cursor
     * points to the node the XMLStreamReader starts to read.
     * 
     * @param paramSession
     *            Session for read XML
     * @param paramNodeKey
     *            Node Key
     * @param paramBuilder
     *            Builder of XML Serializer
     * @param paramVersions
     *            Version to serailze
     */
    private XMLSerializer(final ISession paramSession, final long paramNodeKey,
        final XMLSerializerBuilder paramBuilder, final long... paramVersions) {
        super(paramSession, paramNodeKey, paramVersions);
        mOut = new BufferedOutputStream(paramBuilder.mStream, 4096);
        mIndent = paramBuilder.mIndent;
        mSerializeXMLDeclaration = paramBuilder.mDeclaration;
        mSerializeRest = paramBuilder.mREST;
        mSerializeId = paramBuilder.mID;
        mIndentSpaces = paramBuilder.mIndentSpaces;
    }

    /**
     * Emit node (start element or characters).
     * 
     * @throws IOException
     */
    @Override
    protected void emitStartElement(final IReadTransaction paramRTX) {
        try {
            switch (paramRTX.getNode().getKind()) {
            case ROOT_KIND:
                if (mIndent) {
                    mOut.write(ECharsForSerializing.NEWLINE.getBytes());
                }
                break;
            case ELEMENT_KIND:
                // Emit start element.
                indent();
                mOut.write(ECharsForSerializing.OPEN.getBytes());
                mOut.write(paramRTX.rawNameForKey(paramRTX.getNode().getNameKey()));
                final long key = paramRTX.getNode().getNodeKey();
                // Emit namespace declarations.
                for (int index = 0, length = ((ElementNode)paramRTX.getNode()).getNamespaceCount(); index < length; index++) {
                    paramRTX.moveToNamespace(index);
                    if (paramRTX.nameForKey(paramRTX.getNode().getNameKey()).length() == 0) {
                        mOut.write(ECharsForSerializing.XMLNS.getBytes());
                        write(paramRTX.nameForKey(paramRTX.getNode().getURIKey()));
                        mOut.write(ECharsForSerializing.QUOTE.getBytes());
                    } else {
                        mOut.write(ECharsForSerializing.XMLNS_COLON.getBytes());
                        write(paramRTX.nameForKey(paramRTX.getNode().getNameKey()));
                        mOut.write(ECharsForSerializing.EQUAL_QUOTE.getBytes());
                        write(paramRTX.nameForKey(paramRTX.getNode().getURIKey()));
                        mOut.write(ECharsForSerializing.QUOTE.getBytes());
                    }
                    paramRTX.moveTo(key);
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
                    write(paramRTX.getNode().getNodeKey());
                    mOut.write(ECharsForSerializing.QUOTE.getBytes());
                }

                // Iterate over all persistent attributes.
                for (int index = 0; index < ((ElementNode)paramRTX.getNode()).getAttributeCount(); index++) {
                    paramRTX.moveToAttribute(index);
                    mOut.write(ECharsForSerializing.SPACE.getBytes());
                    mOut.write(paramRTX.rawNameForKey(paramRTX.getNode().getNameKey()));
                    mOut.write(ECharsForSerializing.EQUAL_QUOTE.getBytes());
                    mOut.write(paramRTX.getNode().getRawValue());
                    mOut.write(ECharsForSerializing.QUOTE.getBytes());
                    paramRTX.moveTo(key);
                }
                if (((AbsStructNode)paramRTX.getNode()).hasFirstChild()) {
                    mOut.write(ECharsForSerializing.CLOSE.getBytes());
                } else {
                    mOut.write(ECharsForSerializing.SLASH_CLOSE.getBytes());
                }
                if (mIndent) {
                    mOut.write(ECharsForSerializing.NEWLINE.getBytes());
                }
                break;
            case TEXT_KIND:
                indent();
                mOut.write(paramRTX.getNode().getRawValue());
                if (mIndent) {
                    mOut.write(ECharsForSerializing.NEWLINE.getBytes());
                }
                break;
            }
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }
    }

    /**
     * Emit end element.
     * 
     * @param paramRTX
     *            Read Transaction
     * @throws IOException
     */
    @Override
    protected void emitEndElement(final IReadTransaction paramRTX) {
        try {
            indent();
            mOut.write(ECharsForSerializing.OPEN_SLASH.getBytes());
            mOut.write(paramRTX.rawNameForKey(paramRTX.getNode().getNameKey()));
            mOut.write(ECharsForSerializing.CLOSE.getBytes());
            if (mIndent) {
                mOut.write(ECharsForSerializing.NEWLINE.getBytes());
            }
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }
    }

    @Override
    protected void emitStartDocument() {
        try {
            if (mSerializeXMLDeclaration) {
                write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            }
            if (mSerializeRest) {
                write("<rest:sequence xmlns:rest=\"REST\"><rest:item>");
            }
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }
    }

    @Override
    protected void emitEndDocument() {
        try {
            if (mSerializeRest) {
                write("</rest:item></rest:sequence>");
            }
            mOut.flush();
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }

    }

    @Override
    protected void emitStartManualElement(final long mVersion) {
        try {
            write("<tt revision=\"");
            write(Long.toString(mVersion));
            write("\">");
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }

    }

    @Override
    protected void emitEndManualElement(final long mVersion) {
        try {
            write("</tt>");
        } catch (final IOException exc) {
            LOGWRAPPER.error(exc);
        }
    }

    /**
     * Indentation of output.
     * 
     * @throws IOException
     *             if can't indent output
     */
    private void indent() throws IOException {
        if (mIndent) {
            for (int i = 0; i < mStack.size() * mIndentSpaces; i++) {
                mOut.write(" ".getBytes());
            }
        }
    }

    /**
     * Write characters of string.
     * 
     * @param mString
     *            String to write
     * @throws IOException
     *             if can't write to string
     * @throws UnsupportedEncodingException
     *             if unsupport encoding
     */
    protected void write(final String mString) throws UnsupportedEncodingException, IOException {
        mOut.write(mString.getBytes(IConstants.DEFAULT_ENCODING));
    }

    /**
     * Write non-negative non-zero long as UTF-8 bytes.
     * 
     * @param mValue
     *            Value to write
     * @throws IOException
     *             if can't write to string
     */
    private void write(final long mValue) throws IOException {
        final int length = (int)Math.log10((double)mValue);
        int digit = 0;
        long remainder = mValue;
        for (int i = length; i >= 0; i--) {
            digit = (byte)(remainder / LONG_POWERS[i]);
            mOut.write((byte)(digit + ASCII_OFFSET));
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
    public static void main(final String... args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: XMLSerializer input-TT output.xml");
            System.exit(1);
        }

        System.out.print("Serializing '" + args[0] + "' to '" + args[1] + "' ... ");
        final long time = System.currentTimeMillis();
        final File target = new File(args[1]);
        target.delete();
        final FileOutputStream outputStream = new FileOutputStream(target);

        final IDatabase db = Database.openDatabase(new File(args[0]));
        final ISession session = db.getSession();

        final XMLSerializer serializer = new XMLSerializerBuilder(session, outputStream).build();
        serializer.call();

        session.close();
        db.close();
        outputStream.close();

        System.out.println(" done [" + (System.currentTimeMillis() - time) + "ms].");
    }

    /**
     * XMLSerializerBuilder to setup the XMLSerializer.
     */
    public static final class XMLSerializerBuilder {
        /**
         * Intermediate boolean for indendation, not necessary.
         */
        private transient boolean mIndent;

        /**
         * Intermediate boolean for rest serialization, not necessary.
         */
        private transient boolean mREST;

        /**
         * Intermediate boolean for XML-Decl serialization, not necessary.
         */
        private transient boolean mDeclaration = true;

        /**
         * Intermediate boolean for ids, not necessary.
         */
        private transient boolean mID;

        /**
         * Intermediate number of spaces to indent, not necessary.
         */
        private transient int mIndentSpaces = 2;

        /** Stream to pipe to. */
        private final transient OutputStream mStream;

        /** Session to use. */
        private final transient ISession mSession;

        /** Versions to use. */
        private final transient long[] mVersions;

        /** Node key of subtree to shredder. */
        private final transient long mNodeKey;

        /**
         * Constructor, setting the necessary stuff.
         * 
         * @param paramSession
         *            {@link ISession} to Serialize.
         * @param paramStream
         *            {@link OutputStream}.
         * @param paramVersions
         *            Version to Serialize.
         */
        public XMLSerializerBuilder(final ISession paramSession, final OutputStream paramStream,
            final long... paramVersions) {
            mNodeKey = 0;
            mStream = paramStream;
            mSession = paramSession;
            mVersions = paramVersions;
        }

        /**
         * Constructor.
         * 
         * @param paramSession
         *            {@link ISession}.
         * @param paramNodeKey
         *            Root node key of subtree to shredder.
         * @param paramStream
         *            {@link OutputStream}.
         * @param paramProperties
         *            {@link XMLSerializerProperties}.
         * @param paramVersions
         *            Versions to serialize.
         */
        public XMLSerializerBuilder(final ISession paramSession, final long paramNodeKey,
            final OutputStream paramStream, final XMLSerializerProperties paramProperties,
            final long... paramVersions) {
            mSession = paramSession;
            mNodeKey = paramNodeKey;
            mStream = paramStream;
            mVersions = paramVersions;
            final ConcurrentMap<?, ?> map = paramProperties.getmProps();
            mIndent = (Boolean)map.get(S_INDENT[0]);
            mREST = (Boolean)map.get(S_REST[0]);
            mID = (Boolean)map.get(S_ID[0]);
            mIndentSpaces = (Integer)map.get(S_INDENT_SPACES[0]);
            mDeclaration = (Boolean)map.get(S_XMLDECL[0]);
        }

        /**
         * Setting the indention.
         * 
         * @param paramIndent
         *            to set
         * @return XMLSerializerBuilder reference.
         */
        public XMLSerializerBuilder setIndend(final boolean paramIndent) {
            mIndent = paramIndent;
            return this;
        }

        /**
         * Setting the RESTful output.
         * 
         * @param paramREST
         *            to set
         * @return XMLSerializerBuilder reference.
         */
        public XMLSerializerBuilder setREST(final boolean paramREST) {
            mREST = paramREST;
            return this;
        }

        /**
         * Setting the declaration.
         * 
         * @param paramDeclaration
         *            to set
         * @return XMLSerializerBuilder reference.
         */
        public XMLSerializerBuilder setDeclaration(final boolean paramDeclaration) {
            mDeclaration = paramDeclaration;
            return this;
        }

        /**
         * Setting the ids on nodes.
         * 
         * @param paramID
         *            to set
         * @return XMLSerializerBuilder reference.
         */
        public XMLSerializerBuilder setID(final boolean paramID) {
            mID = paramID;
            return this;
        }

        /**
         * Building new Serializer.
         * 
         * @return a new instance
         */
        public XMLSerializer build() {
            return new XMLSerializer(mSession, mNodeKey, this, mVersions);
        }
    }

}

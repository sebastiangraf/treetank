/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.service.xml.serialize;

import static org.treetank.node.IConstants.ELEMENT;
import static org.treetank.node.IConstants.ROOT;
import static org.treetank.node.IConstants.TEXT;
import static org.treetank.service.xml.serialize.XMLSerializerProperties.S_ID;
import static org.treetank.service.xml.serialize.XMLSerializerProperties.S_INDENT;
import static org.treetank.service.xml.serialize.XMLSerializerProperties.S_INDENT_SPACES;
import static org.treetank.service.xml.serialize.XMLSerializerProperties.S_REST;
import static org.treetank.service.xml.serialize.XMLSerializerProperties.S_XMLDECL;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import org.treetank.access.Storage;
import org.treetank.access.conf.ConstructorProps;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.INodeReadTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.node.ElementNode;
import org.treetank.node.NodeMetaPageFactory;
import org.treetank.node.TreeNodeFactory;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.Guice;
import com.google.inject.Injector;

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

    enum ECharsForSerializing {

        /** " ". */
        SPACE(new byte[] {
            32
        }),

        /** "&lt;". */
        OPEN(new byte[] {
            60
        }),

        /** "&gt;". */
        CLOSE(new byte[] {
            62
        }),

        /** "/". */
        SLASH(new byte[] {
            47
        }),

        /** "=". */
        EQUAL(new byte[] {
            61
        }),

        /** "\"". */
        QUOTE(new byte[] {
            34
        }),

        /** "=\"". */
        EQUAL_QUOTE(EQUAL.getBytes(), QUOTE.getBytes()),

        /** "&lt;/". */
        OPEN_SLASH(OPEN.getBytes(), SLASH.getBytes()),

        /** "/&gt;". */
        SLASH_CLOSE(SLASH.getBytes(), CLOSE.getBytes()),

        /** " rest:"". */
        REST_PREFIX(SPACE.getBytes(), new byte[] {
            114, 101, 115, 116, 58
        }),

        /** "ttid". */
        ID(new byte[] {
            116, 116, 105, 100
        }),

        /** " xmlns=\"". */
        XMLNS(SPACE.getBytes(), new byte[] {
            120, 109, 108, 110, 115
        }, EQUAL.getBytes(), QUOTE.getBytes()),

        /** " xmlns:". */
        XMLNS_COLON(SPACE.getBytes(), new byte[] {
            120, 109, 108, 110, 115, 58
        }),

        /** Newline. */
        NEWLINE(System.getProperty("line.separator").getBytes());

        /** Getting the bytes for the char. */
        private final byte[] mBytes;

        /**
         * Private constructor.
         * 
         * @param paramBytes
         *            the bytes for the chars
         */
        ECharsForSerializing(final byte[]... paramBytes) {
            int index = 0;
            for (final byte[] runner : paramBytes) {
                index = index + runner.length;
            }
            this.mBytes = new byte[index];
            index = 0;
            for (final byte[] runner : paramBytes) {
                System.arraycopy(runner, 0, mBytes, index, runner.length);
                index = index + runner.length;
            }
        }

        /**
         * Getting the bytes.
         * 
         * @return the bytes for the char.
         */
        public byte[] getBytes() {
            return mBytes;
        }

    }

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
     * @throws TTIOException
     */
    @Override
    protected void emitStartElement(final INodeReadTrx paramRTX) throws TTIOException {
        try {
            switch (paramRTX.getNode().getKind()) {
            case ROOT:
                if (mIndent) {
                    mOut.write(ECharsForSerializing.NEWLINE.getBytes());
                }
                break;
            case ELEMENT:
                // Emit start element.
                indent();
                final INameNode namenode = (INameNode)paramRTX.getNode();
                mOut.write(ECharsForSerializing.OPEN.getBytes());
                mOut.write(paramRTX.nameForKey(namenode.getNameKey()).getBytes());
                final long key = paramRTX.getNode().getNodeKey();
                // Emit namespace declarations.
                for (int index = 0, length = ((ElementNode)namenode).getNamespaceCount(); index < length; index++) {
                    paramRTX.moveToNamespace(index);
                    if (paramRTX.nameForKey(((INameNode)paramRTX.getNode()).getNameKey()).length() == 0) {
                        mOut.write(ECharsForSerializing.XMLNS.getBytes());
                        write(paramRTX.nameForKey(((INameNode)paramRTX.getNode()).getURIKey()));
                        mOut.write(ECharsForSerializing.QUOTE.getBytes());
                    } else {
                        mOut.write(ECharsForSerializing.XMLNS_COLON.getBytes());
                        write(paramRTX.nameForKey(((INameNode)paramRTX.getNode()).getNameKey()));
                        mOut.write(ECharsForSerializing.EQUAL_QUOTE.getBytes());
                        write(paramRTX.nameForKey(((INameNode)paramRTX.getNode()).getURIKey()));
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
                    mOut.write(paramRTX.nameForKey(((INameNode)paramRTX.getNode()).getNameKey()).getBytes());
                    mOut.write(ECharsForSerializing.EQUAL_QUOTE.getBytes());
                    mOut.write(paramRTX.getValueOfCurrentNode().getBytes());
                    mOut.write(ECharsForSerializing.QUOTE.getBytes());
                    paramRTX.moveTo(key);
                }
                if (((IStructNode)paramRTX.getNode()).hasFirstChild()) {
                    mOut.write(ECharsForSerializing.CLOSE.getBytes());
                } else {
                    mOut.write(ECharsForSerializing.SLASH_CLOSE.getBytes());
                }
                if (mIndent) {
                    mOut.write(ECharsForSerializing.NEWLINE.getBytes());
                }
                break;
            case TEXT:
                indent();
                mOut.write(paramRTX.getValueOfCurrentNode().getBytes());
                if (mIndent) {
                    mOut.write(ECharsForSerializing.NEWLINE.getBytes());
                }
                break;
            }
        } catch (final IOException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Emit end element.
     * 
     * @param paramRTX
     *            Read Transaction
     */
    @Override
    protected void emitEndElement(final INodeReadTrx paramRTX) {
        try {
            indent();
            mOut.write(ECharsForSerializing.OPEN_SLASH.getBytes());
            mOut.write(paramRTX.nameForKey(((INameNode)paramRTX.getNode()).getNameKey()).getBytes());
            mOut.write(ECharsForSerializing.CLOSE.getBytes());
            if (mIndent) {
                mOut.write(ECharsForSerializing.NEWLINE.getBytes());
            }
        } catch (final IOException exc) {
            exc.printStackTrace();
        }
    }

    /** {@inheritDoc} */
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
            exc.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void emitEndDocument() {
        try {
            if (mSerializeRest) {
                write("</rest:item></rest:sequence>");
            }
            mOut.flush();
        } catch (final IOException exc) {
            exc.printStackTrace();
        }

    }

    /** {@inheritDoc} */
    @Override
    protected void emitStartManualElement(final long mVersion) {
        try {
            write("<tt revision=\"");
            write(Long.toString(mVersion));
            write("\">");
        } catch (final IOException exc) {
            exc.printStackTrace();
        }

    }

    /** {@inheritDoc} */
    @Override
    protected void emitEndManualElement(final long mVersion) {
        try {
            write("</tt>");
        } catch (final IOException exc) {
            exc.printStackTrace();
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
        mOut.write(mString.getBytes("UTF-8"));
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

        Injector injector = Guice.createInjector(new ModuleSetter().setNodeFacClass(TreeNodeFactory.class).setMetaFacClass(NodeMetaPageFactory.class).createModule());
        IBackendFactory storage = injector.getInstance(IBackendFactory.class);
        IRevisioning revision = injector.getInstance(IRevisioning.class);

        final File target = new File(args[1]);
        target.delete();
        final FileOutputStream outputStream = new FileOutputStream(target);

        final StorageConfiguration config = new StorageConfiguration(new File(args[0]));
        Storage.createStorage(config);
        final IStorage db = Storage.openStorage(new File(args[0]));
        Properties props = new Properties();
        props.setProperty(ConstructorProps.STORAGEPATH, target.getAbsolutePath());
        props.setProperty(ConstructorProps.RESOURCE, "shredded");
        db.createResource(new ResourceConfiguration(props, storage, revision, new TreeNodeFactory(),new NodeMetaPageFactory()));
        final ISession session = db.getSession(new SessionConfiguration("shredded", StandardSettings.KEY));

        final XMLSerializer serializer = new XMLSerializerBuilder(session, outputStream).build();
        serializer.call();

        session.close();
        outputStream.close();
        db.close();

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
        private final OutputStream mStream;

        /** Session to use. */
        private final ISession mSession;

        /** Versions to use. */
        private transient long[] mVersions;

        /** Node key of subtree to shredder. */
        private final long mNodeKey;

        /**
         * Constructor, setting the necessary stuff.
         * 
         * @param paramSession
         *            {@link ISession} to Serialize
         * @param paramStream
         *            {@link OutputStream}
         * @param paramVersions
         *            version(s) to Serialize
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
         *            {@link ISession}
         * @param paramNodeKey
         *            root node key of subtree to shredder
         * @param paramStream
         *            {@link OutputStream}
         * @param paramProperties
         *            {@link XMLSerializerProperties}
         * @param paramVersions
         *            version(s) to serialize
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
         * Setting the ids on nodes.
         * 
         * @param paramVersions
         *            to set
         * @return XMLSerializerBuilder reference.
         */
        public XMLSerializerBuilder setVersions(final long[] paramVersions) {
            mVersions = paramVersions;
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

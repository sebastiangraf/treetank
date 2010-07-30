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

package com.treetank.service.xml.shredder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.node.ENodes;
import com.treetank.settings.EFixed;
import com.treetank.utils.FastStack;
import com.treetank.utils.TypedValue;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * This class appends a given {@link XMLStreamReader} to a {@link IWriteTransaction}. The content of the
 * stream is added as a subtree.
 * Based on a boolean which identifies the point of insertion, the subtree is
 * either added as subtree or as rightsibling.
 * 
 * @author Marc Kramis, Seabix
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class XMLShredder implements Callable<Long> {
    
    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(XMLShredder.class));

    /** {@link IWriteTransaction}. */
    protected final transient IWriteTransaction mWtx;

    /** {@link XMLEventReader}. */
    protected final transient XMLEventReader mReader;

    /** Append as first child or not. */
    private final transient boolean mFirstChildAppend;

    /** File to shredder. */
    protected static File mFile;

    /** StAX parser used to check descendants. */
    protected static XMLEventReader mParser;

    /**
     * Normal constructor to invoke a shredding process on a existing {@link WriteTransaction}.
     * 
     * @param mWtx
     *            where the new XML Fragment should be placed
     * @param mReader
     *            of the XML Fragment
     * @param mAddAsFirstChild
     *            if the insert is occuring on a node in an existing tree. <code>false</code> is not possible
     *            when wtx is on root node.
     * @throws TreetankUsageException
     *             if insertasfirstChild && updateOnly is both true OR if wtx is
     *             not pointing to doc-root and updateOnly= true
     */
    public XMLShredder(final IWriteTransaction mWtx, final XMLEventReader mReader,
            final boolean mAddAsFirstChild)
        throws TreetankUsageException {
        this.mWtx = mWtx;
        this.mReader = mReader;
        this.mFirstChildAppend = mAddAsFirstChild;
    }

    /**
     * Invoking the shredder.
     * 
     * @throws Exception
     *             handling treetank exception 
     * @return revision
     *             return revision
     */
    public Long call() throws Exception {
        final long revision = mWtx.getRevisionNumber();
        insertNewContent();
        mWtx.commit();
        return revision;
    }

    /**
     * Insert new content.
     * 
     * @throws TreetankException
     *             handling treetank exception 
     */
    protected final void insertNewContent() throws TreetankException {
        try {

            FastStack<Long> leftSiblingKeyStack = new FastStack<Long>();

            leftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
            boolean firstElement = true;

            // Iterate over all nodes.
            while (mReader.hasNext()) {
                final XMLEvent event = mReader.nextEvent();
                switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    leftSiblingKeyStack =
                        addNewElement(firstElement, leftSiblingKeyStack, (StartElement)event);
                    firstElement = false;
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    leftSiblingKeyStack.pop();
                    mWtx.moveTo(leftSiblingKeyStack.peek());
                    break;

                case XMLStreamConstants.CHARACTERS:
                    leftSiblingKeyStack = addNewText(leftSiblingKeyStack, (Characters)event);
                    break;
                default:
                    // Node kind not known.
                }
            }
        } catch (final XMLStreamException mExc) {
            LOGWRAPPER.error(mExc);
            throw new TreetankIOException(mExc);
        }
    }

    /**
     * Add a new element node.
     * 
     * @param mFirstElement
     *            Is it the first element?
     * @param mLeftSiblingKeyStack
     *            Stack used to determine if the new element has to be inserted
     *            as a right sibling or as a new child (in the latter case is
     *            NULL on top of the stack).
     * @param mEvent
     *            The current event from the StAX parser.
     * @return the modified stack.
     * @throws TreetankException
     *             In case anything went wrong.
     */
    protected final FastStack<Long> addNewElement(final boolean mFirstElement,
        final FastStack<Long> mLeftSiblingKeyStack, final StartElement mEvent) throws TreetankException {
        long key;

        final QName name = mEvent.getName();

        if (mFirstElement && !mFirstChildAppend) {
            if (mWtx.getNode().getKind() == ENodes.ROOT_KIND) {
                throw new TreetankUsageException("Subtree can not be inserted as sibling of Root");
            }
            key = mWtx.insertElementAsRightSibling(name);
        } else {

            if (mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                key = mWtx.insertElementAsFirstChild(name);
            } else {
                key = mWtx.insertElementAsRightSibling(name);
            }
        }

        mLeftSiblingKeyStack.pop();
        mLeftSiblingKeyStack.push(key);
        mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());

        // Parse namespaces.
        for (final Iterator<?> it = mEvent.getNamespaces(); it.hasNext();) {
            final Namespace namespace = (Namespace)it.next();
            mWtx.insertNamespace(namespace.getNamespaceURI(), namespace.getPrefix());
            mWtx.moveTo(key);
        }

        // Parse attributes.
        for (final Iterator<?> it = mEvent.getAttributes(); it.hasNext();) {
            final Attribute attribute = (Attribute)it.next();
            mWtx.insertAttribute(attribute.getName(), attribute.getValue());
            mWtx.moveTo(key);
        }
        return mLeftSiblingKeyStack;
    }

    /**
     * Add a new text node.
     * 
     * @param mLeftSiblingKeyStack
     *            Stack used to determine if the new element has to be inserted
     *            as a right sibling or as a new child (in the latter case is
     *            NULL on top of the stack).
     * @param mEvent
     *            The current event from the StAX parser.
     * @return the modified stack.
     * @throws TreetankException
     *             In case anything went wrong.
     */
    protected final FastStack<Long> addNewText(final FastStack<Long> mLeftSiblingKeyStack,
        final Characters mEvent) throws TreetankException {
        final String text = mEvent.getData().trim();
        long key;
        final ByteBuffer textByteBuffer = ByteBuffer.wrap(TypedValue.getBytes(text));
        if (textByteBuffer.array().length > 0) {

            if (mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                key = mWtx.insertTextAsFirstChild(new String(textByteBuffer.array()));
            } else {
                key = mWtx.insertTextAsRightSibling(new String(textByteBuffer.array()));
            }

            mLeftSiblingKeyStack.pop();
            mLeftSiblingKeyStack.push(key);

        }
        return mLeftSiblingKeyStack;
    }

    /**
     * Main method.
     * 
     * @param mArgs
     *            Input and output files.
     * @throws Exception
     *             In case of any exception.
     */
    public static void main(final String... mArgs) throws Exception {
        if (mArgs.length != 2) {
            System.out.println("Usage: XMLShredder input.xml output.tnk");
            System.exit(1);
        }

        System.out.print("Shredding '" + mArgs[0] + "' to '" + mArgs[1] + "' ... ");
        final long time = System.currentTimeMillis();
        final File target = new File(mArgs[1]);
        Database.truncateDatabase(target);
        Database.createDatabase(new DatabaseConfiguration(target));
        final IDatabase db = Database.openDatabase(target);
        final ISession session = db.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        mFile = new File(mArgs[0]);
        final XMLEventReader reader = createReader(null);
        final XMLShredder shredder = new XMLShredder(wtx, reader, true);
        shredder.call();

        wtx.close();
        session.close();
        db.close();

        System.out.println(" done [" + (System.currentTimeMillis() - time) + "ms].");
    }

    /**
     * Create a StAX reader.
     * 
     * @param file
     *            File to shredder.
     * @return an XMLEventReader.
     * @throws IOException
     *             In case of any I/O error.
     * @throws XMLStreamException
     *             In case of any XML parser error.
     */
    public static synchronized XMLEventReader createReader(final File file) throws IOException,
        XMLStreamException {
        InputStream in;
        if (file == null) {
            in = new FileInputStream(mFile);
        } else {
            mFile = file;
            in = new FileInputStream(file);
        }
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        final XMLEventReader parser = factory.createXMLEventReader(in);
        return parser;
    }
}

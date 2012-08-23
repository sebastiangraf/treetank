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

package org.treetank.service.xml.shredder;

import static org.treetank.node.IConstants.NULL_NODE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.treetank.access.Database;
import org.treetank.access.NodeWriteTrx;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.access.conf.DatabaseConfiguration;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IDatabase;
import org.treetank.api.INodeWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.IStorage.IStorageFactory;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.ElementNode;
import org.treetank.node.IConstants;
import org.treetank.node.TreeNodeFactory;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.revisioning.IRevisioning.IRevisioningFactory;
import org.treetank.service.xml.StandardXMLSettings;
import org.treetank.utils.TypedValue;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class appends a given {@link XMLStreamReader} to a {@link IWriteTransaction}. The content of the
 * stream is added as a subtree.
 * Based on an enum which identifies the point of insertion, the subtree is
 * either added as first child or as right sibling.
 * 
 * @author Marc Kramis, Seabix
 * @author Sebastian Graf, University of Konstanz
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class XMLShredder implements Callable<Void> {

    /** {@link IWriteTransaction}. */
    protected final transient INodeWriteTrx mWtx;

    /** {@link XMLEventReader}. */
    protected transient XMLEventReader mReader;

    /** Append as first child or not. */
    protected transient EShredderInsert mFirstChildAppend;

    /** Determines if changes are going to be commit right after shredding. */
    private transient EShredderCommit mCommit;

    /**
     * {@link CountDownLatch} reference to allow other threads to wait for the
     * shredding to finish.
     */
    private transient CountDownLatch mLatch;

    /**
     * Normal constructor to invoke a shredding process on a existing {@link WriteTransaction}.
     * 
     * @param paramWtx
     *            where the new XML Fragment should be placed
     * @param paramReader
     *            of the XML Fragment
     * @param paramAddAsFirstChild
     *            if the insert is occuring on a node in an existing tree. <code>false</code> is not possible
     *            when wtx is on root node.
     * @throws TTUsageException
     *             if insertasfirstChild && updateOnly is both true OR if wtx is
     *             not pointing to doc-root and updateOnly= true
     */
    public XMLShredder(final INodeWriteTrx paramWtx, final XMLEventReader paramReader,
        final EShredderInsert paramAddAsFirstChild) throws TTException {
        this(paramWtx, paramReader, paramAddAsFirstChild, EShredderCommit.COMMIT);
    }

    /**
     * Normal constructor to invoke a shredding process on a existing {@link WriteTransaction}.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} where the new XML Fragment should be
     *            placed
     * @param paramReader
     *            {@link XMLEventReader} to parse the xml fragment, which should
     *            be inserted
     * @param paramAddAsFirstChild
     *            determines if the insert is occuring on a node in an existing
     *            tree. <code>false</code> is not possible when wtx is on root
     *            node
     * @param paramCommit
     *            determines if inserted nodes should be commited right
     *            afterwards
     * @throws TTUsageException
     *             if insertasfirstChild && updateOnly is both true OR if wtx is
     *             not pointing to doc-root and updateOnly= true
     */
    public XMLShredder(final INodeWriteTrx paramWtx, final XMLEventReader paramReader,
        final EShredderInsert paramAddAsFirstChild, final EShredderCommit paramCommit) throws TTException {
        if (paramWtx == null || paramReader == null || paramAddAsFirstChild == null || paramCommit == null) {
            throw new IllegalArgumentException("None of the constructor parameters may be null!");
        }
        mWtx = paramWtx;
        mReader = paramReader;
        mFirstChildAppend = paramAddAsFirstChild;
        mCommit = paramCommit;
        mLatch = new CountDownLatch(1);
        if (mWtx.getNode() == null) {
            final NodeDelegate nodeDel = new NodeDelegate(0, NULL_NODE, 0);
            mWtx.getPageWtx().createNode(
                new DocumentRootNode(nodeDel, new StructNodeDelegate(nodeDel, NULL_NODE, NULL_NODE,
                    NULL_NODE, 0)));
            mWtx.moveTo(org.treetank.node.IConstants.ROOT_NODE);
        }
    }

    /**
     * Invoking the shredder.
     * 
     * @throws TTException
     *             if any kind of Treetank exception which has occured
     * @return revision of file
     */
    @Override
    public Void call() throws TTException {
        insertNewContent();

        if (mCommit == EShredderCommit.COMMIT) {
            mWtx.commit();
        }
        return null;
    }

    /**
     * Insert new content based on a StAX parser {@link XMLStreamReader}.
     * 
     * @throws TTException
     *             if something went wrong while inserting
     */
    protected final void insertNewContent() throws TTException {
        try {
            Stack<Long> leftSiblingKeyStack = new Stack<Long>();

            leftSiblingKeyStack.push(NULL_NODE);
            boolean firstElement = true;
            int level = 0;
            QName rootElement = null;
            boolean endElemReached = false;
            StringBuilder sBuilder = new StringBuilder();

            // Iterate over all nodes.
            while (mReader.hasNext() && !endElemReached) {
                final XMLEvent event = mReader.nextEvent();

                switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    level++;
                    leftSiblingKeyStack = addNewElement(leftSiblingKeyStack, (StartElement)event);
                    if (firstElement) {
                        firstElement = false;
                        rootElement = event.asStartElement().getName();
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    level--;
                    if (level == 0 && rootElement != null
                        && rootElement.equals(event.asEndElement().getName())) {
                        endElemReached = true;
                    }
                    leftSiblingKeyStack.pop();
                    mWtx.moveTo(leftSiblingKeyStack.peek());
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (mReader.peek().getEventType() == XMLStreamConstants.CHARACTERS) {
                        sBuilder.append(event.asCharacters().getData().trim());
                    } else {
                        sBuilder.append(event.asCharacters().getData().trim());
                        leftSiblingKeyStack = addNewText(leftSiblingKeyStack, sBuilder.toString());
                        sBuilder = new StringBuilder();
                    }
                    break;
                default:
                    // Node kind not known.
                }
            }
        } catch (final XMLStreamException e) {
            throw new TTIOException(e);
        }
    }

    /**
     * Add a new element node.
     * 
     * @param paramLeftSiblingKeyStack
     *            stack used to determine if the new element has to be inserted
     *            as a right sibling or as a new child (in the latter case is
     *            NULL on top of the stack)
     * @param paramEvent
     *            the current event from the StAX parser
     * @return the modified stack
     * @throws TTException
     *             if adding {@link ElementNode} fails
     */
    protected final Stack<Long> addNewElement(final Stack<Long> paramLeftSiblingKeyStack,
        final StartElement paramEvent) throws TTException {
        assert paramLeftSiblingKeyStack != null && paramEvent != null;
        long key;

        final QName name = paramEvent.getName();

        if (mFirstChildAppend == EShredderInsert.ADDASRIGHTSIBLING) {
            if (mWtx.getNode().getKind() == IConstants.ROOT) {
                throw new TTUsageException("Subtree can not be inserted as sibling of Root");
            }
            key = mWtx.insertElementAsRightSibling(name);
            mFirstChildAppend = EShredderInsert.ADDASFIRSTCHILD;
        } else {
            if (paramLeftSiblingKeyStack.peek() == NULL_NODE) {
                key = mWtx.insertElementAsFirstChild(name);
            } else {
                key = mWtx.insertElementAsRightSibling(name);
            }
        }

        paramLeftSiblingKeyStack.pop();
        paramLeftSiblingKeyStack.push(key);
        paramLeftSiblingKeyStack.push(NULL_NODE);

        // Parse namespaces.
        for (final Iterator<?> it = paramEvent.getNamespaces(); it.hasNext();) {
            final Namespace namespace = (Namespace)it.next();
            mWtx.insertNamespace(new QName(namespace.getNamespaceURI(), "", namespace.getPrefix()));
            mWtx.moveTo(key);
        }

        // Parse attributes.
        for (final Iterator<?> it = paramEvent.getAttributes(); it.hasNext();) {
            final Attribute attribute = (Attribute)it.next();
            mWtx.insertAttribute(attribute.getName(), attribute.getValue());
            mWtx.moveTo(key);
        }
        return paramLeftSiblingKeyStack;
    }

    /**
     * Add a new text node.
     * 
     * @param paramLeftSiblingKeyStack
     *            stack used to determine if the new element has to be inserted
     *            as a right sibling or as a new child (in the latter case is
     *            NULL on top of the stack)
     * @param paramText
     *            the text string to add
     * @return the modified stack
     * @throws TTException
     *             if adding text fails
     */
    protected final Stack<Long>
        addNewText(final Stack<Long> paramLeftSiblingKeyStack, final String paramText) throws TTException {
        assert paramLeftSiblingKeyStack != null;
        final String text = paramText;
        long key;
        final ByteBuffer textByteBuffer = ByteBuffer.wrap(TypedValue.getBytes(text));
        if (textByteBuffer.array().length > 0) {

            if (paramLeftSiblingKeyStack.peek() == NULL_NODE) {
                key = mWtx.insertTextAsFirstChild(new String(textByteBuffer.array()));
            } else {
                key = mWtx.insertTextAsRightSibling(new String(textByteBuffer.array()));
            }

            paramLeftSiblingKeyStack.pop();
            paramLeftSiblingKeyStack.push(key);

        }
        return paramLeftSiblingKeyStack;
    }

    /**
     * Main method.
     * 
     * @param paramArgs
     *            input and output files
     * @throws Exception
     *             if any exception occurs
     */
    public static void main(final String... paramArgs) throws Exception {
        if (paramArgs.length != 2) {
            throw new IllegalArgumentException("Usage: XMLShredder input.xml output.tnk");
        }

        System.out.print("Shredding '" + paramArgs[0] + "' to '" + paramArgs[1] + "' ... ");
        final long time = System.currentTimeMillis();

        Injector injector = Guice.createInjector(new StandardXMLSettings());
        IStorageFactory storage = injector.getInstance(IStorageFactory.class);
        IRevisioningFactory revision = injector.getInstance(IRevisioningFactory.class);

        final File target = new File(paramArgs[1]);
        final DatabaseConfiguration config = new DatabaseConfiguration(target);
        Database.truncateDatabase(config);
        Database.createDatabase(config);
        final IDatabase db = Database.openDatabase(target);
        Properties props = new Properties();
        props.put(org.treetank.io.IConstants.DBFILE, target);
        props.put(org.treetank.io.IConstants.RESOURCE, "shredded");
        db.createResource(new ResourceConfiguration(props, 1, storage, revision, new TreeNodeFactory()));
        final ISession session = db.getSession(new SessionConfiguration("shredded", StandardSettings.KEY));
        final INodeWriteTrx wtx =
            new NodeWriteTrx(session, session.beginPageWriteTransaction(), HashKind.Rolling);
        // generating root node
        final NodeDelegate nodeDel = new NodeDelegate(0, NULL_NODE, 0);
        wtx.getPageWtx()
            .createNode(
                new DocumentRootNode(nodeDel, new StructNodeDelegate(nodeDel, NULL_NODE, NULL_NODE,
                    NULL_NODE, 0)));
        wtx.moveTo(org.treetank.node.IConstants.ROOT_NODE);

        final XMLEventReader reader = createFileReader(new File(paramArgs[0]));
        final XMLShredder shredder = new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();

        wtx.close();
        session.close();

        System.out.println(" done [" + (System.currentTimeMillis() - time) + "ms].");
    }

    /**
     * Create a new StAX reader on a file.
     * 
     * @param paramFile
     *            the XML file to parse
     * @return an {@link XMLEventReader}
     * @throws IOException
     *             if I/O operation fails
     * @throws XMLStreamException
     *             if any parsing error occurs
     */
    public static synchronized XMLEventReader createFileReader(final File paramFile) throws IOException,
        XMLStreamException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        final InputStream in = new FileInputStream(paramFile);
        return factory.createXMLEventReader(in);
    }

    /**
     * Create a new StAX reader on a string.
     * 
     * @param paramString
     *            the XML file as a string to parse
     * @return an {@link XMLEventReader}
     * @throws IOException
     *             if I/O operation fails
     * @throws XMLStreamException
     *             if any parsing error occurs
     */
    public static synchronized XMLEventReader createStringReader(final String paramString)
        throws IOException, XMLStreamException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        final InputStream in = new ByteArrayInputStream(paramString.getBytes());
        return factory.createXMLEventReader(in);
    }

    /**
     * Create a new StAX reader based on a List of {@link XMLEvent}s.
     * 
     * @param paramEvents
     *            {@link XMLEvent}s
     * @return an {@link XMLEventReader}
     * @throws IOException
     *             if I/O operation fails
     * @throws XMLStreamException
     *             if any parsing error occurs
     */
    public static synchronized XMLEventReader createListReader(final List<XMLEvent> paramEvents)
        throws IOException, XMLStreamException {
        if (paramEvents == null) {
            throw new IllegalArgumentException("paramEvents may not be null!");
        }
        return new ListEventReader(paramEvents);
    }

    /**
     * Get latch.
     * 
     * @return {@link CountDownLatch} reference
     */
    public final CountDownLatch getLatch() {
        return mLatch;
    }
}

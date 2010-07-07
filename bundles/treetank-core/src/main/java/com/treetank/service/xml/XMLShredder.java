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
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.WriteTransaction;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.settings.EFixed;
import com.treetank.settings.EXMLSerializing;
import com.treetank.utils.FastStack;
import com.treetank.utils.TypedValue;

/**
 * This class appends a given {@link XMLStreamReader} to a
 * {@link IWriteTransaction}. The content of the stream is added as a subtree.
 * Based on a boolean which identifies the point of insertion, the subtree is
 * either added as subtree or as rightsibling.
 * 
 * @author Marc Kramis, Seabix
 * @author Sebastian Graf, University of Konstanz
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLShredder implements Callable<Long> {

    private final IWriteTransaction mWtx;

    private final XMLEventReader mReader;

    private final boolean mFirstChildAppend;

    private final boolean mInsertOnlyModified;

    private static File mFile;

    private XMLEventReader mParser;

    /**
     * Normal constructor to invoke a shredding process on a existing
     * {@link WriteTransaction}
     * 
     * @param wtx
     *            where the new XML Fragment should be placed
     * @param reader
     *            of the XML Fragment
     * @param addAsFirstChild
     *            if the insert is occuring on a node in an existing tree.
     *            <code>false</code> is not possible when wtx is on root node.
     * @throws TreetankUsageException
     *             if insertasfirstChild && updateOnly is both true OR if wtx is
     *             not pointing to doc-root and updateOnly= true
     * @deprecated use constructor with XMLEventReader instead
     */
    @Deprecated
    public XMLShredder(final IWriteTransaction wtx,
            final XMLStreamReader reader, final boolean addAsFirstChild)
            throws TreetankUsageException {
        this(wtx, getReader(reader), addAsFirstChild, false);
    }

    /**
     * Static convencience method to provide still a stream interface for
     * treetank
     * 
     * @param streamReader
     *            for streams as an input
     * @return {@link XMLEventReader} as return since Treetank works with this
     *         internally
     * @throws TreetankUsageException
     */
    private final static XMLEventReader getReader(
            final XMLStreamReader streamReader) throws TreetankUsageException {
        XMLInputFactory fac = XMLInputFactory.newInstance();
        try {
            return fac.createXMLEventReader(streamReader);
        } catch (final XMLStreamException exc) {
            throw new TreetankUsageException(exc.toString());
        }
    }

    /**
     * Normal constructor to invoke a shredding process on a existing
     * {@link WriteTransaction}
     * 
     * @param wtx
     *            where the new XML Fragment should be placed
     * @param reader
     *            of the XML Fragment
     * @param addAsFirstChild
     *            if the insert is occuring on a node in an existing tree.
     *            <code>false</code> is not possible when wtx is on root node.
     * @throws TreetankUsageException
     *             if insertasfirstChild && updateOnly is both true OR if wtx is
     *             not pointing to doc-root and updateOnly= true
     */
    public XMLShredder(final IWriteTransaction wtx,
            final XMLEventReader reader, final boolean addAsFirstChild)
            throws TreetankUsageException {
        this(wtx, reader, addAsFirstChild, false);
    }

    /**
     * Normal constructor to invoke a shredding process on a existing
     * {@link WriteTransaction}
     * 
     * @param wtx
     *            where the new XML Fragment should be placed
     * @param reader
     *            of the XML Fragment
     * @param addAsFirstChild
     *            if the insert is occuring on a node in an existing tree.
     *            <code>false</code> is not possible when wtx is on root node.
     * @param updateOnly
     *            if true, only modified nodes are updated in the structure.
     *            Note that this method is time consuming and makes only use of
     *            the {@link EXMLSerializing#ID} tag when used.
     * @throws TreetankUsageException
     *             if insertasfirstChild && updateOnly is both true OR if wtx is
     *             not pointing to doc-root and updateOnly= true
     */
    public XMLShredder(final IWriteTransaction wtx,
            final XMLEventReader reader, final boolean addAsFirstChild,
            final boolean updateOnly) throws TreetankUsageException {
        mWtx = wtx;
        mReader = reader;
        if (updateOnly && wtx.getNode().getKind() != ENodes.ROOT_KIND) {
            throw new TreetankUsageException(
                    "WriteTransaction must point to doc-root at the beginning!");
        }
        mFirstChildAppend = addAsFirstChild;
        mInsertOnlyModified = updateOnly;
    }

    /**
     * Invoking the shredder.
     */
    public Long call() throws Exception {
        final long revision = mWtx.getRevisionNumber();

        if (mInsertOnlyModified) {
            updateOnly();
        } else {
            insertNewContent();
        }

        mWtx.commit();
        return revision;
    }

    /**
     * Update a shreddered file.
     * 
     * @throws TreetankException
     */
    private void updateOnly() throws TreetankException {
        try {
            // Setting the maxNodeKey for the compare-wtx.
            final long maxNodeKey = mWtx.getMaxNodeKey();

            /*
             * Setting up boolean-Stack. This stack is for holding the current
             * position to determine if an insertasright-sib should occure.
             */
            FastStack<Long> leftSiblingKeyStack = new FastStack<Long>();
            leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty());

            // Setup up of first element of the data.
            XMLEvent event = mReader.nextEvent();
            mWtx.moveToDocumentRoot();

            // If structure already exists, make a sync against the current
            // structure.
            if (maxNodeKey != 0) {
                // Find the start key for the update operation.
                long startkey = (Long) EFixed.ROOT_NODE_KEY
                        .getStandardProperty() + 1;
                while (!mWtx.moveTo(startkey)) {
                    startkey++;
                }

                // Is it the same node?
                boolean isSame = false;

                // Levels to go up to the parent after nodes were not the same.
                int levelsUp = 0;

                // Last position of mWtx where nodes were equal.
                long lastPosKey = 0;

                // Elements which have been inserted in the current subtree.
                int insertLevel = 0;

                // Level where the parser is in the file to shredder.
                int levelToParse = 0;

                // Insert node at the bottom (no closing tags occured until
                // now).
                boolean insertAtTop = true;

                // Has an element been inserted before?
                boolean insertedElement = false;

                // Last key in storage?
                boolean isLastNode = false;

                // Iterate over all nodes.
                do {
                    switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        System.out.println("TO SHREDDER: "
                                + ((StartElement) event).getName());
                        System.out.println("SHREDDERED: "
                                + mWtx.getQNameOfCurrentNode());

                        levelToParse++;
                        final long nodeKey = mWtx.getNode().getNodeKey();
                        boolean found = false;
                        boolean isRightsibling = false;
                        long keyMatches;
                        do {
                            /*
                             * Check if an element in the shreddered file on the
                             * same level equals the current element node.
                             */
                            found = checkElement((StartElement) event);

                            if (mWtx.getNode().getNodeKey() != nodeKey) {
                                isRightsibling = true;
                            }

                            keyMatches = mWtx.getNode().getNodeKey();

                            if (found && isRightsibling) {
                                /*
                                 * Root element of next subtree in shreddered
                                 * file matches so check all descendants. If
                                 * they match the node must be inserted.
                                 */
                                found = checkDescendants(levelToParse,
                                        (StartElement) event, true);
                                mWtx.moveTo(keyMatches);
                            }
                        } while (!found && (mWtx.moveToRightSibling()));
                        mWtx.moveTo(nodeKey);

                        /*
                         * If current node in the file which has to be
                         * shreddered is found in one of the right siblings of
                         * the current node nodes have been removed, otherwise
                         * it has to be inserted. If they match at the current
                         * position do nothing.
                         */
                        if (found && isRightsibling) {
                            /*
                             * If found in one of the rightsiblings in the
                             * current shreddered structure remove all nodes
                             * until the transaction points to the found node
                             * (keyMatches).
                             */
                            isSame = false;
                            insertedElement = false;

                            do {
                                mWtx.remove();
                                leftSiblingKeyStack.pop();
                                leftSiblingKeyStack.push(mWtx.getNode()
                                        .getNodeKey());
                            } while (mWtx.moveToRightSibling()
                                    && mWtx.getNode().getNodeKey() != keyMatches);
                            // Move to parent if there is no former right
                            // sibling.
                            if (!((AbsStructNode) mWtx.getNode())
                                    .hasRightSibling()) {
                                mWtx.moveToParent();
                                leftSiblingKeyStack.pop();
                                leftSiblingKeyStack.push(mWtx.getNode()
                                        .getNodeKey());
                                leftSiblingKeyStack
                                        .push((Long) EFixed.NULL_NODE_KEY
                                                .getStandardProperty());
                            }
                            break;
                        } else if (!found) {
                            /*
                             * Add node if it's either not found among right
                             * siblings (and the cursor on the shreddered file
                             * is on a right sibling) or if it's not found in
                             * the structure and it is a new last right sibling.
                             */
                            isSame = false;
                            insertedElement = true;

                            if (insertAtTop) {
                                mWtx.moveToParent();

                                // Update stack.
                                // Remove NULL.
                                leftSiblingKeyStack.pop();
                                leftSiblingKeyStack
                                        .push((Long) EFixed.NULL_NODE_KEY
                                                .getStandardProperty());

                                leftSiblingKeyStack = addNewElement(false,
                                        leftSiblingKeyStack,
                                        (StartElement) event);

                                insertLevel++;
                                break;
                            } else
                            /*
                             * Move to last position before nodes were unequal
                             * if it's the "root" node of the subtree to insert
                             * (in case of a whole subtree).
                             */
                            if (levelsUp > 0) {
                                mWtx.moveTo(lastPosKey);

                                // Move up levels to the right parent.
                                for (int i = 0; i < levelsUp - 1; i++) {
                                    mWtx.moveToParent();
                                }

                                /*
                                 * Make sure that it's inserted as a right
                                 * sibling if the transaction has move to at
                                 * least one parent before.
                                 */
                                if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                                        .getStandardProperty()) {
                                    leftSiblingKeyStack.pop();
                                }

                                // Insert new node as right sibling.
                                // Push dummy on top.
                                leftSiblingKeyStack.push(0L);
                                leftSiblingKeyStack = addNewElement(false,
                                        leftSiblingKeyStack,
                                        (StartElement) event);

                                levelsUp = 0;
                                insertLevel++;
                                break;
                            } else if (insertLevel != -1) {
                                // After a first insert.
                                insertLevel++;

                                leftSiblingKeyStack = addNewElement(false,
                                        leftSiblingKeyStack,
                                        (StartElement) event);
                                break;
                            }
                        } else if (found) {
                            // Nodes are the same.
                            levelsUp = 0;
                            isSame = true;
                            insertedElement = false;
                            System.out.println("FOUND: "
                                    + mWtx.getQNameOfCurrentNode()
                                    + mWtx.getNode().getNodeKey());

                            // Update last position key.
                            lastPosKey = mWtx.getNode().getNodeKey();

                            // Move transaction.
                            if (((ElementNode) mWtx.getNode()).hasFirstChild()) {
                                insertAtTop = true;
                                // Update stack.
                                if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                                        .getStandardProperty()) {
                                    // Remove NULL.
                                    leftSiblingKeyStack.pop();
                                }
                                mWtx.moveToFirstChild();
                            } else if (((ElementNode) mWtx.getNode())
                                    .hasRightSibling()) {
                                insertAtTop = false;
                                // Empty element.
                                // Update stack.
                                if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                                        .getStandardProperty()) {
                                    // Remove NULL.
                                    leftSiblingKeyStack.pop();
                                }
                                /*
                                 * Remove element (the tag must have been
                                 * closed, thus remove it from the stack!).
                                 */
                                leftSiblingKeyStack.pop();
                                mWtx.moveToRightSibling();
                            } else if (mWtx.getNode().hasParent()) {
                                insertAtTop = false;

                                // Is it the last node key in storage?
                                if (mWtx.getMaxNodeKey() == mWtx.getNode()
                                        .getNodeKey()) {
                                    isLastNode = true;
                                }

                                // Update last position key.
                                lastPosKey = mWtx.getNode().getNodeKey();

                                if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                                        .getStandardProperty()) {
                                    leftSiblingKeyStack.pop();
                                }

                                if (!leftSiblingKeyStack.empty()) {
                                    leftSiblingKeyStack.pop();
                                    do {
                                        long key = mWtx.getNode().getNodeKey();
                                        if (!leftSiblingKeyStack.empty()) {
                                            mWtx.moveTo(leftSiblingKeyStack
                                                    .peek());
                                        }
                                        System.out.println("NAME: "
                                                + mWtx.getQNameOfCurrentNode());
                                        mWtx.moveTo(key);
                                        if (!leftSiblingKeyStack.empty()) {
                                            leftSiblingKeyStack.pop();
                                        }
                                        mWtx.moveToParent();
                                    } while (!((AbsStructNode) mWtx.getNode())
                                            .hasRightSibling()
                                            && !leftSiblingKeyStack.empty());
                                    mWtx.moveToRightSibling();
                                }
                            }

                            // Update stack.
                            leftSiblingKeyStack.push(mWtx.getNode()
                                    .getNodeKey());

                            if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                                // Update stack.
                                leftSiblingKeyStack
                                        .push((Long) EFixed.NULL_NODE_KEY
                                                .getStandardProperty());
                            }

                            break;
                        }
                    case XMLStreamConstants.CHARACTERS:
                        final String text = ((Characters) event).getData()
                                .trim();
                        if (mWtx.getNode().getKind() == ENodes.TEXT_KIND
                                && mWtx.getValueOfCurrentNode().equals(text)) {
                            levelsUp = 0;
                            isSame = true;
                            insertedElement = false;
                            lastPosKey = mWtx.getNode().getNodeKey();
                            leftSiblingKeyStack.pop();

                            // Move to a parent which has the next right sibling
                            // in pre order.
                            while (!((AbsStructNode) mWtx.getNode())
                                    .hasRightSibling()) {
                                // Move to parent element node.
                                mWtx.moveToParent();

                                long key = mWtx.getNode().getNodeKey();
                                mWtx.moveTo(leftSiblingKeyStack.peek());
                                System.out.println("NAME: "
                                        + mWtx.getQNameOfCurrentNode());
                                mWtx.moveTo(key);

                                // Update stack.
                                // Remove text node or parent nodes which have
                                // no right sibl.
                                leftSiblingKeyStack.pop();
                            }
                            mWtx.moveToRightSibling();

                            // Update stack.
                            leftSiblingKeyStack.push(mWtx.getNode()
                                    .getNodeKey());

                            if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                                leftSiblingKeyStack
                                        .push((Long) EFixed.NULL_NODE_KEY
                                                .getStandardProperty());
                            }
                        } else {
                            final ByteBuffer textByteBuffer = ByteBuffer
                                    .wrap(TypedValue.getBytes(text));
                            if (textByteBuffer.array().length > 0) {
                                boolean foundNode = false;
                                /*
                                 * If a text node on it's own (without a
                                 * previous element node has been inserted) move
                                 * back to the last position key and insert as a
                                 * right sibling.
                                 */
                                if (levelsUp > 0 && !insertedElement) {
                                    mWtx.moveTo(lastPosKey);

                                    // Move up levels to the right parent.
                                    for (int i = 0; i < levelsUp - 1; i++) {
                                        mWtx.moveToParent();
                                    }

                                    /*
                                     * Make sure that it's inserted as a right
                                     * sibling if the transaction has move to at
                                     * least one parent before.
                                     */
                                    if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                                            .getStandardProperty()) {
                                        leftSiblingKeyStack.pop();
                                    }

                                    // Insert new node as right sibling.
                                    // Push dummy on top.
                                    leftSiblingKeyStack.push(0L);

                                    // Just to be sure it has the right value.
                                    levelsUp = 0;
                                } else if (insertAtTop) {
                                    lastPosKey = mWtx.getNode().getNodeKey();
                                    mWtx.moveToParent();

                                    // Update stack.
                                    // Remove NULL.
                                    leftSiblingKeyStack.pop();

                                    if (levelToParse == 1) {
                                        // Child of root element level.
                                        // Remove node.
                                        leftSiblingKeyStack.pop();
                                    }
                                    leftSiblingKeyStack
                                            .push((Long) EFixed.NULL_NODE_KEY
                                                    .getStandardProperty());
                                } else {
                                    /*
                                     * Precondition: Transaction cursor moves
                                     * down in the tree so a start element has
                                     * been read before the characters event.
                                     * 
                                     * Check if next event equals node on
                                     * transaction points to.
                                     */
                                    final XMLEvent xmlEvent = skipWhitespaces();
                                    final long nodekey = mWtx.getNode()
                                            .getNodeKey();
                                    System.out.println(mWtx
                                            .getQNameOfCurrentNode());
                                    switch (mReader.peek().getEventType()) {
                                    case XMLStreamConstants.START_ELEMENT:
                                        System.out
                                                .println(((StartElement) xmlEvent)
                                                        .getName());
                                        foundNode = checkElement((StartElement) xmlEvent);
                                        break;
                                    case XMLStreamConstants.CHARACTERS:
                                        if (mWtx.getNode().getKind() == ENodes.TEXT_KIND
                                                && mWtx.getValueOfCurrentNode() == ((Characters) xmlEvent)
                                                        .getData().trim()) {
                                            foundNode = true;
                                        }
                                    }
                                    mWtx.moveTo(nodekey);

                                    if (foundNode) {
                                        // Go back to parent.
                                        mWtx.moveTo(lastPosKey);

                                        // Update stack.
                                        if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                                                .getStandardProperty()) {
                                            leftSiblingKeyStack.pop();
                                        }
                                        leftSiblingKeyStack.pop();
                                        leftSiblingKeyStack
                                                .push((Long) EFixed.NULL_NODE_KEY
                                                        .getStandardProperty());
                                    }
                                }

                                isSame = false;

                                // Insert text.
                                leftSiblingKeyStack = addNewText(
                                        leftSiblingKeyStack, (Characters) event);

                                if (foundNode) {
                                    // Move transaction to right sibling and
                                    // update stack.
                                    mWtx.moveToRightSibling();
                                    leftSiblingKeyStack.pop();
                                    leftSiblingKeyStack.push(mWtx.getNode()
                                            .getNodeKey());
                                } else {
                                    /*
                                     * If right sibling of current node is the
                                     * next event move cursor and update stack.
                                     */
                                    if (((AbsStructNode) mWtx.getNode())
                                            .hasRightSibling()) {
                                        checkRightSibling(leftSiblingKeyStack,
                                                true, insertAtTop);
                                    }
                                }

                                if (insertAtTop) {
                                    insertAtTop = false;
                                }
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        levelToParse--;

                        if (!isSame) {
                            /*
                             * If insertLevel == 0 move to topmost stack element
                             * which means .
                             */
                            if (insertLevel != -1) {
                                insertLevel--;
                                levelsUp = 0;
                                if (!leftSiblingKeyStack.empty()) {
                                    leftSiblingKeyStack.pop();

                                    if (!leftSiblingKeyStack.empty()) {
                                        mWtx.moveTo(leftSiblingKeyStack.peek());
                                    }
                                }
                                System.out.println("END ELEM: "
                                        + mWtx.getQNameOfCurrentNode());

                                /*
                                 * Insert occured at the top of the tree, thus
                                 * no closed tags were parsed so far. Move to
                                 * the right sibling of the root node of the
                                 * inserted node only if the new event is equal
                                 * to the right sibling.
                                 */
                                if (insertAtTop) {
                                    checkRightSibling(leftSiblingKeyStack,
                                            false, insertAtTop);
                                } else if (insertLevel == 0) {
                                    /*
                                     * Check if move to element before any
                                     * inserts occured would move to a node
                                     * which equals the next node of the next
                                     * event. If not move back to current top
                                     * element on stack (which is the parent of
                                     * the currently inserted node).
                                     */
                                    // if (!checkRightSibling(
                                    // leftSiblingKeyStack,
                                    // false,
                                    // insertAtTop)) {
                                    final XMLEvent xmlEvent = skipWhitespaces();

                                    if (xmlEvent.getEventType() != XMLStreamConstants.END_ELEMENT) {
                                        if (!leftSiblingKeyStack.empty()) {
                                            // Move to the node before an insert
                                            // occured.
                                            final long keyOnStack = leftSiblingKeyStack
                                                    .peek();
                                            final long keyInShreddered = mWtx
                                                    .getNode().getNodeKey();
                                            leftSiblingKeyStack.pop();

                                            if (!leftSiblingKeyStack.empty()) {
                                                mWtx.moveTo(leftSiblingKeyStack
                                                        .peek());
                                                switch (xmlEvent.getEventType()) {
                                                case XMLStreamConstants.CHARACTERS:
                                                    final String data = ((Characters) xmlEvent)
                                                            .getData().trim();

                                                    if (!(!data.isEmpty()
                                                            && mWtx.getNode()
                                                                    .getKind() == ENodes.TEXT_KIND && mWtx
                                                            .getValueOfCurrentNode()
                                                            .equals(data))) {
                                                        mWtx.moveTo(keyInShreddered);
                                                        leftSiblingKeyStack
                                                                .push(keyOnStack);
                                                    }
                                                    break;
                                                case XMLStreamConstants.START_ELEMENT:
                                                    if (!checkElement((StartElement) xmlEvent)) {
                                                        mWtx.moveTo(keyInShreddered);
                                                        leftSiblingKeyStack
                                                                .push(keyOnStack);
                                                    }
                                                    break;
                                                }
                                            } else {
                                                // Stack is empty, so the
                                                // node(s) was/were inserted
                                                // somewhere at the top of the
                                                // XML file.
                                                /*
                                                 * Check if right sibling after
                                                 * an insert equals next event.
                                                 */
                                                if (mWtx.moveToRightSibling()) {
                                                    if (!checkElement((StartElement) xmlEvent)) {
                                                        mWtx.moveToLeftSibling();
                                                    }
                                                } else {
                                                    /*
                                                     * Move to a parent which
                                                     * has the next right
                                                     * sibling in pre order.
                                                     */
                                                    while (!((AbsStructNode) mWtx
                                                            .getNode())
                                                            .hasRightSibling()) {
                                                        // Move to parent
                                                        // element node.
                                                        mWtx.moveToParent();
                                                    }
                                                }

                                                leftSiblingKeyStack
                                                        .push(mWtx.getNode()
                                                                .getNodeKey());
                                            }
                                        }
                                    }
                                    // }
                                }
                            }
                        } else {
                            insertLevel = 0;
                            levelsUp++;
                        }
                        // if (insertAtTop) {
                        // insertAtTop = false;
                        // }
                        break;
                    }

                    // Parsing the next event.
                    event = mReader.nextEvent();
                } while (mReader.hasNext());

                mReader.close();
            }
            // If no content is in the XML, a normal insertNewContent is
            // executed.
            else {
                insertNewContent();
            }

        } catch (final XMLStreamException exc1) {
            throw new TreetankIOException(exc1);
        } catch (final IOException exc2) {
            throw new TreetankIOException(exc2);
        }

    }

    /**
     * Compare right sibling of current node with the next event.
     * 
     * @param leftSiblingKeyStack
     *            The Stack which has to be modified.
     * @throws XMLStreamException
     *             In case the xml parser encounters an error.
     */
    // TODO: BUG!
    private final boolean checkRightSibling(
            final FastStack<Long> leftSiblingKeyStack, final boolean text,
            final boolean insertAtTop) throws XMLStreamException {
        boolean retVal = false;
        mWtx.moveToRightSibling();
        final XMLEvent xmlEvent = skipWhitespaces();

        switch (xmlEvent.getEventType()) {
        case XMLStreamConstants.CHARACTERS:
            final String data = ((Characters) xmlEvent).getData().trim();

            if (!data.isEmpty() && mWtx.getNode().getKind() == ENodes.TEXT_KIND
                    && mWtx.getValueOfCurrentNode().equals(data)) {
                retVal = true;
            } else {
                mWtx.moveToLeftSibling();
            }
            break;
        case XMLStreamConstants.START_ELEMENT:
            if (checkElement((StartElement) xmlEvent)) {
                retVal = true;
            } else {
                mWtx.moveToLeftSibling();
            }
            break;
        }

        // Update stack.
        // Remove inserted text node.
        if (text && !insertAtTop) {
            leftSiblingKeyStack.pop();
        }

        // Remove NULL.
        if (!leftSiblingKeyStack.empty()) {
            if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty()) {
                leftSiblingKeyStack.pop();
            }
        }

        if (!leftSiblingKeyStack.empty()) {
            if (leftSiblingKeyStack.peek() != mWtx.getNode().getNodeKey()) {
                leftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
            }
        } else {
            leftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
        }

        return retVal;
    }

    /**
     * Skip all whitespace events between nodes.
     * 
     * @throws XMLStreamException
     *             In case of any error while parsing the file to shredder into
     *             an existing storage.
     */
    private final XMLEvent skipWhitespaces() throws XMLStreamException {
        // Check if next event equals node on top of stack.
        // Ignore all whitespace between elements.
        while (mReader.peek().getEventType() == XMLStreamConstants.CHARACTERS
                && (((Characters) mReader.peek()).isIgnorableWhiteSpace() || ((Characters) mReader
                        .peek()).isWhiteSpace())) {
            mReader.nextEvent();
        }

        return mReader.peek();
    }

    /**
     * Check if descendants match.
     * 
     * @param levelToParse
     *            The level on which the currently parsed node of the parser,
     *            which parses the file to shredder currently is.
     * @param elem
     *            The start element where the StAX parser currently is.
     * @param first
     *            Determines if it is the first call the method is invoked (a
     *            new StAX parser
     * @return true if they match, otherwise false.
     * @throws XMLStreamException
     *             In case of any streamining exception in the source document.
     * @throws IOException
     *             In case of any I/O exception while opening the target file.
     */
    private final boolean checkDescendants(final int levelToParse,
            final StartElement elem, final boolean first)
            throws XMLStreamException, IOException {
        boolean found = false;
        final long key = mWtx.getNode().getNodeKey();

        // Setup stack.
        final FastStack<Long> stack = new FastStack<Long>();

        if (first) {
            /*
             * Setup new StAX parser and move it to the node, where the current
             * StAX parser currently is.
             */
            stack.push((Long) EFixed.NULL_NODE_KEY.getStandardProperty());
            int level = 0;
            boolean foundParsedElement = false;
            mParser = createReader(null);
            while (mParser.hasNext() && !foundParsedElement) {
                final XMLEvent xmlEvent = mParser.nextEvent();
                switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    level++;

                    if (level == levelToParse
                            && checkStAXElement(mParser, elem)) {
                        // Found corresponding start element.
                        foundParsedElement = true;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    level--;
                    break;
                }
            }
        }

        // Move cursor.
        boolean moved = false;
        if (stack.peek() == (Long) EFixed.NULL_NODE_KEY.getStandardProperty()) {
            moved = mWtx.moveToFirstChild();
        } else {
            moved = mWtx.moveToRightSibling();
        }

        System.out.println(mWtx.getQNameOfCurrentNode().getLocalPart());

        if (moved) {
            final XMLEvent xmlEvent = mParser.nextEvent();
            switch (xmlEvent.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                // Update stack.
                if (stack.peek() == (Long) EFixed.NULL_NODE_KEY
                        .getStandardProperty()) {
                    stack.pop();
                }
                stack.push(mWtx.getNode().getNodeKey());
                stack.push((Long) EFixed.NULL_NODE_KEY.getStandardProperty());

                found = checkElement((StartElement) xmlEvent);
                break;
            case XMLStreamConstants.CHARACTERS:
                final String text = ((Characters) xmlEvent).getData().trim();

                if (!text.isEmpty()) {
                    // Update stack.
                    stack.pop();
                    stack.push(mWtx.getNode().getNodeKey());

                    if (mWtx.getNode().getKind() == ENodes.TEXT_KIND) {
                        found = ((Characters) xmlEvent).getData().equals(
                                mWtx.getValueOfCurrentNode());
                    }
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                stack.pop();
                mWtx.moveTo(stack.peek());
                break;
            }
        } else {
            found = moved;
        }

        if (found) {
            checkDescendants(levelToParse, elem, false);
        } else {
            mWtx.moveTo(key);
        }

        return found;
    }

    /**
     * Check if start element of two StAX parsers match.
     * 
     * @param parseToSameEvent
     *            Event to check against the position of the current StAX
     *            parser.
     * @param elem
     *            Event of the StAX parser, where it is currently.
     * @return True if start elements match.
     * @throws XMLStreamException
     */
    private final boolean checkStAXElement(
            final XMLEventReader parseToSameEvent, final StartElement elem)
            throws XMLStreamException {
        boolean retVal = false;
        final XMLEvent xmlEvent = parseToSameEvent.peek();
        if (xmlEvent.getEventType() == XMLStreamConstants.START_ELEMENT
                && ((StartElement) xmlEvent).getName().equals(elem.getName())) {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Insert new content.
     * 
     * @throws TreetankException
     */
    private final void insertNewContent() throws TreetankException {
        try {

            FastStack<Long> leftSiblingKeyStack = new FastStack<Long>();

            leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty());
            boolean firstElement = true;

            // Iterate over all nodes.
            while (mReader.hasNext()) {

                final XMLEvent event = mReader.nextEvent();
                switch (event.getEventType()) {

                case XMLStreamConstants.START_ELEMENT:
                    leftSiblingKeyStack = addNewElement(firstElement,
                            leftSiblingKeyStack, (StartElement) event);
                    firstElement = false;
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    leftSiblingKeyStack.pop();
                    mWtx.moveTo(leftSiblingKeyStack.peek());
                    break;

                case XMLStreamConstants.CHARACTERS:
                    leftSiblingKeyStack = addNewText(leftSiblingKeyStack,
                            (Characters) event);
                    break;
                }
            }
        } catch (final XMLStreamException exc1) {
            throw new TreetankIOException(exc1);
        }
    }

    private final FastStack<Long> addNewElement(final boolean firstElement,
            final FastStack<Long> leftSiblingKeyStack, final StartElement event)
            throws TreetankException {
        long key;

        final QName name = event.getName();

        if (firstElement && !mFirstChildAppend) {
            if (mWtx.getNode().getKind() == ENodes.ROOT_KIND) {
                throw new TreetankUsageException(
                        "Subtree can not be inserted as sibling of Root");
            }
            key = mWtx.insertElementAsRightSibling(name);
        } else {

            if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty()) {
                key = mWtx.insertElementAsFirstChild(name);
            } else {
                key = mWtx.insertElementAsRightSibling(name);
            }
        }

        leftSiblingKeyStack.pop();
        leftSiblingKeyStack.push(key);
        leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                .getStandardProperty());

        // Parse namespaces.
        for (Iterator<?> it = event.getNamespaces(); it.hasNext();) {
            final Namespace namespace = (Namespace) it.next();
            mWtx.insertNamespace(namespace.getNamespaceURI(),
                    namespace.getPrefix());
            mWtx.moveTo(key);
        }

        // Parse attributes.
        for (Iterator<?> it = event.getAttributes(); it.hasNext();) {
            final Attribute attribute = (Attribute) it.next();
            mWtx.insertAttribute(attribute.getName(), attribute.getValue());
            mWtx.moveTo(key);
        }
        return leftSiblingKeyStack;
    }

    private final FastStack<Long> addNewText(
            final FastStack<Long> leftSiblingKeyStack, final Characters event)
            throws TreetankException {
        final String text = event.getData().trim();
        long key;
        final ByteBuffer textByteBuffer = ByteBuffer.wrap(TypedValue
                .getBytes(text));
        if (textByteBuffer.array().length > 0) {

            if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty()) {
                key = mWtx.insertTextAsFirstChild(new String(textByteBuffer
                        .array()));
            } else {
                key = mWtx.insertTextAsRightSibling(new String(textByteBuffer
                        .array()));
            }

            leftSiblingKeyStack.pop();
            leftSiblingKeyStack.push(key);

        }
        return leftSiblingKeyStack;
    }

    /**
     * Check if current element matches the element in the shreddered file.
     * 
     * @param event
     *            StartElement event, from the XML file to shredder.
     * @return true if they are equal, false otherwise.
     */
    private final boolean checkElement(final StartElement event) {
        boolean retVal = false;
        final long nodeKey = mWtx.getNode().getNodeKey();

        // Matching element names?
        if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND
                && mWtx.getQNameOfCurrentNode().equals(event.getName())) {
            // Check if atts and namespaces are the same.

            // Check attributes.
            boolean foundAtts = false;
            boolean hasAtts = false;
            for (Iterator<?> it = event.getAttributes(); it.hasNext();) {
                hasAtts = true;
                final Attribute attribute = (Attribute) it.next();
                for (int i = 0, attCount = ((ElementNode) mWtx.getNode())
                        .getAttributeCount(); i < attCount; i++) {
                    mWtx.moveToAttribute(i);
                    if (attribute.getName()
                            .equals(mWtx.getQNameOfCurrentNode())
                            && attribute.getValue().equals(
                                    mWtx.getValueOfCurrentNode())) {
                        foundAtts = true;
                        mWtx.moveTo(nodeKey);
                        break;
                    }
                    mWtx.moveTo(nodeKey);
                }

                if (!foundAtts) {
                    break;
                }
            }
            if (!hasAtts) {
                foundAtts = true;
            }

            // Check namespaces.
            boolean foundNamesps = false;
            boolean hasNamesps = false;
            for (Iterator<?> namespIt = event.getNamespaces(); namespIt
                    .hasNext();) {
                hasNamesps = true;
                final Namespace namespace = (Namespace) namespIt.next();
                for (int i = 0, namespCount = ((ElementNode) mWtx.getNode())
                        .getNamespaceCount(); i < namespCount; i++) {
                    mWtx.moveToNamespace(i);
                    if (namespace.getNamespaceURI().equals(
                            mWtx.nameForKey(mWtx.getNode().getURIKey()))
                            && namespace.getPrefix()
                                    .equals(mWtx.nameForKey(mWtx.getNode()
                                            .getNameKey()))) {
                        foundNamesps = true;
                        mWtx.moveTo(nodeKey);
                        break;
                    }
                    mWtx.moveTo(nodeKey);
                }

                if (!foundNamesps) {
                    break;
                }
            }
            if (!hasNamesps) {
                foundNamesps = true;
            }

            // Check if atts and namespaces are the same.
            if (foundAtts && foundNamesps) {
                retVal = true;
            } else {
                retVal = false;
            }
        }

        return retVal;
    }

    /**
     * Main method.
     * 
     * @param args
     *            Input and output files.
     * @throws Exception
     *             In case of any exception.
     */
    public static void main(String... args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.out
                    .println("Usage: XMLShredder input.xml output.tnk [true/false]");
            System.exit(1);
        }

        System.out.print("Shredding '" + args[0] + "' to '" + args[1]
                + "' ... ");
        long time = System.currentTimeMillis();
        final File target = new File(args[1]);

        if (args.length == 2 || "false".equals(args[2])) {
            Database.truncateDatabase(target);
        }
        Database.createDatabase(new DatabaseConfiguration(target));
        final IDatabase db = Database.openDatabase(target);
        final ISession session = db.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        mFile = new File(args[0]);
        final XMLEventReader reader = createReader(null);
        boolean updateOnly = false;
        if (args.length == 3) {
            updateOnly = Boolean.parseBoolean(args[2]);
        }
        final XMLShredder shredder = new XMLShredder(wtx, reader, true,
                updateOnly);
        shredder.call();

        wtx.close();
        session.close();
        db.close();

        System.out.println(" done [" + (System.currentTimeMillis() - time)
                + "ms].");
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
    public static XMLEventReader createReader(final File file)
            throws IOException, XMLStreamException {
        InputStream in;
        if (file == null) {
            in = new FileInputStream(mFile);
        } else {
            in = new FileInputStream(file);
        }
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        final XMLEventReader parser = factory.createXMLEventReader(in);
        return parser;
    }
}

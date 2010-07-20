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
 */

package com.treetank.service.xml.shredder;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import com.treetank.utils.FastStack;
import com.treetank.utils.LogHelper;

/**
 * This class appends a given {@link XMLStreamReader} to a
 * {@link IWriteTransaction}. The content of the stream is added as a subtree.
 * Based on a boolean which identifies the point of insertion, the subtree is
 * either added as subtree or as rightsibling. Only updated nodes are inserted
 * or removed.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLUpdateShredder extends XMLShredder implements
        Callable<Long> {

    // ===================== LOGGER =======================
    /** Logger. */
    private static final Log LOGGER = LogFactory
            .getLog(XMLUpdateShredder.class);

    // ===================== Initial setup ================

    /** Determines if the nodes match or not. */
    private static boolean isSame = false;

    /** Levels to go up to the parent after nodes are not equal (insert). */
    private static int levelsUp = 0;

    /** Last position of mWtx before the current. */
    private static long lastPosKey = 0;

    /** Level in the current subtree, which is going to be inserted. */
    private static int insertLevel = 0;

    /** Level where the parser is in the file to shredder. */
    private static int levelInToShredder;

    /** Level where the cursor is in the shreddered file. */
    private static int levelInShreddered;

    /** Insert node at the top of a subtree. */
    private static boolean insertAtTop = true;

    /** Determines if an element has been inserted immediately before. */
    private static boolean insertedElement = false;

    /**
     * Levels to move up after inserts at some level and further inserts on a
     * level above the current level.
     */
    private static int levelsUpAfterInserts = 0;

    /** Determines if the cursor has to move up some levels during to inserts. */
    private static boolean moveUp;

    /** Determines if a node or nodes have been deleted immediately before. */
    private static boolean removed;

    /** Node key. */
    private static long nodeKey;

    /** Determines if a node is found in the Treetank storage or not. */
    private static boolean found;

    /**
     * This stack is for holding the current position to determine if an
     * insertAsRightSibling() or insertAsFirstChild() should occure.
     */
    private static FastStack<Long> leftSiblingKeyStack;

    /**
     * Determines if it's a right sibling from the currently parsed node, where
     * the parsed node and the node in the Treetank storage match.
     */
    private static boolean isRightSibling;

    /**
     * The key of the node, when the nodes are equal if at all (used to check
     * right siblings and therefore if nodes have been deleted).
     */
    private static long keyMatches;

    /** Maximum node key in revision. */
    private static long maxNodeKey;

    /** Determines if it's the last node or not. */
    private static boolean isLastNode;

    /** Determines if an insert occured. */
    private static boolean insert;

    /**
     * Normal constructor to invoke a shredding process on a existing
     * {@link WriteTransaction}
     * 
     * @param wtx
     *            {@link IWriteTransaction} where the new XML Fragment should be
     *            placed.
     * @param reader
     *            {@link XMLEventReader} (StAX parser) of the XML Fragment.
     * @param addAsFirstChild
     *            If the insert is occuring on a node in an existing tree.
     *            <code>false</code> is not possible when wtx is on root node.
     * @throws TreetankUsageException
     *             If insertasfirstChild && updateOnly is both true OR if wtx is
     *             not pointing to doc-root and updateOnly= true
     * @throws TreetankIOException
     *             If Treetank cannot access node keys.
     * 
     */
    public XMLUpdateShredder(final IWriteTransaction wtx,
            final XMLEventReader reader, final boolean addAsFirstChild)
            throws TreetankUsageException, TreetankIOException {
        super(wtx, reader, addAsFirstChild);
        if (wtx.getNode().getKind() != ENodes.ROOT_KIND) {
            throw new TreetankUsageException(
                    "WriteTransaction must point to doc-root at the beginning!");
        }
        maxNodeKey = mWtx.getMaxNodeKey();
    }

    /**
     * Invoking the shredder.
     */
    public Long call() throws TreetankException {
        final long revision = mWtx.getRevisionNumber();
        updateOnly();
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
            final long start = System.currentTimeMillis();
            final LogHelper log = new LogHelper(LOGGER);
            log.info("Start... ");

            // Setting up boolean-Stack.
            leftSiblingKeyStack = new FastStack<Long>();
            leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty());

            // Setup up of first element of the data.
            XMLEvent event = mReader.nextEvent();
            mWtx.moveToDocumentRoot();

            // Get root element.
            QName rootElem;
            if (event.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                event = mReader.nextEvent();
                assert event.getEventType() == XMLStreamConstants.START_ELEMENT;
            }
            rootElem = ((StartElement) event).getName();

            // Initialize.
            levelInToShredder = 0;
            levelInShreddered = 0;
            isLastNode = false;
            moveUp = false;
            removed = false;

            // If structure already exists, make a sync against the current
            // structure.
            if (maxNodeKey != 0) {
                // Find the start key for the update operation.
                long startkey = (Long) EFixed.ROOT_NODE_KEY
                        .getStandardProperty() + 1;
                while (!mWtx.moveTo(startkey)) {
                    startkey++;
                }

                // Iterate over all nodes.
                do {
                    switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (LogHelper.DEBUG) {
                            // Debugging output.
                            log.debug("TO SHREDDER: "
                                    + ((StartElement) event).getName());

                            if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                                log.debug("SHREDDERED: "
                                        + mWtx.getQNameOfCurrentNode());
                            } else {
                                log.debug("SHREDDERED: "
                                        + mWtx.getValueOfCurrentNode());
                            }
                        }

                        // Initialize variables.
                        initializeVars();

                        // Increment levels.
                        levelInToShredder++;
                        levelInShreddered++;

                        algorithm(event);

                        if (found && isRightSibling) {
                            deleteNodes();
                        } else if (!found) {
                            insertElementNodes(event);
                        } else if (found) {
                            sameElementNodes();
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        // Initialize variables.
                        initializeVars();

                        final String text = ((Characters) event).getData()
                                .trim();
                        if (!text.isEmpty()) {
                            algorithm(event);

                            if (found && isRightSibling) {
                                deleteNodes();
                            } else if (!found) {
                                insertTextNodes(event, text);
                            } else if (found) {
                                sameNodes();
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        levelInToShredder--;
                        levelsUpAfterInserts++;

                        if (!isSame && !removed) {
                            levelsUp = 0;
                            if (insertLevel >= 0) {
                                insertLevel--;
                                if (!leftSiblingKeyStack.empty()) {
                                    leftSiblingKeyStack.pop();
                                    levelInShreddered--;

                                    if (!leftSiblingKeyStack.empty()) {
                                        mWtx.moveTo(leftSiblingKeyStack.peek());
                                    }
                                }

                                System.out.println("END ELEM: "
                                        + mWtx.getQNameOfCurrentNode());

                                /*
                                 * If insertLevel == 0 move to topmost stack
                                 * element which means .
                                 */
                                if (insertLevel >= 0 || moveUp) {
                                    /*
                                     * Insert occured at the top of the tree,
                                     * thus no closed tags were parsed so far.
                                     * Move to the right sibling of the root
                                     * node of the inserted node only if the new
                                     * event is equal to the right sibling.
                                     */
                                    if (insertAtTop) {
                                        moveUp = false;
                                        checkRightSibling(leftSiblingKeyStack,
                                                false, insertAtTop);
                                    } else if (insertLevel == 0 || moveUp) {
                                        /*
                                         * Check if move to element before any
                                         * inserts occured would move to a node
                                         * which equals the next node of the
                                         * next event. If not move back to
                                         * current top element on stack (which
                                         * is the parent of the currently
                                         * inserted node).
                                         */
                                        final XMLEvent xmlEvent = skipWhitespaces();

                                        if (xmlEvent.getEventType() != XMLStreamConstants.END_ELEMENT
                                                && !leftSiblingKeyStack.empty()) {
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
                                                boolean foundNode = false;
                                                switch (xmlEvent.getEventType()) {
                                                case XMLStreamConstants.CHARACTERS:
                                                    final String data = ((Characters) xmlEvent)
                                                            .getData().trim();

                                                    if (!data.isEmpty()
                                                            && mWtx.getNode()
                                                                    .getKind() == ENodes.TEXT_KIND
                                                            && mWtx.getValueOfCurrentNode()
                                                                    .equals(data)) {
                                                        foundNode = true;
                                                    } else {
                                                        mWtx.moveTo(keyInShreddered);
                                                        leftSiblingKeyStack
                                                                .push(keyOnStack);
                                                    }
                                                    break;
                                                case XMLStreamConstants.START_ELEMENT:
                                                    foundNode = checkElement((StartElement) xmlEvent);
                                                    if (!foundNode) {
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
                                }
                            } else {
                                moveUp = true;
                            }
                        } else {
                            levelsUp++;
                        }
                        break;
                    default:
                        // Other nodes which are currently not supported by
                        // Treetank.
                    }

                    // Parsing the next event.
                    if (removed) {
                        levelInToShredder--;
                        levelInShreddered--;
                        removed = false;
                    } else {
                        // After an insert or after nodes were the same.
                        event = mReader.nextEvent();

                        if (event.getEventType() == XMLStreamConstants.END_ELEMENT
                                && rootElem.equals(((EndElement) event)
                                        .equals(rootElem))
                                && levelInToShredder == 0) {
                            // End with shredding if end_elem equals root-elem.
                            break;
                        }
                    }
                } while (mReader.hasNext());

                /*
                 * If still nodes are on the stack, they have been removed, thus
                 * remove them.
                 */
                if (!leftSiblingKeyStack.empty() && !isLastNode) {
                    if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                            .getStandardProperty()) {
                        leftSiblingKeyStack.pop();
                    }
                    while (!leftSiblingKeyStack.empty()) {
                        mWtx.moveTo(leftSiblingKeyStack.peek());
                        mWtx.remove();
                        leftSiblingKeyStack.pop();
                    }
                }

                mReader.close();
            }
            // If no content is in the XML, a normal insertNewContent is
            // executed.
            else {
                insertNewContent();
            }

            log.info("Done [" + (System.currentTimeMillis() - start) + "]");
            // TODO: use Java7 multi-catch feature.
        } catch (final XMLStreamException exc1) {
            throw new TreetankIOException(exc1);
        } catch (final IOException exc2) {
            throw new TreetankIOException(exc2);
        }

    }

    /**
     * Initialize variables needed for the main algorithm.
     */
    private void initializeVars() {
        nodeKey = mWtx.getNode().getNodeKey();
        found = false;
        isRightSibling = false;
        keyMatches = -1;
    }

    /**
     * Insert element nodes.
     * 
     * @param event
     *            Event, which is currently being parsed.
     * @throws TreetankException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     */
    private void insertElementNodes(final XMLEvent event)
            throws TreetankException {
        /*
         * Add node if it's either not found among right siblings (and the
         * cursor on the shreddered file is on a right sibling) or if it's not
         * found in the structure and it is a new last right sibling.
         */
        isSame = false;
        insertedElement = true;
        removed = false;
        insertLevel++;
        insert = true;

        if (insertAtTop) {
            // Insert at the top of a tree (after start tags).
            mWtx.moveToParent();
            // Update stack.
            assert !leftSiblingKeyStack.empty();
            leftSiblingKeyStack.pop();
            leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty());

            leftSiblingKeyStack = addNewElement(false, leftSiblingKeyStack,
                    (StartElement) event);

            updateLastPosKey();

            levelsUp = 0;
            levelsUpAfterInserts = 0;
            insertAtTop = false;
        } else if (levelsUp > 0) {
            levelsUp();

            // Insert new node as right sibling.
            leftSiblingKeyStack = addNewElement(false, leftSiblingKeyStack,
                    (StartElement) event);

            updateLastPosKey();

            levelsUp = 0;
            levelsUpAfterInserts = 0;
        } else if (insertLevel >= 0) {
            if (moveUp) {
                // After a first insert.
                moveUp();
                assert insertLevel == 1;
            }

            leftSiblingKeyStack = addNewElement(false, leftSiblingKeyStack,
                    (StartElement) event);

            updateLastPosKey();

            levelsUpAfterInserts = 0;
        }
    }

    /**
     * Insert text nodes.
     * 
     * @param event
     *            XMLStream event from the StAX parser.
     * @param text
     *            Text string.
     * @throws TreetankException
     *             In case of any error while inserting the node.
     * @throws XMLStreamException
     *             If StAX failes to parse some XML fragment.
     * 
     */
    private void insertTextNodes(final XMLEvent event, final String text)
            throws TreetankException, XMLStreamException {
        insert = true;
        if (moveUp) {
            moveUp();
            assert insertLevel == 0;
        } else
        /*
         * If a text node on it's own (without a previous element node has been
         * inserted) move back to the last position key and insert as a right
         * sibling.
         */
        if (levelsUp > 0 && !insertedElement) {
            levelsUp();
        } else if (insertAtTop) {
            lastPosKey = mWtx.getNode().getNodeKey();
            mWtx.moveToParent();

            assert !leftSiblingKeyStack.empty();

            // // Update stack.
            // // Remove NULL.
            // assert leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
            // .getStandardProperty();
            // leftSiblingKeyStack.pop();
            //
            // // if (levelInToShredder == 1) {
            // // // Child of root element level.
            // // // Remove node.
            // // leftSiblingKeyStack.pop();
            // // }
            // leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
            // .getStandardProperty());
        }

        isSame = false;

        // Insert text.
        leftSiblingKeyStack = addNewText(leftSiblingKeyStack,
                (Characters) event);

        /*
         * If right sibling of current node is the next event move cursor and
         * update stack.
         */
        if (((AbsStructNode) mWtx.getNode()).hasRightSibling()) {
            checkRightSibling(leftSiblingKeyStack, true, insertAtTop);
        }

        if (insertAtTop) {
            insertAtTop = false;
        }
    }

    /**
     * In case they are the same nodes move cursor to next node and update
     * stack.
     */
    private void sameNodes() {
        System.out.println("mWtx text: " + mWtx.getValueOfCurrentNode());
        insertLevel = 0;
        moveUp = false;
        removed = false;
        levelsUp = 0;
        isSame = true;
        insert = false;
        insertedElement = false;
        lastPosKey = mWtx.getNode().getNodeKey();
        leftSiblingKeyStack.pop();

        // Move to a parent which has the next right sibling in pre order.
        while (!((AbsStructNode) mWtx.getNode()).hasRightSibling()) {
            // Move to parent element node.
            mWtx.moveToParent();

            levelInShreddered--;

            final long key = mWtx.getNode().getNodeKey();
            mWtx.moveTo(leftSiblingKeyStack.peek());
            System.out.println("NAME: " + mWtx.getQNameOfCurrentNode());
            mWtx.moveTo(key);

            // Update stack.
            // Remove text node or parent nodes which have no right sibl.
            leftSiblingKeyStack.pop();
        }
        mWtx.moveToRightSibling();

        // Update stack.
        leftSiblingKeyStack.push(mWtx.getNode().getNodeKey());

        if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
            leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty());
        }
    }

    /**
     * Main algorithm to determine if nodes are equal, have to be inserted, or
     * have to be removed.
     * 
     * @param keyMatches
     *            The key of the node, when the nodes are equal if at all (used
     *            to check right siblings and therefore if nodes have been
     *            deleted).
     * @param event
     *            The currently parsed StAX event.
     * @throws IOException
     *             In case the open operation fails (delegated from
     *             checkDescendants(...)).
     * @throws XMLStreamException
     *             In case any StAX parser problem occurs.
     */
    private void algorithm(final XMLEvent event) throws IOException,
            XMLStreamException {
        if (levelInToShredder < levelInShreddered) {
            /*
             * Node or nodes were removed on a level which is higher than the
             * current one.
             */
            keyMatches = -1;
            found = true;
            isRightSibling = true;

            while (((AbsStructNode) mWtx.getNode()).hasRightSibling()) {
                keyMatches = mWtx.getNode().getNodeKey();
                mWtx.moveToRightSibling();
            }
        } else {
            // Check current and right sibling nodes.
            final FastStack<Long> stack = new FastStack<Long>();
            do {
                stack.clear();
                /*
                 * Check if a node in the shreddered file on the same level
                 * equals the current element node.
                 */
                if (event instanceof StartElement) {
                    found = checkElement((StartElement) event);
                } else if (event instanceof Characters) {
                    found = checkText((Characters) event);
                }
                if (mWtx.getNode().getNodeKey() != nodeKey) {
                    isRightSibling = true;
                }

                keyMatches = mWtx.getNode().getNodeKey();

                if (found && isRightSibling) {
                    /*
                     * Root element of next subtree in shreddered file matches
                     * so check all descendants. If they match the node must be
                     * inserted.
                     */
                    if (event instanceof StartElement) {
                        found = checkDescendants(levelInToShredder,
                                (StartElement) event, stack, true);
                    } else {
                        found = checkText((Characters) event);
                    }
                    mWtx.moveTo(keyMatches);
                }
            } while (!found && mWtx.moveToRightSibling());
            mWtx.moveTo(nodeKey);
        }
    }

    /**
     * Nodes match, thus update stack and move cursor.
     * 
     * @throws TreetankIOException
     *             In case Treetank cannot read the max node key.
     */
    private void sameElementNodes() throws TreetankIOException {
        // Nodes are the same.
        levelsUpAfterInserts = 0;
        insertLevel = 0;
        moveUp = false;
        removed = false;
        levelsUp = 0;
        isSame = true;
        insertedElement = false;
        insert = false;
        System.out.println("FOUND: " + mWtx.getQNameOfCurrentNode()
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
        } else if (((ElementNode) mWtx.getNode()).hasRightSibling()) {
            insertAtTop = false;
            levelInShreddered--;
            // Empty element.
            // Update stack.
            if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty()) {
                // Remove NULL.
                leftSiblingKeyStack.pop();
            }
            /*
             * Remove element (the tag must have been closed, thus remove it
             * from the stack!).
             */
            leftSiblingKeyStack.pop();
            mWtx.moveToRightSibling();
        } else if (mWtx.getNode().hasParent()) {
            insertAtTop = false;
            levelInShreddered--;

            if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty()) {
                leftSiblingKeyStack.pop();
            }

            if (!leftSiblingKeyStack.empty()) {
                leftSiblingKeyStack.pop();
                do {
                    if (!leftSiblingKeyStack.empty()) {
                        leftSiblingKeyStack.pop();
                    }
                    levelInShreddered--;
                    mWtx.moveToParent();
                } while (!((AbsStructNode) mWtx.getNode()).hasRightSibling()
                        && !leftSiblingKeyStack.empty());
                if (!mWtx.moveToRightSibling()) {
                    isLastNode = true;
                }
            }
        }

        // Update stack.
        leftSiblingKeyStack.push(mWtx.getNode().getNodeKey());

        if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
            // Update stack.
            leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty());
        }
    }

    /**
     * Check if text event and text in Treetank storage match.
     * 
     * @param event
     *            StAX event.
     * @return true if they match, otherwise false.
     */
    private boolean checkText(Characters event) {
        final String text = event.getData().trim();
        return mWtx.getNode().getKind() == ENodes.TEXT_KIND
                && mWtx.getValueOfCurrentNode().equals(text);
    }

    /**
     * Delete nodes.
     * 
     * @throws TreetankException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     */
    private void deleteNodes() throws TreetankException {
        /*
         * If found in one of the rightsiblings in the current shreddered
         * structure remove all nodes until the transaction points to the found
         * node (keyMatches).
         */
        isSame = false;
        insertedElement = false;
        removed = true;

        if (insert) {
            if (!mWtx.moveToRightSibling()) {
                // do {
                // mWtx.moveToParent();
                // } while (!((AbsStructNode)mWtx).hasRightSibling());
            }
        }

        do {
            mWtx.remove();

            // Update stack.
            if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty()) {
                leftSiblingKeyStack.pop();
            }

            if (insert) {
                // Remove inserted node from stack.
                leftSiblingKeyStack.pop();
            }

            leftSiblingKeyStack.pop();
            leftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
            leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                    .getStandardProperty());
        } while (mWtx.getNode().getNodeKey() != keyMatches
                && mWtx.moveToRightSibling());
        // Move up anchestors if there is no former right sibling.
        boolean moveToParents = false;
        while (!((AbsStructNode) mWtx.getNode()).hasRightSibling()) {
            moveToParents = true;
            mWtx.moveToParent();
            levelInShreddered--;

            // Update stack.
            // Remove NULL.
            if (leftSiblingKeyStack.peek() == EFixed.NULL_NODE_KEY
                    .getStandardProperty()) {
                leftSiblingKeyStack.pop();
            }
            leftSiblingKeyStack.pop();
        }

        if (moveToParents) {
            // Move to right sibling and update stack.
            mWtx.moveToRightSibling();
            leftSiblingKeyStack.pop();
            leftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
        }

        insert = false;
    }

    /**
     * Update last position key after an insert.
     */
    private void updateLastPosKey() {
        assert leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                .getStandardProperty();
        leftSiblingKeyStack.pop();
        lastPosKey = leftSiblingKeyStack.peek();
        leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
                .getStandardProperty());
    }

    /**
     * Compare right sibling of current node with the next event.
     * 
     * @param leftSiblingKeyStack
     *            The Stack which has to be modified.
     * @throws XMLStreamException
     *             In case the xml parser encounters an error.
     */
    private boolean checkRightSibling(
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
        default:
            // Do nothing (other nodes currently not supported in Treetank).
        }

        // Update stack.
        // Remove inserted text node.
        if (text) {
            leftSiblingKeyStack.pop();
        }

        // Remove NULL.
        if (!leftSiblingKeyStack.empty()
                && leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                        .getStandardProperty()) {
            leftSiblingKeyStack.pop();
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
    private XMLEvent skipWhitespaces() throws XMLStreamException {
        // Check if next event equals node on top of stack.
        // Ignore all whitespace between elements.
        while (mReader.peek().getEventType() == XMLStreamConstants.CHARACTERS
                && (((Characters) mReader.peek()).isIgnorableWhiteSpace() || ((Characters) mReader
                        .peek()).isWhiteSpace())) {
            mReader.nextEvent();
        }

        return mReader.peek();
    }

    private void levelsUp() {
        mWtx.moveTo(lastPosKey);

        // Move up levels to the right parent.
        for (int i = 0; i < levelsUp - 1; i++) {
            mWtx.moveToParent();
        }

        /*
         * Make sure that it's inserted as a right sibling if the transaction
         * has move to at least one parent before.
         */
        if (!leftSiblingKeyStack.empty()
                && leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                        .getStandardProperty()) {
            leftSiblingKeyStack.pop();
        }

        // Insert new node as right sibling.
        // Push dummy on top.
        leftSiblingKeyStack.push(0L);

        // Just to be sure it has the right value.
        levelsUp = 0;
    }

    /**
     * Move up levels if inserts occured immediately before and now inserts go
     * on at a lower level (as a right sibling on one of the parent nodes).
     */
    private void moveUp() {
        // After a first insert.
        mWtx.moveTo(lastPosKey);

        // Move up levels to the right parent.
        for (int i = 0; i < levelsUpAfterInserts - 1; i++) {
            mWtx.moveToParent();
        }

        /*
         * Make sure that it's inserted as a right sibling if the transaction
         * has move to at least one parent before.
         */
        if (!leftSiblingKeyStack.empty()
                && leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
                        .getStandardProperty()) {
            leftSiblingKeyStack.pop();
        }

        // Insert new node as right sibling.
        // Push dummy on top.
        leftSiblingKeyStack.push(0L);

        moveUp = false;
        insertLevel++;
    }

    /**
     * Check if descendants match.
     * 
     * @param levelInToShredder
     *            The level on which the currently parsed node of the parser,
     *            which parses the file to shredder currently is.
     * @param elem
     *            The start element where the StAX parser currently is.
     * @param stack
     *            Used to determine if moveToFirstChild() or
     *            moveToRightSibling() has to be invoked.
     * @param first
     *            Determines if it is the first call the method is invoked (a
     *            new StAX parser
     * @return true if they match, otherwise false.
     * @throws XMLStreamException
     *             In case of any streamining exception in the source document.
     * @throws IOException
     *             In case of any I/O exception while opening the target file.
     */
    private boolean checkDescendants(final int levelInToShredder,
            final StartElement elem, final FastStack<Long> stack,
            final boolean first) throws XMLStreamException, IOException {
        boolean found = false;

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

                    if (level == levelInToShredder
                            && checkStAXElement((StartElement) xmlEvent, elem)) {
                        // Found corresponding start element.
                        foundParsedElement = true;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    level--;
                    break;
                default:
                    // Do nothing.
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

        if (moved) {
            if (mParser.hasNext()) {
                final XMLEvent xmlEvent = mParser.nextEvent();
                switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    // Update stack.
                    if (stack.peek() == (Long) EFixed.NULL_NODE_KEY
                            .getStandardProperty()) {
                        stack.pop();
                    }
                    stack.push(mWtx.getNode().getNodeKey());
                    stack.push((Long) EFixed.NULL_NODE_KEY
                            .getStandardProperty());

                    found = checkElement((StartElement) xmlEvent);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    final String text = ((Characters) xmlEvent).getData()
                            .trim();

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
                checkDescendants(levelInToShredder, elem, stack, false);
            }
        } else {
            found = true;
        }

        return found;
    }

    /**
     * Check if start element of two StAX parsers match.
     * 
     * @param startTag
     *            StartTag to check against.
     * @param elem
     *            StartTag of the StAX parser, where it is currently (the "real"
     *            StAX parser over the whole document).
     * @return True if start elements match.
     * @throws XMLStreamException
     */
    private final boolean checkStAXElement(final StartElement startTag,
            final StartElement elem) throws XMLStreamException {
        boolean retVal = false;
        if (startTag.getEventType() == XMLStreamConstants.START_ELEMENT
                && startTag.getName().equals(elem.getName())) {
            // Check attributes.
            boolean foundAtts = false;
            boolean hasAtts = false;
            for (final Iterator<?> itStartTag = startTag.getAttributes(); itStartTag
                    .hasNext();) {
                hasAtts = true;
                final Attribute attStartTag = (Attribute) itStartTag.next();
                for (final Iterator<?> itElem = elem.getAttributes(); itElem
                        .hasNext();) {
                    final Attribute attElem = (Attribute) itElem.next();
                    if (attStartTag.getName().equals(attElem.getName())
                            && attStartTag.getName().equals(attElem.getName())) {
                        foundAtts = true;
                        break;
                    }
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
            for (final Iterator<?> itStartTag = startTag.getNamespaces(); itStartTag
                    .hasNext();) {
                hasNamesps = true;
                final Namespace nsStartTag = (Namespace) itStartTag.next();
                for (final Iterator<?> itElem = elem.getNamespaces(); itElem
                        .hasNext();) {
                    final Namespace nsElem = (Namespace) itElem.next();
                    if (nsStartTag.getName().equals(nsElem.getName())
                            && nsStartTag.getName().equals(nsElem.getName())) {
                        foundNamesps = true;
                        break;
                    }
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
     */
    public static void main(final String... args) {
        if (args.length != 2) {
            System.out.println("Usage: XMLShredder input.xml output.tnk");
            System.exit(1);
        }

        System.out.print("Shredding '" + args[0] + "' to '" + args[1]
                + "' ... ");
        long time = System.currentTimeMillis();
        final File target = new File(args[1]);

        try {
            Database.createDatabase(new DatabaseConfiguration(target));
            final IDatabase db = Database.openDatabase(target);
            final ISession session = db.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            mFile = new File(args[0]);
            final XMLEventReader reader = createReader(null);
            final XMLUpdateShredder shredder = new XMLUpdateShredder(wtx,
                    reader, true);
            shredder.call();

            wtx.close();
            session.close();
            db.close();
        } catch (final TreetankException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (final XMLStreamException e) {
            LOGGER.error(e.getMessage(), e);
        }

        System.out.println(" done [" + (System.currentTimeMillis() - time)
                + "ms].");
    }
}

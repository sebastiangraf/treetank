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
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
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
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.settings.EFixed;
import com.treetank.utils.FastStack;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

/**
 * This class appends a given {@link XMLStreamReader} to a {@link IWriteTransaction}. The content of the
 * stream is added as a subtree.
 * Based on a boolean which identifies the point of insertion, the subtree is
 * either added as subtree or as rightsibling. Only updated nodes are inserted
 * or removed.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLUpdateShredder extends XMLShredder implements Callable<Long> {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory
        .getLogger(XMLUpdateShredder.class));

    // ===================== Initial setup ================

    /** Determines if the nodes match or not. */
    private boolean mIsSame;

    /** Levels to go up to the parent after nodes are not equal (insert). */
    private int mLevelsUp;

    /** Last position of mWtx before the current. */
    private long mLastPosKey;

    /** Level in the current subtree, which is going to be inserted. */
    private int mInsertLevel;

    /** Level where the parser is in the file to shredder. */
    private int mLevelInToShredder;

    /** Level where the cursor is in the shreddered file. */
    private int mLevelInShreddered;

    /** Insert node at the top of a subtree. */
    private boolean mInsertAtTop = true;

    /** Determines if an element has been inserted immediately before. */
    private boolean mInsertedElement;

    /**
     * Levels to move up after inserts at some level and further inserts on a
     * level above the current level.
     */
    private int mLevelsUpAfterInserts;

    /** Determines if the cursor has to move up some levels during to inserts. */
    private boolean mMoveUp;

    /** Determines if a node or nodes have been deleted immediately before. */
    private boolean mRemoved;

    /** Node key. */
    private long mNodeKey;

    /** Determines if a node is found in the Treetank storage or not. */
    private boolean mFound;

    /**
     * This stack is for holding the current position to determine if an
     * insertAsRightSibling() or insertAsFirstChild() should occure.
     */
    private FastStack<Long> mLeftSiblingKeyStack;

    /**
     * Determines if it's a right sibling from the currently parsed node, where
     * the parsed node and the node in the Treetank storage match.
     */
    private boolean mIsRightSibling;

    /**
     * The key of the node, when the nodes are equal if at all (used to check
     * right siblings and therefore if nodes have been deleted).
     */
    private long keyMatches;

    /** Maximum node key in revision. */
    private transient long mMaxNodeKey;

    /** Determines if it's the last node or not. */
    private boolean mIsLastNode;

    /** Determines if an insert occured. */
    private boolean mInsert;

    /**
     * Normal constructor to invoke a shredding process on a existing {@link WriteTransaction}.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} where the new XML Fragment should be
     *            placed.
     * @param paramReader
     *            {@link XMLEventReader} (StAX parser) of the XML Fragment.
     * @param paramAddAsFirstChild
     *            If the insert is occuring on a node in an existing tree. <code>false</code> is not possible
     *            when wtx is on root node.
     * @throws TreetankUsageException
     *             If insertasfirstChild && updateOnly is both true OR if wtx is
     *             not pointing to doc-root and updateOnly= true
     * @throws TreetankIOException
     *             If Treetank cannot access node keys.
     * 
     */
    public XMLUpdateShredder(final IWriteTransaction paramWtx, final XMLEventReader paramReader,
        final boolean paramAddAsFirstChild) throws TreetankUsageException, TreetankIOException {
        super(paramWtx, paramReader, paramAddAsFirstChild);
        if (paramWtx.getNode().getKind() != ENodes.ROOT_KIND) {
            throw new TreetankUsageException("WriteTransaction must point to doc-root at the beginning!");
        }
        mMaxNodeKey = mWtx.getMaxNodeKey();
    }

    /**
     * Invoking the shredder.
     * 
     * @throws TreetankException
     *             In case any Treetank exception occured.
     * @return revision of last revision (before commit).
     */
    @Override
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
     *             In case of any Treetank error.
     */
    private void updateOnly() throws TreetankException {
        try {

            // Setting up boolean-Stack.
            mLeftSiblingKeyStack = new FastStack<Long>();
            mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());

            // Setup up of first element of the data.
            XMLEvent event = mReader.nextEvent();
            mWtx.moveToDocumentRoot();

            // Get root element of subtree or whole XML document to shredder.
            QName rootElem;
            if (event.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                event = mReader.nextEvent();
                assert event.getEventType() == XMLStreamConstants.START_ELEMENT;
            }
            rootElem = ((StartElement)event).getName();

            // Initialize.
            mLevelInToShredder = 0;
            mLevelInShreddered = 0;
            mIsLastNode = false;
            mMoveUp = false;
            mRemoved = false;

            // If structure already exists, make a sync against the current
            // structure.
            if (mMaxNodeKey != 0) {
                // Find the start key for the update operation.
                long startkey = (Long)EFixed.ROOT_NODE_KEY.getStandardProperty() + 1;
                while (!mWtx.moveTo(startkey)) {
                    startkey++;
                }

                // Iterate over all nodes.
                do {
                    switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        /*
                         * if (LOGGER.isDebugEnabled()) {
                         * // Debugging output.
                         * LOGGER.debug("TO SHREDDER: " + ((StartElement)event).getName());
                         * 
                         * if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                         * LOGGER.debug("SHREDDERED: " + mWtx.getQNameOfCurrentNode());
                         * } else {
                         * LOGGER.debug("SHREDDERED: " + mWtx.getValueOfCurrentNode());
                         * }
                         * }
                         */

                        // Initialize variables.
                        initializeVars();

                        // Increment levels.
                        mLevelInToShredder++;
                        mLevelInShreddered++;

                        algorithm(event);

                        if (mFound && mIsRightSibling) {
                            deleteNodes();
                        } else if (!mFound) {
                            insertElementNodes(event);
                        } else if (mFound) {
                            sameElementNodes();
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        // Initialize variables.
                        initializeVars();

                        final String text = ((Characters)event).getData().trim();
                        if (!text.isEmpty()) {
                            algorithm(event);

                            if (mFound && mIsRightSibling) {
                                deleteNodes();
                            } else if (!mFound) {
                                insertTextNodes(event);
                            } else if (mFound) {
                                sameNodes();
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        mLevelInToShredder--;
                        mLevelsUpAfterInserts++;

                        if (!mIsSame && !mRemoved) {
                            mLevelsUp = 0;
                            if (mInsertLevel >= 0) {
                                mInsertLevel--;
                                if (!mLeftSiblingKeyStack.empty()) {
                                    mLeftSiblingKeyStack.pop();
                                    mLevelInShreddered--;

                                    if (!mLeftSiblingKeyStack.empty()) {
                                        mWtx.moveTo(mLeftSiblingKeyStack.peek());
                                    }
                                }

                                System.out.println("END ELEM: " + mWtx.getQNameOfCurrentNode());

                                /*
                                 * If insertLevel == 0 move to topmost stack
                                 * element which means .
                                 */
                                if (mInsertLevel >= 0 || mMoveUp) {
                                    /*
                                     * Insert occured at the top of the tree,
                                     * thus no closed tags were parsed so far.
                                     * Move to the right sibling of the root
                                     * node of the inserted node only if the new
                                     * event is equal to the right sibling.
                                     */
                                    if (mInsertAtTop) {
                                        mMoveUp = false;
                                        checkRightSibling(mLeftSiblingKeyStack, false);
                                    } else if (mInsertLevel == 0 || mMoveUp) {
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
                                            && !mLeftSiblingKeyStack.empty()) {
                                            // Move to the node before an insert
                                            // occured.
                                            final long keyOnStack = mLeftSiblingKeyStack.peek();
                                            final long keyInShreddered = mWtx.getNode().getNodeKey();
                                            mLeftSiblingKeyStack.pop();

                                            if (mLeftSiblingKeyStack.empty()) {
                                                // Stack is empty, so the node(s) was/were inserted
                                                // somewhere at the top of the XML file.
                                                /*
                                                 * Check if right sibling after
                                                 * an insert equals next event.
                                                 */
                                                if (mWtx.moveToRightSibling()) {
                                                    if (!checkElement((StartElement)xmlEvent)) {
                                                        mWtx.moveToLeftSibling();
                                                    }
                                                } else {
                                                    /*
                                                     * Move to a parent which
                                                     * has the next right
                                                     * sibling in pre order.
                                                     */
                                                    while (!((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
                                                        // Move to parent
                                                        // element node.
                                                        mWtx.moveToParent();
                                                    }
                                                }

                                                mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
                                            } else {
                                                // Stack is not empty.
                                                mWtx.moveTo(mLeftSiblingKeyStack.peek());
                                                boolean foundNode = false;
                                                switch (xmlEvent.getEventType()) {
                                                case XMLStreamConstants.CHARACTERS:
                                                    final String data =
                                                        ((Characters)xmlEvent).getData().trim();

                                                    if (!data.isEmpty()
                                                        && mWtx.getNode().getKind() == ENodes.TEXT_KIND
                                                        && mWtx.getValueOfCurrentNode().equals(data)) {
                                                        foundNode = true;
                                                    } else {
                                                        mWtx.moveTo(keyInShreddered);
                                                        mLeftSiblingKeyStack.push(keyOnStack);
                                                    }
                                                    break;
                                                case XMLStreamConstants.START_ELEMENT:
                                                    foundNode = checkElement((StartElement)xmlEvent);
                                                    if (!foundNode) {
                                                        mWtx.moveTo(keyInShreddered);
                                                        mLeftSiblingKeyStack.push(keyOnStack);
                                                    }
                                                    break;
                                                default:
                                                    // Do nothing.
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                mMoveUp = true;
                            }
                        } else {
                            mLevelsUp++;
                        }
                        break;
                    default:
                        // Other nodes which are currently not supported by
                        // Treetank.
                    }

                    // Parsing the next event.
                    if (mRemoved) {
                        mLevelInToShredder--;
                        mLevelInShreddered--;
                        mRemoved = false;
                    } else {
                        // After an insert or after nodes were the same.
                        event = mReader.nextEvent();

                        if (event.getEventType() == XMLStreamConstants.END_ELEMENT
                            && rootElem.equals(((EndElement)event).getName()) && mLevelInToShredder == 0) {
                            // End with shredding if end_elem equals root-elem.
                            break;
                        }
                    }
                } while (mReader.hasNext());

                /*
                 * If still nodes are on the stack, they have been removed, thus
                 * remove them.
                 */
                if (!mLeftSiblingKeyStack.empty() && !mIsLastNode) {
                    if (mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                        mLeftSiblingKeyStack.pop();
                    }
                    while (!mLeftSiblingKeyStack.empty()) {
                        mWtx.moveTo(mLeftSiblingKeyStack.peek());
                        mWtx.remove();
                        mLeftSiblingKeyStack.pop();
                    }
                }
                mReader.close();
            }
            // If no content is in the XML, a normal insertNewContent is
            // executed.
            else {
                insertNewContent();
            }

            // TODO: use Java7 multi-catch feature.
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e);
            throw new TreetankIOException(e);
        } catch (final IOException e) {
            LOGWRAPPER.error(e);
            throw new TreetankIOException(e);
        }

    }

    /**
     * Initialize variables needed for the main algorithm.
     */
    private void initializeVars() {
        mNodeKey = mWtx.getNode().getNodeKey();
        mFound = false;
        mIsRightSibling = false;
        keyMatches = -1;
    }

    /**
     * Insert element nodes.
     * 
     * @param mEevent
     *            Event, which is currently being parsed.
     * @throws TreetankException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     */
    private void insertElementNodes(final XMLEvent mEevent) throws TreetankException {
        /*
         * Add node if it's either not found among right siblings (and the
         * cursor on the shreddered file is on a right sibling) or if it's not
         * found in the structure and it is a new last right sibling.
         */
        mIsSame = false;
        mInsertedElement = true;
        mRemoved = false;
        mInsertLevel++;
        mInsert = true;

        if (mInsertAtTop) {
            // Insert at the top of a tree (after start tags).
            mWtx.moveToParent();
            // Update stack.
            assert !mLeftSiblingKeyStack.empty();
            mLeftSiblingKeyStack.pop();
            mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());

            mLeftSiblingKeyStack = addNewElement(false, mLeftSiblingKeyStack, (StartElement)mEevent);

            updateLastPosKey();

            mLevelsUp = 0;
            mLevelsUpAfterInserts = 0;
            mInsertAtTop = false;
        } else if (mLevelsUp > 0) {
            levelsUp();

            // Insert new node as right sibling.
            mLeftSiblingKeyStack = addNewElement(false, mLeftSiblingKeyStack, (StartElement)mEevent);

            updateLastPosKey();

            mLevelsUp = 0;
            mLevelsUpAfterInserts = 0;
        } else if (mInsertLevel >= 0) {
            if (mMoveUp) {
                // After a first insert.
                moveUp();
                assert mInsertLevel == 1;
            }

            mLeftSiblingKeyStack = addNewElement(false, mLeftSiblingKeyStack, (StartElement)mEevent);

            updateLastPosKey();

            mLevelsUpAfterInserts = 0;
        }
    }

    /**
     * Insert text nodes.
     * 
     * @param mEvent
     *            XMLStream event from the StAX parser.
     * @throws TreetankException
     *             In case of any error while inserting the node.
     * @throws XMLStreamException
     *             If StAX failes to parse some XML fragment.
     * 
     */
    private void insertTextNodes(final XMLEvent mEvent) throws TreetankException, XMLStreamException {
        mInsert = true;
        if (mMoveUp) {
            moveUp();
            assert mInsertLevel == 0;
        } else
        /*
         * If a text node on it's own (without a previous element node has been
         * inserted) move back to the last position key and insert as a right
         * sibling.
         */
        if (mLevelsUp > 0 && !mInsertedElement) {
            levelsUp();
        } else if (mInsertAtTop) {
            mLastPosKey = mWtx.getNode().getNodeKey();
            mWtx.moveToParent();
            assert !mLeftSiblingKeyStack.empty();

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

        mIsSame = false;

        // Insert text.
        mLeftSiblingKeyStack = addNewText(mLeftSiblingKeyStack, (Characters)mEvent);

        /*
         * If right sibling of current node is the next event move cursor and
         * update stack.
         */
        if (((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
            checkRightSibling(mLeftSiblingKeyStack, true);
        }

        if (mInsertAtTop) {
            mInsertAtTop = false;
        }
    }

    /**
     * In case they are the same nodes move cursor to next node and update
     * stack.
     */
    private void sameNodes() {
        System.out.println("mWtx text: " + mWtx.getValueOfCurrentNode());
        mInsertLevel = 0;
        mMoveUp = false;
        mRemoved = false;
        mLevelsUp = 0;
        mIsSame = true;
        mInsert = false;
        mInsertedElement = false;
        mLastPosKey = mWtx.getNode().getNodeKey();
        mLeftSiblingKeyStack.pop();

        // Move to a parent which has the next right sibling in pre order.
        while (!((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
            // Move to parent element node.
            mWtx.moveToParent();

            mLevelInShreddered--;

            final long key = mWtx.getNode().getNodeKey();
            mWtx.moveTo(mLeftSiblingKeyStack.peek());
            System.out.println("NAME: " + mWtx.getQNameOfCurrentNode());
            mWtx.moveTo(key);

            // Update stack.
            // Remove text node or parent nodes which have no right sibl.
            mLeftSiblingKeyStack.pop();
        }
        mWtx.moveToRightSibling();

        // Update stack.
        mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());

        if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
            mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
        }
    }

    /**
     * Main algorithm to determine if nodes are equal, have to be inserted, or
     * have to be removed.
     * 
     * @param mEvent
     *            The currently parsed StAX event.
     * @throws IOException
     *             In case the open operation fails (delegated from
     *             checkDescendants(...)).
     * @throws XMLStreamException
     *             In case any StAX parser problem occurs.
     */
    private void algorithm(final XMLEvent mEvent) throws IOException, XMLStreamException {
        if (mLevelInToShredder < mLevelInShreddered) {
            /*
             * Node or nodes were removed on a level which is higher than the
             * current one.
             */
            keyMatches = -1;
            mFound = true;
            mIsRightSibling = true;

            while (((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
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
                if (mEvent instanceof StartElement) {
                    mFound = checkElement((StartElement)mEvent);
                } else if (mEvent instanceof Characters) {
                    mFound = checkText((Characters)mEvent);
                }
                if (mWtx.getNode().getNodeKey() != mNodeKey) {
                    mIsRightSibling = true;
                }

                keyMatches = mWtx.getNode().getNodeKey();

                if (mFound && mIsRightSibling) {
                    /*
                     * Root element of next subtree in shreddered file matches
                     * so check all descendants. If they match the node must be
                     * inserted.
                     */
                    if (mEvent instanceof StartElement) {
                        mFound = checkDescendants(mLevelInToShredder, (StartElement)mEvent, stack, true);
                    } else {
                        mFound = checkText((Characters)mEvent);
                    }
                    mWtx.moveTo(keyMatches);
                }
            } while (!mFound && mWtx.moveToRightSibling());
            mWtx.moveTo(mNodeKey);
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
        mLevelsUpAfterInserts = 0;
        mInsertLevel = 0;
        mMoveUp = false;
        mRemoved = false;
        mLevelsUp = 0;
        mIsSame = true;
        mInsertedElement = false;
        mInsert = false;
        System.out.println("FOUND: " + mWtx.getQNameOfCurrentNode() + mWtx.getNode().getNodeKey());
        // Update last position key.
        mLastPosKey = mWtx.getNode().getNodeKey();

        // Move transaction.
        if (((ElementNode)mWtx.getNode()).hasFirstChild()) {
            mInsertAtTop = true;
            // Update stack.
            if (mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                // Remove NULL.
                mLeftSiblingKeyStack.pop();
            }
            mWtx.moveToFirstChild();
        } else if (((ElementNode)mWtx.getNode()).hasRightSibling()) {
            mInsertAtTop = false;
            mLevelInShreddered--;
            // Empty element.
            // Update stack.
            if (mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                // Remove NULL.
                mLeftSiblingKeyStack.pop();
            }
            /*
             * Remove element (the tag must have been closed, thus remove it
             * from the stack!).
             */
            mLeftSiblingKeyStack.pop();
            mWtx.moveToRightSibling();
        } else if (mWtx.getNode().hasParent()) {
            mInsertAtTop = false;
            mLevelInShreddered--;

            if (mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                mLeftSiblingKeyStack.pop();
            }

            if (!mLeftSiblingKeyStack.empty()) {
                mLeftSiblingKeyStack.pop();
                do {
                    if (!mLeftSiblingKeyStack.empty()) {
                        mLeftSiblingKeyStack.pop();
                    }
                    mLevelInShreddered--;
                    mWtx.moveToParent();
                } while (!((AbsStructNode)mWtx.getNode()).hasRightSibling() && !mLeftSiblingKeyStack.empty());
                if (!mWtx.moveToRightSibling()) {
                    mIsLastNode = true;
                }
            }
        }

        // Update stack.
        mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());

        if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
            // Update stack.
            mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
        }
    }

    /**
     * Check if text event and text in Treetank storage match.
     * 
     * @param mEvent
     *            StAX event.
     * @return true if they match, otherwise false.
     */
    private boolean checkText(final Characters mEvent) {
        final String text = mEvent.getData().trim();
        return mWtx.getNode().getKind() == ENodes.TEXT_KIND && mWtx.getValueOfCurrentNode().equals(text);
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
        mIsSame = false;
        mInsertedElement = false;
        mRemoved = true;

        if (mInsert) {
            // Cursor is on the inserted node, so move to right sibling.
            mWtx.moveToRightSibling();
            // Remove inserted node from stack.
            mLeftSiblingKeyStack.pop();
        }

        do {
            if (mWtx.getNode().getNodeKey() != keyMatches) {
                mWtx.remove();

                // Update stack.
                if (mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                    mLeftSiblingKeyStack.pop();
                }

                mLeftSiblingKeyStack.pop();
                mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
                mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
            }
        } while (mWtx.getNode().getNodeKey() != keyMatches
            && ((AbsStructNode)mWtx.getNode()).hasRightSibling());
        // Move up anchestors if there is no former right sibling.
        boolean moveToParents = false;
        while (!((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
            moveToParents = true;
            mWtx.moveToParent();
            mLevelInShreddered--;

            // Update stack.
            // Remove NULL.
            if (mLeftSiblingKeyStack.peek() == EFixed.NULL_NODE_KEY.getStandardProperty()) {
                mLeftSiblingKeyStack.pop();
            }
            mLeftSiblingKeyStack.pop();
        }

        if (moveToParents) {
            // Move to right sibling and update stack.
            mWtx.moveToRightSibling();
            mLeftSiblingKeyStack.pop();
            mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
        }

        mInsert = false;
    }

    /**
     * Update last position key after an insert.
     */
    private void updateLastPosKey() {
        assert mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty();
        mLeftSiblingKeyStack.pop();
        mLastPosKey = mLeftSiblingKeyStack.peek();
        mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
    }

    /**
     * Compare right sibling of current node with the next event.
     * 
     * @param mLeftSiblingKeyStack
     *            The Stack which has to be modified.
     * @param mText
     *            Determines if a text node has been inserted.
     * @return retVal
     *         return value.
     * @throws XMLStreamException
     *             In case the xml parser encounters an error.
     */
    private boolean checkRightSibling(final FastStack<Long> mLeftSiblingKeyStack, final boolean mText)
        throws XMLStreamException {
        boolean retVal = false;
        mWtx.moveToRightSibling();
        final XMLEvent xmlEvent = skipWhitespaces();

        switch (xmlEvent.getEventType()) {
        case XMLStreamConstants.CHARACTERS:
            final String data = ((Characters)xmlEvent).getData().trim();

            if (!data.isEmpty() && mWtx.getNode().getKind() == ENodes.TEXT_KIND
                && mWtx.getValueOfCurrentNode().equals(data)) {
                retVal = true;
            } else {
                mWtx.moveToLeftSibling();
            }
            break;
        case XMLStreamConstants.START_ELEMENT:
            if (checkElement((StartElement)xmlEvent)) {
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
        if (mText) {
            mLeftSiblingKeyStack.pop();
        }

        // Remove NULL.
        if (!mLeftSiblingKeyStack.empty()
            && mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
            mLeftSiblingKeyStack.pop();
        }

        if (!mLeftSiblingKeyStack.empty()) {
            if (mLeftSiblingKeyStack.peek() != mWtx.getNode().getNodeKey()) {
                mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
            }
        } else {
            mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
        }

        return retVal;
    }

    /**
     * Skip all whitespace events between nodes.
     * 
     * @return mReader.peek()
     *         return peek.
     * @throws XMLStreamException
     *             In case of any error while parsing the file to shredder into
     *             an existing storage.
     */
    private XMLEvent skipWhitespaces() throws XMLStreamException {
        // Check if next event equals node on top of stack.
        // Ignore all whitespace between elements.
        while (mReader.peek().getEventType() == XMLStreamConstants.CHARACTERS
            && (((Characters)mReader.peek()).isIgnorableWhiteSpace() || ((Characters)mReader.peek())
                .isWhiteSpace())) {
            mReader.nextEvent();
        }

        return mReader.peek();
    }

    private void levelsUp() {
        mWtx.moveTo(mLastPosKey);

        // Move up levels to the right parent.
        for (int i = 0; i < mLevelsUp - 1; i++) {
            mWtx.moveToParent();
        }

        /*
         * Make sure that it's inserted as a right sibling if the transaction
         * has move to at least one parent before.
         */
        if (!mLeftSiblingKeyStack.empty()
            && mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
            mLeftSiblingKeyStack.pop();
        }

        // Insert new node as right sibling.
        // Push dummy on top.
        mLeftSiblingKeyStack.push(0L);

        // Just to be sure it has the right value.
        mLevelsUp = 0;
    }

    /**
     * Move up levels if inserts occured immediately before and now inserts go
     * on at a lower level (as a right sibling on one of the parent nodes).
     */
    private void moveUp() {
        // After a first insert.
        mWtx.moveTo(mLastPosKey);

        // Move up levels to the right parent.
        for (int i = 0; i < mLevelsUpAfterInserts - 1; i++) {
            mWtx.moveToParent();
        }

        /*
         * Make sure that it's inserted as a right sibling if the transaction
         * has move to at least one parent before.
         */
        if (!mLeftSiblingKeyStack.empty()
            && mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
            mLeftSiblingKeyStack.pop();
        }

        // Insert new node as right sibling.
        // Push dummy on top.
        mLeftSiblingKeyStack.push(0L);

        mMoveUp = false;
        mInsertLevel++;
    }

    /**
     * Check if descendants match.
     * 
     * @param mLevelInToShredder
     *            The level on which the currently parsed node of the parser,
     *            which parses the file to shredder currently is.
     * @param mElem
     *            The start element where the StAX parser currently is.
     * @param mStack
     *            Used to determine if moveToFirstChild() or
     *            moveToRightSibling() has to be invoked.
     * @param mFirst
     *            Determines if it is the first call the method is invoked (a
     *            new StAX parser
     * @return true if they match, otherwise false.
     * @throws XMLStreamException
     *             In case of any streamining exception in the source document.
     * @throws IOException
     *             In case of any I/O exception while opening the target file.
     */
    private boolean checkDescendants(final int mLevelInToShredder, final StartElement mElem,
        final FastStack<Long> mStack, final boolean mFirst) throws XMLStreamException, IOException {
        boolean found = false;

        if (mFirst) {
            /*
             * Setup new StAX parser and move it to the node, where the current
             * StAX parser currently is.
             */
            mStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
            int level = 0;
            boolean foundParsedElement = false;
            mParser = createReader(null);
            while (mParser.hasNext() && !foundParsedElement) {
                final XMLEvent xmlEvent = mParser.nextEvent();
                switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    level++;

                    if (level == mLevelInToShredder && checkStAXElement((StartElement)xmlEvent, mElem)) {
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
        if (mStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
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
                    if (mStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                        mStack.pop();
                    }
                    mStack.push(mWtx.getNode().getNodeKey());
                    mStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());

                    found = checkElement((StartElement)xmlEvent);
                    break;
                case XMLStreamConstants.CHARACTERS:
                    final String text = ((Characters)xmlEvent).getData().trim();

                    if (!text.isEmpty()) {
                        // Update stack.
                        mStack.pop();
                        mStack.push(mWtx.getNode().getNodeKey());

                        if (mWtx.getNode().getKind() == ENodes.TEXT_KIND) {
                            found = ((Characters)xmlEvent).getData().equals(mWtx.getValueOfCurrentNode());
                        }
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    mStack.pop();
                    mWtx.moveTo(mStack.peek());
                    break;
                default:
                    // Do nothing.
                }
                checkDescendants(mLevelInToShredder, mElem, mStack, false);
            }
        } else {
            found = true;
        }

        return found;
    }

    /**
     * Check if start element of two StAX parsers match.
     * 
     * @param mStartTag
     *            StartTag to check against.
     * @param mElem
     *            StartTag of the StAX parser, where it is currently (the "real"
     *            StAX parser over the whole document).
     * @return True if start elements match.
     * @throws XMLStreamException
     *             handling XML Stream Exception
     */
    private boolean checkStAXElement(final StartElement mStartTag, final StartElement mElem)
        throws XMLStreamException {
        boolean retVal = false;
        if (mStartTag.getEventType() == XMLStreamConstants.START_ELEMENT
            && mStartTag.getName().equals(mElem.getName())) {
            // Check attributes.
            boolean foundAtts = false;
            boolean hasAtts = false;
            for (final Iterator<?> itStartTag = mStartTag.getAttributes(); itStartTag.hasNext();) {
                hasAtts = true;
                final Attribute attStartTag = (Attribute)itStartTag.next();
                for (final Iterator<?> itElem = mElem.getAttributes(); itElem.hasNext();) {
                    final Attribute attElem = (Attribute)itElem.next();
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
            for (final Iterator<?> itStartTag = mStartTag.getNamespaces(); itStartTag.hasNext();) {
                hasNamesps = true;
                final Namespace nsStartTag = (Namespace)itStartTag.next();
                for (final Iterator<?> itElem = mElem.getNamespaces(); itElem.hasNext();) {
                    final Namespace nsElem = (Namespace)itElem.next();
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
     * @param mEvent
     *            StartElement event, from the XML file to shredder.
     * @return true if they are equal, false otherwise.
     */
    private boolean checkElement(final StartElement mEvent) {
        boolean retVal = false;
        final long nodeKey = mWtx.getNode().getNodeKey();

        // Matching element names?
        if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND
            && mWtx.getQNameOfCurrentNode().equals(mEvent.getName())) {
            // Check if atts and namespaces are the same.

            // Check attributes.
            boolean foundAtts = false;
            boolean hasAtts = false;
            for (final Iterator<?> it = mEvent.getAttributes(); it.hasNext();) {
                hasAtts = true;
                final Attribute attribute = (Attribute)it.next();
                for (int i = 0, attCount = ((ElementNode)mWtx.getNode()).getAttributeCount(); i < attCount; i++) {
                    mWtx.moveToAttribute(i);
                    if (attribute.getName().equals(mWtx.getQNameOfCurrentNode())
                        && attribute.getValue().equals(mWtx.getValueOfCurrentNode())) {
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
            for (final Iterator<?> namespIt = mEvent.getNamespaces(); namespIt.hasNext();) {
                hasNamesps = true;
                final Namespace namespace = (Namespace)namespIt.next();
                for (int i = 0, namespCount = ((ElementNode)mWtx.getNode()).getNamespaceCount(); i < namespCount; i++) {
                    mWtx.moveToNamespace(i);
                    if (namespace.getNamespaceURI().equals(mWtx.nameForKey(mWtx.getNode().getURIKey()))
                        && namespace.getPrefix().equals(mWtx.nameForKey(mWtx.getNode().getNameKey()))) {
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

        System.out.print("Shredding '" + args[0] + "' to '" + args[1] + "' ... ");
        final long time = System.currentTimeMillis();
        final File target = new File(args[1]);

        try {
            Database.createDatabase(new DatabaseConfiguration(target));
            final IDatabase db = Database.openDatabase(target);
            final ISession session = db.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            mFile = new File(args[0]);
            final XMLEventReader reader = createReader(null);
            final XMLUpdateShredder shredder = new XMLUpdateShredder(wtx, reader, true);
            shredder.call();

            wtx.close();
            session.close();
            db.close();
        } catch (final TreetankException e) {
            LOGWRAPPER.error(e);
        } catch (final IOException e) {
            LOGWRAPPER.error(e);
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e);
        }

        System.out.println(" done [" + (System.currentTimeMillis() - time) + "ms].");
    }
}

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
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.LoggerFactory;

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
import com.treetank.utils.LogWrapper;

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
    private static final LogWrapper LOGWRAPPER =
        new LogWrapper(LoggerFactory.getLogger(XMLUpdateShredder.class));

    // ===================== Initial setup ================

    /** Determines if the nodes match or not. */
    private transient boolean mIsSame;

    /** Levels to go up to the parent after nodes are not equal (insert). */
    private transient int mLevelsUp;

    /** Last position of mWtx before the current. */
    private transient long mLastPosKey;

    /** Level in the current subtree, which is going to be inserted. */
    private transient int mInsertLevel;

    /** Level where the parser is in the file to shredder. */
    private transient int mLevelInToShredder;

    /** Level where the cursor is in the shreddered file. */
    private transient int mLevelInShreddered;

    /** Insert node at the top of a subtree. */
    private transient boolean mInsertAtTop;

    /** Determines if an element has been inserted immediately before. */
    private transient boolean mInsertedElement;

    /**
     * Levels to move up after inserts at some level and further inserts on a
     * level above the current level.
     */
    private transient int mLevelsUpAfterInserts;

    /** Determines if the cursor has to move up some levels during inserts. */
    private transient boolean mMoveUp;

    /** Determines if a node or nodes have been deleted immediately before. */
    private transient boolean mRemoved;

    /** Node key. */
    private transient long mNodeKey;

    /** Determines if a node is found in the Treetank storage or not. */
    private transient boolean mFound;

    /**
     * This stack is for holding the current position to determine if an
     * insertAsRightSibling() or insertAsFirstChild() should occure.
     */
    private transient FastStack<Long> mLeftSiblingKeyStack;

    /**
     * Determines if it's a right sibling from the currently parsed node, where
     * the parsed node and the node in the Treetank storage match.
     */
    private transient boolean mIsRightSibling;

    /**
     * The key of the node, when the nodes are equal if at all (used to check
     * right siblings and therefore if nodes have been deleted).
     */
    private transient long mKeyMatches;

    /** Maximum node key in revision. */
    private transient long mMaxNodeKey;

    /** Determines if it's the last node or not. */
    private transient boolean mIsLastNode;

    /** Determines if an insert occured. */
    private transient boolean mInsert;

    /** Determines if changes should be commited. */
    private transient boolean mCommit;

    /** {@link XMLEventParser} used to check descendants. */
    private transient XMLEventReader mParser;

    /** Determines how many {@link XMLEvent}s currently have been parsed. */
    private transient long mElemsParsed;

    /** File to parse. */
    protected transient File mFile;

    /** Events to parse. */
    protected transient List<XMLEvent> mEvents;

    /** {@link QName} of root node from which to shredder the subtree. */
    private transient QName mRootElem;

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
     * @param paramData
     *            The data the update shredder operates on. Either a {@link List} of {@link XMLEvent}s or a
     *            {@link File}.
     * @param paramCommit
     *            Determines if changes should be commited.
     * @throws TreetankUsageException
     *             If insertasfirstChild && updateOnly is both true OR if wtx is
     *             not pointing to doc-root and updateOnly= true
     * @throws TreetankIOException
     *             If Treetank cannot access node keys.
     * 
     */
    @SuppressWarnings("unchecked")
    public XMLUpdateShredder(final IWriteTransaction paramWtx, final XMLEventReader paramReader,
        final boolean paramAddAsFirstChild, final Object paramData, final boolean paramCommit)
        throws TreetankUsageException, TreetankIOException {
        super(paramWtx, paramReader, paramAddAsFirstChild);
        mMaxNodeKey = mWtx.getMaxNodeKey();
        mCommit = paramCommit;

        if (paramData instanceof File) {
            mFile = (File)paramData;
        } else if (paramData instanceof List<?>) {
            mEvents = (List<XMLEvent>)paramData;
        }
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
        if (mCommit) {
            mWtx.commit();
        }
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

            // Initialize variables.
            mLevelInToShredder = 0;
            mLevelInShreddered = 0;
            mElemsParsed = 0;
            mIsLastNode = false;
            mMoveUp = false;
            mRemoved = false;
            mInsertAtTop = true;
            boolean firstEvent = true;

            // If structure already exists, make a sync against the current structure.
            if (mMaxNodeKey != 0) {
                if (mWtx.getNode().getKind() == ENodes.ROOT_KIND) {
                    // Find the start key for the update operation.
                    long startkey = (Long)EFixed.ROOT_NODE_KEY.getStandardProperty() + 1;
                    while (!mWtx.moveTo(startkey)) {
                        startkey++;
                    }
                }

                XMLEvent event = null;

                // Iterate over all nodes.
                while (mReader.hasNext()) {
                    // Parsing the next event.
                    if (mRemoved) {
                        // Do not move StAX parser forward if nodes have been deleted before.
                        // mLevelInToShredder--;
                        // mLevelInShreddered--;
                        mRemoved = false;
                    } else {
                        // After an insert or after nodes were equal.
                        event = mReader.nextEvent();
                        mElemsParsed++;

                        if (event.isCharacters() && event.asCharacters().isWhiteSpace()) {
                            continue;
                        }

                        assert event != null;
                        if (firstEvent) {
                            // Setup start element from StAX parser.
                            firstEvent = false;

                            if (event.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                                while (mReader.hasNext()
                                    && event.getEventType() != XMLStreamConstants.START_ELEMENT) {
                                    event = mReader.nextEvent();
                                }
                                assert event.getEventType() == XMLStreamConstants.START_ELEMENT;
                                mElemsParsed++;
                            }
                            if (event.getEventType() != XMLStreamConstants.START_ELEMENT) {
                                throw new IllegalStateException(
                                    "StAX parser has to be on START_DOCUMENT or START_ELEMENT event!");
                            }

                            // Get root element of subtree or whole XML document to shredder.
                            mRootElem = event.asStartElement().getName();
                        } else if (event.isEndElement() && mRootElem.equals(event.asEndElement().getName())
                            && mLevelInToShredder == 1) {
                            // End with shredding if end_elem equals root-elem.
                            break;
                        }
                    }

                    assert event != null;

                    switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        // Log debugging messages.
                        LOGWRAPPER.debug("TO SHREDDER: " + ((StartElement)event).getName());
                        if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                            LOGWRAPPER.debug("SHREDDERED: " + mWtx.getQNameOfCurrentNode());
                        } else {
                            LOGWRAPPER.debug("SHREDDERED: " + mWtx.getValueOfCurrentNode());
                        }

                        // Initialize variables.
                        initializeVars();

                        // Increment levels.
                        mLevelInToShredder++;
                        mLevelInShreddered++;

                        algorithm(event);

                        if (mFound && mIsRightSibling) {
                            deleteNode();
                        } else if (!mFound) {
                            insertElementNode(event);
                        } else if (mFound) {
                            sameElementNode();
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        // Initialize variables.
                        initializeVars();

                        final String text = ((Characters)event).getData().trim();
                        if (!text.isEmpty()) {
                            algorithm(event);

                            if (mFound && mIsRightSibling) {
                                deleteNode();
                            } else if (!mFound) {
                                insertTextNode(event);
                            } else if (mFound) {
                                sameTextNode();
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        endElement();
                        break;
                    default:
                        // Other nodes which are currently not supported by Treetank.
                    }
                }

                if (!mIsLastNode) {
                    if (mInsert && mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                        /*
                         * Remove next node after node, which was inserted, because it must have been deleted.
                         * Note: Cursor is located at the inserted node if it's an element node, otherwise
                         * it's already located at the right sibling.
                         */
                        if (mWtx.moveToRightSibling()) {
                            mWtx.remove();
                        }
                    } else {
                        // Remove current node (cursor has been moved to the next node already).
                        mWtx.remove();
                    }

                    // Also remove any siblings.
                    boolean hasRightSibling = false;
                    while (((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
                        hasRightSibling = true;
                        mWtx.remove();
                    }
                    if (hasRightSibling) {
                        mWtx.remove();
                    }
                }
                mReader.close();
            } else {
                // If no content is in the XML, a normal insertNewContent is executed.
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
        mKeyMatches = -1;
    }

    /**
     * Process an end element.
     * 
     * @throws XMLStreamException
     *             In case any parsing exception occurs.
     */
    private void endElement() throws XMLStreamException {
        mLevelInToShredder--;
        mLevelsUpAfterInserts++;
        mInsertAtTop = false; // TODO

        if (!mIsSame && !mRemoved) {
            // An insert has been made before, so move to parent and update stack.
            mLevelsUp = 0;
            if (mInsertLevel >= 0) {
                mInsertLevel--;
                mLevelInShreddered--;
                if (!mLeftSiblingKeyStack.empty()) {
                    mLeftSiblingKeyStack.pop();

                    if (!mLeftSiblingKeyStack.empty()) {
                        mWtx.moveTo(mLeftSiblingKeyStack.peek());
                    }
                }

                LOGWRAPPER.debug("END ELEM: " + mWtx.getQNameOfCurrentNode());

                if (mInsertLevel == 0 || mMoveUp) {
                    checkNextNode();
                }
            } else {
                // Inserts go on a lower level after some inserts have occured immediately before.
                mMoveUp = true;
            }
        } else {
            mLevelsUp++;
        }
    }

    /**
     * Check if move to element before any inserts occured would move to a node which equals the next node of
     * the next event. If not move back to current top element on stack (which is the parent of the currently
     * inserted node).
     * 
     * @return true if move to node before insert in shreddered file equals next node in the file to shredder.
     * @throws XMLStreamException
     *             In case of any parsing error.
     */
    private boolean checkNextNode() throws XMLStreamException {
        boolean retVal = false;
        final XMLEvent xmlEvent = skipWhitespaces();

        if (xmlEvent.getEventType() != XMLStreamConstants.END_ELEMENT && !mLeftSiblingKeyStack.empty()) {
            // Move to the node before an insert occured.
            final long keyOnStack = mLeftSiblingKeyStack.peek();
            final long keyInShreddered = mWtx.getNode().getNodeKey();
            mLeftSiblingKeyStack.pop();

            if (mLeftSiblingKeyStack.empty()) {
                // Stack is empty, so the node(s) was/were inserted
                // somewhere at the top of the XML file.
                /*
                 * Check if right sibling after an insert equals next event.
                 */
                if (mWtx.moveToRightSibling()) {
                    if (checkElement((StartElement)xmlEvent)) {
                        retVal = true;
                    } else {
                        mWtx.moveToLeftSibling();
                    }
                } else {
                    // Move to a parent which has the next right sibling in pre order.
                    while (!((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
                        // Move to parent node.
                        mWtx.moveToParent();
                    }
                    mWtx.moveToRightSibling();
                }

                mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
            } else {
                // Stack is not empty.
                mWtx.moveTo(mLeftSiblingKeyStack.peek());
                switch (xmlEvent.getEventType()) {
                case XMLStreamConstants.CHARACTERS:
                    final String data = ((Characters)xmlEvent).getData().trim();

                    if (!data.isEmpty() && mWtx.getNode().getKind() == ENodes.TEXT_KIND
                        && mWtx.getValueOfCurrentNode().equals(data)) {
                        retVal = true;
                    } else {
                        mWtx.moveTo(keyInShreddered);
                        mLeftSiblingKeyStack.push(keyOnStack);
                    }
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    if (checkElement((StartElement)xmlEvent)) {
                        retVal = true;
                    } else {
                        mWtx.moveTo(keyInShreddered);
                        mLeftSiblingKeyStack.push(keyOnStack);
                    }
                    break;
                default:
                    // Do nothing.
                }
            }
        }

        return retVal;
    }

    /**
     * Insert an element node.
     * 
     * @param paramEvent
     *            Event, which is currently being parsed.
     * @throws TreetankException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     */
    private void insertElementNode(final XMLEvent paramEvent) throws TreetankException {
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

            // Update stack, pop current element.
            assert !mLeftSiblingKeyStack.empty();
            mLeftSiblingKeyStack.pop();
            mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());

            mLeftSiblingKeyStack = addNewElement(false, mLeftSiblingKeyStack, paramEvent.asStartElement());

            updateLastPosKey();

            // mLevelsUp = 0;
            mLevelsUpAfterInserts = 0;
            mInsertAtTop = false;
        } else if (mLevelsUp > 0) {
            levelsUp();

            // Insert new node as right sibling.
            mLeftSiblingKeyStack = addNewElement(false, mLeftSiblingKeyStack, paramEvent.asStartElement());

            updateLastPosKey();

            mLevelsUpAfterInserts = 0;
        } else if (mInsertLevel >= 0) {
            if (mMoveUp) {
                // After a first insert.
                moveUp();
                assert mInsertLevel == 1;
            }

            mLeftSiblingKeyStack = addNewElement(false, mLeftSiblingKeyStack, paramEvent.asStartElement());

            updateLastPosKey();

            mLevelsUpAfterInserts = 0;
        }
    }

    /**
     * Insert a text node.
     * 
     * @param mEvent
     *            XMLStream event from the StAX parser.
     * @throws TreetankException
     *             In case of any error while inserting the node.
     * @throws XMLStreamException
     *             If StAX failes to parse some XML fragment.
     * 
     */
    private void insertTextNode(final XMLEvent mEvent) throws TreetankException, XMLStreamException {
        mInsert = true;
        if (mMoveUp) {
            moveUp();
            assert mInsertLevel == 0;
        } else if (mLevelsUp > 0 && !mInsertedElement) {
            /*
             * If a text node on it's own (without a previous element node has been
             * inserted) move back to the last position key and insert as a right
             * sibling.
             */
            levelsUp();
        } else if (mInsertAtTop) {
            mInsertAtTop = false;
            mLastPosKey = mWtx.getNode().getNodeKey();
            mWtx.moveToParent();
            assert !mLeftSiblingKeyStack.empty();

            // Make sure a NULL_NODE_KEY is on top of the stack (insert as first child).
            if (mLeftSiblingKeyStack.peek() != (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
            }
        }

        mIsSame = false;

        // Insert text.
        mLeftSiblingKeyStack = addNewText(mLeftSiblingKeyStack, (Characters)mEvent);

        /*
         * If right sibling of current node is the next event move cursor and
         * update stack.
         */
        if (((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
            checkRightSibling(true);
        }
    }

    /**
     * In case they are the same nodes move cursor to next node and update
     * stack.
     */
    private void sameTextNode() {
        // Update variables.
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
        while (!((AbsStructNode)mWtx.getNode()).hasRightSibling() && !mLeftSiblingKeyStack.empty()) {
            // Move to parent element node and update stack.
            mLevelInShreddered--;
            mWtx.moveTo(mLeftSiblingKeyStack.peek());
            mLeftSiblingKeyStack.pop();
        }
        if (!mWtx.moveToRightSibling()) {
            mIsLastNode = true;
        }

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
     * @param paramEvent
     *            The currently parsed StAX event.
     * @throws IOException
     *             In case the open operation fails (delegated from
     *             checkDescendants(...)).
     * @throws XMLStreamException
     *             In case any StAX parser problem occurs.
     */
    private void algorithm(final XMLEvent paramEvent) throws IOException, XMLStreamException {
        if (mLevelInToShredder < mLevelInShreddered) {
            /*
             * Node or nodes were removed on a level which is higher than the
             * current one.
             */
            mKeyMatches = -1;
            mFound = true;
            mIsRightSibling = true;

            while (((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
                mKeyMatches = mWtx.getNode().getNodeKey();
                mWtx.moveToRightSibling();
            }
        } else {
            do {
                /*
                 * Check if a node in the shreddered file on the same level
                 * equals the current element node.
                 */
                if (paramEvent.isStartElement()) {
                    mFound = checkElement(paramEvent.asStartElement());
                } else if (paramEvent.isCharacters()) {
                    mFound = checkText(paramEvent.asCharacters());
                }
                if (mWtx.getNode().getNodeKey() != mNodeKey) {
                    mIsRightSibling = true;
                }

                mKeyMatches = mWtx.getNode().getNodeKey();

                if (mFound && mIsRightSibling) {
                    /*
                     * Root element of next subtree in shreddered file matches
                     * so check all descendants. If they match the node must be
                     * inserted.
                     */
                    if (paramEvent.isStartElement()) {
                        mFound = checkDescendants(paramEvent.asStartElement(), true);
                    } else if (paramEvent.isCharacters()) {
                        mFound = checkText(paramEvent.asCharacters());
                    }
                    mWtx.moveTo(mKeyMatches);
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
    private void sameElementNode() throws TreetankIOException {
        // Update variables.
        mLevelsUpAfterInserts = 0;
        mInsertLevel = 0;
        mMoveUp = false;
        mRemoved = false;
        mLevelsUp = 0;
        mIsSame = true;
        mInsertedElement = false;
        mInsert = false;

        // Log debugging messages.
        LOGWRAPPER.debug("FOUND: " + mWtx.getQNameOfCurrentNode() + mWtx.getNode().getNodeKey());

        // Update last position key.
        mLastPosKey = mWtx.getNode().getNodeKey();

        // Move transaction.
        final ElementNode element = (ElementNode)mWtx.getNode();
        if (element.hasFirstChild()) {
            mInsertAtTop = true;
            // Update stack.
            if (mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                // Remove NULL.
                mLeftSiblingKeyStack.pop();
            }
            mWtx.moveToFirstChild();
        } else if (element.hasRightSibling()) {
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
     * Delete node.
     * 
     * @throws TreetankException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     */
    private void deleteNode() throws TreetankException {
        /*
         * If found in one of the rightsiblings in the current shreddered
         * structure remove all nodes until the transaction points to the found
         * node (keyMatches).
         */
        mIsSame = false;
        mInsertedElement = false;
        mRemoved = true;

        if (mInsert) {
            if (((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
                // Cursor is on the inserted node, so move to right sibling.
                mWtx.moveToRightSibling();
                // Remove inserted node from stack.
                mLeftSiblingKeyStack.pop();
            }
        }

        boolean moveToParents = false;

        do {
            if (mWtx.getNode().getNodeKey() != mKeyMatches) {
                if (!((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
                    moveToParents = true;
                    mLevelInShreddered--;
                }
                mWtx.remove();

                // Update stack.
                if (mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
                    mLeftSiblingKeyStack.pop();
                }

                mLeftSiblingKeyStack.pop();
                mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
                mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
            }
        } while (mWtx.getNode().getNodeKey() != mKeyMatches
            && ((AbsStructNode)mWtx.getNode()).hasRightSibling() && mLevelInToShredder < mLevelInShreddered);
        // Move up anchestors if there is no former right sibling.
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
     * @param paramText
     *            Determines if a text node has been inserted.
     * @return true if nodes are equal othwerwise false.
     * @throws XMLStreamException
     *             In case the xml parser encounters an error.
     */
    private boolean checkRightSibling(final boolean paramText) throws XMLStreamException {
        boolean retVal = false;
        mWtx.moveToRightSibling();
        final XMLEvent xmlEvent = skipWhitespaces();

        switch (xmlEvent.getEventType()) {
        case XMLStreamConstants.CHARACTERS:
            final String data = xmlEvent.asCharacters().getData().trim();

            if (!data.isEmpty() && mWtx.getNode().getKind() == ENodes.TEXT_KIND
                && mWtx.getValueOfCurrentNode().equals(data)) {
                retVal = true;
            } else {
                mWtx.moveToLeftSibling();
            }
            break;
        case XMLStreamConstants.START_ELEMENT:
            if (checkElement(xmlEvent.asStartElement())) {
                retVal = true;
            } else {
                mWtx.moveToLeftSibling();
            }
            break;
        default:
            // Do nothing (other nodes currently not supported by Treetank).
        }

        // Update stack.
        // Remove inserted text node.
        if (paramText) {
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
        /*
         * Check if next event equals node on top of stack. Ignore all whitespace between elements.
         */
        while (mReader.hasNext()
            && mReader.peek().getEventType() == XMLStreamConstants.CHARACTERS
            && (mReader.peek().asCharacters().isIgnorableWhiteSpace() || mReader.peek().asCharacters()
                .isWhiteSpace())) {
            mReader.nextEvent();
            mElemsParsed++;
        }

        return mReader.peek();
    }

    /**
     * Move cursor back to last position key and then up mLevelsUp-times.
     */
    private void levelsUp() {
        mWtx.moveTo(mLastPosKey);

        final int levelsUp = mWtx.getNode().getKind() == ENodes.ELEMENT_KIND ? mLevelsUp - 1 : mLevelsUp;

        // Move up levels to the right parent.
        for (int i = 0; i < levelsUp; i++) {
            mWtx.moveToParent();
        }

        /*
         * Make sure that it's inserted as a right sibling.
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
     * Move up levels if inserts have occured immediately before and now inserts go
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
     * Check if descendants match. Beware of the fact that you have to move the write transaction to the
     * node it was before this method has been invoked.
     * 
     * @param paramElem
     *            The start element where the StAX parser currently is.
     * @param paramFirst
     *            Determines if it is the first call the method is invoked (a
     *            new StAX parser
     * @return true if they match, otherwise false.
     * @throws XMLStreamException
     *             In case of any streamining exception in the source document.
     * @throws IOException
     *             In case of any I/O exception while opening the target file.
     */
    private boolean checkDescendants(final StartElement paramElem, final boolean paramFirst)
        throws XMLStreamException, IOException {
        boolean found = false;

        if (paramFirst) {
            /*
             * Setup new StAX parser and move it to the node, where the current
             * StAX parser is.
             */
            if (mFile != null) {
                mParser = createReader(mFile);
            } else if (mEvents != null) {
                mParser = createListReader(mEvents);
            } else {
                throw new IllegalStateException("Your XMLEventReader implementation is not supported!");
            }

            long elemsParsed = 0;
            XMLEvent event = null;
            do {
                event = mParser.nextEvent();
                elemsParsed++;
            } while (mParser.hasNext() && elemsParsed != mElemsParsed);

            assert event != null && event.isStartElement()
                && event.asStartElement().getName().equals(paramElem.getName());
        }

        assert mParser != null;

        // Move write transaction to next node.
        boolean moved = false;
        if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
            final ElementNode element = (ElementNode)mWtx.getNode();

            if (element.hasFirstChild()) {
                moved = mWtx.moveToFirstChild();
            } else if (element.hasRightSibling()) {
                moved = mWtx.moveToRightSibling();
            } else {
                do {
                    mWtx.moveToParent();
                } while (!((AbsStructNode)mWtx.getNode()).hasRightSibling());

                if (((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
                    moved = mWtx.moveToRightSibling();
                }
            }
        }

        // Check if nodes are equal.
        if (moved) {
            if (mParser.hasNext()) {
                final XMLEvent event = mParser.nextEvent();

                switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    found = checkElement(event.asStartElement());
                    break;
                case XMLStreamConstants.CHARACTERS:
                    found = checkText(event.asCharacters());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    found = mWtx.moveToParent();
                    break;
                default:
                    // Do nothing.
                }

                if (found) {
                    checkDescendants(paramElem, false);
                }
            }
        }

        mParser.close();
        return found;
    }

    /**
     * Check if start element of two StAX parsers match.
     * 
     * @param mStartTag
     *            StartTag of the StAX parser, where it is currently (the "real"
     *            StAX parser over the whole document).
     * @param mElem
     *            StartTag to check against.
     * @return True if start elements match.
     * @throws XMLStreamException
     *             handling XML Stream Exception
     */
    static boolean checkStAXStartElement(final StartElement mStartTag, final StartElement mElem)
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

            // Check if qname, atts and namespaces are the same.
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
            final XMLEventReader reader = createReader(new File(args[0]));
            final XMLUpdateShredder shredder =
                new XMLUpdateShredder(wtx, reader, true, new File(args[0]), true);
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

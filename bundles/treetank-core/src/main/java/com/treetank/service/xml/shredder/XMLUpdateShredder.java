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
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
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
import com.treetank.utils.TypedValue;

/**
 * 
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class XMLUpdateShredder extends XMLShredder implements Callable<Long> {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER =
        new LogWrapper(LoggerFactory.getLogger(XMLUpdateShredder.class));

    /** File to parse. */
    protected transient File mFile;

    /** Events to parse. */
    protected transient List<XMLEvent> mEvents;

    /** Determines if the nodes match or not. */
    private transient boolean mIsSame;

    /** Node key. */
    private transient long mNodeKey;

    /** Determines if a node is found in the Treetank storage or not. */
    private transient boolean mFound;

    /** Determines if an insert occured. */
    private transient boolean mInsert;

    /** Determines if an end tag has been read while inserting nodes. */
    private transient boolean mInsertedEndTag;

    /** Determines if node has to be inserted at the top of a subtree. */
    private transient boolean mInsertAtTop;

    /** Determines if transaction has been moved to right sibling. */
    private transient boolean mMovedToRightSibling;

    /**
     * Determines if it's a right sibling from the currently parsed node, where
     * the parsed node and the node in the Treetank storage match.
     */
    private transient boolean mIsRightSibling;

    /** Last node key. */
    private transient long mLastNodeKey;

    /**
     * The key of the node, when the nodes are equal if at all (used to check
     * right siblings and therefore if nodes have been deleted).
     */
    private transient long mKeyMatches;

    /** Maximum node key in revision. */
    private transient long mMaxNodeKey;

    /** Determines if changes should be commited. */
    private transient boolean mCommit;

    /** {@link XMLEventParser} used to check descendants. */
    private transient XMLEventReader mParser;

    /** Determines how many {@link XMLEvent}s currently have been parsed. */
    private transient long mElemsParsed;

    /** Level where the parser is in the file to shredder. */
    private transient int mLevelInToShredder;

    /** Level where the cursor is in the shreddered file. */
    private transient int mLevelInShreddered;

    /** {@link QName} of root node from which to shredder the subtree. */
    private transient QName mRootElem;

    /** Determines if it's the last node or not. */
    private transient boolean mIsLastNode;

    /** Determines if a node or nodes have been deleted immediately before. */
    private transient boolean mRemoved;

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
    public final Long call() throws TreetankException {
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
            // Initialize variables.
            mLevelInToShredder = 0;
            mLevelInShreddered = 0;
            mElemsParsed = 0;
            mIsLastNode = false;
            mRemoved = false;
            mMovedToRightSibling = false;
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
                        } else if (event != null && event.isEndElement()
                            && mRootElem.equals(event.asEndElement().getName()) && mLevelInToShredder == 1) {
                            // End with shredding if end_elem equals root-elem.
                            break;
                        }
                    }

                    assert event != null;

                    switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        processStartTag(event.asStartElement());
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        processCharacters(event.asCharacters());
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        processEndTag();
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
     * Process start tag.
     * 
     * @param paramElem
     *            {@link StartElement} currently parsed.
     * @throws XMLStreamException
     *             In case of any StAX parsing error.
     * @throws IOException
     *             In case of any I/O error.
     * @throws TreetankException
     *             In case of any Treetank error.
     */
    private void processStartTag(final StartElement paramElem) throws IOException, XMLStreamException,
        TreetankException {
        // Log debugging messages.
        LOGWRAPPER.debug("TO SHREDDER: " + paramElem.getName());
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

        algorithm(paramElem);

        if (mFound && mIsRightSibling) {
            // deleteNode();
        } else if (!mFound) {
            insertElementNode(paramElem);
        } else if (mFound) {
            sameElementNode();
        }
    }

    /**
     * Process characters.
     * 
     * @param paramText
     *            {@link Characters} currently parsed.
     * @throws XMLStreamException
     *             In case of any StAX parsing error.
     * @throws IOException
     *             In case of any I/O error.
     * @throws TreetankException
     *             In case of any Treetank error.
     */
    private void processCharacters(final Characters paramText) throws IOException, XMLStreamException,
        TreetankException {
        initializeVars();
        final String text = paramText.getData().trim();
        if (!text.isEmpty()) {
            algorithm(paramText);

            if (mFound && mIsRightSibling) {
                // deleteNode();
            } else if (!mFound) {
                insertTextNode(paramText);
            } else if (mFound) {
                sameTextNode();
            }
        }
    }

    /**
     * Process end tag.
     * 
     * @throws XMLStreamException
     *             In case of any parsing error.
     * @throws TreetankIOException
     *             In case or any Treetank I/O error.
     */
    private void processEndTag() throws XMLStreamException, TreetankIOException {
        mLevelInToShredder--;
        mLevelInShreddered--;

        if (mInsert) {
            mInsertedEndTag = true;
        }

        // Move cursor to parent.
        if (mWtx.getNode().getNodeKey() == mLastNodeKey) {
            /*
             * An end tag must have been parsed immediately before and it must have been an empty element at
             * the end of a subtree.
             */
            assert mWtx.getNode().hasParent() && mWtx.getNode().getKind() == ENodes.ELEMENT_KIND;
            mWtx.moveToParent();
        } else {
            if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                final ElementNode element = (ElementNode)mWtx.getNode();
                if (element.hasFirstChild() && element.hasParent()) {
                    // It's not an empty element, thus move to parent.
                    mWtx.moveToParent();
                }
            } else if (((AbsStructNode)mWtx.getNode()).hasParent()) {
                mWtx.moveToParent();
            }
        }

        mLastNodeKey = mWtx.getNode().getNodeKey();

        // Move cursor to right sibling if it has one.
        if (((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
            mWtx.moveToRightSibling();
            mMovedToRightSibling = true;
        } else {
            mMovedToRightSibling = false;
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

    /**
     * Check if text event and text in Treetank storage match.
     * 
     * @param paramEvent
     *            {@link XMLEvent}.
     * @return true if they match, otherwise false.
     */
    private boolean checkText(final Characters paramEvent) {
        final String text = paramEvent.getData().trim();
        return mWtx.getNode().getKind() == ENodes.TEXT_KIND && mWtx.getValueOfCurrentNode().equals(text);
    }

    /**
     * In case they are the same nodes move cursor to next node and update
     * stack.
     * 
     * @throws TreetankIOException
     *             In case of any Treetank error.
     * @throws XMLStreamException
     *             In case of any StAX parsing error.
     */
    private void sameTextNode() throws TreetankIOException, XMLStreamException {
        // Update variables.
        mRemoved = false;
        mIsSame = true;
        mInsert = false;
        mInsertAtTop = false;
        mInsertedEndTag = false;

        // Check if last node reached.
        checkIfLastNode();

        // Move to right sibling if next node isn't an end tag.
        if (mReader.peek().getEventType() != XMLStreamConstants.END_ELEMENT) {
            if (mWtx.moveToRightSibling()) {
                mMovedToRightSibling = true;
            } else {
                mMovedToRightSibling = false;
            }
        }
    }

    /**
     * Check if it's the last node in the shreddered file and modify flag mIsLastNode
     * if it is the last node.
     */
    private void checkIfLastNode() {
        // Last node or not?
        int level = mLevelInShreddered;
        if (level > 1) {
            final long nodeKey = mWtx.getNode().getNodeKey();
            while (!((AbsStructNode)mWtx.getNode()).hasRightSibling() && level != 1) {
                mWtx.moveToParent();
                level--;
                if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND
                    && mWtx.getQNameOfCurrentNode().equals(mRootElem) && level == 1) {
                    mIsLastNode = true;
                    break;
                }
            }
            mWtx.moveTo(nodeKey);
        }
    }

    /**
     * Nodes match, thus update stack and move cursor to first child if it is not a leaf node.
     * 
     * @throws TreetankIOException
     *             In case Treetank cannot read the max node key.
     */
    private void sameElementNode() throws TreetankIOException {
        // Update variables.
        mRemoved = false;
        mIsSame = true;
        mInsert = false;
        mInsertAtTop = false;
        mInsertedEndTag = false;

        // Check if last node reached.
        checkIfLastNode();

        // Log debugging messages.
        LOGWRAPPER.debug("FOUND: " + mWtx.getQNameOfCurrentNode() + mWtx.getNode().getNodeKey());

        // Move transaction.
        final ElementNode element = (ElementNode)mWtx.getNode();
        if (element.hasFirstChild()) {
            mInsertAtTop = true;
            mWtx.moveToFirstChild();
        }
    }

    /**
     * Insert an element node.
     * 
     * @param paramElement
     *            {@link StartElement}, which is going to be inserted.
     * @throws TreetankException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     * @throws XMLStreamException
     *             In case of any StAX parsing error.
     */
    private void insertElementNode(final StartElement paramElement) throws TreetankException,
        XMLStreamException {
        /*
         * Add node if it's either not found among right siblings (and the
         * cursor on the shreddered file is on a right sibling) or if it's not
         * found in the structure and it is a new last right sibling.
         */
        mIsSame = false;
        mRemoved = false;

        if (mInsertAtTop) {
            // We are at the top of a subtree, no end tag has been parsed before.
            mInsertAtTop = false;

            // Has to be inserted on the parent node.
            mWtx.moveToParent();

            // Insert element as first child.
            addNewElement(false, true, paramElement);
        } else if (mInsert) {
            // Inserts have been made before.
            boolean insertAsFirstChild = true;

            if (mInsertedEndTag) {
                /*
                 * An end tag has been read while inserting, thus insert node as right sibling of parent node.
                 */
                insertAsFirstChild = false;
                mInsertedEndTag = false;
            }

            // Possibly move one sibling back if transaction already moved to next node.
            if (mMovedToRightSibling) {
                mWtx.moveToLeftSibling();
            }
            
            // Make sure if transaction is on a text node the node is inserted as a right sibling.
            if (mWtx.getNode().getKind() == ENodes.TEXT_KIND) {
                insertAsFirstChild = false;
            }

            addNewElement(false, insertAsFirstChild, paramElement);
        } else {
            // Insert occurs at the middle or end of a subtree.

            // Move one sibling back.
            if (mMovedToRightSibling) {
                mWtx.moveToLeftSibling();
                mMovedToRightSibling = false;
            }

            // Insert element as right sibling.
            addNewElement(false, false, paramElement);
        }

        mInsert = true;
    }

    /**
     * Insert a text node.
     * 
     * @param paramText
     *            {@link Characters}, which is going to be inserted.
     * @throws TreetankException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     * @throws XMLStreamException
     *             In case of any StAX parsing error.
     */
    private void insertTextNode(final Characters paramText) throws TreetankException, XMLStreamException {
        /*
         * Add node if it's either not found among right siblings (and the
         * cursor on the shreddered file is on a right sibling) or if it's not
         * found in the structure and it is a new last right sibling.
         */
        mIsSame = false;
        mRemoved = false;

        if (mInsertAtTop) {
            // Insert occurs at the top of a subtree (no end tag has been parsed immediately before).
            mInsertAtTop = false;

            // Move to parent.
            mWtx.moveToParent();

            // Insert as first child.
            addNewText(true, paramText);

            // Move to next node if no end tag follows (thus cursor isn't moved to parent in processEndTag()).
            if (mReader.peek().getEventType() != XMLStreamConstants.END_ELEMENT) {
                if (mWtx.moveToRightSibling()) {
                    mMovedToRightSibling = true;
                } else {
                    mMovedToRightSibling = false;
                }
            }
        } else if (mInsert) {
            // Inserts have been made before.
            boolean insertAsFirstChild = true;

            if (mInsertedEndTag) {
                /*
                 * An end tag has been read while inserting, so move back to left sibling if there is one and
                 * insert as right sibling.
                 */
                if (mMovedToRightSibling) {
                    mWtx.moveToLeftSibling();
                }
                insertAsFirstChild = false;
                mInsertedEndTag = false;
            }

            // Insert element as right sibling.
            addNewText(insertAsFirstChild, paramText);

            // Move to next node if no end tag follows (thus cursor isn't moved to parent in processEndTag()).
            if (mReader.peek().getEventType() != XMLStreamConstants.END_ELEMENT) {
                if (mWtx.moveToRightSibling()) {
                    mMovedToRightSibling = true;
                } else {
                    mMovedToRightSibling = false;
                }
            }
        } else {
            // Insert occurs in the middle or end of a subtree.

            // Move one sibling back.
            if (mMovedToRightSibling) {
                mWtx.moveToLeftSibling();
            }

            // Insert element as right sibling.
            addNewText(false, paramText);

            // Move to next node.
            mWtx.moveToRightSibling();
        }

        mInsert = true;
    }

    /**
     * Delete node.
     * 
     * @throws TreetankException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     */
    private void deleteNode() throws TreetankException {
        // /*
        // * If found in one of the rightsiblings in the current shreddered
        // * structure remove all nodes until the transaction points to the found
        // * node (keyMatches).
        // */
        // mIsSame = false;
        // mRemoved = true;
        //
        // if (mInsert) {
        // if (((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
        // // Cursor is on the inserted node, so move to right sibling.
        // mWtx.moveToRightSibling();
        // // Remove inserted node from stack.
        // mLeftSiblingKeyStack.pop();
        // }
        // }
        //
        // boolean moveToParents = false;
        //
        // do {
        // if (mWtx.getNode().getNodeKey() != mKeyMatches) {
        // if (!((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
        // moveToParents = true;
        // mLevelInShreddered--;
        // }
        // mWtx.remove();
        //
        // // Update stack.
        // if (mLeftSiblingKeyStack.peek() == (Long)EFixed.NULL_NODE_KEY.getStandardProperty()) {
        // mLeftSiblingKeyStack.pop();
        // }
        //
        // mLeftSiblingKeyStack.pop();
        // mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
        // mLeftSiblingKeyStack.push((Long)EFixed.NULL_NODE_KEY.getStandardProperty());
        // }
        // } while (mWtx.getNode().getNodeKey() != mKeyMatches
        // && ((AbsStructNode)mWtx.getNode()).hasRightSibling() && mLevelInToShredder < mLevelInShreddered);
        // // Move up anchestors if there is no former right sibling.
        // while (!((AbsStructNode)mWtx.getNode()).hasRightSibling()) {
        // moveToParents = true;
        // mWtx.moveToParent();
        // mLevelInShreddered--;
        //
        // // Update stack.
        // // Remove NULL.
        // if (mLeftSiblingKeyStack.peek() == EFixed.NULL_NODE_KEY.getStandardProperty()) {
        // mLeftSiblingKeyStack.pop();
        // }
        // mLeftSiblingKeyStack.pop();
        // }
        //
        // if (moveToParents) {
        // // Move to right sibling and update stack.
        // mWtx.moveToRightSibling();
        // mLeftSiblingKeyStack.pop();
        // mLeftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
        // }
        //
        // mInsert = false;
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
     * Add a new text node.
     * 
     * @param paramAsFirstChild
     *            If true text node is inserted as first child, otherwise as right sibling.
     * @param paramTextEvent
     *            The current event from the StAX parser.
     * @throws TreetankException
     *             In case anything went wrong.
     */
    protected final void addNewText(final boolean paramAsFirstChild, final Characters paramTextEvent)
        throws TreetankException {
        final String text = paramTextEvent.getData().trim();
        final ByteBuffer textByteBuffer = ByteBuffer.wrap(TypedValue.getBytes(text));
        if (textByteBuffer.array().length > 0) {
            if (paramAsFirstChild) {
                mWtx.insertTextAsFirstChild(new String(textByteBuffer.array()));
            } else {
                mWtx.insertTextAsRightSibling(new String(textByteBuffer.array()));
            }
        }
    }

    /**
     * Add a new element node.
     * 
     * @param paramFirstElement
     *            Is it the first element?
     * @param paramAsFirstChild
     *            If true element node is inserted as first child, otherwise as right sibling.
     * @param paramStartElement
     *            The current {@link StartElement} .
     * @throws TreetankException
     *             In case anything went wrong.
     */
    protected final void addNewElement(final boolean paramFirstElement, final boolean paramAsFirstChild,
        final StartElement paramStartElement) throws TreetankException {
        final QName name = paramStartElement.getName();
        long key;

        if (paramFirstElement) {
            key = mWtx.insertElementAsRightSibling(name);
        } else {
            if (paramAsFirstChild) {
                key = mWtx.insertElementAsFirstChild(name);
            } else {
                key = mWtx.insertElementAsRightSibling(name);
            }
        }

        // Parse namespaces.
        for (final Iterator<?> it = paramStartElement.getNamespaces(); it.hasNext();) {
            final Namespace namespace = (Namespace)it.next();
            mWtx.insertNamespace(namespace.getNamespaceURI(), namespace.getPrefix());
            mWtx.moveTo(key);
        }

        // Parse attributes.
        for (final Iterator<?> it = paramStartElement.getAttributes(); it.hasNext();) {
            final Attribute attribute = (Attribute)it.next();
            mWtx.insertAttribute(attribute.getName(), attribute.getValue());
            mWtx.moveTo(key);
        }
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

        // Matching element names?
        if (mWtx.getNode().getKind() == ENodes.ELEMENT_KIND
            && mWtx.getQNameOfCurrentNode().equals(mEvent.getName())) {
            // Check if atts and namespaces are the same.
            final long nodeKey = mWtx.getNode().getNodeKey();

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
            final OldUpdateShredder shredder =
                new OldUpdateShredder(wtx, reader, true, new File(args[0]), true);
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

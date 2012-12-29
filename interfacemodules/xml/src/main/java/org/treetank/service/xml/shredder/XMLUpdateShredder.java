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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.treetank.node.IConstants.ROOT_NODE;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.treetank.access.NodeWriteTrx;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.access.Storage;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.INodeWriteTrx;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.node.ElementNode;
import org.treetank.node.IConstants;
import org.treetank.node.TreeNodeFactory;
import org.treetank.node.interfaces.INameNode;
import org.treetank.node.interfaces.IStructNode;
import org.treetank.revisioning.IRevisioning.IRevisioningFactory;
import org.treetank.service.xml.StandardXMLSettings;
import org.treetank.utils.TypedValue;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * <h1>XMLUpdateShredder</h1>
 * 
 * <p>
 * Shredder, which updates a Treetank revision to the next revision, as thus it just inserts or deletes nodes,
 * which have been changed. Renames are treated as insert new node, delete old node.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLUpdateShredder extends XMLShredder implements Callable<Void> {

    /** File to parse. */
    protected transient File mFile;

    /** Events to parse. */
    protected transient List<XMLEvent> mEvents;

    /** Node key. */
    private transient long mNodeKey;

    /** Determines if a node is found in the Treetank storage or not. */
    private transient boolean mFound;

    /** Determines if an end tag has been read while inserting nodes. */
    private transient boolean mInsertedEndTag;

    /** Determines if transaction has been moved to right sibling. */
    private transient boolean mMovedToRightSibling;

    /** Determines if node has been removed at the end of a subtree. */
    private transient boolean mRemovedNode;

    /**
     * Determines if it's a right sibling from the currently parsed node, where
     * the parsed node and the node in the Treetank storage match.
     */
    private transient boolean mIsRightSibling;

    /** Last node key. */
    private transient long mLastNodeKey;

    // /** Last node key in descendant check. */
    // private transient long mLastDescCheckNodeKey;

    /**
     * The key of the node, when the nodes are equal if at all (used to check
     * right siblings and therefore if nodes have been deleted).
     */
    private transient long mKeyMatches;

    /** Determines if changes should be commited. */
    private transient EShredderCommit mCommit;

    // /** {@link XMLEventParser} used to check descendants. */
    // private transient XMLEventReader mParser;
    //
    // /** Determines how many {@link XMLEvent}s currently have been parsed. */
    // private transient long mElemsParsed;

    /** Level where the parser is in the file to shredder. */
    private transient int mLevelInToShredder;

    // /** Level where the cursor is in the shreddered file. */
    // private transient int mLevelInShreddered;

    /** {@link QName} of root node from which to shredder the subtree. */
    private transient QName mRootElem;

    // /** Determines if it's the last node or not. */
    // private transient boolean mIsLastNode;

    /** Determines where an insert in the tree occurs. */
    private enum EInsert {
        /** Insert right after a start tag is parsed. */
        ATTOP,

        /** Insert at the start or at the middle of a subtree. */
        ATMIDDLEBOTTOM,

        /** Inserts have been made right before. */
        INTERMEDIATE,

        /** No insert occured. */
        NOINSERT
    }

    /** Determines where a delete in the tree occurs. */
    private transient EInsert mInsert;

    /** Determines where a delete in the tree occurs. */
    private enum EDelete {
        /** Delete at the start or at the middle of a subtree. */
        ATSTARTMIDDLE,

        /** Delete right before an end tag is parsed. */
        ATBOTTOM,

        /** No delete occured. */
        NODELETE
    }

    /** Determines where a delete in the tree occured. */
    private transient EDelete mDelete;

    // /** Cursor moved. */
    // private enum EMoved {
    // /** Cursor moved to first node. */
    // FIRSTNODE,
    //
    // /** Cursor moved to first child or right sibling. */
    // NOTTOPARENT,
    //
    // /**
    // * Cursor either didn't move or it moved to parent or right sibl. of
    // * parent.
    // */
    // TOPARENT,
    // }

    // /** Determines cursor movement in checkDescendants(StartElement). */
    // private transient EMoved mMoved;

    /** Determines how to add a new node. */
    private enum EAdd {
        /** Add as first child. */
        ASFIRSTCHILD,

        /** Add as right sibling. */
        ASRIGHTSIBLING
    }

    /** Determines if a node has been inserted into Treetank. */
    private transient boolean mInserted;

    // /** Level in the checkDescendant(...) method. */
    // private transient int mDescendantLevel;

    /**
     * Determines if it's an empty element before an insert at the top of a
     * subtree.
     */
    private transient boolean mEmptyElement;

    /**
     * Normal constructor to invoke a shredding process on a existing {@link IWriteTransaction}.
     * 
     * @param paramWtx
     *            {@link IWriteTransaction} where the new XML Fragment should be
     *            placed
     * @param paramReader
     *            {@link XMLEventReader} (StAX parser) of the XML Fragment
     * @param paramAddAsFirstChild
     *            if the insert is occuring on a node in an existing tree. <code>false</code> is not possible
     *            when wtx is on root node
     * @param paramData
     *            the data the update shredder operates on. Either a {@link List} of {@link XMLEvent}s or a
     *            {@link File}
     * @param paramCommit
     *            determines if changes should be commited
     * @throws TTUsageException
     *             if insertasfirstChild && updateOnly is both true OR if wtx is
     *             not pointing to doc-root and updateOnly= true
     * @throws TTIOException
     *             if Treetank cannot access node keys
     * 
     */
    @SuppressWarnings("unchecked")
    public XMLUpdateShredder(final INodeWriteTrx paramWtx, final XMLEventReader paramReader,
        final EShredderInsert paramAddAsFirstChild, final Object paramData, final EShredderCommit paramCommit)
        throws TTException {
        super(paramWtx, paramReader, paramAddAsFirstChild);
        checkNotNull(paramData);
        checkNotNull(paramCommit);
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
     * @throws TTException
     *             if Treetank encounters something went wrong
     * @return revision of last revision (before commit)
     */
    @Override
    public Void call() throws TTException {
        updateOnly();
        if (mCommit == EShredderCommit.COMMIT) {
            mWtx.commit();
        }
        return null;
    }

    /**
     * Update a shreddered file.
     * 
     * @throws TTException
     *             if Treetank encounters something went wrong
     */
    private void updateOnly() throws TTException {
        try {
            // Initialize variables.
            mLevelInToShredder = 0;
            // mElemsParsed = 0;
            // mIsLastNode = false;
            mMovedToRightSibling = false;
            boolean firstEvent = true;

            // // If structure already exists, make a sync against the current
            // // structure.
            // if (mMaxNodeKey == 0) {
            // // If no content is in the XML, a normal insertNewContent is
            // // executed.
            // insertNewContent();
            // } else {
            if (mWtx.getNode().getKind() == IConstants.ROOT) {
                // Find the start key for the update operation.
                long startkey = ROOT_NODE + 1;
                while (!mWtx.moveTo(startkey)) {
                    startkey++;
                }
            }

            XMLEvent event = null;
            StringBuilder sBuilder = new StringBuilder();
            final XMLEventFactory fac = XMLEventFactory.newInstance();

            // Iterate over all nodes.
            while (mReader.hasNext()) {
                // Parsing the next event.
                if (mDelete == EDelete.ATSTARTMIDDLE) {
                    /*
                     * Do not move StAX parser forward if nodes have been
                     * deleted at the start or in the middle of a subtree.
                     */
                    mDelete = EDelete.NODELETE;
                } else {
                    // After an insert or after nodes were equal.
                    event = mReader.nextEvent();
                    if (event.isCharacters() && event.asCharacters().isWhiteSpace()) {
                        continue;
                    }
                    // mElemsParsed++;

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
                            // mElemsParsed++;
                        }
                        checkState(event.getEventType() == XMLStreamConstants.START_ELEMENT,
                            "StAX parser has to be on START_DOCUMENT or START_ELEMENT event!");

                        // Get root element of subtree or whole XML document
                        // to shredder.
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
                    sBuilder.append(event.asCharacters().getData());
                    while (mReader.peek().getEventType() == XMLStreamConstants.CHARACTERS) {
                        sBuilder.append(mReader.nextEvent().asCharacters().getData());
                    }
                    final Characters text = fac.createCharacters(sBuilder.toString().trim());
                    processCharacters(text);
                    sBuilder = new StringBuilder();
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    processEndTag();
                    break;
                default:
                    // Other nodes which are currently not supported by
                    // Treetank.
                }
                // }

                // if (!mIsLastNode) {
                // if (mInserted) {
                // // Remove next node after node, which was inserted, because
                // it must have been deleted.
                // if (mWtx.moveToRightSibling()) {
                // mWtx.remove();
                // }
                // } else {
                // // Remove current node (cursor has been moved to the next
                // node already).
                // mWtx.remove();
                // }
                //
                // // Also remove any siblings.
                // boolean hasRightSibling = false;
                // while (mWtx.getStructuralNode().hasRightSibling()) {
                // hasRightSibling = true;
                // mWtx.remove();
                // }
                // if (hasRightSibling) {
                // mWtx.remove();
                // }
                // }

                mReader.close();
            }
            // TODO: use Java7 multi-catch feature.
        } catch (final XMLStreamException e) {
            throw new TTIOException(e);
        } catch (final IOException e) {
            throw new TTIOException(e);
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
     * @throws TTException
     *             In case of any Treetank error.
     */
    private void processStartTag(final StartElement paramElem) throws IOException, XMLStreamException,
        TTException {
        assert paramElem != null;

        // Initialize variables.
        initializeVars();

        // Main algorithm to determine if same, insert or a delete has to be
        // made.
        algorithm(paramElem);

        if (mFound && mIsRightSibling) {
            mDelete = EDelete.ATSTARTMIDDLE;
            deleteNode();
        } else if (!mFound) {
            // Increment levels.
            mLevelInToShredder++;

            insertElementNode(paramElem);
        } else if (mFound) {
            // Increment levels.
            mLevelInToShredder++;

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
     * @throws TTException
     *             In case of any Treetank error.
     */
    private void processCharacters(final Characters paramText) throws IOException, XMLStreamException,
        TTException {
        assert paramText != null;
        // Initialize variables.
        initializeVars();
        final String text = paramText.getData().toString();
        if (!text.isEmpty()) {
            // Main algorithm to determine if same, insert or a delete has to be
            // made.
            algorithm(paramText);

            if (mFound && mIsRightSibling) {
                /*
                 * Cannot happen because if text node after end tag get's
                 * deleted it's done already while parsing the end tag. If text
                 * node should be deleted at the top of a subtree (right after a
                 * start tag has been parsed) it's done in
                 * processStartTag(StartElement).
                 */
                // mDelete = EDelete.ATSTARTMIDDLE;
                // deleteNode();
                throw new AssertionError("");
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
     * @throws TTException
     *             In case anything went wrong while moving/deleting nodes in
     *             Treetank.
     */
    private void processEndTag() throws XMLStreamException, TTException {
        mLevelInToShredder--;

        if (mInserted) {
            mInsertedEndTag = true;
        }

        if (mRemovedNode) {
            mRemovedNode = false;
        } else {

            // Move cursor to parent.
            if (mWtx.getNode().getNodeKey() == mLastNodeKey) {
                /*
                 * An end tag must have been parsed immediately before and it
                 * must have been an empty element at the end of a subtree, thus
                 * move this time to parent node.
                 */
                assert mWtx.getNode().hasParent() && mWtx.getNode().getKind() == IConstants.ELEMENT;
                mWtx.moveTo(mWtx.getNode().getParentKey());
            } else {
                if (mWtx.getNode().getKind() == IConstants.ELEMENT) {
                    final ElementNode element = (ElementNode)mWtx.getNode();
                    if (element.hasFirstChild() && element.hasParent()) {
                        // It's not an empty element, thus move to parent.
                        mWtx.moveTo(mWtx.getNode().getParentKey());
                    }
                    // } else {
                    // checkIfLastNode(true);
                    // }
                } else if (((IStructNode)mWtx.getNode()).hasParent()) {
                    if (((IStructNode)mWtx.getNode()).hasRightSibling()) {
                        mWtx.moveTo(((IStructNode)mWtx.getNode()).getRightSiblingKey());
                        /*
                         * Means next event is an end tag in StAX reader, but
                         * something different where the Treetank transaction
                         * points to, which also means it has to be deleted.
                         */
                        mKeyMatches = -1;
                        mDelete = EDelete.ATBOTTOM;
                        deleteNode();
                    }
                    mWtx.moveTo(mWtx.getNode().getParentKey());
                }

            }

            mLastNodeKey = mWtx.getNode().getNodeKey();

            // Move cursor to right sibling if it has one.
            if (((IStructNode)mWtx.getNode()).hasRightSibling()) {
                mWtx.moveTo(((IStructNode)mWtx.getNode()).getRightSiblingKey());
                mMovedToRightSibling = true;

                skipWhitespaces(mReader);
                if (mReader.peek().getEventType() == XMLStreamConstants.END_ELEMENT) {
                    /*
                     * Means next event is an end tag in StAX reader, but
                     * something different where the Treetank transaction points
                     * to, which also means it has to be deleted.
                     */
                    mKeyMatches = -1;
                    mDelete = EDelete.ATBOTTOM;
                    deleteNode();
                }
            } else {
                mMovedToRightSibling = false;
            }
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
     * @throws TTIOException 
     */
    private void algorithm(final XMLEvent paramEvent) throws IOException, XMLStreamException, TTIOException {
        assert paramEvent != null;
        do {
            /*
             * Check if a node in the shreddered file on the same level equals
             * the current element node.
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
            //
            // if (mFound && mIsRightSibling) {
            // /*
            // * Root element of next subtree in shreddered file matches
            // * so check all descendants. If they match the node must be
            // * inserted.
            // */
            // switch (paramEvent.getEventType()) {
            // case XMLStreamConstants.START_ELEMENT:
            // mMoved = EMoved.FIRSTNODE;
            // //mFound = checkDescendants(paramEvent.asStartElement());
            // mFound = checkDescendants(paramEvent.asStartElement());
            // break;
            // case XMLStreamConstants.CHARACTERS:
            // mFound = checkText(paramEvent.asCharacters());
            // break;
            // default:
            // // throw new
            // AssertionError("Node type not known or not implemented!");
            // }
            // mWtx.moveTo(mKeyMatches);
            // }
        } while (!mFound && mWtx.moveTo(((IStructNode)mWtx.getNode()).getRightSiblingKey()));
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
        assert paramEvent != null;
        final String text = paramEvent.getData().trim();
        return mWtx.getNode().getKind() == IConstants.TEXT && mWtx.getValueOfCurrentNode().equals(text);
    }

    /**
     * In case they are the same nodes move cursor to next node and update
     * stack.
     * 
     * @throws TTIOException
     *             In case of any Treetank error.
     * @throws XMLStreamException
     *             In case of any StAX parsing error.
     */
    private void sameTextNode() throws TTIOException, XMLStreamException {
        // Update variables.
        mInsert = EInsert.NOINSERT;
        mDelete = EDelete.NODELETE;
        mInserted = false;
        mInsertedEndTag = false;
        mRemovedNode = false;

        // Check if last node reached.
        // checkIfLastNode(false);

        // Skip whitespace events.
        skipWhitespaces(mReader);

        // Move to right sibling if next node isn't an end tag.
        if (mReader.peek().getEventType() != XMLStreamConstants.END_ELEMENT) {
            // // Check if next node matches or not.
            // boolean found = false;
            // if (mReader.peek().getEventType() ==
            // XMLStreamConstants.START_ELEMENT) {
            // found = checkElement(mReader.peek().asStartElement());
            // } else if (mReader.peek().getEventType() ==
            // XMLStreamConstants.CHARACTERS) {
            // found = checkText(mReader.peek().asCharacters());
            // }
            //
            // // If next node doesn't match/isn't the same move on.
            // if (!found) {
            if (mWtx.moveTo(((IStructNode)mWtx.getNode()).getRightSiblingKey())) {
                mMovedToRightSibling = true;
            } else {
                mMovedToRightSibling = false;
            }
            // }
        }

        mInsert = EInsert.ATMIDDLEBOTTOM;
    }

    // /**
    // * Check if it's the last node in the shreddered file and modify flag
    // mIsLastNode
    // * if it is the last node.
    // *
    // * @param paramDeleted
    // * Determines if method is invoked inside deleteNode()
    // */
    // private void checkIfLastNode(final boolean paramDeleted) {
    // // Last node or not?
    // int level = mLevelInShreddered;
    //
    // if (paramDeleted && level == 1 && mWtx.getNode().getKind() ==
    // ENode.ELEMENT_KIND
    // && mWtx.getQNameOfCurrentNode().equals(mRootElem) && level == 1) {
    // mIsLastNode = true;
    // }
    //
    // if (!mIsLastNode) {
    // if (paramDeleted && level == 1) {
    // level++;
    // }
    // if (level > 0) {
    // final long nodeKey = mWtx.getNode().getNodeKey();
    // while (!mWtx.getStructuralNode().hasRightSibling() && level != 0) {
    // mWtx.moveToParent();
    // level--;
    // if (mWtx.getNode().getKind() == ENode.ELEMENT_KIND
    // && mWtx.getQNameOfCurrentNode().equals(mRootElem) && level == 1) {
    // mIsLastNode = true;
    // break;
    // }
    // }
    // mWtx.moveTo(nodeKey);
    // }
    // }
    // }

    /**
     * Nodes match, thus update stack and move cursor to first child if it is
     * not a leaf node.
     * 
     * @throws XMLStreamException
     *             In case of any StAX parsing error.
     * @throws TTException
     *             In case anything went wrong while moving the Treetank
     *             transaction.
     */
    private void sameElementNode() throws XMLStreamException, TTException {
        // Update variables.
        mInsert = EInsert.NOINSERT;
        mDelete = EDelete.NODELETE;
        mInserted = false;
        mInsertedEndTag = false;
        mRemovedNode = false;

        // Check if last node reached.
        // checkIfLastNode(false);

        // Skip whitespace events.
        skipWhitespaces(mReader);

        // Move transaction.
        final ElementNode element = (ElementNode)mWtx.getNode();

        if (element.hasFirstChild()) {
            /*
             * If next event needs to be inserted, it has to be inserted at the
             * top of the subtree, as first child.
             */
            mInsert = EInsert.ATTOP;
            mWtx.moveTo(((IStructNode)mWtx.getNode()).getFirstChildKey());

            if (mReader.peek().getEventType() == XMLStreamConstants.END_ELEMENT) {
                /*
                 * Next event is an end tag, so the current child element, where
                 * the transaction currently is located needs to be removed.
                 */
                mKeyMatches = -1;
                mDelete = EDelete.ATBOTTOM;
                deleteNode();
            }
            // } else if (mReader.peek().getEventType() ==
            // XMLStreamConstants.END_ELEMENT
            // &&
            // !mReader.peek().asEndElement().getName().equals(mWtx.getQNameOfCurrentNode()))
            // {
            // /*
            // * Node must be removed when next end tag doesn't match the
            // current name and it has no children.
            // */
            // mKeyMatches = -1;
            // mDelete = EDelete.ATBOTTOM;
            // deleteNode();
        } else if (mReader.peek().getEventType() != XMLStreamConstants.END_ELEMENT) {
            /*
             * Treetank transaction can't find a child node, but StAX parser
             * finds one, so it must be inserted as a first child of the current
             * node.
             */
            mInsert = EInsert.ATTOP;
            mEmptyElement = true;
        } else {
            mInsert = EInsert.ATMIDDLEBOTTOM;
        }
    }

    /**
     * Skip any whitespace event.
     * 
     * @param paramReader
     *            the StAX {@link XMLEventReader} to use
     * @throws XMLStreamException
     *             if any parsing error occurs while moving the StAX parser
     */
    private void skipWhitespaces(final XMLEventReader paramReader) throws XMLStreamException {
        while (paramReader.peek().getEventType() == XMLStreamConstants.CHARACTERS
            && paramReader.peek().asCharacters().isWhiteSpace()) {
            paramReader.nextEvent();
        }
    }

    /**
     * Insert an element node.
     * 
     * @param paramElement
     *            {@link StartElement}, which is going to be inserted.
     * @throws TTException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     * @throws XMLStreamException
     *             In case of any StAX parsing error.
     */
    private void insertElementNode(final StartElement paramElement) throws TTException, XMLStreamException {
        assert paramElement != null;
        /*
         * Add node if it's either not found among right siblings (and the
         * cursor on the shreddered file is on a right sibling) or if it's not
         * found in the structure and it is a new last right sibling.
         */
        mDelete = EDelete.NODELETE;
        mRemovedNode = false;

        switch (mInsert) {
        case ATTOP:
            // We are at the top of a subtree, no end tag has been parsed
            // before.
            if (!mEmptyElement) {
                // Has to be inserted on the parent node.
                mWtx.moveTo(mWtx.getNode().getParentKey());
            }

            // Insert element as first child.
            addNewElement(EAdd.ASFIRSTCHILD, paramElement);
            mInsert = EInsert.INTERMEDIATE;
            break;
        case INTERMEDIATE:
            // Inserts have been made before.
            EAdd insertNode = EAdd.ASFIRSTCHILD;

            if (mInsertedEndTag) {
                /*
                 * An end tag has been read while inserting, thus insert node as
                 * right sibling of parent node.
                 */
                mInsertedEndTag = false;
                insertNode = EAdd.ASRIGHTSIBLING;
            }

            // Possibly move one sibling back if transaction already moved to
            // next node.
            if (mMovedToRightSibling) {
                mWtx.moveTo(((IStructNode)mWtx.getNode()).getLeftSiblingKey());
            }

            // Make sure if transaction is on a text node the node is inserted
            // as a right sibling.
            if (mWtx.getNode().getKind() == IConstants.TEXT) {
                insertNode = EAdd.ASRIGHTSIBLING;
            }

            addNewElement(insertNode, paramElement);
            break;
        case ATMIDDLEBOTTOM:
            // Insert occurs at the middle or end of a subtree.

            // Move one sibling back.
            if (mMovedToRightSibling) {
                mMovedToRightSibling = false;
                mWtx.moveTo(((IStructNode)mWtx.getNode()).getLeftSiblingKey());
            }

            // Insert element as right sibling.
            addNewElement(EAdd.ASRIGHTSIBLING, paramElement);
            mInsert = EInsert.INTERMEDIATE;
            break;
        default:
            throw new AssertionError("Enum value not known!");
        }

        mInserted = true;
    }

    /**
     * Insert a text node.
     * 
     * @param paramText
     *            {@link Characters}, which is going to be inserted.
     * @throws TTException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     * @throws XMLStreamException
     *             In case of any StAX parsing error.
     */
    private void insertTextNode(final Characters paramText) throws TTException, XMLStreamException {
        assert paramText != null;
        /*
         * Add node if it's either not found among right siblings (and the
         * cursor on the shreddered file is on a right sibling) or if it's not
         * found in the structure and it is a new last right sibling.
         */
        mDelete = EDelete.NODELETE;
        mRemovedNode = false;

        switch (mInsert) {
        case ATTOP:
            // Insert occurs at the top of a subtree (no end tag has been parsed
            // immediately before).

            // Move to parent.
            mWtx.moveTo(mWtx.getNode().getParentKey());

            // Insert as first child.
            addNewText(EAdd.ASFIRSTCHILD, paramText);

            // Move to next node if no end tag follows (thus cursor isn't moved
            // to parent in processEndTag()).
            if (mReader.peek().getEventType() != XMLStreamConstants.END_ELEMENT) {
                if (mWtx.moveTo(((IStructNode)mWtx.getNode()).getRightSiblingKey())) {
                    mMovedToRightSibling = true;
                } else {
                    mMovedToRightSibling = false;
                }
            } else if (((IStructNode)mWtx.getNode()).hasRightSibling()) {
                mMovedToRightSibling = false;
                mInserted = true;
                mKeyMatches = -1;
                mDelete = EDelete.ATBOTTOM;
                deleteNode();
            }
            mInsert = EInsert.INTERMEDIATE;
            break;
        case INTERMEDIATE:
            // Inserts have been made before.

            EAdd addNode = EAdd.ASFIRSTCHILD;

            if (mInsertedEndTag) {
                /*
                 * An end tag has been read while inserting, so move back to
                 * left sibling if there is one and insert as right sibling.
                 */
                if (mMovedToRightSibling) {
                    mWtx.moveTo(((IStructNode)mWtx.getNode()).getLeftSiblingKey());
                }
                addNode = EAdd.ASRIGHTSIBLING;
                mInsertedEndTag = false;
            }

            // Insert element as right sibling.
            addNewText(addNode, paramText);

            // Move to next node if no end tag follows (thus cursor isn't moved
            // to parent in processEndTag()).
            if (mReader.peek().getEventType() != XMLStreamConstants.END_ELEMENT) {
                if (mWtx.moveTo(((IStructNode)mWtx.getNode()).getRightSiblingKey())) {
                    mMovedToRightSibling = true;
                } else {
                    mMovedToRightSibling = false;
                }
            }
            break;
        case ATMIDDLEBOTTOM:
            // Insert occurs in the middle or end of a subtree.

            // Move one sibling back.
            if (mMovedToRightSibling) {
                mWtx.moveTo(((IStructNode)mWtx.getNode()).getLeftSiblingKey());
            }

            // Insert element as right sibling.
            addNewText(EAdd.ASRIGHTSIBLING, paramText);

            // Move to next node.
            mWtx.moveTo(((IStructNode)mWtx.getNode()).getRightSiblingKey());

            mInsert = EInsert.INTERMEDIATE;
            break;
        default:
            throw new AssertionError("Enum value not known!");
        }

        mInserted = true;
    }

    /**
     * Delete node.
     * 
     * @throws TTException
     *             In case any exception occurs while moving the cursor or
     *             deleting nodes in Treetank.
     */
    private void deleteNode() throws TTException {
        /*
         * If found in one of the rightsiblings in the current shreddered
         * structure remove all nodes until the transaction points to the found
         * node (keyMatches).
         */
        if (mInserted && !mMovedToRightSibling) {
            mInserted = false;
            if (((IStructNode)mWtx.getNode()).hasRightSibling()) {
                // Cursor is on the inserted node, so move to right sibling.
                mWtx.moveTo(((IStructNode)mWtx.getNode()).getRightSiblingKey());
            }
        }

        // // Check if transaction is on the last node in the shreddered file.
        // checkIfLastNode(true);

        // Determines if transaction has moved to the parent node after a delete
        // operation.
        boolean movedToParent = false;

        // Determines if ldeleteNodeast node in a subtree is going to be
        // deleted.
        boolean isLast = false;

        do {
            if (mWtx.getNode().getNodeKey() != mKeyMatches) {
                final IStructNode node = (IStructNode)mWtx.getNode();
                if (!node.hasRightSibling() && !node.hasLeftSibling()) {
                    // if (mDelete == EDelete.ATSTARTMIDDLE) {
                    // // If the delete occurs right before an end tag the
                    // // level hasn't been incremented.
                    // mLevelInShreddered--;
                    // }
                    /*
                     * Node has no right and no left sibling, so the transaction
                     * moves to the parent after the delete.
                     */
                    movedToParent = true;
                } else if (!node.hasRightSibling()) {
                    // Last node has been reached, which means that the
                    // transaction moves to the left sibling.
                    isLast = true;
                }

                mWtx.remove();
            }
        } while (mWtx.getNode().getNodeKey() != mKeyMatches && !movedToParent && !isLast);

        if (movedToParent) {
            if (mDelete == EDelete.ATBOTTOM) {
                /*
                 * Deleted right before an end tag has been parsed, thus don't
                 * move transaction to next node in processEndTag().
                 */
                mRemovedNode = true;
            }
            /*
             * Treetank transaction has been moved to parent, because all child
             * nodes have been deleted, thus to right sibling.
             */
            mWtx.moveTo(((IStructNode)mWtx.getNode()).getRightSiblingKey());
        } else {
            if (((IStructNode)mWtx.getNode()).hasFirstChild()) {
                if (mDelete == EDelete.ATBOTTOM && isLast) {
                    /*
                     * Deleted right before an end tag has been parsed, thus
                     * don't move transaction to next node in processEndTag().
                     */
                    mRemovedNode = true;
                }

                if (isLast) {
                    // If last node of a subtree has been removed, move to
                    // parent and right sibling.
                    mWtx.moveTo(mWtx.getNode().getParentKey());
                    mWtx.moveTo(((IStructNode)mWtx.getNode()).getRightSiblingKey());

                    // // If the delete occurs right before an end tag the level
                    // // hasn't been incremented.
                    // if (mDelete == EDelete.ATSTARTMIDDLE) {
                    // mLevelInShreddered--;
                    // }
                }
            }
        }

        // Check if transaction is on the last node in the shreddered file.
        // checkIfLastNode(true);
        mInsert = EInsert.NOINSERT;
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
     * @param paramAdd
     *            determines how to add the node
     * @param paramTextEvent
     *            the current {@link Character} event from the StAX parser.
     * @throws TTException
     *             if adding text node fails
     */
    private void addNewText(final EAdd paramAdd, final Characters paramTextEvent) throws TTException {
        assert paramTextEvent != null;
        final String text = paramTextEvent.getData().trim();
        final ByteBuffer textByteBuffer = ByteBuffer.wrap(TypedValue.getBytes(text));
        if (textByteBuffer.array().length > 0) {
            if (paramAdd == EAdd.ASFIRSTCHILD) {
                mWtx.insertTextAsFirstChild(new String(textByteBuffer.array()));
            } else {
                mWtx.insertTextAsRightSibling(new String(textByteBuffer.array()));
            }
        }
    }

    /**
     * Add a new element node.
     * 
     * @param paramAdd
     *            determines wether node is added as first child or right
     *            sibling
     * @param paramStartElement
     *            the current {@link StartElement}
     * @throws TTException
     *             if inserting node fails
     */
    private void addNewElement(final EAdd paramAdd, final StartElement paramStartElement) throws TTException {
        assert paramStartElement != null;
        final QName name = paramStartElement.getName();
        long key;

        if (mFirstChildAppend == EShredderInsert.ADDASRIGHTSIBLING) {
            key = mWtx.insertElementAsRightSibling(name);
        } else {
            if (paramAdd == EAdd.ASFIRSTCHILD) {
                key = mWtx.insertElementAsFirstChild(name);
            } else {
                key = mWtx.insertElementAsRightSibling(name);
            }
        }

        // Parse namespaces.
        for (final Iterator<?> it = paramStartElement.getNamespaces(); it.hasNext();) {
            final Namespace namespace = (Namespace)it.next();
            mWtx.insertNamespace(new QName(namespace.getNamespaceURI(), "", namespace.getPrefix()));
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
     * Check if current element matches the element in the shreddered file.
     * 
     * @param mEvent
     *            StartElement event, from the XML file to shredder.
     * @return true if they are equal, false otherwise.
     * @throws TTIOException 
     */
    private boolean checkElement(final StartElement mEvent) throws TTIOException {
        assert mEvent != null;
        boolean retVal = false;

        // Matching element names?
        if (mWtx.getNode().getKind() == IConstants.ELEMENT
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
            if (!hasAtts && ((ElementNode)mWtx.getNode()).getAttributeCount() == 0) {
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
                    final INameNode namenode = (INameNode)mWtx.getNode();
                    if (namespace.getNamespaceURI().equals(mWtx.nameForKey(namenode.getURIKey()))
                        && namespace.getPrefix().equals(mWtx.nameForKey(namenode.getNameKey()))) {
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
            if (!hasNamesps && ((ElementNode)mWtx.getNode()).getNamespaceCount() == 0) {
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
            System.err.println("Usage: XMLShredder input.xml output.tnk");
            System.exit(1);
        }

        System.out.print("Shredding '" + args[0] + "' to '" + args[1] + "' ... ");
        final long time = System.currentTimeMillis();
        final File target = new File(args[1]);

        Injector injector = Guice.createInjector(new StandardXMLSettings());
        IBackendFactory storage = injector.getInstance(IBackendFactory.class);
        IRevisioningFactory revision = injector.getInstance(IRevisioningFactory.class);

        try {
            final StorageConfiguration config = new StorageConfiguration(target);
            Storage.createStorage(config);
            final IStorage db = Storage.openStorage(target);
            Properties props = new Properties();
            props.setProperty(org.treetank.access.conf.ContructorProps.STORAGEPATH, target.getAbsolutePath());
            props.setProperty(org.treetank.access.conf.ContructorProps.RESOURCE, "shredded");
            db.createResource(new ResourceConfiguration(props, storage, revision, new TreeNodeFactory()));
            final ISession session =
                db.getSession(new SessionConfiguration("shredded", StandardSettings.KEY));
            final INodeWriteTrx wtx =
                new NodeWriteTrx(session, session.beginPageWriteTransaction(), HashKind.Rolling);
            final XMLEventReader reader = createFileReader(new File(args[0]));
            final XMLUpdateShredder shredder =
                new XMLUpdateShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD, new File(args[0]),
                    EShredderCommit.COMMIT);
            shredder.call();

            wtx.close();
            session.close();
        } catch (final TTException exc) {
            exc.printStackTrace();
        } catch (final IOException exc) {
            exc.printStackTrace();
        } catch (final XMLStreamException exc) {
            exc.printStackTrace();
        }

        System.out.println(" done [" + (System.currentTimeMillis() - time) + "ms].");
    }

}

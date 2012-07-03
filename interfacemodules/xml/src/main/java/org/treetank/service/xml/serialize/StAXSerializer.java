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

import static org.treetank.node.IConstants.ATTRIBUTE;
import static org.treetank.node.IConstants.ELEMENT;
import static org.treetank.node.IConstants.NAMESPACE;
import static org.treetank.node.IConstants.ROOT;
import static org.treetank.node.IConstants.TEXT;

import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;

import org.treetank.api.INodeReadTrx;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.DescendantAxis;
import org.treetank.axis.FilterAxis;
import org.treetank.axis.filter.TextFilter;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ElementNode;
import org.treetank.node.interfaces.IStructNode;

/**
 * <h1>StAXSerializer</h1>
 * 
 * <p>
 * Provides a StAX implementation (event API) for retrieving a Treetank database.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class StAXSerializer implements XMLEventReader {

    /**
     * Determines if start tags have to be closed, thus if end tags have to be
     * emitted.
     */
    private transient boolean mCloseElements;

    /** {@link XMLEvent}. */
    private transient XMLEvent mEvent;

    /** {@link XMLEventFactory} to create events. */
    private transient XMLEventFactory mFac = XMLEventFactory.newFactory();

    /** Current node key. */
    private transient long mKey;

    /** Determines if all end tags have been emitted. */
    private transient boolean mCloseElementsEmitted;

    /** Determines if nextTag() method has been called. */
    private transient boolean mNextTag;

    /** {@link IAxis} for iteration. */
    private final transient AbsAxis mAxis;

    /** Stack for reading end element. */
    private final transient Stack<Long> mStack;

    /**
     * Determines if the cursor has to move back after empty elements (used in
     * getElementText().
     */
    private transient boolean mGoBack;

    /**
     * Determines if the cursor has moved up and therefore has to move back
     * after to the right node (used in getElementText()).
     */
    private transient boolean mGoUp;

    /**
     * Last emitted key (start tags, text... except end tags; used in
     * getElementText()).
     */
    private transient long mLastKey;

    /**
     * Determines if {@link IReadTransaction} should be closed afterwards.
     * */
    private transient boolean mCloseRtx;

    /**
     * Rtx for access to the data.
     */
    private final INodeReadTrx mRtx;

    /**
     * Initialize XMLStreamReader implementation with transaction. The cursor
     * points to the node the XMLStreamReader starts to read. Do not serialize
     * the tank ids.
     * 
     * @param paramAxis
     *            {@link AbsAxis} which is used to iterate over and generate
     *            StAX events.
     */
    public StAXSerializer(final AbsAxis paramAxis, final INodeReadTrx pRtx) {
        this(paramAxis, pRtx, true);
    }

    /**
     * Initialize XMLStreamReader implementation with transaction. The cursor
     * points to the node the XMLStreamReader starts to read. Do not serialize
     * the tank ids.
     * 
     * @param paramAxis
     *            {@link AbsAxis} which is used to iterate over and generate
     *            StAX events.
     * @param paramCloseRtx
     *            Determines if rtx should be closed afterwards.
     */
    public StAXSerializer(final AbsAxis paramAxis, final INodeReadTrx pRtx, final boolean paramCloseRtx) {
        mNextTag = false;
        mAxis = paramAxis;
        mCloseRtx = paramCloseRtx;
        mStack = new Stack<Long>();
        mRtx = pRtx;
    }

    /**
     * Emit end tag.
     * 
     * @param paramRTX
     *            Treetank reading transaction {@link IReadTransaction}.
     */
    private void emitEndTag() {
        final long nodeKey = mRtx.getNode().getNodeKey();
        mEvent = mFac.createEndElement(mRtx.getQNameOfCurrentNode(), new NamespaceIterator(mRtx));
        mRtx.moveTo(nodeKey);
    }

    /**
     * Emit a node.
     * 
     * @param paramRTX
     *            Treetank reading transaction {@link IReadTransaction}.
     */
    private void emitNode() {
        switch (mRtx.getNode().getKind()) {
        case ROOT:
            mEvent = mFac.createStartDocument();
            break;
        case ELEMENT:
            final long key = mRtx.getNode().getNodeKey();
            final QName qName = mRtx.getQNameOfCurrentNode();
            mEvent = mFac.createStartElement(qName, new AttributeIterator(mRtx), new NamespaceIterator(mRtx));
            mRtx.moveTo(key);
            break;
        case TEXT:
            mEvent = mFac.createCharacters(mRtx.getValueOfCurrentNode());
            break;
        default:
            throw new IllegalStateException("Kind not known!");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws XMLStreamException {
        if (mCloseRtx) {
            try {
                mAxis.close();
            } catch (final AbsTTException exc) {
                exc.printStackTrace();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getElementText() throws XMLStreamException {

        final long nodeKey = mAxis.getNode().getNodeKey();

        /*
         * The cursor has to move back (once) after determining, that a closing
         * tag would be the next event (precond: closeElement and either goBack
         * or goUp is true).
         */
        if (mCloseElements && (mGoBack || mGoUp)) {
            if (mGoUp) {
                mAxis.moveTo(mLastKey);
                mGoUp = false;
            } else if (mGoBack) {
                mAxis.moveTo(mStack.peek());
                mGoBack = false;
            }
        }

        if (mEvent.getEventType() != XMLStreamConstants.START_ELEMENT) {
            mAxis.moveTo(nodeKey);
            throw new XMLStreamException("getElementText() only can be called on a start element");
        }
        final FilterAxis textFilterAxis =
            new FilterAxis(new DescendantAxis(mRtx), mRtx, new TextFilter(mRtx));
        final StringBuilder strBuilder = new StringBuilder();

        while (textFilterAxis.hasNext()) {
            textFilterAxis.next();
            strBuilder.append(mRtx.getValueOfCurrentNode());
        }

        mRtx.moveTo(nodeKey);
        return strBuilder.toString();
    }

    /** {@inheritDoc} */
    @Override
    public Object getProperty(final String mName) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        boolean retVal = false;

        if (!mStack.empty() && (mCloseElements || mCloseElementsEmitted)) {
            /*
             * mAxis.hasNext() can't be used in this case, because it would
             * iterate to the next node but at first all end-tags have to be
             * emitted.
             */
            retVal = true;
        } else {
            retVal = mAxis.hasNext();
        }

        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        try {
            if (!mCloseElements && !mCloseElementsEmitted) {
                mKey = mAxis.next();

                if (mNextTag) {
                    if (mAxis.getNode().getKind() != ELEMENT) {
                        throw new XMLStreamException("The next tag isn't a start- or end-tag!");
                    }
                    mNextTag = false;
                }
            }
            emit();
        } catch (final IOException exc) {
            exc.printStackTrace();
        }

        return mEvent;
    }

    /** {@inheritDoc} */
    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        mNextTag = true;
        return nextEvent();
    }

    /** {@inheritDoc} */
    @Override
    public XMLEvent peek() throws XMLStreamException {
        final long currNodeKey = mAxis.getNode().getNodeKey();
        try {
            if (mCloseElements) {
                mRtx.moveTo(mStack.peek());
                emitEndTag();
            } else {
                final int nodeKind = mRtx.getNode().getKind();
                if (((IStructNode)mRtx.getNode()).hasFirstChild()) {
                    mRtx.moveTo(((IStructNode)mRtx.getNode()).getFirstChildKey());
                    emitNode();
                } else if (((IStructNode)mRtx.getNode()).hasRightSibling()) {
                    mRtx.moveTo(((IStructNode)mRtx.getNode()).getRightSiblingKey());
                    processNode(nodeKind);
                } else if (((IStructNode)mRtx.getNode()).hasParent()) {
                    mRtx.moveTo(mRtx.getNode().getParentKey());
                    emitEndTag();
                }
            }
        } catch (final IOException exc) {
            exc.printStackTrace();
        }

        mRtx.moveTo(currNodeKey);
        return mEvent;
    }

    /**
     * Just calls nextEvent().
     * 
     * @return next event.
     */
    @Override
    public Object next() {
        try {
            mEvent = nextEvent();
        } catch (final XMLStreamException exc) {
            exc.printStackTrace();
        }

        return mEvent;
    }

    /** {@inheritDoc} */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported!");
    }

    /**
     * Determines if a node or an end element has to be emitted.
     * 
     * @param paramNodeKind
     *            the node kind
     * @throws IOException
     *             In case of any I/O error.
     */
    private void processNode(final int paramNodeKind) throws IOException {
        switch (paramNodeKind) {
        case ELEMENT:
            emitEndTag();
            break;
        case TEXT:
            emitNode();
            break;
        default:
            // Do nothing.
        }
    }

    /**
     * Move to node and emit it.
     * 
     * @throws IOException
     *             In case of any I/O error.
     */
    private void emit() throws IOException {
        // Emit pending end elements.
        if (mCloseElements) {
            if (!mStack.empty() && mStack.peek() != ((IStructNode)mRtx.getNode()).getLeftSiblingKey()) {
                mRtx.moveTo(mStack.pop());
                emitEndTag();
                mRtx.moveTo(mKey);
            } else if (!mStack.empty()) {
                mRtx.moveTo(mStack.pop());
                emitEndTag();
                mRtx.moveTo(mKey);
                mCloseElements = false;
                mCloseElementsEmitted = true;
            }
        } else {
            mCloseElementsEmitted = false;

            // Emit node.
            emitNode();

            final long nodeKey = mRtx.getNode().getNodeKey();
            mLastKey = nodeKey;

            // Push end element to stack if we are a start element.
            if (mRtx.getNode().getKind() == ELEMENT) {
                mStack.push(nodeKey);
            }

            // Remember to emit all pending end elements from stack if
            // required.
            if (!((IStructNode)mRtx.getNode()).hasFirstChild()
                && !((IStructNode)mRtx.getNode()).hasRightSibling()) {
                mGoUp = true;
                moveToNextNode();
            } else if (mRtx.getNode().getKind() == ELEMENT
                && !((ElementNode)mRtx.getNode()).hasFirstChild()) {
                // Case: Empty elements with right siblings.
                mGoBack = true;
                moveToNextNode();
            }
        }
    }

    /**
     * Move to next node in tree either in case of a right sibling of an empty
     * element or if no further child and no right sibling can be found, so that
     * the next node is in the following axis.
     */
    private void moveToNextNode() {
        mCloseElements = true;
        if (mAxis.hasNext()) {
            mKey = mAxis.next();
        }
    }

    /**
     * Implements an iterator for attributes.
     */
    final class AttributeIterator implements Iterator<Attribute> {

        /**
         * Treetank {@link IReadTransaction}.
         */
        private final INodeReadTrx mRTX;

        /** Number of attribute nodes. */
        private final int mAttCount;

        /** Index of attribute node. */
        private int mIndex;

        /** Node key. */
        private final long mNodeKey;

        /** Factory to create nodes {@link XMLEventFactory}. */
        private final transient XMLEventFactory mFac = XMLEventFactory.newFactory();

        /**
         * Constructor.
         * 
         * @param rtx
         *            Treetank reading transaction.
         */
        public AttributeIterator(final INodeReadTrx rtx) {
            mRTX = rtx;
            mNodeKey = mRTX.getNode().getNodeKey();
            mIndex = 0;

            if (mRTX.getNode().getKind() == ELEMENT) {
                mAttCount = ((ElementNode)mRTX.getNode()).getAttributeCount();
            } else {
                mAttCount = 0;
            }
        }

        @Override
        public boolean hasNext() {
            boolean retVal = false;

            if (mIndex < mAttCount) {
                retVal = true;
            }

            return retVal;
        }

        @Override
        public Attribute next() {
            mRTX.moveTo(mNodeKey);
            mRTX.moveToAttribute(mIndex++);
            assert mRTX.getNode().getKind() == ATTRIBUTE;
            final QName qName = mRTX.getQNameOfCurrentNode();
            final String value = mRTX.getValueOfCurrentNode();
            mRTX.moveTo(mNodeKey);
            return mFac.createAttribute(qName, value);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported!");
        }
    }

    /**
     * Implements a namespace iterator, which is needed for the StAX
     * implementation.
     * 
     * @author Johannes Lichtenberger, University of Konstanz
     * 
     */
    final class NamespaceIterator implements Iterator<Namespace> {

        /**
         * Treetank {@link IReadTransaction}.
         */
        private final INodeReadTrx mRTX;

        /** Number of namespace nodes. */
        private final int mNamespCount;

        /** Index of namespace node. */
        private int mIndex;

        /** Node key. */
        private final long mNodeKey;

        /** Factory to create nodes {@link XMLEventFactory}. */
        private final transient XMLEventFactory mFac = XMLEventFactory.newInstance();

        /**
         * Constructor.
         * 
         * @param rtx
         *            Treetank reading transaction.
         */
        public NamespaceIterator(final INodeReadTrx rtx) {
            mRTX = rtx;
            mNodeKey = mRTX.getNode().getNodeKey();
            mIndex = 0;

            if (mRTX.getNode().getKind() == ELEMENT) {
                mNamespCount = ((ElementNode)mRTX.getNode()).getNamespaceCount();
            } else {
                mNamespCount = 0;
            }
        }

        @Override
        public boolean hasNext() {
            boolean retVal = false;

            if (mIndex < mNamespCount) {
                retVal = true;
            }

            return retVal;
        }

        @Override
        public Namespace next() {
            mRTX.moveTo(mNodeKey);
            mRTX.moveToNamespace(mIndex++);
            assert mRTX.getNode().getKind() == NAMESPACE;
            final QName qName = mRTX.getQNameOfCurrentNode();
            mRTX.moveTo(mNodeKey);
            return mFac.createNamespace(qName.getLocalPart(), qName.getNamespaceURI());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported!");
        }
    }
}

package com.treetank.service.xml.serialize;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.axis.FilterAxis;
import com.treetank.axis.TextFilter;
import com.treetank.exception.TreetankException;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.utils.AttributeIterator;
import com.treetank.utils.FastStack;
import com.treetank.utils.NamespaceIterator;

/**
 * <h1>StAXSerializer</h1>
 * 
 * <p>
 * Provides a StAX implementation (event API) for retrieving a Treetank
 * database.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class StAXSerializer implements XMLEventReader {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StAXSerializer.class);

    /**
     * Text filter axis {@link TextFilterAxis}.
     */
    private FilterAxis textFilterAxis;

    /**
     * Determines if start tags have to be closed, thus if end tags have to be
     * emitted.
     */
    private boolean closeElements;

    /**
     * {@inheritDoc}
     */
    private XMLEvent event;

    /**
     * XMLEventFactory to create events.
     * 
     * @see XMLEventFactory
     */
    private final XMLEventFactory fac = XMLEventFactory.newFactory();

    /** Node key. */
    private long key;

    /**
     * Determines if all end tags have been emitted.
     */
    private boolean closeElementsEmitted = false;

    /** Determines if nextTag() method has been called. */
    private boolean nextTag = false;

    /** axis for iteration */
    private final IAxis mAxis;

    /** Stack for reading end element. */
    private final FastStack<Long> mStack;

    /**
     * Initialize XMLStreamReader implementation with transaction. The cursor
     * points to the node the XMLStreamReader starts to read. Do not serialize
     * the tank ids.
     * 
     * @param rtx
     *            Transaction with cursor pointing to start node.
     * @param map
     *            Properties map.
     */
    public StAXSerializer(final IAxis axis) {
        nextTag = false;
        mAxis = axis;
        mStack = new FastStack<Long>();

    }

    private void emitEndElement(final IReadTransaction rtx) {
        final long nodeKey = rtx.getNode().getNodeKey();
        event = fac.createEndElement(rtx.getQNameOfCurrentNode(),
                new NamespaceIterator(rtx));
        rtx.moveTo(nodeKey);
    }

    private void emitNode(final IReadTransaction rtx) {
        switch (rtx.getNode().getKind()) {
        case ROOT_KIND:
            event = fac.createStartDocument();
            break;
        case ELEMENT_KIND:
            final long key = rtx.getNode().getNodeKey();
            final QName qName = rtx.getQNameOfCurrentNode();
            event = fac.createStartElement(qName, new AttributeIterator(rtx),
                    new NamespaceIterator(rtx));
            rtx.moveTo(key);
            break;
        case TEXT_KIND:
            event = fac.createCharacters(rtx.getValueOfCurrentNode());
            break;
        }
    }

    @Override
    public void close() throws XMLStreamException {
        try {
            mAxis.getTransaction().close();
        } catch (final TreetankException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String getElementText() throws XMLStreamException {
        textFilterAxis = new FilterAxis(mAxis, new TextFilter(
                mAxis.getTransaction()));
        final StringBuilder sb = new StringBuilder();

        while (textFilterAxis.hasNext()) {
            textFilterAxis.next();
            sb.append(mAxis.getTransaction().getValueOfCurrentNode());
        }

        return sb.toString();
    }

    @Override
    public Object getProperty(String arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasNext() {
        if (!mStack.empty() && (closeElements || closeElementsEmitted)) {
            return true;
        }
        return mAxis.hasNext();
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        try {
            if (!closeElements && !closeElementsEmitted) {
                key = mAxis.next();

                if (nextTag) {
                    if (mAxis.getTransaction().getNode().getKind() != ENodes.ELEMENT_KIND) {
                        throw new XMLStreamException(
                                "The next tag isn't a start- or end-tag!");
                    }
                    nextTag = false;
                }
            }
            emit(mAxis.getTransaction());
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return event;
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        nextTag = true;
        return nextEvent();
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        final long currNodeKey = mAxis.getTransaction().getNode().getNodeKey();
        final IReadTransaction rtx = mAxis.getTransaction();
        try {
            if (closeElements) {
                rtx.moveTo(mStack.peek());
                emitEndElement(rtx);
            } else {
                final ENodes nodeKind = rtx.getNode().getKind();
                if (((AbsStructNode) rtx.getNode()).hasFirstChild()) {
                    rtx.moveToFirstChild();
                    emitNode(rtx);
                } else if (((AbsStructNode) rtx.getNode()).hasRightSibling()) {
                    rtx.moveToRightSibling();
                    processNode(nodeKind);
                } else if (((AbsStructNode) rtx.getNode()).hasParent()) {
                    rtx.moveToParent();
                    emitEndElement(rtx);
                }
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        rtx.moveTo(currNodeKey);
        return event;
    }

    /**
     * Just calls nextEvent().
     */
    @Override
    public Object next() {
        try {
            event = nextEvent();
        } catch (final XMLStreamException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return event;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported!");
    }

    /**
     * Determines if a node or an end element has to be emitted.
     * 
     * @param nodeKind
     *            The node kind.
     * @throws IOException
     *             In case of any I/O error.
     */
    private void processNode(final ENodes nodeKind) throws IOException {
        switch (nodeKind) {
        case ELEMENT_KIND:
            emitEndElement(mAxis.getTransaction());
            break;
        case TEXT_KIND:
            emitNode(mAxis.getTransaction());
            break;
        default:
            // Do nothing.
        }
    }

    /**
     * Move to node and emit it.
     * 
     * @param key
     *            Current node key.
     * @throws IOException
     *             In case of any I/O error.
     */
    private void emit(final IReadTransaction rtx) throws IOException {
        // Emit pending end elements.
        if (closeElements) {
            if (!mStack.empty()
                    && mStack.peek() != ((AbsStructNode) rtx.getNode())
                            .getLeftSiblingKey()) {
                rtx.moveTo(mStack.pop());
                emitEndElement(rtx);
                rtx.moveTo(key);
            } else if (!mStack.empty()) {
                rtx.moveTo(mStack.pop());
                emitEndElement(rtx);
                rtx.moveTo(key);
                closeElements = false;
                closeElementsEmitted = true;
            }
        } else {
            closeElementsEmitted = false;

            // Emit node.
            emitNode(rtx);

            // Push end element to stack if we are a start element.
            if (rtx.getNode().getKind() == ENodes.ELEMENT_KIND) {
                mStack.push(rtx.getNode().getNodeKey());
            }

            // Remember to emit all pending end elements from stack if
            // required.
            if (!((AbsStructNode) rtx.getNode()).hasFirstChild()
                    && !((AbsStructNode) rtx.getNode()).hasRightSibling()) {
                moveToNextNode();
            } else if (rtx.getNode().getKind() == ENodes.ELEMENT_KIND
                    && !((ElementNode) rtx.getNode()).hasFirstChild()) {
                // Case: Empty elements with right siblings.
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
        closeElements = true;
        if (mAxis.hasNext()) {
            key = mAxis.next();
        }
    }

    // /**
    // * Main method.
    // *
    // * @param args
    // * args[0] specifies the path to the TT-storage from which to
    // * generate SAX events.
    // * @throws Exception
    // */
    // public static void main(final String... args) throws Exception {
    // if (args.length != 1) {
    // LOGGER.error("Usage: StAXSerializer input-TT");
    // }
    //
    // final IDatabase database = Database.openDatabase(new File(args[0]));
    // final ISession session = database.getSession();
    // final IReadTransaction rtx = session.beginReadTransaction();
    //
    // final XMLEventReader reader = new
    // // new StAXSerializer(rtx, new SerializerProperties(null).getmProps());
    //
    // rtx.close();
    // session.close();
    // database.close();
    // }
}

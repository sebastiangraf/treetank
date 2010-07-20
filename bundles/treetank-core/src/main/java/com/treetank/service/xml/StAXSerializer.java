package com.treetank.service.xml;

import static com.treetank.service.xml.SerializerProperties.S_ID;
import static com.treetank.service.xml.SerializerProperties.S_REST;
import static com.treetank.service.xml.SerializerProperties.S_XMLDECL;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.ElementFilter;
import com.treetank.axis.FilterAxis;
import com.treetank.axis.TextFilter;
import com.treetank.exception.TreetankException;
import com.treetank.node.AbsStructNode;
import com.treetank.node.ENodes;
import com.treetank.utils.AttributeIterator;
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
public class StAXSerializer extends AbsSerializeStorage implements
        XMLEventReader {

    /** Logger. */
    private static final Log LOGGER = LogFactory.getLog(StAXSerializer.class);

    /**
     * Element filter axis.
     * 
     * @see ElementFilterAxis
     */
    private static FilterAxis elemFilterAxis;

    /**
     * Text filter axis.
     * 
     * @see TextFilterAxis
     */
    private static FilterAxis textFilterAxis;

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

    // /**
    // * Namespace index.
    // */
    // private int namespIndex;
    //
    // /**
    // * Attribute index.
    // */
    // private int attIndex;

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
    public StAXSerializer(final IReadTransaction rtx,
            final ConcurrentMap<String, Object> map) {
        this(rtx, (Boolean) map.get(S_XMLDECL), (Boolean) map.get(S_REST),
                (Boolean) map.get(S_ID));
    }

    /**
     * {@inheritDoc}
     */
    public StAXSerializer(IReadTransaction rtx,
            boolean serializeXMLDeclaration, boolean serializeRest,
            boolean serializeId) {
        super(rtx, serializeXMLDeclaration, serializeRest, serializeId);
    }

    @Override
    public void emitEndElement() throws IOException {
        // namespIndex = 0;
        // attIndex = 0;
        event = fac.createEndElement(mRTX.getQNameOfCurrentNode(),
                new NamespaceIterator(mRTX));
    }

    @Override
    public void emitNode() throws IOException {
        switch (mRTX.getNode().getKind()) {
        case ROOT_KIND:
            event = fac.createStartDocument();
            break;
        case ELEMENT_KIND:
            // final long key = mRTX.getNode().getNodeKey();
            // final int namespCount =
            // ((ElementNode) mRTX.getNode()).getNamespaceCount();
            // final int attCount = ((ElementNode)
            // mRTX.getNode()).getAttributeCount();
            final QName qName = mRTX.getQNameOfCurrentNode();

            // if (namespIndex < namespCount) {
            // mRTX.moveToNamespace(namespIndex++);
            // event = fac.createNamespace(qName.getPrefix(),
            // qName.getNamespaceURI());
            // } else if (attIndex < attCount) {
            // mRTX.moveToAttribute(attIndex++);
            // event = fac.createAttribute(qName, mRTX.getValueOfCurrentNode());
            // } else {
            event = fac.createStartElement(qName, new AttributeIterator(mRTX),
                    new NamespaceIterator(mRTX));
            // }

            // mRTX.moveTo(key);
            break;
        case TEXT_KIND:
            // namespIndex = 0;
            // attIndex = 0;
            event = fac.createCharacters(mRTX.getValueOfCurrentNode());
            break;
        }
    }

    @Override
    public void close() throws XMLStreamException {
        try {
            mRTX.close();
        } catch (TreetankException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String getElementText() throws XMLStreamException {
        textFilterAxis = new FilterAxis(mAxis, new TextFilter(mRTX));
        final StringBuilder sb = new StringBuilder();

        while (textFilterAxis.hasNext()) {
            textFilterAxis.next();
            sb.append(mRTX.getValueOfCurrentNode());
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
        return mAxis.hasNext();
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        long key = mRTX.getNode().getNodeKey();

        try {
            if (mAxis.hasNext()) {
                key = mAxis.next();
                emit(key);
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return event;
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        long key = mRTX.getNode().getNodeKey();
        elemFilterAxis = new FilterAxis(mAxis, new ElementFilter(mRTX));

        try {
            if (elemFilterAxis.hasNext()) {
                key = elemFilterAxis.next();
                emit(key);
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return event;
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        final long currNodeKey = mRTX.getNode().getNodeKey();
        final ENodes nodeKind = mRTX.getNode().getKind();

        try {
            if (((AbsStructNode) mRTX.getNode()).hasFirstChild()) {
                mRTX.moveToFirstChild();
                emitNode();
            } else if (((AbsStructNode) mRTX.getNode()).hasRightSibling()) {
                mRTX.moveToRightSibling();
                processNode(nodeKind);
            } else if (((AbsStructNode) mRTX.getNode()).hasParent()) {
                mRTX.moveToParent();
                emitEndElement();
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        mRTX.moveTo(currNodeKey);
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
            emitEndElement();
            break;
        case TEXT_KIND:
            emitNode();
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
    private void emit(final long key) throws IOException {
        // Emit pending end elements.
        if (closeElements) {
            if (!mStack.empty()
                    && mStack.peek() != ((AbsStructNode) mRTX.getNode())
                            .getLeftSiblingKey()) {
                mRTX.moveTo(mStack.pop());
                emitEndElement();
                mRTX.moveTo(key);
            } else if (!mStack.empty()) {
                mRTX.moveTo(mStack.pop());
                emitEndElement();
            } else {
                mRTX.moveTo(key);
                closeElements = false;
            }
        } else {
            // Emit node.
            emitNode();

            // Push end element to stack if we are a start element with
            // children.
            if (mRTX.getNode().getKind() == ENodes.ELEMENT_KIND
                    && ((AbsStructNode) mRTX.getNode()).hasFirstChild()) {
                mStack.push(mRTX.getNode().getNodeKey());
            }

            // Remember to emit all pending end elements from stack if
            // required.
            if (!((AbsStructNode) mRTX.getNode()).hasFirstChild()
                    && !((AbsStructNode) mRTX.getNode()).hasRightSibling()) {
                closeElements = true;
            }
        }
    }

    /**
     * Main method.
     * 
     * @param args
     *            args[0] specifies the path to the TT-storage from which to
     *            generate SAX events.
     * @throws Exception
     */
    public static void main(final String... args) throws Exception {
        if (args.length != 1) {
            LOGGER.error("Usage: StAXSerializer input-TT");
        }

        final IDatabase database = Database.openDatabase(new File(args[0]));
        final ISession session = database.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();

        new StAXSerializer(rtx, new SerializerProperties(null).getmProps());

        rtx.close();
        session.close();
        database.close();
    }
}

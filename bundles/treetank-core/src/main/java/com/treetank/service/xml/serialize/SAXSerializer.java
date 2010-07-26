package com.treetank.service.xml.serialize;

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.treetank.access.Database;
import com.treetank.access.WriteTransactionState;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.node.ElementNode;

/**
 * <h1>SaxSerializer</h1>
 * 
 * <p>
 * Generates SAX events from a Treetank database.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class SAXSerializer extends AbsSerializer implements XMLReader {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SAXSerializer.class);

    /** SAX content handler. */
    private transient ContentHandler mContHandler;

    /**
     * {@inheritDoc}
     */
    public SAXSerializer(final ISession session, final ContentHandler handler, final long... versions) {
        super(session, versions);
        mContHandler = handler;
    }

    @Override
    protected void emitStartElement(final IReadTransaction rtx) {
        final String URI = rtx.nameForKey(rtx.getNode().getURIKey());
        final QName qName = rtx.getQNameOfCurrentNode();
        try {
            mContHandler.endElement(URI, qName.getLocalPart(), WriteTransactionState.buildName(qName));
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    protected void emitEndElement(final IReadTransaction rtx) {
        switch (rtx.getNode().getKind()) {
        case ELEMENT_KIND:
            generateElement(rtx);
            break;
        case TEXT_KIND:
            generateText(rtx);
            break;
        default:
            throw new UnsupportedOperationException("Kind not supported by Treetank!");
        }
    }

    @Override
    protected void emitStartManualElement(final long revision) {
        final AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "revision", "tt", "", Long.toString(revision));
        try {
            mContHandler.startElement("", "tt", "tt", atts);
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    @Override
    protected void emitEndManualElement(final long revision) {
        try {
            mContHandler.endElement("", "tt", "tt");
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Generate a start element event.
     */
    private void generateElement(final IReadTransaction rtx) {
        final AttributesImpl atts = new AttributesImpl();
        final long key = rtx.getNode().getNodeKey();

        // Process namespace nodes.
        for (int i = 0, namesCount = ((ElementNode)rtx.getNode()).getNamespaceCount(); i < namesCount; i++) {
            rtx.moveToNamespace(i);
            final String URI = rtx.nameForKey(rtx.getNode().getURIKey());
            if (rtx.nameForKey(rtx.getNode().getNameKey()).length() == 0) {
                atts.addAttribute(URI, "xmlns", "xmlns", "CDATA", URI);
            } else {
                atts.addAttribute(URI, "xmlns", "xmlns:" + rtx.getQNameOfCurrentNode().getLocalPart(),
                    "CDATA", URI);
            }
            rtx.moveTo(key);
        }

        // Process attributes.
        for (int i = 0, attCount = ((ElementNode)rtx.getNode()).getAttributeCount(); i < attCount; i++) {
            rtx.moveToAttribute(i);
            final String URI = rtx.nameForKey(rtx.getNode().getURIKey());
            final QName qName = rtx.getQNameOfCurrentNode();
            atts.addAttribute(URI, qName.getLocalPart(), WriteTransactionState.buildName(qName), rtx
                .getTypeOfCurrentNode(), rtx.getValueOfCurrentNode());
            rtx.moveTo(key);
        }

        // Create SAX events.
        try {
            final QName qName = rtx.getQNameOfCurrentNode();
            mContHandler.startElement(rtx.nameForKey(rtx.getNode().getURIKey()), qName.getLocalPart(),
                WriteTransactionState.buildName(qName), atts);

            // Empty elements.
            if (!((ElementNode)rtx.getNode()).hasFirstChild()) {
                mContHandler.endElement(rtx.nameForKey(rtx.getNode().getURIKey()), qName.getLocalPart(),
                    WriteTransactionState.buildName(qName));
            }
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Generate a text event.
     */
    private void generateText(final IReadTransaction rtx) {
        try {
            mContHandler.characters(rtx.getValueOfCurrentNode().toCharArray(), 0, rtx.getValueOfCurrentNode()
                .length());
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
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
            LOGGER.error("Usage: SAXSerializer input-TT");
        }

        final IDatabase database = Database.openDatabase(new File(args[0]));
        final ISession session = database.getSession();

        final DefaultHandler defHandler = new DefaultHandler();

        final SAXSerializer serializer = new SAXSerializer(session, defHandler);
        serializer.call();

        session.close();
        database.close();
    }

    @Override
    protected void emitStartDocument() {
        try {
            mContHandler.startDocument();
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    protected void emitEndDocument() {
        try {
            mContHandler.endDocument();
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /* Implements XMLReader method. */
    @Override
    public ContentHandler getContentHandler() {
        return mContHandler;
    }

    /* Implements XMLReader method. */
    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }

    /* Implements XMLReader method. */
    @Override
    public EntityResolver getEntityResolver() {
        return null;
    }

    /* Implements XMLReader method. */
    @Override
    public ErrorHandler getErrorHandler() {
        return null;
    }

    /* Implements XMLReader method. */
    @Override
    public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    /* Implements XMLReader method. */
    @Override
    public void parse(final InputSource input) throws IOException, SAXException {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }

    /* Implements XMLReader method. */
    @Override
    public void parse(final String systemID) throws IOException, SAXException {
        emitStartDocument();
        try {
            super.call();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        emitEndDocument();
    }

    /* Implements XMLReader method. */
    @Override
    public void setContentHandler(final ContentHandler content) {
        mContHandler = content;
    }

    /* Implements XMLReader method. */
    @Override
    public void setDTDHandler(final DTDHandler handler) {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }

    /* Implements XMLReader method. */
    @Override
    public void setEntityResolver(final EntityResolver resolver) {
        throw new UnsupportedOperationException("Not supported by Treetank!");

    }

    /* Implements XMLReader method. */
    @Override
    public void setErrorHandler(final ErrorHandler handler) {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }

    /* Implements XMLReader method. */
    @Override
    public void setFeature(final String name, final boolean value) throws SAXNotRecognizedException,
        SAXNotSupportedException {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }

    /* Implements XMLReader method. */
    @Override
    public void setProperty(final String name, final Object value) throws SAXNotRecognizedException,
        SAXNotSupportedException {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }
}

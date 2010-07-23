package com.treetank.service.xml.serialize;

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.treetank.access.Database;
import com.treetank.access.WriteTransactionState;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.DescendantAxis;
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
public final class SAXSerializer extends AbsSerializer {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(SAXSerializer.class);

    /** SAX default handler. */
    private transient final ContentHandler mHandler;

    /**
     * {@inheritDoc}
     */
    SAXSerializer(final IAxis axis, final ContentHandler handler) {
        super(axis);
        mHandler = handler;
    }

    @Override
    public Void call() throws Exception {
        mHandler.startDocument();
        super.call();
        mHandler.endDocument();
        return null;
    }

    @Override
    protected void emitEndElement() throws IOException {
        final String URI = mRTX.nameForKey(mRTX.getNode().getURIKey());
        final QName qName = mRTX.getQNameOfCurrentNode();
        try {
            mHandler.endElement(URI, qName.getLocalPart(),
                    WriteTransactionState.buildName(qName));
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    protected void emitNode() throws IOException {
        switch (mRTX.getNode().getKind()) {
        case ELEMENT_KIND:
            generateElement();
            break;
        case TEXT_KIND:
            generateText();
            break;
        default:
            throw new UnsupportedOperationException(
                    "Kind not supported by Treetank!");
        }
    }

    /**
     * Generate a start element event.
     */
    private void generateElement() {
        final AttributesImpl atts = new AttributesImpl();
        final long key = mRTX.getNode().getNodeKey();

        // Process namespace nodes.
        for (int i = 0, namesCount = ((ElementNode) mRTX.getNode())
                .getNamespaceCount(); i < namesCount; i++) {
            mRTX.moveToNamespace(i);
            final String URI = mRTX.nameForKey(mRTX.getNode().getURIKey());
            if (mRTX.nameForKey(mRTX.getNode().getNameKey()).length() == 0) {
                atts.addAttribute(URI, "xmlns", "xmlns", "CDATA", URI);
            } else {
                atts.addAttribute(URI, "xmlns", "xmlns:"
                        + mRTX.getQNameOfCurrentNode().getLocalPart(), "CDATA",
                        URI);
            }
            mRTX.moveTo(key);
        }

        // Process attributes.
        for (int i = 0, attCount = ((ElementNode) mRTX.getNode())
                .getAttributeCount(); i < attCount; i++) {
            mRTX.moveToAttribute(i);
            final String URI = mRTX.nameForKey(mRTX.getNode().getURIKey());
            final QName qName = mRTX.getQNameOfCurrentNode();
            atts.addAttribute(URI, qName.getLocalPart(),
                    WriteTransactionState.buildName(qName),
                    mRTX.getTypeOfCurrentNode(), mRTX.getValueOfCurrentNode());
            mRTX.moveTo(key);
        }

        // Create SAX events.
        try {
            final QName qName = mRTX.getQNameOfCurrentNode();
            mHandler.startElement(mRTX.nameForKey(mRTX.getNode().getURIKey()),
                    qName.getLocalPart(),
                    WriteTransactionState.buildName(qName), atts);

            // Empty elements.
            if (!((ElementNode) mRTX.getNode()).hasFirstChild()) {
                mHandler.endElement(
                        mRTX.nameForKey(mRTX.getNode().getURIKey()),
                        qName.getLocalPart(),
                        WriteTransactionState.buildName(qName));
            }
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Generate a text event.
     */
    private void generateText() {
        try {
            mHandler.characters(mRTX.getValueOfCurrentNode().toCharArray(), 0,
                    mRTX.getValueOfCurrentNode().length());
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
        final IReadTransaction rtx = session.beginReadTransaction();

        final DefaultHandler defHandler = new DefaultHandler();

        final SAXSerializer serializer = new SAXSerializer(new DescendantAxis(
                rtx), defHandler);
        serializer.call();

        rtx.close();
        session.close();
        database.close();
    }
}

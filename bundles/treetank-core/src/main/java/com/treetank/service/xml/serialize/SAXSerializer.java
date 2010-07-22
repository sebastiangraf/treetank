package com.treetank.service.xml.serialize;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.node.ElementNode;
import com.treetank.service.xml.serialize.SerializerBuilder.SAXSerializerBuilder;

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
public final class SAXSerializer extends AbsSerializer implements
        Callable<Void> {

    /** Logger. */
    private static final Log LOGGER = LogFactory.getLog(SAXSerializer.class);

    /** SAX default handler. */
    private static final DefaultHandler handler = new DefaultHandler();

    /**
     * {@inheritDoc}
     */
    SAXSerializer(final SAXSerializerBuilder builder) {
        super(builder);
    }

    @Override
    public Void call() throws Exception {
        handler.startDocument();
        serialize();
        handler.endDocument();
        return null;
    }

    @Override
    public void emitEndElement() throws IOException {
        final String URI = mRTX.nameForKey(mRTX.getNode().getURIKey());
        final QName qName = mRTX.getQNameOfCurrentNode();
        try {
            handler.endElement(URI, qName.getLocalPart(), qName.toString());
        } catch (final SAXException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void emitNode() throws IOException {
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
                atts.addAttribute(URI, "xmlns",
                        "xmlns:" + mRTX.getQNameOfCurrentNode(), "CDATA", URI);
            }
            mRTX.moveTo(key);
        }

        // Process attributes.
        for (int i = 0, attCount = ((ElementNode) mRTX.getNode())
                .getAttributeCount(); i < attCount; i++) {
            mRTX.moveToAttribute(i);
            final String URI = mRTX.nameForKey(mRTX.getNode().getURIKey());
            final QName qName = mRTX.getQNameOfCurrentNode();
            atts.addAttribute(URI, qName.getLocalPart(), qName.toString(),
                    mRTX.getTypeOfCurrentNode(), mRTX.getValueOfCurrentNode());
            mRTX.moveTo(key);
        }

        // Create SAX events.
        try {
            final QName qName = mRTX.getQNameOfCurrentNode();
            handler.startElement(mRTX.nameForKey(mRTX.getNode().getURIKey()),
                    qName.getLocalPart(), qName.toString(), atts);

            // Empty elements.
            if (!((ElementNode) mRTX.getNode()).hasFirstChild()) {
                handler.endElement(mRTX.nameForKey(mRTX.getNode().getURIKey()),
                        qName.getLocalPart(), qName.toString());
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
            handler.characters(mRTX.getValueOfCurrentNode().toCharArray(), 0,
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

        // new SAXSerializer(rtx, new SerializerProperties(null).getmProps())
        // .call();

        rtx.close();
        session.close();
        database.close();
    }
}

/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.slf4j.LoggerFactory;
import org.treetank.access.Database;
import org.treetank.access.WriteTransactionState;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.node.ElementNode;
import org.treetank.utils.LogWrapper;

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

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(SAXSerializer.class));

    /** SAX content handler. */
    private transient ContentHandler mContHandler;

    /**
     * Constructor.
     * 
     * @param paramSession
     *            Treetank session {@link ISession}.
     * @param paramHandler
     *            SAX ContentHandler {@link ContentHandler}.
     * @param paramVersions
     *            Revisions to serialize.
     */
    public SAXSerializer(final ISession paramSession, final ContentHandler paramHandler,
        final long... paramVersions) {
        super(paramSession, paramVersions);
        mContHandler = paramHandler;
    }

    @Override
    protected void emitStartElement(final IReadTransaction rtx) {
        switch (rtx.getNode().getKind()) {
        case ROOT_KIND:
            break;
        case ELEMENT_KIND:
            generateElement(rtx);
            break;
        case TEXT_KIND:
            generateText(rtx);
            break;
        default:
            throw new UnsupportedOperationException("Node kind not supported by Treetank!");
        }
    }

    @Override
    protected void emitEndElement(final IReadTransaction rtx) {
        final String mURI = rtx.nameForKey(rtx.getNode().getURIKey());
        final QName qName = rtx.getQNameOfCurrentNode();
        try {
            mContHandler.endElement(mURI, qName.getLocalPart(), WriteTransactionState.buildName(qName));
        } catch (final SAXException e) {
            LOGWRAPPER.error(e);
        }
    }

    @Override
    protected void emitStartManualElement(final long revision) {
        final AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "revision", "tt", "", Long.toString(revision));
        try {
            mContHandler.startElement("", "tt", "tt", atts);
        } catch (final SAXException e) {
            LOGWRAPPER.error(e);
        }

    }

    @Override
    protected void emitEndManualElement(final long revision) {
        try {
            mContHandler.endElement("", "tt", "tt");
        } catch (final SAXException e) {
            LOGWRAPPER.error(e);
        }
    }

    /**
     * Generate a start element event.
     * 
     * @param mRtx
     *            Read Transaction
     */
    private void generateElement(final IReadTransaction mRtx) {
        final AttributesImpl atts = new AttributesImpl();
        final long key = mRtx.getNode().getNodeKey();

        // Process namespace nodes.
        for (int i = 0, namesCount = ((ElementNode)mRtx.getNode()).getNamespaceCount(); i < namesCount; i++) {
            mRtx.moveToNamespace(i);
            final String mURI = mRtx.nameForKey(mRtx.getNode().getURIKey());
            if (mRtx.nameForKey(mRtx.getNode().getNameKey()).length() == 0) {
                atts.addAttribute(mURI, "xmlns", "xmlns", "CDATA", mURI);
            } else {
                atts.addAttribute(mURI, "xmlns", "xmlns:" + mRtx.getQNameOfCurrentNode().getLocalPart(),
                    "CDATA", mURI);
            }
            mRtx.moveTo(key);
        }

        // Process attributes.
        for (int i = 0, attCount = ((ElementNode)mRtx.getNode()).getAttributeCount(); i < attCount; i++) {
            mRtx.moveToAttribute(i);
            final String mURI = mRtx.nameForKey(mRtx.getNode().getURIKey());
            final QName qName = mRtx.getQNameOfCurrentNode();
            atts.addAttribute(mURI, qName.getLocalPart(), WriteTransactionState.buildName(qName), mRtx
                .getTypeOfCurrentNode(), mRtx.getValueOfCurrentNode());
            mRtx.moveTo(key);
        }

        // Create SAX events.
        try {
            final QName qName = mRtx.getQNameOfCurrentNode();
            mContHandler.startElement(mRtx.nameForKey(mRtx.getNode().getURIKey()), qName.getLocalPart(),
                WriteTransactionState.buildName(qName), atts);

            // Empty elements.
            if (!((ElementNode)mRtx.getNode()).hasFirstChild()) {
                mContHandler.endElement(mRtx.nameForKey(mRtx.getNode().getURIKey()), qName.getLocalPart(),
                    WriteTransactionState.buildName(qName));
            }
        } catch (final SAXException e) {
            LOGWRAPPER.error(e);
        }
    }

    /**
     * Generate a text event.
     * 
     * @param mRtx
     *            Read Transaction.
     */
    private void generateText(final IReadTransaction mRtx) {
        try {
            mContHandler.characters(mRtx.getValueOfCurrentNode().toCharArray(), 0, mRtx
                .getValueOfCurrentNode().length());
        } catch (final SAXException e) {
            LOGWRAPPER.error(e);
        }
    }

    /**
     * Main method.
     * 
     * @param args
     *            args[0] specifies the path to the TT-storage from which to
     *            generate SAX events.
     * @throws Exception
     *             handling treetank exception
     */
    public static void main(final String... args) throws Exception {
        if (args.length != 1) {
            LOGWRAPPER.error("Usage: SAXSerializer input-TT");
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
            LOGWRAPPER.error(e);
        }
    }

    @Override
    protected void emitEndDocument() {
        try {
            mContHandler.endDocument();
        } catch (final SAXException e) {
            LOGWRAPPER.error(e);
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
    public boolean getFeature(final String mName) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    /* Implements XMLReader method. */
    @Override
    public Object getProperty(final String mName) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    /* Implements XMLReader method. */
    @Override
    public void parse(final InputSource mInput) throws IOException, SAXException {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }

    /* Implements XMLReader method. */
    @Override
    public void parse(final String mSystemID) throws IOException, SAXException {
        emitStartDocument();
        try {
            super.call();
        } catch (final Exception e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        emitEndDocument();
    }

    /* Implements XMLReader method. */
    @Override
    public void setContentHandler(final ContentHandler mContent) {
        mContHandler = mContent;
    }

    /* Implements XMLReader method. */
    @Override
    public void setDTDHandler(final DTDHandler mHandler) {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }

    /* Implements XMLReader method. */
    @Override
    public void setEntityResolver(final EntityResolver resolver) {
        throw new UnsupportedOperationException("Not supported by Treetank!");

    }

    /* Implements XMLReader method. */
    @Override
    public void setErrorHandler(final ErrorHandler mHandler) {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }

    /* Implements XMLReader method. */
    @Override
    public void setFeature(final String mName, final boolean mValue) throws SAXNotRecognizedException,
        SAXNotSupportedException {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }

    /* Implements XMLReader method. */
    @Override
    public void setProperty(final String mName, final Object mValue) throws SAXNotRecognizedException,
        SAXNotSupportedException {
        throw new UnsupportedOperationException("Not supported by Treetank!");
    }
}

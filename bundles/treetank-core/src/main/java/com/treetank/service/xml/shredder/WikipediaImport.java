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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.node.AbsStructNode;
import com.treetank.node.DocumentRootNode;
import com.treetank.node.ENodes;
import com.treetank.node.ElementNode;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.utils.LogWrapper;

/**
 * <h1>WikipediaImport</h1>
 * 
 * <p>
 * Import sorted Wikipedia revisions. Precondition is a file, which is produced from a Hadoop job.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class WikipediaImport {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(WikipediaImport.class);

    /**
     * Log wrapper {@link LogWrapper}.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LOGGER);

    /** StAX parser {@link XMLEventReader}. */
    private transient XMLEventReader mReader;

    /** TT Session {@link ISession}. */
    private transient ISession mSession;

    /** TT Write Transaction {@link IWriteTransaction}. */
    private transient IWriteTransaction mWTX;

    /**
     * Constructor.
     * 
     * @param paramXMLFile
     *            The XML file to import.
     * @param paramTTDir
     *            The Treetank destination storage directory.
     * 
     */
    public WikipediaImport(final File paramXMLFile, final File paramTTDir) {
        final XMLInputFactory xmlif = XMLInputFactory.newInstance();
        try {
            mReader = xmlif.createXMLEventReader(new FileInputStream(paramXMLFile));
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final FileNotFoundException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        try {
            Database.truncateDatabase(paramTTDir);
            final IDatabase db = Database.openDatabase(paramTTDir);
            mSession = db.getSession();
            mWTX = mSession.beginWriteTransaction();
        } catch (final TreetankException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    /**
     * Import data.
     * 
     * @param paramTimestamp
     *            Timestamp start tag {@link StartElement}.
     * @param paramPage
     *            Page start tag {@link StartElement}.
     * @param paramRev
     *            Revision start tag {@link StartElement}.
     * @param paramID
     *            Page-ID start tag {@link StartElement}.
     * @param paramDateRange
     *            Date range, the following values are possible:
     *            <dl>
     *            <dt>h</dt>
     *            <dd>hourly revisions</dd>
     *            <dt>d</dt>
     *            <dd>daily revisions</dd>
     *            <dt>w</dt>
     *            <dd>weekly revisions (currently unsupported)</dd>
     *            <dt>m</dt>
     *            <dd>monthly revisions</dd> </ul>
     * @throws XMLStreamException
     *             In case of any XML parsing errors.
     * @throws TreetankException
     *             In case of any Treetank errors.
     */
    public void importData(final StartElement paramTimestamp, final StartElement paramPage,
        final StartElement paramRev, final StartElement paramID, final char paramDateRange)
        throws XMLStreamException, TreetankException {

        // Initialize variables.
        String timestamp = null;
        boolean found = false;
        final QName text = new QName("text");
        String idText = null;
        boolean isRev = false;
        boolean isFirst = true;
        final List<XMLEvent> pageEvents = new ArrayList<XMLEvent>();
        final List<XMLEvent> revEvents = new ArrayList<XMLEvent>();

        while (mReader.hasNext()) {
            XMLEvent event = mReader.nextEvent();
            XMLEvent nextEvent = mReader.peek();

            if (event.isStartElement()) {
                if (XMLUpdateShredder.checkStAXElement(event.asStartElement(), paramPage) && !isFirst) {
                    isRev = false;
                    pageEvents.clear();
                    revEvents.clear();
                } else if (XMLUpdateShredder.checkStAXElement(event.asStartElement(), paramRev)) {
                    isRev = true;
                }
            }

            if (isRev) {
                revEvents.add(event);
            } else {
                pageEvents.add(event);
            }

            if (event.isStartElement()) {
                if (XMLUpdateShredder.checkStAXElement(event.asStartElement(), paramID)) {
                    event = mReader.nextEvent();
                    if (isRev) {
                        revEvents.add(event);
                    } else {
                        pageEvents.add(event);
                    }
                    idText = event.asCharacters().getData();
                } else if (XMLUpdateShredder.checkStAXElement(event.asStartElement(), paramTimestamp)) {
                    event = mReader.nextEvent();
                    revEvents.add(event);

                    if (event.isCharacters()) {
                        // Timestamp.
                        if (timestamp == null) {
                            timestamp = parseTimestamp(paramDateRange, event.asCharacters().getData());
                        } else if (!parseTimestamp(paramDateRange, event.asCharacters().getData()).equals(
                            timestamp)) {
                            mWTX.commit();
                            mWTX.close();
                            mWTX = mSession.beginWriteTransaction();
                        }
                    } else {
                        LOGGER.warn("No characters after timestamp start element found!");
                    }

                    assert idText != null;

                    final QName page = paramPage.getName();
                    final QName id = paramID.getName();
                    final QName rev = paramRev.getName();
                    final String query =
                        "//" + qNameToString(page) + "[fn:string(./" + qNameToString(id) + ") = '" + idText
                            + "']/" + qNameToString(rev);
                    final XPathAxis axis = new XPathAxis(mWTX, query);

                    found = false;
                    int resCounter = 0;
                    while (axis.hasNext()) {
                        axis.next();
                        found = true;
                        resCounter++;

                        assert resCounter == 1;
                        mWTX.moveToParent();
                        mWTX.moveToParent();

                        assert mWTX.getQNameOfCurrentNode().equals(paramPage);
                    }
                }
            }

            // Shredding.
            if (nextEvent.isStartElement()
                && nextEvent.asStartElement().getName().getLocalPart().equals(text.getLocalPart())) {
                if (found) {
                    // Remove revision metadata.
                    mWTX.moveToFirstChild();
                    while (((AbsStructNode)mWTX.getNode()).hasRightSibling()
                        && !mWTX.getQNameOfCurrentNode().getLocalPart().equals(text.getLocalPart())) {
                        mWTX.remove();
                    }
                    mWTX.moveToParent();
                    assert mWTX.getQNameOfCurrentNode().equals(paramPage);

                    // Shredder revision metadata.
                    final XMLShredder shredder =
                        new XMLShredder(mWTX, false, false, revEvents.toArray(new XMLEvent[revEvents.size()]));
                    shredder.call();

                    // Shredder into existing file.
                    final XMLUpdateShredder updateShredder = new XMLUpdateShredder(mWTX, mReader, false);
                    updateShredder.call();
                } else {
                    // Move wtx to end of file and append page.
                    mWTX.moveToDocumentRoot();
                    final boolean hasFirstChild = ((DocumentRootNode)mWTX.getNode()).hasFirstChild();
                    if (hasFirstChild) {
                        // All subsequent shredders, move cursor to the end.
                        mWTX.moveToFirstChild();
                        mWTX.moveToFirstChild();

                        assert mWTX.getNode().getKind() == ENodes.ELEMENT_KIND;
                        assert mWTX.getQNameOfCurrentNode().equals(paramPage.asStartElement().getName());
                        while (((ElementNode)mWTX.getNode()).hasRightSibling()) {
                            mWTX.moveToRightSibling();
                        }
                        assert mWTX.getNode().getKind() == ENodes.ELEMENT_KIND;
                        assert mWTX.getQNameOfCurrentNode().equals(paramPage.asStartElement().getName());
                    }

                    final XMLEvent[] xmlPageEvents = new XMLEvent[pageEvents.size()];
                    XMLShredder shredder;
                    if (hasFirstChild) {
                        shredder = new XMLShredder(mWTX, false, false, pageEvents.toArray(xmlPageEvents));
                    } else {
                        shredder = new XMLShredder(mWTX, true, false, pageEvents.toArray(xmlPageEvents));
                    }
                    try {
                        // Shredder page metadata.
                        shredder.call();

                        // Shredder revision metadata.
                        shredder =
                            new XMLShredder(mWTX, true, false, revEvents.toArray(new XMLEvent[revEvents
                                .size()]));
                        shredder.call();

                        assert text.equals(nextEvent.asStartElement().getName());

                        // Shredder text of revision.
                        shredder = new XMLShredder(mWTX, mReader, false);
                        shredder.call();
                    } catch (final TreetankException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    }

                    /* Move StAX parser to </page> tag and shredder next revision if another revision with the
                       same timestamp exists. */
                    while (mReader.hasNext()
                        && !(event.isEndElement() && event.asEndElement().getName()
                            .equals(paramPage.getName()))) {
                        event = mReader.nextEvent();
                        nextEvent = mReader.peek();
                        
                        if (nextEvent.isStartElement() && nextEvent.asStartElement().equals(paramRev)) {
                            shredder = new XMLShredder(mWTX, mReader, false);
                            shredder.call();
                        }
                    }
                    
                    event = mReader.nextEvent();
                }
            }
        }
    }

    /**
     * Parses a given Timestamp-String to extract time interval which is used.
     * 
     * @param paramDateRange
     *            Date Range which is used for revisioning.
     * @param paramTimestamp
     *            The timestamp to parse.
     * @return parsed and truncated String.
     */
    private String parseTimestamp(final char paramDateRange, final String paramTimestamp) {
        String retVal = null;
        final StringBuilder sb = new StringBuilder();

        switch (paramDateRange) {
        case 'h':
            final String[] splittedHour = paramTimestamp.split(":");
            sb.append(splittedHour[0]);
            sb.append(":");
            sb.append(splittedHour[1]);
            break;
        case 'd':
            final String[] splittedDay = paramTimestamp.split("T");
            sb.append(splittedDay[0]);
            break;
        case 'w':
            throw new UnsupportedOperationException("Not supported right now!");
        case 'm':
            final String[] splittedMonth = paramTimestamp.split("-");
            sb.append(splittedMonth[0]);
            sb.append("-");
            sb.append(splittedMonth[1]);
            break;
        default:
            throw new IllegalStateException("Date range not known!");
        }

        return retVal;
    }

    /**
     * Move StAX-Parser to revision start tag.
     * 
     * @param paramRev
     *            Revision StartElement
     * @throws XMLStreamException
     *             In case any XML parser error occurs.
     */
    private void moveStAXParserToRev(final StartElement paramRev) throws XMLStreamException {
        // Move StAX parser to revision element.
        while (mReader.hasNext()) {
            XMLEvent event = mReader.nextEvent();

            if (event.isStartElement()) {
                if (XMLUpdateShredder.checkStAXElement(event.asStartElement(), paramRev)) {
                    break;
                }
            }
        }
    }

    /**
     * Get prefix:localname or localname String representation of a qName.
     * 
     * @param paramQName
     *            The full qualified name.
     * @return string representation.
     */
    private static String qNameToString(final QName paramQName) {
        String retVal = null;
        if (null == paramQName.getPrefix() || "" == paramQName.getPrefix()) {
            retVal = paramQName.getLocalPart();
        } else {
            retVal = paramQName.getPrefix() + ":" + paramQName.getLocalPart();
        }
        return retVal;
    }

    /**
     * Main method.
     * 
     * @param args
     *            Arguments.
     */
    public static void main(final String... args) {
        if (args.length != 2) {
            System.err.println("usage: WikipediaImport path/to/xmlFile path/to/TTStorage");
        }

        // Create necessary element nodes.
        final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        final StartElement timestamp = eventFactory.createStartElement(new QName("timestamp"), null, null);
        final StartElement page = eventFactory.createStartElement(new QName("page"), null, null);
        final StartElement rev = eventFactory.createStartElement(new QName("revision"), null, null);
        final StartElement id = eventFactory.createStartElement(new QName("id"), null, null);
        try {
            new WikipediaImport(new File(args[0]), new File(args[1])).importData(timestamp, page, rev, id,
                'h');
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final TreetankException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }
}

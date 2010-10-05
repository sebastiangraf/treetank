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
public final class WikipediaImport {

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

    /** {@link XMLEvent}s which specify the page metadata. */
    private final transient List<XMLEvent> mPageEvents = new ArrayList<XMLEvent>();

    /** {@link XMLEvent}s which specify the revision metadata. */
    private final transient List<XMLEvent> mRevEvents = new ArrayList<XMLEvent>();

    /** Determines if page has been found in Treetank storage. */
    private transient boolean mFound;

    /** String value of text-element. */
    private transient String mIdText;

    /** Determines if StAX parser is currently parsing revision metadata. */
    private transient boolean mIsRev;

    /** Timestamp of each revision as a simple String. */
    private transient String mTimestamp;

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
     *            <dd>monthly revisions</dd>
     *            </dl>
     * @throws XMLStreamException
     *             In case of any XML parsing errors.
     * @throws TreetankException
     *             In case of any Treetank errors.
     */
    public void importData(final StartElement paramTimestamp, final StartElement paramPage,
        final StartElement paramRev, final StartElement paramID, final char paramDateRange)
        throws XMLStreamException, TreetankException {

        // Initialize variables.
        mFound = false;
        mIsRev = false;
        final QName text = new QName("text");
        boolean isFirst = true;

        while (mReader.hasNext()) {
            XMLEvent event = mReader.nextEvent();
            XMLEvent nextEvent = mReader.peek();

            if (isWhitespace(event)
                && !(nextEvent.isStartElement() && nextEvent.asStartElement().getName().equals(text))) {
                continue;
            }

            if (event.isStartElement()) {
                if (XMLUpdateShredder.checkStAXElement(event.asStartElement(), paramPage) && !isFirst) {
                    // StAX parser in page metadata.
                    mIsRev = false;
                    mPageEvents.clear();
                    mRevEvents.clear();
                } else if (XMLUpdateShredder.checkStAXElement(event.asStartElement(), paramRev)) {
                    // StAX parser in rev metadata.
                    isFirst = false;
                    mIsRev = true;
                }
            }

            addEvent(event);

            if (event.isStartElement()) {
                parseStartTag(event, paramTimestamp, paramPage, paramRev, paramID, paramDateRange);
            }

            // Shredding.
            if (nextEvent != null && nextEvent.isStartElement()
                && nextEvent.asStartElement().getName().getLocalPart().equals(text.getLocalPart())) {
                if (mFound) {
                    // // Remove page metadata.
                    // mWTX.moveToFirstChild();
                    // while (((AbsStructNode)mWTX.getNode()).hasRightSibling()
                    // && !mWTX.getQNameOfCurrentNode().equals(paramRev.getName())) {
                    // mWTX.remove();
                    // }
                    // mWTX.moveToParent();
                    // assert mWTX.getQNameOfCurrentNode().equals(paramPage.getName());
                    //
                    // // Shredder page metadata.
                    // XMLShredder shredder =
                    // new XMLShredder(mWTX, true, false, mPageEvents
                    // .toArray(new XMLEvent[mPageEvents.size()]));
                    // shredder.call();
                    //                    
                    //                    
                    //                    
                    // // Shredder revision metadata.
                    // shredder =
                    // new XMLShredder(mWTX, true, false, mRevEvents
                    // .toArray(new XMLEvent[mRevEvents.size()]));
                    // shredder.call();

                    // Shredder into existing file.
                    XMLUpdateShredder updateShredder =
                        new XMLUpdateShredder(mWTX, new ListEventReader(mPageEvents), false, false);
                    updateShredder.call();
                    updateShredder =
                        new XMLUpdateShredder(mWTX, new ListEventReader(mRevEvents), false, false);
                    updateShredder.call();
                    updateShredder = new XMLUpdateShredder(mWTX, mReader, false, false);
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

                    final XMLEvent[] xmlmPageEvents = new XMLEvent[mPageEvents.size()];
                    XMLShredder shredder;
                    if (hasFirstChild) {
                        shredder = new XMLShredder(mWTX, false, false, mPageEvents.toArray(xmlmPageEvents));
                    } else {
                        shredder = new XMLShredder(mWTX, true, false, mPageEvents.toArray(xmlmPageEvents));
                    }
                    try {
                        // Shredder page metadata.
                        shredder.call();

                        // Shredder revision metadata.
                        shredder =
                            new XMLShredder(mWTX, false, false, mRevEvents.toArray(new XMLEvent[mRevEvents
                                .size()]));
                        shredder.call();

                        assert text.equals(nextEvent.asStartElement().getName());

                        // Shredder text of revision.
                        shredder = new XMLShredder(mWTX, mReader, false, false);
                        shredder.call();

                        mWTX.moveToParent();
                        assert mWTX.getQNameOfCurrentNode().equals(paramRev.getName());
                    } catch (final TreetankException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    }

                    /*
                     * Move StAX parser to </page> tag and shredder subsequent revisions before </page> if
                     * two or more revisions have been done at the same time (the timestamp is the same for
                     * all subsequent revisions).
                     */
                    while (mReader.hasNext()
                        && !(event.isEndElement() && event.asEndElement().getName().equals(
                            paramPage.getName()))) {
                        event = mReader.nextEvent();
                        nextEvent = mReader.peek();

                        if (nextEvent.isStartElement()
                            && XMLUpdateShredder.checkStAXElement(nextEvent.asStartElement(), paramRev)) {
                            shredder = new XMLShredder(mWTX, mReader, false, false);
                            shredder.call();
                        }
                    }

                    // Move cursor in TT to page element.
                    while (((AbsStructNode)mWTX.getNode()).hasParent()
                        && !mWTX.getQNameOfCurrentNode().equals(paramPage.getName())) {
                        mWTX.moveToParent();
                    }

                    assert mWTX.getQNameOfCurrentNode().equals(paramPage.getName());
                }
            }
        }

        mWTX.commit();
        mWTX.close();
        mSession.close();
    }

    /**
     * Parses a start tag.
     * 
     * @param paramEvent
     *            Current StAX {@link XMLEvent}.
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
     *            <dd>monthly revisions</dd>
     *            </dl>
     * @throws XMLStreamException
     *             In case of any XML parsing errors.
     * @throws TreetankException
     *             In case of any Treetank errors.
     */
    private void parseStartTag(final XMLEvent paramEvent, final StartElement paramTimestamp,
        final StartElement paramPage, final StartElement paramRev, final StartElement paramID,
        final char paramDateRange) throws XMLStreamException, TreetankException {
        XMLEvent event = paramEvent;

        if (XMLUpdateShredder.checkStAXElement(event.asStartElement(), paramID)) {
            event = mReader.nextEvent();
            addEvent(event);

            if (!mIsRev) {
                mIdText = event.asCharacters().getData();
            }
        } else if (XMLUpdateShredder.checkStAXElement(event.asStartElement(), paramTimestamp)) {
            // Timestamp start tag found.
            event = mReader.nextEvent();
            mRevEvents.add(event);

            if (event.isCharacters()) {
                final String currTimestamp = event.asCharacters().getData();

                // Timestamp.
                if (mTimestamp == null) {
                    mTimestamp = parseTimestamp(paramDateRange, currTimestamp);
                } else if (!parseTimestamp(paramDateRange, currTimestamp).equals(mTimestamp)) {
                    mTimestamp = currTimestamp;
                    mWTX.commit();
                    mWTX.close();
                    mWTX = mSession.beginWriteTransaction();
                }
            } else {
                LOGGER.warn("No characters after timestamp start element found!");
            }

            assert mIdText != null;

            // Search for existing page.
            final QName page = paramPage.getName();
            final QName id = paramID.getName();
            final QName rev = paramRev.getName();
            final String query =
                "//" + qNameToString(page) + "[fn:string(" + qNameToString(id) + ") = '" + mIdText + "']/"
                    + qNameToString(rev);
            System.out.println(query);
            final XPathAxis axis = new XPathAxis(mWTX, query);

            mFound = false;
            int resCounter = 0;
            long key = 0;
            while (axis.hasNext()) {
                axis.next();
                mFound = true;
                resCounter++;

                assert resCounter == 1;
                mWTX.moveToParent();
                assert mWTX.getQNameOfCurrentNode().equals(paramPage.getName());
                key = mWTX.getNode().getNodeKey();
            }
            mWTX.moveTo(key);
        }
    }

    /**
     * Add an event to either page or revision list.
     * 
     * @param paramEvent
     *            {@link XMLEvent} to add to list.
     */
    private void addEvent(final XMLEvent paramEvent) {
        if (mIsRev) {
            mRevEvents.add(paramEvent);
        } else {
            mPageEvents.add(paramEvent);
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

        return sb.toString();
    }

    /**
     * Determines if the current event is a whitespace event.
     * 
     * @param paramEvent
     *            {@link XMLEvent} to check.
     * @return true if it is whitespace, otherwise false.
     */
    private boolean isWhitespace(final XMLEvent paramEvent) {
        return paramEvent.isCharacters() && paramEvent.asCharacters().isWhiteSpace();
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
        if (null == paramQName.getPrefix() || "".equals(paramQName.getPrefix())) {
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

        final long start = System.currentTimeMillis();
        System.out.print("Importing wikipedia...");

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

        System.out.println(" done in " + (System.currentTimeMillis() - start) / 1000 + "[s].");
    }
}

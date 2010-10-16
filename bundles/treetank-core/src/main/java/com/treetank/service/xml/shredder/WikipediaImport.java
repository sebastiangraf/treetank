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
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.LoggerFactory;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
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
    private static final LogWrapper LOGWRAPPER =
        new LogWrapper(LoggerFactory.getLogger(WikipediaImport.class));

    /** StAX parser {@link XMLEventReader}. */
    private transient XMLEventReader mReader;

    /** TT Session {@link ISession}. */
    private transient ISession mSession;

    /** TT Write Transaction {@link IWriteTransaction}. */
    private transient IWriteTransaction mWTX;

    /** {@link XMLEvent}s which specify the page metadata. */
    private final transient List<XMLEvent> mPageEvents;

    /** Determines if page has been found in Treetank storage. */
    private transient boolean mFound;

    /** String value of text-element. */
    private transient String mIdText;

    /** Determines if StAX parser is currently parsing revision metadata. */
    private transient boolean mIsRev;

    /** Timestamp of each revision as a simple String. */
    private transient String mTimestamp;

    /** File to shredder. */
    private final transient File mFile;

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
        mFile = paramXMLFile;
        mPageEvents = new LinkedList<XMLEvent>();
        final XMLInputFactory xmlif = XMLInputFactory.newInstance();
        try {
            mReader = xmlif.createXMLEventReader(new FileInputStream(paramXMLFile));
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final FileNotFoundException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        try {
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
     * @param paramText
     *            Revision text start tag {@link StartElement}.
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
     * @throws IOException
     *             In case of any I/O error.
     */
    public void importData(final StartElement paramTimestamp, final StartElement paramPage,
        final StartElement paramRev, final StartElement paramID, final StartElement paramText,
        final char paramDateRange) throws XMLStreamException, TreetankException, IOException {

        // Initialize variables.
        mFound = false;
        mIsRev = false;
        boolean isFirst = true;

        while (mReader.hasNext()) {
            final XMLEvent event = mReader.nextEvent();

            if (isWhitespace(event)) {
                continue;
            }

            // Add event to page or revision metadata list if it's not a whitespace.
            if (!isWhitespace(event)) {
                mPageEvents.add(event);
            }

            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                if (XMLUpdateShredder.checkStAXStartElement(event.asStartElement(), paramRev)) {
                    // StAX parser in rev metadata.
                    isFirst = false;
                    mIsRev = true;
                } else {
                    parseStartTag(event, paramTimestamp, paramPage, paramRev, paramID, paramDateRange);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().equals(paramPage.getName()) && !isFirst) {
                    // StAX parser is located at the end of an article/page.
                    mIsRev = false;

                    if (mFound) {
                        try {
                            final XMLShredder updateShredder =
                                new XMLUpdateShredder(mWTX, XMLShredder.createListReader(mPageEvents), false,
                                    mPageEvents, false);
                            updateShredder.call();
                        } catch (final IOException e) {
                            LOGWRAPPER.error(e.getMessage(), e);
                        }
                    } else {
                        // Move wtx to end of file and append page.
                        mWTX.moveToDocumentRoot();
                        final boolean hasFirstChild = ((DocumentRootNode)mWTX.getNode()).hasFirstChild();
                        if (hasFirstChild) {
                            moveToLastPage(paramPage);
                        }

                        XMLShredder shredder = null;
                        if (hasFirstChild) {
                            // Shredder as child.
                            shredder =
                                new XMLShredder(mWTX, XMLShredder.createListReader(mPageEvents), false, false);
                        } else {
                            // Shredder as right sibling.
                            shredder =
                                new XMLShredder(mWTX, XMLShredder.createListReader(mPageEvents), true, false);
                        }

                        shredder.call();
                        assert mWTX.getQNameOfCurrentNode().equals(paramPage.getName());
                    }

                    mPageEvents.clear();
                }
                break;
            default:
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

        if (XMLUpdateShredder.checkStAXStartElement(event.asStartElement(), paramID)) {
            event = mReader.nextEvent();
            mPageEvents.add(event);

            if (!mIsRev) {
                mIdText = event.asCharacters().getData();
            }
        } else if (XMLUpdateShredder.checkStAXStartElement(event.asStartElement(), paramTimestamp)) {
            // Timestamp start tag found.
            event = mReader.nextEvent();
            mPageEvents.add(event);

            if (event.isCharacters()) {
                final String currTimestamp = event.asCharacters().getData();

                // Timestamp.
                if (mTimestamp == null) {
                    mTimestamp = parseTimestamp(paramDateRange, currTimestamp);
                } else if (!parseTimestamp(paramDateRange, currTimestamp).equals(mTimestamp)) {
                    mTimestamp = parseTimestamp(paramDateRange, currTimestamp);
                    mWTX.commit();
                    mWTX.close();
                    mWTX = mSession.beginWriteTransaction();
                }
            } else {
                LOGWRAPPER.warn("No characters after timestamp start element found!");
            }

            assert mIdText != null;

            // Search for existing page.
            final QName page = paramPage.getName();
            final QName id = paramID.getName();
            final String query =
                "//" + qNameToString(page) + "[fn:string(" + qNameToString(id) + ") = '" + mIdText + "']";
            System.out.println(query);
            mWTX.moveToDocumentRoot();
            final XPathAxis axis = new XPathAxis(mWTX, query);

            mFound = false; // Determines if page is found in shreddered file.
            int resCounter = 0; // Counts found page.
            long key = mWTX.getNode().getNodeKey();
            while (axis.hasNext()) {
                axis.next();

                // Page is found.
                mFound = true;

                // Make sure no more than one page with a unique id is found.
                resCounter++;
                assert resCounter == 1;

                // Make sure the transaction is on the page element found.
                assert mWTX.getQNameOfCurrentNode().equals(paramPage.getName());
                key = mWTX.getNode().getNodeKey();
            }
            mWTX.moveTo(key);
        }
    }

    /**
     * Parses a given Timestamp-String to extract time interval which is used (simple String value to improve
     * performance).
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
     * Moves {@link IWriteTransaction} to last shreddered article/page.
     * 
     * @param paramPage
     *                  {@link StartElement} page.
     */
    private void moveToLastPage(final StartElement paramPage) {
        // All subsequent shredders, move cursor to the end.
        mWTX.moveToFirstChild();
        mWTX.moveToFirstChild();

        assert mWTX.getNode().getKind() == ENodes.ELEMENT_KIND;
        assert mWTX.getQNameOfCurrentNode().equals(paramPage.getName());
        while (((ElementNode)mWTX.getNode()).hasRightSibling()) {
            mWTX.moveToRightSibling();
        }
        assert mWTX.getNode().getKind() == ENodes.ELEMENT_KIND;
        assert mWTX.getQNameOfCurrentNode().equals(paramPage.getName());
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
        final File xml = new File(args[0]);
        final File tnk = new File(args[1]);
        Database.truncateDatabase(tnk);

        // Create necessary element nodes.
        final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        final StartElement timestamp = eventFactory.createStartElement(new QName("timestamp"), null, null);
        final StartElement page = eventFactory.createStartElement(new QName("page"), null, null);
        final StartElement rev = eventFactory.createStartElement(new QName("revision"), null, null);
        final StartElement id = eventFactory.createStartElement(new QName("id"), null, null);
        final StartElement text = eventFactory.createStartElement(new QName("text"), null, null);
        try {
            new WikipediaImport(xml, tnk).importData(timestamp, page, rev, id, text, 'h');
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final TreetankException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final IOException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        System.out.println(" done in " + (System.currentTimeMillis() - start) / 1000 + "[s].");
    }
}

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
package com.treetank.wikipedia.hadoop;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.utils.LogWrapper;

/**
 * <h1>XMLMapper</h1>
 * 
 * <p>
 * Maps single revisions. Output key is of type {@link DateWritable} which implements {@link Writable} and
 * represents the timestamp of the revision, the value is the revision subtree and saved as {@link Text}.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLMap extends Mapper<DateWritable, Text, DateWritable, Text> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLMap.class);

    /**
     * Log wrapper {@link LogWrapper}.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LOGGER);

    /** The page element, which specifies an article. */
    private transient QName mPage;

    /** The revision element, which specifies a revision of an article. */
    private transient QName mRevision;

    /** Records processed. */
    private transient long mNumRecords;

    /** Timestamp element. */
    private transient QName mTimestamp;

    /** Input file. */
    private transient String mInputFile;

    /** {@link DateFormat}. */
    private final transient DateFormat mFormatter =
        new SimpleDateFormat("yyyy.MM.dd HH.mm.ss", Locale.ENGLISH);

    /**
     * Default constructor.
     */
    public XMLMap() {
        // To make Checkstyle happy.
    }

    @Override
    public void setup(final Context paramContext) throws IOException, InterruptedException {
        final Configuration config = paramContext.getConfiguration();
        mTimestamp = new QName(config.get("timestamp"));
        mPage = new QName(config.get("page"));
        mRevision = new QName(config.get("revision"));
        mInputFile = config.get("mapreduce.map.input.file");
    }

    @Override
    public void map(final DateWritable paramKey, final Text paramValue, final Context paramContext) {
        boolean isTimestamp = false;
        final List<XMLEvent> page = new LinkedList<XMLEvent>();
        final List<XMLEvent> rev = new LinkedList<XMLEvent>();
        boolean isPage = true;
        final DateWritable key = new DateWritable();
        final Text value = new Text();
        final Writer writer = new StringWriter();
        XMLEventReader eventReader = null;
        XMLEventWriter eventWriter = null;
        try {
            eventReader =
                XMLInputFactory.newInstance().createXMLEventReader(new StringReader(paramValue.toString()));
            eventWriter = XMLOutputFactory.newInstance().createXMLEventWriter(writer);
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final FactoryConfigurationError e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        assert eventReader != null;
        while (eventReader.hasNext()) {
            XMLEvent event = null;
            try {
                event = eventReader.nextEvent();
            } catch (final XMLStreamException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            assert event != null;
            // Parse timestamp (key).
            if (event.isStartElement() && event.asStartElement().getName().equals(mTimestamp)) {
                isTimestamp = true;
            } else if (isTimestamp && event.isCharacters() && !event.asCharacters().isWhiteSpace()) {
                isTimestamp = false;
                try {
                    // Parse timestamp.
                    final String text = event.asCharacters().getData();
                    final String[] splitted = text.split("T");
                    final String time = splitted[1].substring(0, splitted[1].length() - 1);
                    key.setTimestamp(mFormatter.parse(splitted[0] + " " + time));
                } catch (final ParseException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                }
            }

            if (event.isStartElement() && event.asStartElement().getName().equals(mRevision)) {
                // Determines if page header end is found.
                isPage = false;
                rev.clear();
            }

            if (isPage) {
                // Inside page header (author, ID, pagename...)
                page.clear();
                rev.clear();
                page.add(event);
            } else {
                // Inside revision.
                rev.add(event);
            }

            if (event.isEndElement() && event.asEndElement().getName().equals(mRevision)) {
                // Write output.
                try {
                    // Make sure to create the page end tag.
                    rev.add(XMLEventFactory.newFactory().createEndElement(mPage, null));

                    // Write key/value pairs.
                    assert eventWriter != null;
                    for (final XMLEvent ev : page) {
                        eventWriter.add(ev);
                    }
                    for (final XMLEvent ev : rev) {
                        eventWriter.add(ev);
                    }
                    eventWriter.flush();
                    final String strValue = writer.toString();
                    value.append(strValue.getBytes(), 0, strValue.length());
                    paramContext.write(key, value);
                    value.clear();
                } catch (final IOException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (final InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (final XMLStreamException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        if ((++mNumRecords % 100) == 0) {
            paramContext.setStatus("Finished processing " + mNumRecords + " records "
                + "from the input file: " + mInputFile);
        }
    }
}

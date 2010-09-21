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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.events.XMLEvent;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.utils.LogWrapper;

/**
 * <h1>XMLMapper</h1>
 * 
 * <p>
 * Maps single revisions. Output key is the date of the revision, the value is the XML-Fragment which
 * represents the revision as a list of XML events.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLMap extends Mapper<Date, List<XMLEvent>, Date, List<XMLEvent>> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLMap.class);

    /**
     * Log wrapper {@link LogWrapper}.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LOGGER);

    /** The revision element, which specifies a revision of an article. */
    private transient QName mRevision;

    /** Records processed. */
    private transient long mNumRecords;

    /** Timestamp element. */
    private transient QName mTimestamp;

    /** Input file. */
    private transient String mInputFile;

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
        mRevision = new QName(config.get("revision"));
        mInputFile = config.get("mapreduce.map.input.file");
    }

    @Override
    public void map(final Date paramKey, final List<XMLEvent> paramValue, final Context paramContext) {
        final DateFormat formatter = new SimpleDateFormat("yyyy.MM.ddTHH.mm.ssZ");
        boolean isTimestamp = false;
        final List<XMLEvent> value = new ArrayList<XMLEvent>();
        final List<XMLEvent> page = new ArrayList<XMLEvent>();
        final List<XMLEvent> rev = new ArrayList<XMLEvent>();
        boolean isPage = true;
        Date key = null;

        for (final XMLEvent event : paramValue) {
            // Parse timestamp (key).
            if (event.isStartElement() && event.asStartElement().getName().equals(mTimestamp)) {
                isTimestamp = true;
            } else if (isTimestamp && event.isCharacters() && !event.asCharacters().isWhiteSpace()) {
                isTimestamp = false;
                try {
                    key = (Date) formatter.parse(event.asCharacters().getData());
                } catch (final ParseException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                }
            }

            if (event.isStartElement() && event.asStartElement().getName().equals(mRevision)) {
                // Determines if page header end is found.
                isPage = false;
            }

            if (isPage) {
                // Inside page header (author, ID, pagename...)
                page.add(event);
                rev.clear();
            } else {
                // Inside revision.
                rev.add(event);
            }

            if (event.isEndElement() && event.asEndElement().getName().equals(mRevision)) {
                // Write output.
                try {
                    value.addAll(page);
                    value.addAll(rev);
                    paramContext.write(key, value);
                    value.clear();
                } catch (final IOException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (final InterruptedException e) {
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

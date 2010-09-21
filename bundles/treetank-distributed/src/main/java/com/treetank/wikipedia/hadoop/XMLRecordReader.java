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

import com.treetank.utils.LogWrapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>XMLRecordReader</h1>
 * 
 * <p>
 * Read an XML record.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLRecordReader extends RecordReader<Date, List<XMLEvent>> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLRecordReader.class);

    /**
     * Log wrapper {@link LogWrapper}.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LOGGER);

    /** Start of record. */
    private long mStart;

    /** End of record. */
    private long mEnd;

    /** Key is a date (timestamp) {@link Date}. */
    private transient Date mKey;

    /** File input stream. */
    private transient FSDataInputStream mFileIn;

    /** Compression of input stream {@link CompressionCodecFactory}. */
    private transient CompressionCodecFactory mCompressionCodecs;

    /** Start of record {@link EventFilter}. */
    private transient EventFilter mBeginFilter;

    /** End of record {@link EventFilter}. */
    private transient EventFilter mEndFilter;

    /** StAX parser {@link XMLEventReader}. */
    private transient XMLEventReader mReader;

    /** Full qualified name of the timestamp element used as the key {link QName}. */
    private transient QName mDate;
    
    /** Value is a list of XML events. */
    private transient List<XMLEvent> mValue = new ArrayList<XMLEvent>();

    /**
     * Constructor.
     */
    public XMLRecordReader() {
        // Default constructor.
    }

    @Override
    public void initialize(final InputSplit paramGenericSplit, final TaskAttemptContext paramContext)
        throws IOException {
        final FileSplit split = (FileSplit) paramGenericSplit;
        final Configuration jobConf = paramContext.getConfiguration();

        mStart = split.getStart();
        mEnd = mStart + split.getLength();
        final Path file = split.getPath();

        // Open the file and seek to the start of the split.
        final FileSystem fs = file.getFileSystem(jobConf);
        final FSDataInputStream fileIn = fs.open(split.getPath());
        fileIn.seek(mStart);

        mCompressionCodecs = new CompressionCodecFactory(jobConf);
        final CompressionCodec codec = mCompressionCodecs.getCodec(file);

        InputStream in = fileIn;
        if (codec != null) {
            in = codec.createInputStream(fileIn);
            mEnd = Long.MAX_VALUE;
        }
        in = new BufferedInputStream(in);

        final XMLInputFactory xmlif = XMLInputFactory.newInstance();
        try {
            mReader = xmlif.createXMLEventReader(in);
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        // Create start/end record filters.
        final String recordIdentifier = jobConf.get("record_element_name");
        final String recordNsPrefix = jobConf.get("namespace_prefix");
        final String recordNsURI = jobConf.get("namespace_URI");

        mBeginFilter = new EventFilter() {
            public boolean accept(final XMLEvent paramEvent) {
                return paramEvent.isStartElement()
                    && paramEvent.asStartElement().getName().getLocalPart().equals(recordIdentifier)
                    && paramEvent.asStartElement().getName().getPrefix().equals(recordNsPrefix)
                    && paramEvent.asStartElement().getName().getNamespaceURI().equals(recordNsURI);
            }
        };
        mEndFilter = new EventFilter() {
            public boolean accept(final XMLEvent paramEvent) {
                return paramEvent.isEndElement()
                    && paramEvent.asEndElement().getName().getLocalPart().equals(recordIdentifier);
            }
        };

        mDate = new QName(jobConf.get("timestamp"));
    }

    @Override
    public Date getCurrentKey() {
        return mKey;
    }

    @Override
    public List<XMLEvent> getCurrentValue() {
        return mValue;
    }

    @Override
    public float getProgress() {
        if (mStart == mEnd) {
            return 0f;
        } else {
            try {
                return Math.min(1.0f, (mFileIn.getPos() - mStart) / (float) (mEnd - mStart));
            } catch (final IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return 0f;
    }

    @Override
    public synchronized void close() throws IOException {
        if (mFileIn != null) {
            mFileIn.close();
        }
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        mValue.clear();

        boolean retVal = false;

        try {
            // Moves to start of record
            final boolean foundStartEvent = moveToEvent(mReader, mBeginFilter, false);

            if (foundStartEvent) {
                final boolean foundEndEvent = moveToEvent(mReader, mEndFilter, true);

                if (foundEndEvent) {
                    // Add last element to the writer.
                    mValue.add(mReader.nextEvent());
                    
                    retVal = true;
                }
            }
            // } else {
            // // Could not successfully find end of record.
            // retVal = false;
            // }
            // } else {
            // // Could not successfully find beginning of record.
            // retVal = false;
            // }
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        return retVal;
    }

    /**
     * Move to beginning of record.
     * 
     * @param paramReader
     *            XML Reader {@link XMLEventReader}.
     * @param paramFilter
     *            XML filter {@link EventFilter}.
     * @param paramIsRecord
     *            Determines if the parser is inside a record or outside.
     * @return false if event was not found and received end of file
     * @throws XMLStreamException
     *             In case any parsing error occurs.
     */
    private boolean moveToEvent(final XMLEventReader paramReader, final EventFilter paramFilter,
        final boolean paramIsRecord) throws XMLStreamException {
        boolean isTimestamp = false;
        while (paramReader.hasNext() && !paramFilter.accept(paramReader.peek())) {
            final XMLEvent event = (XMLEvent) paramReader.next();

            if (isTimestamp && event.isCharacters() && !event.asCharacters().isWhiteSpace()) {
                isTimestamp = false;
                final DateFormat formatter = new SimpleDateFormat("yyyy.MM.ddTHH.mm.ssZ"); 
                try {
                    mKey = (Date)formatter.parse(event.asCharacters().getData());
                } catch (final ParseException e) {
                    LOGWRAPPER.warn(e.getMessage(), e);
                }
            }

            if (paramIsRecord) {
                // Parser currently is located somewhere after the start of a record (inside a record).
                mValue.add(event);

                if (event.isStartElement() && mDate.equals(event.asStartElement().getName())) {
                    isTimestamp = true;
                }
            }
        }

        return paramReader.hasNext();
    }

}

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
package org.treetank.wikipedia.hadoop;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.slf4j.LoggerFactory;

import com.treetank.utils.LogWrapper;

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
public final class XMLRecordReader extends RecordReader<DateWritable, Text> {

    /**
     * Log wrapper {@link LogWrapper}.
     */
    private static final LogWrapper LOGWRAPPER =
        new LogWrapper(LoggerFactory.getLogger(XMLRecordReader.class));

    /** Start of record. */
    private transient long mStart;

    /** End of record. */
    private transient long mEnd;

    /** Start of record {@link EventFilter}. */
    private transient EventFilter mBeginFilter;

    /** End of record {@link EventFilter}. */
    private transient EventFilter mEndFilter;

    /** StAX parser {@link XMLEventReader}. */
    private transient XMLEventReader mReader;

    /** {@link QName} (Date) which determines the key event. */
    private transient QName mDate;

    /** Key is a date (timestamp) {@link Date}. */
    private DateWritable mKey;

    /** Value is of type {@link Text}. */
    private Text mValue;

    /** Record element identifier {@see StartElement}. */
    private transient StartElement mRecordElem;

    /** {@link XMLEventWriter} for event data. */
    private transient XMLEventWriter mEventWriter;

    /** A {@link StringWriter}, where the {@link XMLEventWriter} writes to. */
    private transient StringWriter mWriter;

    /** Counter which counts parsed {@link XMLEvent}s to track process. */
    private transient int mCountEvents;

    /**
     * Constructor.
     */
    public XMLRecordReader() {
        // Default constructor.
    }

    @Override
    public void initialize(final InputSplit paramGenericSplit, final TaskAttemptContext paramContext)
        throws IOException {
        final FileSplit split = (FileSplit)paramGenericSplit;
        final Configuration conf = paramContext.getConfiguration();

        mStart = split.getStart();
        mEnd = mStart + split.getLength();
        mValue = new Text();
        mKey = new DateWritable();
        mWriter = new StringWriter();
        try {
            mEventWriter = XMLOutputFactory.newInstance().createXMLEventWriter(mWriter);
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final FactoryConfigurationError e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        final Path file = split.getPath();

        // Open the file and seek to the start of the split.
        final FileSystem fileSys = file.getFileSystem(conf);
        final FSDataInputStream fileIn = fileSys.open(split.getPath());
        fileIn.seek(mStart);

        final CompressionCodecFactory comprCodecs = new CompressionCodecFactory(conf);
        final CompressionCodec codec = comprCodecs.getCodec(file);

        InputStream input = fileIn;
        if (codec != null) {
            input = codec.createInputStream(fileIn);
            mEnd = Long.MAX_VALUE;
        }
        input = new BufferedInputStream(input);

        final XMLInputFactory xmlif = XMLInputFactory.newInstance();
        try {
            mReader = xmlif.createXMLEventReader(input);
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }

        // Create start/end record filters.
        final String recordIdentifier = conf.get("record_element_name");
        final String recordNsPrefix = conf.get("namespace_prefix");
        final String recordNsURI = conf.get("namespace_URI");

        if (recordIdentifier == null) {
            throw new IllegalStateException("Record identifier must be specified (record_elem_name)!");
        }

        if (recordNsPrefix == null && recordNsURI == null) {
            mRecordElem =
                XMLEventFactory.newFactory().createStartElement(new QName(recordIdentifier), null, null);
        } else {
            mRecordElem =
                XMLEventFactory.newFactory().createStartElement(
                    new QName(recordNsURI, recordIdentifier, recordNsPrefix), null, null);
        }

        mBeginFilter = new EventFilter() {
            @Override
            public boolean accept(final XMLEvent paramEvent) {
                return paramEvent.isStartElement()
                    && paramEvent.asStartElement().getName().equals(mRecordElem.getName());
            }
        };
        mEndFilter = new EventFilter() {
            @Override
            public boolean accept(final XMLEvent paramEvent) {
                return paramEvent.isEndElement()
                    && paramEvent.asEndElement().getName().equals(mRecordElem.getName());
            }
        };

        mDate = new QName(conf.get("timestamp"));
    }

    @Override
    public DateWritable getCurrentKey() {
        return mKey;
    }

    @Override
    public Text getCurrentValue() {
        return mValue;
    }

    @Override
    public float getProgress() {
        return mCountEvents;
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            mReader.close();
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        mValue.clear();
        boolean retVal = false;

        try {
            // Moves to start of record.
            final boolean foundStartEvent = moveToEvent(mReader, mBeginFilter, false);

            if (foundStartEvent) {
                final boolean foundEndEvent = moveToEvent(mReader, mEndFilter, true);

                if (foundEndEvent) {
                    // Add last element to the writer.
                    mEventWriter.add(mReader.nextEvent());
                    retVal = true;

                    mWriter.flush();
                    mValue.set(mWriter.toString());
                    mWriter.getBuffer().setLength(0);
                }
            }
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
        final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        while (paramReader.hasNext() && !paramFilter.accept(paramReader.peek())) {
            final XMLEvent event = paramReader.nextEvent();
            mCountEvents++;

            if (isTimestamp && event.isCharacters() && !event.asCharacters().isWhiteSpace()) {
                isTimestamp = false;
                try {
                    // Parse timestamp.
                    final String text = event.asCharacters().getData();
                    final String[] splitted = text.split("T");
                    final String time = splitted[1].substring(0, splitted[1].length() - 1);
                    mKey.setTimestamp(formatter.parse(splitted[0] + " " + time));
                } catch (final ParseException e) {
                    LOGWRAPPER.warn(e.getMessage(), e);
                }
            }

            if (paramIsRecord) {
                // Parser currently is located somewhere after the start of a record (inside a record).
                mEventWriter.add(event);

                if (event.isStartElement() && mDate.equals(event.asStartElement().getName())) {
                    isTimestamp = true;
                }
            }
        }

        return paramReader.hasNext();
    }

}

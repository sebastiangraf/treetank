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

package org.treetank.wikipedia.hadoop;

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
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
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
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    /**
     * Default constructor.
     */
    public XMLMap() {
        // To make Checkstyle happy.
    }

    @Override
    protected void setup(final Context paramContext) throws IOException, InterruptedException {
        final Configuration config = paramContext.getConfiguration();
        mTimestamp = new QName(config.get("timestamp"));
        mPage = new QName(config.get("record_element_name"));
        mRevision = new QName(config.get("revision"));
        mInputFile = config.get("mapreduce.map.input.file");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void map(final DateWritable paramKey, final Text paramValue, final Context paramContext) {
        boolean isTimestamp = false;
        final List<XMLEvent> page = new LinkedList<XMLEvent>();
        final List<XMLEvent> rev = new LinkedList<XMLEvent>();
        boolean isPage = true;
        final DateWritable key = new DateWritable();
        final Text value = new Text();
        final StringWriter writer = new StringWriter();
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
        assert eventWriter != null;
        while (eventReader.hasNext()) {
            XMLEvent event = null;
            try {
                event = eventReader.nextEvent();
            } catch (final XMLStreamException e) {
                LOGWRAPPER.error(e.getMessage(), e);
            }

            assert event != null;
            
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                // Parse timestamp (key).
                final StartElement startTag = event.asStartElement();
                // TODO: Use Java7 string-switch.
                if (startTag.getName().equals(mTimestamp)) {
                    isTimestamp = true;
                } else if (startTag.getName().equals(mRevision)) {
                    // Determines if page header end is found.
                    isPage = false;
                } else if (startTag.getName().equals(mPage)) {
                    // Determines if page header start is found.
                    isPage = true;
                }
                break;
            case XMLStreamConstants.CHARACTERS:
                if (isTimestamp && !event.asCharacters().isWhiteSpace()) {
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
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (event.asEndElement().getName().equals(mRevision)) {
                    // Write output.
                    try {
                        // Make sure to get the revision end tag.
                        rev.add(event);
                        
                        // Make sure to create the page end tag.
                        rev.add(XMLEventFactory.newFactory().createEndElement(mPage, null));

                        // Append events to an event writer.
                        assert eventWriter != null;
                        for (final XMLEvent ev : page) {
                            eventWriter.add(ev);
                        }
                        for (final XMLEvent ev : rev) {
                            eventWriter.add(ev);
                        }
                        
                        // Flush the event writer and underlying string writer.
                        eventWriter.flush();
                        writer.flush();
                        
                        final String strValue = writer.toString();
                        
                        // Clear buffer.
                        writer.getBuffer().setLength(0);
                        
                        // Append buffered string to Text value.
                        value.append(strValue.getBytes(), 0, strValue.length());
                        
                        // Write key/value pairs.
                        paramContext.write(key, value);
                        
                        // Clear revision list and value text.
                        rev.clear();
                        value.clear();
                    } catch (final IOException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    } catch (final InterruptedException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    } catch (final XMLStreamException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    }
                }               
                break;
            default:
                break;
            }

            if (isPage) {
                // Inside page header (author, ID, pagename...)
                if (!event.isStartDocument()) {
                    page.add(event);
                }
            } else {
                // Inside revision.
                if (!(event.isEndElement() && event.asEndElement().getName().equals(mRevision))) {
                    rev.add(event);
                }
            }
        }

        if ((++mNumRecords % 100) == 0) {
            paramContext.setStatus("Finished processing " + mNumRecords + " records "
                + "from the input file: " + mInputFile);
        }
        
        try {
            eventReader.close();
            eventWriter.close();
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }
}

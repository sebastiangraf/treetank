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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.utils.LogWrapper;

/**
 * <h1>XMLRecordReader</h1>
 * 
 * <p>
 * Appends values to an output file.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLRecordWriter<K, V> extends RecordWriter<K, V> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLRecordWriter.class);

    /**
     * Log wrapper {@link LogWrapper}.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LOGGER);

    /** Writer to write XML events {@link XMLEventWriter}. */
    private transient XMLEventWriter mWriter;

    /** Factory to create events {@link XMLEventFactory}. */
    private transient XMLEventFactory mEventFactory;

    /** Full qualified name of root element {@link QName}. */
    private transient StartElement mRootElem;

    /**
     * Constructor.
     * 
     * @param paramWriter
     *            Instance of {@link XMLEventWriter}.
     * @param paramRootElem
     *            Root element.
     * @throws IOException
     *             In case any I/O operation fails.
     * @throws XMLStreamException
     *             In case any error occurs while creating events.
     */
    public XMLRecordWriter(final XMLEventWriter paramWriter, final StartElement paramRootElem)
        throws IOException, XMLStreamException {
        mWriter = paramWriter;
        mEventFactory = XMLEventFactory.newInstance();
        mRootElem = paramRootElem;
        mWriter.add(mRootElem);
    }

    @Override
    public synchronized void close(final TaskAttemptContext paramContext) throws IOException,
        InterruptedException {
        try {
            mWriter.add(mEventFactory.createEndElement(mRootElem.getName(), null));
            mWriter.flush();
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void write(final K paramKey, final V paramValue) throws IOException,
        InterruptedException {
        if (paramValue instanceof XMLEvent) {
            final XMLEvent[] events = (XMLEvent[])paramValue;
            for (final XMLEvent event : events) {
                try {
                    mWriter.add(event);
                } catch (final XMLStreamException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                }
            }
        }
    }

}

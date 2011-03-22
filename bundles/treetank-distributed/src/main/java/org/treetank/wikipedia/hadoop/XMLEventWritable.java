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

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.hadoop.io.Writable;
import org.slf4j.LoggerFactory;

import com.treetank.utils.LogWrapper;

/**
 * <h1>XMLEventWritable</h1>
 * 
 * <p>
 * Serialize/Deserialize {@link XMLEvent}s. Very inefficient, because fpr deserializing a StAX parser is being
 * created.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLEventWritable implements Writable {

    /**
     * {@link LogWrapper} to log messages.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(XMLReduce.class));
    
    /** The underlying {@link XMLEvent}. */
    private XMLEvent mEvent;
    
    /** {@link Writer}. */
    private Writer mWriter;
    
    /** 
     * Default constructor. 
     */
    public XMLEventWritable() {
        mWriter = new StringWriter();
    }
    
    /** 
     * Constructor.
     * 
     * @param paramEvent
     *                 The {@link XMLEvent} to wrap.
     */
    public XMLEventWritable(final XMLEvent paramEvent) {
        mWriter = new StringWriter();
        mEvent = paramEvent;
    }

    /**
     * Set an event.
     * 
     * @param paramEvent
     *            The {@link XMLEvent} to set.
     */
    public void setEvent(final XMLEvent paramEvent) {
        mEvent = paramEvent;
    }

    @Override
    public void readFields(final DataInput paramIn) throws IOException {
        final String in = paramIn.readUTF();   
        try {
            final XMLEventReader reader = XMLInputFactory.newFactory().createXMLEventReader(new ByteArrayInputStream(in.getBytes("UTF-8")));
            
            if (reader.hasNext()) {
                mEvent = reader.nextEvent();
            }
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        } catch (final FactoryConfigurationError e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
    }

    @Override
    public void write(final DataOutput paramOut) throws IOException {
        try {
        mEvent.writeAsEncodedUnicode(mWriter);
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        mWriter.flush();
        paramOut.writeUTF(mWriter.toString());
    }

}

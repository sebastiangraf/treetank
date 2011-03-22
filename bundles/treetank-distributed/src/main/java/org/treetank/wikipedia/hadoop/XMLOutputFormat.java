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
 *     * Neither the name of the University of Konstanz nor the
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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.treetank.utils.LogWrapper;

/**
 * <h1>XMLOutputFormat</h1>
 * 
 * <p>
 * Outputs only values and appends them
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 */
public class XMLOutputFormat<K, V> extends FileOutputFormat<K, V> {

    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLOutputFormat.class);

    /**
     * Log wrapper {@link LogWrapper}.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LOGGER);

    // /** Root element {@link QName}. */
    // private final transient StartElement mRoot;
    //
    // /**
    // * Empty constructor.
    // *
    // * @param paramRootElem
    // * Root element.
    // */
    // public XMLOutputFormat(final StartElement paramRootElem) {
    // mRoot = paramRootElem;
    // }

    /**
     * Default constructor.
     */
    public XMLOutputFormat() {
        // To make Checkstyle happy.
    }

    @Override
    public RecordWriter<K, V> getRecordWriter(final TaskAttemptContext paramContext) throws IOException,
        InterruptedException {
        final Path file = FileOutputFormat.getOutputPath(paramContext);
        final FileSystem fs = file.getFileSystem(paramContext.getConfiguration());
        final FSDataOutputStream out = fs.create(file);
        final XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLEventWriter writer = null;
        XMLRecordWriter<K, V> recordWriter = null;
        final StartElement root =
            XMLEventFactory.newFactory().createStartElement(paramContext.getConfiguration().get("root"),
                null, null);
        try {
            writer = factory.createXMLEventWriter(out);
            recordWriter = new XMLRecordWriter<K, V>(writer, root);
        } catch (final XMLStreamException e) {
            LOGWRAPPER.error(e.getMessage(), e);
        }
        return recordWriter;
    }

}

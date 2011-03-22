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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.LoggerFactory;

import com.treetank.service.xml.shredder.ListEventReader;
import com.treetank.utils.LogWrapper;

/**
 * <h1>XMLReducer</h1>
 * 
 * <p>
 * After sorting and grouping key's the reducer just emits the results (identity reducer).
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XMLReduce extends Reducer<DateWritable, Text, DateWritable, Text> {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(XMLReduce.class));

    /** Path to stylesheet for XSLT transformation. */
    private static final String STYLESHEET =
        "src" + File.separator + "main" + File.separator + "resources" + File.separator + "wikipedia.xsl";

    /**
     * Empty Constructor.
     */
    public XMLReduce() {
        // To make Checkstyle happy.
    }

    @Override
    public void reduce(final DateWritable paramKey, final Iterable<Text> paramValue,
        final Context paramContext) throws IOException, InterruptedException {
        final Text combined = new Text();

        combined.append("<root>".getBytes(), 0, "<root>".length());
        for (final Text event : paramValue) {
            combined.append(event.getBytes(), 0, event.getLength());
        }
        combined.append("</root>".getBytes(), 0, "</root>".length());

        final Processor proc = new Processor(false);
        final XsltCompiler compiler = proc.newXsltCompiler();
        try {
            final XsltExecutable exec = compiler.compile(new StreamSource(new File(STYLESHEET)));
            final XsltTransformer transform = exec.load();
            transform.setSource(new StreamSource(new StringReader(combined
                .toString())));
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final Serializer serializer = new Serializer();
            serializer.setOutputStream(out);
            transform.setDestination(serializer);
            transform.transform();
            final String value = out.toString();
            System.out.println(value);
            paramContext.write(null, new Text(value));
        } catch (final SaxonApiException e) {
            LOGWRAPPER.error(e);
        }
    }
}

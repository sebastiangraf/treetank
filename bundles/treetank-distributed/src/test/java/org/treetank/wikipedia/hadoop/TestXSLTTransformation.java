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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.treetank.wikipedia.hadoop.XMLReduce;

import com.treetank.TestHelper;
import com.treetank.utils.LogWrapper;

/**
 * <h1>TestXSLTTransformation</h1>
 * 
 * <p>
 * Tests the grouping of revisions in one page, which have the same timestamp.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class TestXSLTTransformation extends TestCase {

    /**
     * Log wrapper for better output.
     */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(XMLReduce.class));

    /** Input XML file. */
    private static final String INPUT =
        "src" + File.separator + "test" + File.separator + "resources" + File.separator + "testXSLTInput.xml";

    /** Path to stylesheet for XSLT transformation. */
    private static final String STYLESHEET =
        "src" + File.separator + "main" + File.separator + "resources" + File.separator + "wikipedia.xsl";

    /** Path to output for XSLT transformation. */
    private static final String EXPECTED =
        "src" + File.separator + "test" + File.separator + "resources" + File.separator
            + "testXSLTOutput.xml";

    /** Default Constructor. */
    public TestXSLTTransformation() {
        // To make Checkstyle happy.
    }

    @Override
    @Before
    public void setUp() throws Exception {
        // XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testTransform() throws Exception {
        final Processor proc = new Processor(false);
        final XsltCompiler compiler = proc.newXsltCompiler();
        try {
            final XsltExecutable exec = compiler.compile(new StreamSource(new File(STYLESHEET)));
            final XsltTransformer transform = exec.load();
            transform.setSource(new StreamSource(new FileInputStream(INPUT)));
            final Serializer serializer = new Serializer();
            final OutputStream out = new ByteArrayOutputStream();
            serializer.setOutputStream(out);
            transform.setDestination(serializer);
            transform.transform();
            final StringBuilder expected = TestHelper.readFile(new File(EXPECTED), false);
            assertEquals("XML files match", expected.toString(), "<root>" + out.toString()
                + "</root>");
        } catch (final SaxonApiException e) {
            LOGWRAPPER.error(e);
        }
    }
}

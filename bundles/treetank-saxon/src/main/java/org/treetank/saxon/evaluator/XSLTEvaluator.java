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

package org.treetank.saxon.evaluator;

import java.io.File;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.api.IDatabase;
import org.treetank.saxon.wrapper.DocumentWrapper;
import org.treetank.saxon.wrapper.NodeWrapper;

/**
 * <h1>XSLT Evaluator</h1>
 * 
 * <p>
 * Transforms an input document according to an XSLT stylesheet and returns a resulting output stream.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XSLTEvaluator implements Callable<OutputStream> {

    /**
     * Log wrapper for better output.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(XSLTEvaluator.class);

    /** Stylesheet file. */
    private final transient File mStylesheet;

    /** Resulting stream of the transformation. */
    private final transient OutputStream mOut;

    /**
     * Serializer to specify serialization output properties and the destination
     * of the Transformation.
     */
    private transient Serializer mSerializer;

    /** Treetank database. */
    private final transient IDatabase mDatabases;

    /**
     * Constructor.
     * 
     * @param database
     *            Treetank database.
     * @param stylesheet
     *            Path to stylesheet.
     * @param out
     *            Resulting stream of the transformation.
     */
    public XSLTEvaluator(final IDatabase database, final File stylesheet, final OutputStream out) {
        this(database, stylesheet, out, null);
    }

    /**
     * Constructor.
     * 
     * @param paramDatabase
     *            Treetank mDatabase.
     * @param paramStyle
     *            Path to stylesheet.
     * @param paramOut
     *            Resulting stream of the transformation.
     * @param paramSerializer
     *            Serializer, for which one can specify output properties.
     */
    public XSLTEvaluator(final IDatabase paramDatabase, final File paramStyle, final OutputStream paramOut,
        final Serializer paramSerializer) {
        mDatabases = paramDatabase;
        mStylesheet = paramStyle;
        mOut = paramOut;
        mSerializer = paramSerializer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream call() {
        final Processor proc = new Processor(false);
        final XsltCompiler comp = proc.newXsltCompiler();
        XsltExecutable exp;
        XdmNode source;

        try {
            final Configuration config = proc.getUnderlyingConfiguration();
            final NodeWrapper doc = (NodeWrapper)new DocumentWrapper(mDatabases, config).wrap();
            exp = comp.compile(new StreamSource(mStylesheet));
            source = proc.newDocumentBuilder().build(doc);

            if (mSerializer == null) {
                final Serializer out = new Serializer();
                out.setOutputProperty(Serializer.Property.METHOD, "xml");
                out.setOutputProperty(Serializer.Property.INDENT, "yes");
                out.setOutputStream(mOut);
                mSerializer = out;
            } else {
                mSerializer.setOutputStream(mOut);
            }

            final XsltTransformer trans = exp.load();
            trans.setInitialContextNode(source);
            trans.setDestination(mSerializer);
            trans.transform();
        } catch (final SaxonApiException e) {
            LOGGER.error("Saxon exception: " + e.getMessage(), e);
        }

        return mOut;
    }

}

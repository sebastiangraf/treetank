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
package com.treetank.saxon.evaluator;

import java.io.OutputStream;
import java.util.concurrent.Callable;

import com.treetank.api.IDatabase;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryExecutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>XQuery evaluator</h1>
 * 
 * <p>
 * Evaluates an XQuery expression against a Treetank storage. Output is available through an output stream.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XQueryEvaluatorOutputStream implements Callable<Void> {
    /**
     * Log wrapper for better output.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(XQueryEvaluatorOutputStream.class);

    /** XQuery expression. */
    private final transient String mExpression;

    /** Treetank database. */
    private final transient IDatabase mDatabase;

    /** Output Stream. */
    private final transient OutputStream mOut;

    /** Serializer to specify serialization output properties. */
    private transient Serializer mSerializer;

    /**
     * Constructor.
     * 
     * @param expression
     *            XQuery expression.
     * @param database
     *            Treetank database.
     * @param out
     *            Output Stream.
     */
    public XQueryEvaluatorOutputStream(final String expression, final IDatabase database,
        final OutputStream out) {
        this(expression, database, out, null);
    }

    /**
     * Constructor.
     * 
     * @param expression
     *            XQuery expression.
     * @param database
     *            Treetank database {@link IDatabase}.
     * @param out
     *            Output Stream.
     * @param serializer
     *            Serializer, for which one can specify output properties {@link Serializer}.
     */
    public XQueryEvaluatorOutputStream(final String expression, final IDatabase database,
        final OutputStream out, final Serializer serializer) {
        mExpression = expression;
        mDatabase = database;
        mOut = out;
        mSerializer = serializer;
    }

    @Override
    public Void call() throws Exception {
        try {
            final Processor proc = new Processor(false);
            final Configuration config = proc.getUnderlyingConfiguration();
            final NodeWrapper doc = (NodeWrapper)new DocumentWrapper(mDatabase, config).wrap();
            final XQueryCompiler comp = proc.newXQueryCompiler();
            final XQueryExecutable exp = comp.compile(mExpression);

            if (mSerializer == null) {
                final Serializer out = new Serializer();
                out.setOutputProperty(Serializer.Property.METHOD, "xml");
                out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
                out.setOutputStream(mOut);
                mSerializer = out;
            }

            final net.sf.saxon.s9api.XQueryEvaluator exe = exp.load();
            exe.setSource(doc);
            exe.run(mSerializer);
            return null;
        } catch (final SaxonApiException e) {
            LOGGER.error("Saxon Exception: " + e.getMessage(), e);
            throw e;
        }
    }
}

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

import java.util.concurrent.Callable;

import org.xml.sax.ContentHandler;

import com.treetank.api.IDatabase;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SAXDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryExecutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>XQuery evaluator</h1>
 * 
 * <p>
 * Evaluates an XQuery expression and returns the result to a given content handler.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XQueryEvaluatorSAXHandler implements Callable<Void> {
    /**
     * Log wrapper for better output.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(XQueryEvaluatorSAXHandler.class);

    /** XQuery expression. */
    private final String mExpression;

    /** Treetank database. */
    private final IDatabase mDatabase;

    /** SAX receiver. */
    private final ContentHandler mHandler;

    /**
     * Constructor.
     * 
     * @param expression
     *            XQuery expression.
     * @param database
     *            Treetank database.
     * @param handler
     *            SAX content handler.
     */
    public XQueryEvaluatorSAXHandler(final String expression, final IDatabase database,
        final ContentHandler handler) {
        mExpression = expression;
        mDatabase = database;
        mHandler = handler;
    }

    @Override
    public Void call() throws Exception {
        try {
            final Processor proc = new Processor(false);
            final Configuration config = proc.getUnderlyingConfiguration();
            final NodeWrapper doc = (NodeWrapper)new DocumentWrapper(mDatabase, config).wrap();
            final XQueryCompiler comp = proc.newXQueryCompiler();
            final XQueryExecutable exp = comp.compile(mExpression);
            final net.sf.saxon.s9api.XQueryEvaluator exe = exp.load();
            exe.setSource(doc);
            exe.run(new SAXDestination(mHandler));
            return null;
        } catch (final SaxonApiException e) {
            LOGGER.error("Saxon Exception: " + e.getMessage(), e);
            throw e;
        }
    }
}

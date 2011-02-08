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

import com.treetank.api.IDatabase;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>XPath Evaluator</h1>
 * 
 * <p>
 * The XPath evaluator takes an XPath expression and evaluates the expression against a wrapped Treetank
 * document.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class XPathEvaluator implements Callable<XPathSelector> {

    /** An XPath expression. */
    private transient final String mExpression;

    /** Treetank database. */
    private transient final IDatabase mDatabase;

    /**
     * Log wrapper for better output.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(XPathEvaluator.class);

    /**
     * Constructor.
     * 
     * @param expression
     *            XPath expression.
     * @param databse
     *            Treetank database.
     */
    public XPathEvaluator(final String expression, final IDatabase database) {
        mExpression = expression;
        mDatabase = database;
    }

    @Override
    public XPathSelector call() throws Exception {
        final Processor proc = new Processor(false);
        final Configuration config = proc.getUnderlyingConfiguration();
        final NodeWrapper doc = (NodeWrapper)new DocumentWrapper(mDatabase, config).wrap();
        final XPathCompiler xpath = proc.newXPathCompiler();
        final DocumentBuilder builder = proc.newDocumentBuilder();
        XPathSelector selector = null;

        try {
            final XdmItem booksDoc = builder.build(doc);
            selector = xpath.compile(mExpression).load();
            selector.setContextItem(booksDoc);
        } catch (final SaxonApiException e) {
            LOGGER.error("Saxon Exception: " + e.getMessage(), e);
            throw e;
        }

        return selector;
    }
}

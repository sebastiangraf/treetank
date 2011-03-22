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

import java.util.concurrent.Callable;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.api.IDatabase;
import org.treetank.saxon.wrapper.DocumentWrapper;
import org.treetank.saxon.wrapper.NodeWrapper;

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
     * @param paramExpression
     *            XPath expression.
     * @param paramDatabase
     *            Treetank database.
     */
    public XPathEvaluator(final String paramExpression, final IDatabase paramDatabase) {
        mExpression = paramExpression;
        mDatabase = paramDatabase;
    }

    /**
     * {@inheritDoc}
     */
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

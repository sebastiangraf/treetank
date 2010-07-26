package com.treetank.saxon.evaluator;

import java.util.concurrent.Callable;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SAXDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryExecutable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ContentHandler;

import com.treetank.api.IDatabase;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

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
public class XQueryEvaluatorSAXHandler implements Callable<Void> {

    /** Logger. */
    private static final Log LOGGER = LogFactory.getLog(XQueryEvaluatorSAXHandler.class);

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

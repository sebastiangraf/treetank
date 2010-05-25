package com.treetank.saxon.evaluator;

import java.io.File;
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

import com.treetank.api.ISession;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

/**
 * <h1>XQuery evaluator</h1>
 * 
 * <p>
 * Evaluates an XQuery expression and returns the result to a content handler.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SAXXQueryEvaluator implements Callable<SAXDestination> {

	/** Logger. */
	private static final Log LOGGER = LogFactory
			.getLog(SAXXQueryEvaluator.class);

	/** XQuery expression. */
	private final String mExpression;

	/** Target of query. */
	private final File mTarget;

	/** Treetank session. */
	private final ISession mSession;

	/** SAX receiver. */
	private final ContentHandler mHandler;

	/**
	 * Constructor.
	 * 
	 * @param expression
	 *            XQuery expression.
	 * @param session
	 *            Treetank session.
	 * @param file
	 *            Target Treetank storage.
	 * @param handler
	 *            SAX content handler.
	 */
	public SAXXQueryEvaluator(final String expression, final ISession session,
			final File file, final ContentHandler handler) {
		mExpression = expression;
		mSession = session;
		mTarget = file;
		mHandler = handler;
	}

	@Override
	public SAXDestination call() {
		final SAXDestination destination = new SAXDestination(mHandler);

		try {
			final Processor proc = new Processor(false);
			final Configuration config = proc.getUnderlyingConfiguration();
			final NodeWrapper doc = (NodeWrapper) new DocumentWrapper(mSession,
					config, mTarget.getAbsolutePath()).wrap();
			final XQueryCompiler comp = proc.newXQueryCompiler();
			final XQueryExecutable exp = comp.compile(mExpression);
			final net.sf.saxon.s9api.XQueryEvaluator exe = exp.load();
			exe.setSource(doc);
			exe.run(destination);
		} catch (final SaxonApiException e) {
			LOGGER.error("Saxon Exception: " + e.getMessage(), e);
		}

		return destination;
	}
}

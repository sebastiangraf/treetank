package com.treetank.saxon.evaluator;

import java.util.concurrent.Callable;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.api.IDatabase;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

/**
 * <h1>XPath Evaluator</h1>
 * 
 * <p>
 * The XPath evaluator takes an XPath expression and evaluates the expression
 * against a wrapped Treetank document.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class XPathEvaluator implements Callable<XPathSelector> {

	/** An XPath expression. */
	private transient final String mExpression;

	/** Treetank database. */
	private transient final IDatabase mDatabase;

	/** Logger. */
	private static final Log LOGGER = LogFactory.getLog(XPathEvaluator.class);

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
		final NodeWrapper doc = (NodeWrapper) new DocumentWrapper(mDatabase,
				config).wrap();
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

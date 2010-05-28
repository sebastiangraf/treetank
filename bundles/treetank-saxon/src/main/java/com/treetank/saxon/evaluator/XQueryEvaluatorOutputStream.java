package com.treetank.saxon.evaluator;

import java.io.File;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryExecutable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.api.ISession;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

/**
 * <h1>XQuery evaluator</h1>
 * 
 * <p>Evaluates an XQuery expression against a Treetank storage. Output is 
 * available through an output stream.</p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class XQueryEvaluatorOutputStream implements Runnable {

	/** Logger. */
	private static final Log LOGGER = LogFactory
			.getLog(com.treetank.saxon.evaluator.XQueryEvaluatorOutputStream.class);

	/** XQuery expression. */
	private transient final String mExpression;

	/** Target of query. */
	private transient final File mTarget;

	/** Treetank session. */
	private transient final ISession mSession;

	/** Output Stream. */
	private transient final OutputStream mOut;

	/** Serializer to specify serialization output properties. */
	private transient Serializer mSerializer;

	/**
	 * Constructor.
	 * 
	 * @param expression
	 *            XQuery expression.
	 * @param session
	 *            Treetank session.
	 * @param file
	 *            Target Treetank storage.
	 * @param out
	 *            Output Stream.
	 */
	public XQueryEvaluatorOutputStream(final String expression, final ISession session,
			final File file, final OutputStream out) {
		this(expression, session, file, out, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param expression
	 *            XQuery expression.
	 * @param session
	 *            Treetank session.
	 * @param file
	 *            Target Treetank storage.
	 * @param out
	 *            Output Stream.
	 * @param serializer
	 *            Serializer, for which one can specify output properties.
	 */
	public XQueryEvaluatorOutputStream(final String expression, final ISession session,
			final File file, final OutputStream out, final Serializer serializer) {
		mExpression = expression;
		mSession = session;
		mTarget = file;
		mOut = out;
		mSerializer = serializer;
	}

	@Override
	public void run() {
		try {
			final Processor proc = new Processor(false);
			final Configuration config = proc.getUnderlyingConfiguration();
			final NodeWrapper doc = (NodeWrapper) new DocumentWrapper(mSession,
					config, mTarget.getAbsolutePath()).wrap();
			final XQueryCompiler comp = proc.newXQueryCompiler();
			final XQueryExecutable exp = comp.compile(mExpression);

			if (mSerializer == null) {
				final Serializer out = new Serializer();
				out.setOutputProperty(Serializer.Property.METHOD, "xml");
				out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION,
						"yes");
				out.setOutputStream(mOut);
				mSerializer = out;
			}

			final net.sf.saxon.s9api.XQueryEvaluator exe = exp.load();
			exe.setSource(doc);
			exe.run(mSerializer);
		} catch (final SaxonApiException e) {
			LOGGER.error("Saxon Exception: " + e.getMessage(), e);
		}
	}
}

package com.treetank.saxon.evaluator;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.api.IDatabase;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

/**
 * <h1>XSLT Evaluator</h1>
 * 
 * <p>
 * Transforms an input document according to an XSLT stylesheet and returns a
 * resulting output stream.
 * </p>
 * 
 * @author johannes
 * 
 */
public class XSLTEvaluator implements Callable<OutputStream> {

	/** Logger. */
	private static final Log LOGGER = LogFactory
			.getLog("com.treetank.saxon.evaluator.XSLTEvaluator.class");

	/** Stylesheet file. */
	private transient final File mStylesheet;

	/** Resulting stream of the transformation. */
	private transient final OutputStream mOut;

	/**
	 * Serializer to specify serialization output properties and the destination
	 * of the Transformation.
	 */
	private transient Serializer mSerializer;

	/** Treetank database. */
	private transient final IDatabase mDatabases;

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
	 * @param mDatabases
	 *            Treetank mDatabases.
	 * @param file
	 *            Treetank storage to which the stylesheet has to be applied.
	 * @param stylesheet
	 *            Path to stylesheet.
	 * @param out
	 *            Resulting stream of the transformation.
	 * @param serializer
	 *            Serializer, for which one can specify output properties.
	 */
	public XSLTEvaluator(final IDatabase database, 
			final File stylesheet, final OutputStream out,
			final Serializer serializer) {
		mDatabases = database;
		mStylesheet = stylesheet;
		mOut = out;
		mSerializer = serializer;
	}

	@Override
	public OutputStream call() {
		final Processor proc = new Processor(false);
		final XsltCompiler comp = proc.newXsltCompiler();
		XsltExecutable exp;
		XdmNode source;

		try {
			final Configuration config = proc.getUnderlyingConfiguration();
			final NodeWrapper doc = (NodeWrapper) new DocumentWrapper(
					mDatabases, config).wrap();
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

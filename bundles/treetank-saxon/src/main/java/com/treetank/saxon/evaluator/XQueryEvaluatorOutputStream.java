package com.treetank.saxon.evaluator;

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

import com.treetank.api.IDatabase;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

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
public class XQueryEvaluatorOutputStream implements Callable<Void> {

    /** Logger. */
    private static final Log LOGGER = LogFactory
        .getLog(com.treetank.saxon.evaluator.XQueryEvaluatorOutputStream.class);

    /** XQuery expression. */
    private transient final String mExpression;

    /** Treetank database. */
    private transient final IDatabase mDatabase;

    /** Output Stream. */
    private transient final OutputStream mOut;

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
     * @param session
     *            Treetank session.
     * @param out
     *            Output Stream.
     * @param serializer
     *            Serializer, for which one can specify output properties.
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

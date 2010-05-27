package com.treetank.saxon.evaluator;

import java.io.File;
import java.util.concurrent.Callable;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmValue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.api.ISession;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

/**
 * <h1>XQuery evaluator</h1>
 * 
 * <p>Evaluates an XQuery expression.</p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class XQueryEvaluator implements Callable<XdmValue> {

  /** Logger. */
  private static final Log LOGGER = LogFactory
      .getLog(com.treetank.saxon.evaluator.XQueryEvaluator.class);

  /** XQuery expression. */
  private transient final String mExpression;

  /** Target of query. */
  private transient final File mTarget;

  /** Treetank session. */
  private transient final ISession mSession;

  /**
   * Constructor.
   * 
   * @param expression
   *            XQuery expression.
   * @param session
   *            Treetank session.
   * @param file
   *            Target Treetank storage.
   */
  public XQueryEvaluator(final String expression, final ISession session,
      final File file) {
    this(expression, session, file, null);
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
   * @param serializer
   *            Serializer, for which one can specify output properties.
   */
  public XQueryEvaluator(final String expression, final ISession session,
      final File file, final Serializer serializer) {
    mExpression = expression;
    mSession = session;
    mTarget = file;
  }

  @Override
  public XdmValue call() {
    XdmValue value = null;

    try {
      final Processor proc = new Processor(false);
      final Configuration config = proc.getUnderlyingConfiguration();
      final NodeWrapper doc = (NodeWrapper) new DocumentWrapper(mSession,
          config, mTarget.getAbsolutePath()).wrap();
      final XQueryCompiler comp = proc.newXQueryCompiler();
      final XQueryExecutable exp = comp.compile(mExpression);
      final net.sf.saxon.s9api.XQueryEvaluator exe = exp.load();
      exe.setSource(doc);
      value = exe.evaluate();
    } catch (final SaxonApiException e) {
      LOGGER.error("Saxon Exception: " + e.getMessage(), e);
    }

    return value;
  }
}

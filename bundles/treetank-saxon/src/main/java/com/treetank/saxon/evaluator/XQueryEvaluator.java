package com.treetank.saxon.evaluator;

import java.io.File;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.NamePool;
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
 * <p>Evaluates an XQuery expression.</p>
 * 
 * @author johannes
 *
 */
public class XQueryEvaluator implements Callable<OutputStream> {

  /** Logger. */
  private static final Log LOGGER = LogFactory.getLog(XQueryEvaluator.class);
  
  /** XQuery expression. */
  private final String mExpression;
  
  /** Target of query. */
  private final File mTarget;
  
  /** Treetank session. */
  private final ISession mSession;
  
  /** Output Stream. */
  private final OutputStream mOut;

  /**
   * Constructor.
   * 
   * @param expression XQuery expression.
   * @param session Treetank session.
   * @param file Target Treetank storage.
   * @param out Output Stream.
   */
  public XQueryEvaluator(final String expression, final ISession session, final File file, final OutputStream out) {
    mExpression = expression;
    mSession = session;
    mTarget = file;
    mOut = out;
  }

  @Override
  public OutputStream call() {
    OutputStream os = null;
    
    try {
      final Processor proc = new Processor(false);
      final Configuration config = proc.getUnderlyingConfiguration();
      final NamePool np = config.getNamePool();
      final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(mSession, config, mTarget
            .getAbsolutePath()).wrap();
      final XQueryCompiler comp = proc.newXQueryCompiler();
      final XQueryExecutable exp = comp.compile(mExpression);
      
      final Serializer out = new Serializer();
      out.setOutputProperty(Serializer.Property.METHOD, "xml");
      out.setOutputProperty(Serializer.Property.INDENT, "yes");
      out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
      out.setOutputStream(mOut);
      
      final net.sf.saxon.s9api.XQueryEvaluator exe = exp.load();
      exe.setSource(doc);
      exe.run(out);
      
      os = (OutputStream)out.getOutputDestination();
    } catch (final SaxonApiException e) {
      LOGGER.error("Saxon Exception: " + e.getMessage(), e);
    }

    return os;
  }
}

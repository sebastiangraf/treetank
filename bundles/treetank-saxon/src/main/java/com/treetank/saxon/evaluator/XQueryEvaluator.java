package com.treetank.saxon.evaluator;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.api.ISession;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SAXDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryExecutable;

/**
 * <h1>XQuery evaluator.</h1>
 * 
 * <p>Takes either a source file or string and evaluates the query
 * against a Treetank storage.</p>
 * 
 * @author lichtenb
 *
 */
public class XQueryEvaluator implements Callable<net.sf.saxon.s9api.XQueryEvaluator> {

  /** Logger. */
  private static final Log LOGGER = LogFactory.getLog(XQueryEvaluator.class);
  
  /** Source which is an XQuery file. */
  private final File mSource;
  
  /** Target of query. */
  private final File mTarget;
  
  /** Treetank session. */
  private final ISession mSession;
  
  public XQueryEvaluator(final File source, final File target, final ISession session) {
    mSource = source;
    mTarget = target;
    mSession = session;
  }
  
  @Override
  public net.sf.saxon.s9api.XQueryEvaluator call() throws Exception {
    net.sf.saxon.s9api.XQueryEvaluator xqe = null;
    
    try {
      final Processor proc = new Processor(false);
      final Configuration config = proc.getUnderlyingConfiguration();
      final NodeWrapper doc =
        (NodeWrapper) new DocumentWrapper(mSession, config, mTarget
            .getAbsolutePath()).wrap();
      final XQueryCompiler comp = proc.newXQueryCompiler();
      final XQueryExecutable exp = comp.compile(mSource);
      xqe = exp.load();
      xqe.setSource(doc);
    } catch (final SaxonApiException e) {
      LOGGER.error("Saxon Exception: " + e.getMessage(), e);
    }
    
    return xqe;
  }
  
}

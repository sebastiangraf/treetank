package com.treetank.saxon.evaluator;

import java.io.File;
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

import com.treetank.api.ISession;
import com.treetank.saxon.wrapper.DocumentWrapper;
import com.treetank.saxon.wrapper.NodeWrapper;

/**
 * <h1>XPath Evaluator</h1>
 * 
 * <p>The XPath evaluator takes an XPath expression and evaluates the expression
 * against a wrapped Treetank document.</p>
 * 
 * @author johannes
 * 
 */
public class XPathEvaluator implements Callable<XPathSelector> {

  /** An XPath expression. */
  private transient final String mExpression;

  /** Target of query. */
  private transient final File mTarget;
  
  /** Treetank session. */
  private transient final ISession mSession;

  /** Logger. */
  private static final Log LOGGER = LogFactory.getLog(XPathEvaluator.class);

  /**
   * Constructor.
   * 
   * @param expression XPath expression.
   * @param session Treetank session.
   * @param file Target Treetank storage.
   */
  public XPathEvaluator(final String expression, final ISession session, final File file) {
    mExpression = expression;
    mSession = session;
    mTarget = file;
  }

  @Override
  public XPathSelector call() {
    final Processor proc = new Processor(false);
    final Configuration config = proc.getUnderlyingConfiguration();
    final NodeWrapper doc =
      (NodeWrapper) new DocumentWrapper(mSession, config, mTarget
          .getAbsolutePath()).wrap();
    final XPathCompiler xpath = proc.newXPathCompiler();
    final DocumentBuilder builder = proc.newDocumentBuilder();
    XPathSelector selector = null;
 
    try {
      final XdmItem booksDoc = builder.build(doc);
      selector = xpath.compile(mExpression).load();
      selector.setContextItem(booksDoc);
    } catch (final SaxonApiException e) {
      LOGGER.error("Saxon Exception: " + e.getMessage(), e);
    }
    
    return selector;
  }
}

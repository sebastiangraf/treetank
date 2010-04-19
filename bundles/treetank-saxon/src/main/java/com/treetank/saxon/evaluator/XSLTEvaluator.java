package com.treetank.saxon.evaluator;

import java.io.File;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * <h1>XSLT Evaluator</h1>
 * 
 * <p>Transforms an input document according to an XSLT stylesheet and returns
 * a resulting output stream.</p>
 * 
 * @author johannes
 *
 */
public class XSLTEvaluator implements Callable<Serializer> {

  /** Logger. */
  private final static Log LOGGER = LogFactory.getLog("XSLTEvaluator.class");
  
  /** Wrapped root node. */
  private final NodeInfo mDoc;

  /** Path to stylesheet. */
  private final String mStylesheet;

  /** Resulting stream of the transformation. */
  private final OutputStream mOut;

  /**
   * Constructor.
   * 
   * @param doc Root node of the wrapped tree.
   * @param stylesheet Path to stylesheet.
   * @param out Resulting stream of the transformation.
   */
  XSLTEvaluator(
      final NodeInfo doc,
      final String stylesheet,
      final OutputStream out) {
    mDoc = doc;
    mStylesheet = stylesheet;
    mOut = out;
  }

  @Override
  public Serializer call() {
    final Processor proc = new Processor(false);
    final XsltCompiler comp = proc.newXsltCompiler();
    XsltExecutable exp;
    XdmNode source;
    Serializer out = null;
    
    try {
      exp = comp.compile(new StreamSource(new File("styles/books.xsl")));
      source = proc.newDocumentBuilder().build(mDoc);
      out = new Serializer();
      out.setOutputProperty(Serializer.Property.METHOD, "html");
      out.setOutputProperty(Serializer.Property.INDENT, "yes");
      out.setOutputStream(mOut);
      XsltTransformer trans = exp.load();
      trans.setInitialContextNode(source);
      trans.setDestination(out);
      trans.transform();
    } catch (SaxonApiException e) {
      LOGGER.error("Saxon exception: " + e.getMessage(), e);
    }
    
    return out;
  }

}

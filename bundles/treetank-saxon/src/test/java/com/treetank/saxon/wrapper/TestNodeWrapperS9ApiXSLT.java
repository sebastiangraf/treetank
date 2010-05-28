package com.treetank.saxon.wrapper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.saxon.evaluator.XSLTEvaluator;
import com.treetank.service.xml.XMLShredder;

/**
 * Test XSLT S9Api.
 * 
 * @author johannes
 * 
 */
public final class TestNodeWrapperS9ApiXSLT extends XMLTestCase {

  /** Stylesheet file. */
  private static final File STYLESHEET =
      new File("src"
          + File.separator
          + "test"
          + File.separator
          + "resources"
          + File.separator
          + "styles"
          + File.separator
          + "books.xsl");

  /** Books XML file. */
  private static final File BOOKS =
      new File("src"
          + File.separator
          + "test"
          + File.separator
          + "resources"
          + File.separator
          + "data"
          + File.separator
          + "books.xml");

  /** Treetank session on Treetank test document. */
  private transient static ISession sessionBooks;

  //
  @Before
  public void setUp() throws Exception {
    Database.truncateDatabase(TestHelper.PATHS.PATH2.getFile());
    final IDatabase database =
        Database.openDatabase(TestHelper.PATHS.PATH2.getFile());
    sessionBooks = database.getSession();
    final IWriteTransaction mWTX = sessionBooks.beginWriteTransaction();
    final XMLEventReader reader = XMLShredder.createReader(BOOKS);
    final XMLShredder shredder = new XMLShredder(mWTX, reader, true);
    shredder.call();
    mWTX.close();

    saxonTransform(BOOKS, STYLESHEET);
    
    XMLUnit.setIgnoreWhitespace(true);
  }

  @After
  public void tearDown() throws TreetankException {
    Database.forceCloseDatabase(TestHelper.PATHS.PATH1.getFile());
    Database.forceCloseDatabase(TestHelper.PATHS.PATH2.getFile());
  }

  @Test
  public void testWithoutSerializer() throws Exception {
    final OutputStream out =
      new XSLTEvaluator(
      sessionBooks,
      BOOKS,
      STYLESHEET,
      new ByteArrayOutputStream()).call();

    final StringBuilder sBuilder = readFile();

    final Diff diff = new Diff(sBuilder.toString(), out.toString());
    diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
    
    assertTrue(diff.toString(), diff.similar());
  }

  @Test
  public void testWithSerializer() throws Exception {
    final Serializer serializer = new Serializer();
    serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
    serializer.setOutputProperty(Serializer.Property.INDENT, "yes");

    final OutputStream out =
        new XSLTEvaluator(
        sessionBooks,
        BOOKS,
        STYLESHEET,
        new ByteArrayOutputStream(),
        serializer).call();

    final StringBuilder sBuilder = readFile();

    final Diff diff = new Diff(sBuilder.toString(), out.toString());
    diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
    
    assertTrue(diff.toString(), diff.similar());
  }

  /**
   * Transform source document with the given stylesheet.
   * 
   * @param xml
   *            Source xml file.
   * @param stylesheet
   *            Stylesheet to transform sourc xml file.
   * @throws SaxonApiException
   *            Exception from Saxon in case anything goes wrong.
   */
  @Ignore("Not a test, utility method only")
  public void saxonTransform(final File xml, final File stylesheet)
      throws SaxonApiException {
    final Processor proc = new Processor(false);
    final XsltCompiler comp = proc.newXsltCompiler();
    final XsltExecutable exp = comp.compile(new StreamSource(stylesheet));
    final XdmNode source =
        proc.newDocumentBuilder().build(new StreamSource(xml));
    final Serializer out = new Serializer();
    out.setOutputProperty(Serializer.Property.METHOD, "xml");
    out.setOutputProperty(Serializer.Property.INDENT, "yes");
    out
        .setOutputFile(new File(TestHelper.PATHS.PATH1.getFile(), "books1.html"));
    final XsltTransformer trans = exp.load();
    trans.setInitialContextNode(source);
    trans.setDestination(out);
    trans.transform();
  }

  /**
   * Read file, which has been generated by "pure" Saxon.
   * 
   * @return StringBuilder instance, which has the string representation of the 
   *         document.
   * @throws IOException
   *              throws an IOException if any I/O operation fails.
   */
  @Ignore("Not a test, utility method only")
  public StringBuilder readFile() throws IOException {
    final BufferedReader in =
        new BufferedReader(new FileReader(new File(TestHelper.PATHS.PATH1
            .getFile(), "books1.html")));
    final StringBuilder sBuilder = new StringBuilder();
    for (String line = in.readLine(); line != null; line = in.readLine()) {
      sBuilder.append(line + "\n");
    }

    // Remove last newline.
    sBuilder.replace(sBuilder.length() - 1, sBuilder.length(), "");
    in.close();

    return sBuilder;
  }
}

package com.treetank.saxon.testsuit;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.*;
import net.sf.saxon.om.*;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.SequenceExtent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.tidy.Tidy;
import org.xml.sax.*;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.saxon.wrapper.DocumentWrapper;

/**
 * This class runs the W3C XQuery Test Suite, driven from the test catalog.
 * Adapted Michael Kay's Testsuit to check Treetank's Saxon implementation.
 * 
 * @author johannes
 */
public final class XQueryTestSuiteDriver {

  /** Logger. */
  private static final Log LOGGER =
      LogFactory.getLog(XQueryTestSuiteDriver.class);

  /** Debugging for logger enabled? */
  private static final boolean DEBUG = LOGGER.isDebugEnabled();

  /** Postfix of XQuery files. */
  private static final String XQ_POSTFIX = ".xq";

  /** Path to Testsuit. */
  private static String testSuiteDir;

  /** Path to Saxon. */
  private static String saxonDir;

  /** Saxon configuration. */
  private static Configuration config;

  /** Parser to parse result. */
  private static XMLReader resultParser;

  /** Parser to parse fragments. */
  private static XMLReader fragmentParser;

  /**
   * Run the testsuite using Saxon.
   *
   * @param args Array of parameters passed to the application.
   * via the command line.
   */
  public static void main(final String[] args) {
    if (args.length == 0 || args[0].equals("-?")) {
      throw new IllegalStateException(
          "XQueryTestSuiteDriver testsuiteDir saxonDir [testNamePattern] [-w] [-onwards] [-unfold] [-pull] [-indent:yes|no] [-spec:1.0|1.1]");
    }

    if (DEBUG) {
      LOGGER.debug("Testing Saxon " + Version.getProductVersion());
    }

    try {
      new XQueryTestSuiteDriver().process(args);
    } catch (final SAXException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (final ParserConfigurationException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  private static boolean usePull = false;

  /** Pattern for the testcase. */
  private static Pattern testPattern = null;

  private static boolean showWarnings = false;

  private static boolean onwards = false;

  private static boolean unfolded = false;

  /** Map to hold hs. */
  private static final Map<String, DocumentInfo> documentCache =
      new HashMap<String, DocumentInfo>(50);

  private static final TransformerFactory tfactory =
      new TransformerFactoryImpl();

  private static Writer results;

  /** Indent resulting XMLs (default: yes). */
  private static String indent = "yes";

  /** Version of spec. */
  private static String specVersion = "1.1";

  /**
   * Create a NameTest.
   * 
   * @param pool Name pool.
   * @param local local Name of node.
   * @return NameTest instance.
   */
  private NameTest elementNameTest(final NamePool pool, final String local) {
    final int nameFP =
        pool.allocate(
            "",
            "http://www.w3.org/2005/02/query-test-XQTSCatalog",
            local)
            & NamePool.FP_MASK;
    return new NameTest(Type.ELEMENT, nameFP, pool);
  }

  /**
   * Get first child element which correspond to the tested pattern.
   * 
   * @param parent 
   *                Parent NodeInfo object.
   * @param child
   *                Child name test.
   * @return NodeInfo element.
   */
  private NodeInfo getChildElement(final NodeInfo parent, final NameTest child) {
    return (NodeInfo) parent.iterateAxis(Axis.CHILD, child).next();
  }

  /**
   * Main work is done here.
   * 
   * @param args args[0] should be testSuite directory. args[1] the saxonDir.
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public void process(final String[] args)
      throws SAXException,
      ParserConfigurationException {

    testSuiteDir = args[0];
    saxonDir = args[1];
    final Set<Object> exceptions = new HashSet<Object>();

    for (int i = 2; i < args.length; i++) {
      if (args[i].startsWith("-")) {
        if (args[i].equals("-w")) {
          showWarnings = true;
        } else if (args[i].equals("-onwards")) {
          onwards = true;
        } else if (args[i].equals("-unfold")) {
          unfolded = true;
        } else if (args[i].equals("-pull")) {
          usePull = true;
        } else if (args[i].startsWith("-indent")) {
          indent = args[i].substring(8);
        } else if (args[i].startsWith("-spec")) {
          specVersion = args[i].substring(6);
        }
      } else {
        testPattern = Pattern.compile(args[i]);
      }
    }

    try {
      //            parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
      //            if (!parser.getFeature("http://xml.org/sax/features/xml-1.1")) {
      //                System.err.println("Warning: XML parser does not support XML 1.1 - " + parser.getClass());
      //            };
      final NamePool pool = new NamePool();
      config = new Configuration();
      config.setNamePool(pool);
      config.setHostLanguage(Configuration.XQUERY);
      //config.setSourceParserClass("com.sun.org.apache.xerces.internal.parsers.SAXParser");
      final XMLReader parser = config.getSourceParser();

      boolean supports11 = false;

      supports11 = parser.getFeature("http://xml.org/sax/features/xml-1.1");

      if (!supports11) {
        LOGGER.warn("Warning: XML parser does not support XML 1.1 - "
            + parser.getClass());
      }
      resultParser = config.getSourceParser();
      resultParser.setEntityResolver(new EntityResolver() {
        public InputSource resolveEntity(
            final String publicId,
            final String systemId) {
          return new InputSource(new StringReader(""));
        }
      });
      fragmentParser = config.getSourceParser();

      results =
          new FileWriter(new File(saxonDir
              + "/results"
              + Version.getProductVersion()
              + ".xml"));

      // Name tests for axis Iterator.
      final NameTest testCaseNT = elementNameTest(pool, "test-case");
      final NameTest inputUriNT = elementNameTest(pool, "input-URI");
      final NameTest inputFileNT = elementNameTest(pool, "input-file");
      final NameTest queryNT = elementNameTest(pool, "query");
      final NameTest inputQueryNT = elementNameTest(pool, "input-query");
      final NameTest contextItemNT = elementNameTest(pool, "contextItem");
      final NameTest outputFileNT = elementNameTest(pool, "output-file");
      final NameTest sourceNT = elementNameTest(pool, "source");
      final NameTest expectedErrorNT = elementNameTest(pool, "expected-error");
      final NameTest collectionNT = elementNameTest(pool, "collection");
      final NameTest defaultCollectionNT =
          elementNameTest(pool, "defaultCollection");

      // Attribute names.
      final int nameAtt = pool.allocate("", "", "name") & NamePool.FP_MASK;
      final int filePathAtt =
          pool.allocate("", "", "FilePath") & NamePool.FP_MASK;
      final int fileNameAtt =
          pool.allocate("", "", "FileName") & NamePool.FP_MASK;
      final int idAtt = pool.allocate("", "", "ID") & NamePool.FP_MASK;
      final int compareAtt =
          pool.allocate("", "", "compare") & NamePool.FP_MASK;
      final int variableAtt =
          pool.allocate("", "", "variable") & NamePool.FP_MASK;
      final int versionAtt =
          pool.allocate("", "", "version") & NamePool.FP_MASK;
      final int specVersionAtt =
          pool.allocate("", "", "spec-version") & NamePool.FP_MASK;

      /*
       * Look for an exceptions.xml document with the general format:
       *
       * <exceptions>
       *   <exception>
       *     <tests>testname1 testname2 ...</tests>
       *     <decription>text explanation</description>
       *   </exception>
       * </exceptions>
       *
       * Tests listed in this file will not be run.
       */
      final DocumentInfo exceptionsDoc =
          config.buildDocument(new StreamSource(new File(saxonDir
              + "/exceptions.xml")));

      final NameTest exceptionTestsNT =
          new NameTest(Type.ELEMENT, pool.allocate("", "", "tests"), pool);
      final AxisIterator exceptionTestCases =
          exceptionsDoc.iterateAxis(Axis.DESCENDANT, exceptionTestsNT);
      while (true) {
        final NodeInfo testCase = (NodeInfo) exceptionTestCases.next();
        if (testCase == null) {
          break;
        }
        StringTokenizer tok = new StringTokenizer(testCase.getStringValue());
        while (tok.hasMoreElements()) {
          exceptions.add(tok.nextElement());
        }
      }

      // Catalog.
      final DocumentInfo catalog =
          config.buildDocument(new StreamSource(new File(testSuiteDir
              + "/XQTScatalog.xml")));

      final String dateTime =
          DateTimeValue.getCurrentDateTime(null).getStringValue();
      final Properties outputProps = new Properties();
      outputProps.setProperty("method", "xml");
      outputProps.setProperty("INDENT", indent);
      results
          .write("<test-suite-result xmlns='http://www.w3.org/2005/02/query-test-XQTSResult'>");
      results.write(" <implementation name='Saxon-HE' version='"
          + Version.getProductVersion()
          + "' anonymous-result-column='false'>\n");
      results
          .write("  <organization name='University of Konstanz' website='http://www.uni-konstanz.de/arbeitsgruppen/disy/' anonymous='false'/>\n");
      results
          .write("  <submittor name='Johannes Lichtenberger' title='' email='Johannes.Lichtenberger@uni-konstanz.de'/>\n");
      outputDiscretionaryItems();
      results.write(" </implementation>\n");
      results.write(" <syntax>XQuery</syntax>");
      results.write(" <test-run dateRun='" + dateTime + "'>");
      results.write("   <test-suite version='1.0.4'/>");
      results.write("   <transformation/>");
      results.write("   <comparison/>");
      results.write("   <otherComments/>");
      results.write(" </test-run>");

      // Load the source documents.
      final Processor proc = new Processor(false);
      final AxisIterator sources =
          catalog.iterateAxis(Axis.DESCENDANT, sourceNT);
      while (true) {
        final NodeInfo sourceElement = (NodeInfo) sources.next();
        if (sourceElement == null) {
          break;
        }
        final String id = sourceElement.getAttributeValue(idAtt);
        String fileName = sourceElement.getAttributeValue(fileNameAtt);

        // Remove ".xml" postfix.
        fileName = fileName.substring(0, fileName.length() - 4);

        LOGGER.info("Loading source " + fileName);

        final File source = new File(testSuiteDir + fileName);
        final IDatabase database = Database.openDatabase(source);
        final ISession session = database.getSession();
        final DocumentWrapper doc =
            (DocumentWrapper) new DocumentWrapper(session, proc
                .getUnderlyingConfiguration(), source.getAbsolutePath()).wrap();
        documentCache.put(id, doc);
      }

      // Process test cases.
      final AxisIterator testCases =
          catalog.iterateAxis(Axis.DESCENDANT, testCaseNT);
      while (true) {
        final NodeInfo testCase = (NodeInfo) testCases.next();
        if (testCase == null) {
          break;
        }

        final String testName = testCase.getAttributeValue(nameAtt);

        // Testcase name doesn't match the given test pattern.
        if (testPattern != null && !testPattern.matcher(testName).matches()) {
          continue;
        }
        if (onwards) {
          testPattern = null;
        }

        // Don't process testcases which are in the Saxon exception list.
        if (exceptions.contains(testName)) {
          continue;
        }

        // Exclude dotnet testcases.
        if (isExcluded(testName)) {
          continue;
        }

        // Get filepath.
        final String filePath = testCase.getAttributeValue(filePathAtt);
        if (filePath.startsWith("StaticTyping")) {
          continue;
        }

        // Get query params.
        final NodeInfo query = getChildElement(testCase, queryNT);
        final String queryName = query.getAttributeValue(nameAtt);
        String languageVersion = query.getAttributeValue(versionAtt);
        if (languageVersion == null) {
          languageVersion = specVersion;
        }

        // Path to query file.
        final String absQueryName =
            testSuiteDir
                + File.separator
                + "Queries"
                + File.separator
                + (unfolded ? "XQUnfolded/" : "XQuery/")
                + filePath
                + queryName
                + XQ_POSTFIX;

        String outputFile = null;

        final StaticQueryContext env = config.newStaticQueryContext();
        env.setModuleURIResolver(new XQTSModuleURIResolver(testCase));
        env.setBaseURI(new File(absQueryName).toURI().toString());
        env.setLanguageVersion(languageVersion);
        XQueryExpression xqe;

        try {
          xqe = env.compileQuery(new FileInputStream(absQueryName), "UTF-8");
        } catch (final XPathException err) {
          processError(err, testCase, testName, filePath
              + queryName
              + XQ_POSTFIX, expectedErrorNT, specVersionAtt);
          continue;
        }

        final DynamicQueryContext dqc = new DynamicQueryContext(config);

        final NodeInfo contextItemElement =
            getChildElement(testCase, contextItemNT);
        if (contextItemElement != null) {
          //          final String contextItem = contextItemElement.getStringValue();
          //          ShredderFile.shredder(new File(contextItem), new File(contextItem.substring(0, contextItem.length()-4)));
          //          final DocumentWrapper doc = new DocumentWrapper().wrap();
          final DocumentInfo contextNode =
              loadDocument(contextItemElement.getStringValue());
          dqc.setContextItem(contextNode);
        }

        processInputQueries(
            testCase,
            inputQueryNT,
            variableAtt,
            nameAtt,
            filePath,
            dqc);

        processInputDocuments(testCase, inputFileNT, variableAtt, dqc);

        setQueryParameters(
            catalog,
            testCase,
            dqc,
            inputUriNT,
            variableAtt,
            collectionNT,
            idAtt);

        final NodeInfo defaultCollection =
            getChildElement(testCase, defaultCollectionNT);
        if (defaultCollection != null) {
          final String docName = defaultCollection.getStringValue();
          final NodeInfo collectionElement =
              getCollectionElement(catalog, docName, collectionNT, idAtt);
          final CollectionURIResolver uriResolver =
              new XQTSCollectionURIResolver(catalog, collectionElement, true);
          config.setCollectionURIResolver(uriResolver);
        }

        // Run the query
        String outputDir = saxonDir + "/results/" + filePath;
        if (outputDir.endsWith("/")) {
          outputDir = outputDir.substring(0, outputDir.length() - 1);
        }
        new File(outputDir).mkdirs();
        outputFile = outputDir + "/" + testName + ".out";
        File outputFileF = new File(outputFile);
        outputFileF.createNewFile();
        StreamResult result = new StreamResult(outputFileF);
        try {
          if (usePull) {
            xqe.pull(dqc, result, outputProps);
          } else {
            xqe.run(dqc, result, outputProps);
          }
        } catch (final XPathException err) {
          processError(err, testCase, testName, filePath
              + queryName
              + "XQ_POSTFIX", expectedErrorNT, specVersionAtt);
          continue;
        }

        // Compare the results
        boolean resultsMatched = false;
        boolean possibleMatch = false;
        final SequenceIterator expectedResults =
            testCase.iterateAxis(Axis.CHILD, outputFileNT);
        boolean multipleResults = false;
        final SequenceIterator ccc = expectedResults.getAnother();
        ccc.next();
        if (ccc.next() != null) {
          multipleResults = true;
        }
        while (true) {
          final NodeInfo outputFileElement = (NodeInfo) expectedResults.next();
          if (outputFileElement == null) {
            break;
          }
          final String appliesTo =
              outputFileElement.getAttributeValue(specVersionAtt);
          if (appliesTo != null && !appliesTo.contains(specVersion)) {
            continue; // results apply to a different version
          }
          final String resultsDir =
              testSuiteDir + "/ExpectedTestResults/" + filePath;
          final String resultsPath =
              resultsDir + outputFileElement.getStringValue();
          final String comparisonMethod =
              outputFileElement.getAttributeValue(compareAtt);
          boolean comparisonResult;
          if ("Ignore".equals(comparisonMethod)) {
            comparisonResult = true;
          } else {
            comparisonResult =
                compare(
                    outputFile,
                    resultsPath,
                    comparisonMethod,
                    multipleResults);
          }
          if (comparisonResult) {
            // exact match
            results.write("  <test-case name='"
                + testName
                + "' result='pass'"
                + "/>\n");
            resultsMatched = true;
            break;
          } else if ("false".equals(comparisonResult)) {
            //continue;
          } else {
            possibleMatch = comparisonResult;
            //continue;
          }
        }

        if (!resultsMatched) {
          if (multipleResults) {
            LOGGER.info("*** Failed to match any of the permitted results");
          }
          NodeInfo expectedError = null;
          SequenceIterator eit =
              testCase.iterateAxis(Axis.CHILD, expectedErrorNT);
          while (true) {
            final NodeInfo e = (NodeInfo) eit.next();
            if (e == null) {
              break;
            }
            String appliesTo = e.getAttributeValue(specVersionAtt);
            if (appliesTo != null && !appliesTo.contains(specVersion)) {
              continue; // results apply to a different version
            }
            expectedError = e;
            break;
          }

          if (possibleMatch) {
            results.write("  <test-case name='"
                + testName
                + "' result='pass' comment='"
                + possibleMatch
                + "'/>\n");
          } else if (expectedError != null) {
            results.write("  <test-case name='"
                + testName
                + "' result='fail' comment='"
                + "expected "
                + expectedError.getStringValue()
                + ", got success'/>\n");
          } else {
            results.write("  <test-case name='"
                + testName
                + "' result='fail'/>\n");
          }
          results.write("  <?file " + filePath + queryName + "XQ_POSTFIX ?>\n");
        }
      }

      results.write("</test-suite-result>");
      results.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setQueryParameters(
      final DocumentInfo catalog,
      final NodeInfo testCase,
      final DynamicQueryContext dqc,
      final NameTest inputUriNT,
      final int variableAtt,
      final NameTest collectionNT,
      final int idAtt) throws XPathException {
    SequenceIterator inputURIs = testCase.iterateAxis(Axis.CHILD, inputUriNT);
    while (true) {
      NodeInfo inputURI = (NodeInfo) inputURIs.next();
      if (inputURI == null) {
        break;
      }
      String variableName = inputURI.getAttributeValue(variableAtt);
      variableName = toClarkName(variableName);
      if (variableName != null) {
        String docName = inputURI.getStringValue();
        if (docName.startsWith("collection")) {
          NodeInfo collectionElement =
              getCollectionElement(catalog, docName, collectionNT, idAtt);
          CollectionURIResolver r =
              new XQTSCollectionURIResolver(catalog, collectionElement, false);
          config.setCollectionURIResolver(r);
          dqc.setParameterValue(variableName, new AnyURIValue(docName));
        } else {
          DocumentInfo doc = loadDocument(docName);
          if (doc == null) {
            dqc.setParameterValue(variableName, new AnyURIValue(
                "error-document" + docName));
          } else {
            String uri = doc.getSystemId();
            dqc.setParameterValue(variableName, new AnyURIValue(uri));
          }
        }
      }
    }
  }

  /**
   * Process input documents.
   * 
   * @param testCase
   *                  Current testcase.
   * @param inputFileNT
   *                  Input file.
   * @param variableAtt
   *                  
   *                  
   * @param dqc
   * @throws XPathException
   */
  private void processInputDocuments(
      final NodeInfo testCase,
      final NameTest inputFileNT,
      final int variableAtt,
      final DynamicQueryContext dqc) throws XPathException {
    final SequenceIterator inputFiles =
        testCase.iterateAxis(Axis.CHILD, inputFileNT);
    while (true) {
      final NodeInfo inputFile = (NodeInfo) inputFiles.next();
      if (inputFile == null) {
        break;
      }
      final String variableName =
          toClarkName(inputFile.getAttributeValue(variableAtt));
      if (variableName != null) {
        final DocumentInfo inputDoc = loadDocument(inputFile.getStringValue());
        dqc.setParameterValue(variableName, inputDoc);
        //System.err.println("Set parameter " + variableName + " := " + inputDoc.getSystemId());
      }
    }
  }

  private void processInputQueries(
      final NodeInfo testCase,
      final NameTest inputQueryNT,
      final int variableAtt,
      final int nameAtt,
      final String filePath,
      final DynamicQueryContext dqc) throws XPathException, IOException {
    final SequenceIterator inputQueries =
        testCase.iterateAxis(Axis.CHILD, inputQueryNT);
    while (true) {
      final NodeInfo inputQuery = (NodeInfo) inputQueries.next();
      if (inputQuery == null) {
        break;
      }
      String variableName = inputQuery.getAttributeValue(variableAtt);
      variableName = toClarkName(variableName);
      if (variableName != null) {
        String preQueryName = inputQuery.getAttributeValue(nameAtt);
        String subQueryFile =
            testSuiteDir
                + "/Queries/XQuery/"
                + filePath
                + preQueryName
                + XQ_POSTFIX;
        StaticQueryContext sqc2 = config.newStaticQueryContext();
        XQueryExpression subQuery =
            sqc2.compileQuery(new FileReader(subQueryFile));
        SequenceIterator subQueryResult =
            subQuery.iterator(new DynamicQueryContext(config));
        dqc.setParameterValue(variableName, SequenceExtent
            .makeSequenceExtent(subQueryResult));
      }
    }
  }

  private NodeInfo getCollectionElement(
      DocumentInfo catalog,
      String docName,
      NameTest collectionNT,
      int idAtt) {
    NodeInfo collectionElement = null;
    AxisIterator colls = catalog.iterateAxis(Axis.DESCENDANT, collectionNT);
    while (true) {
      NodeInfo coll = (NodeInfo) colls.next();
      if (coll == null) {
        break;
      }
      if (docName.equals(coll.getAttributeValue(idAtt))) {
        collectionElement = coll;
      }
    }
    return collectionElement;
  }

  /**
   * Exclude test names with the prefix "dotnet".
   * 
   * @param testName
   *                 Name of test case.
   */
  protected boolean isExcluded(final String testName) {
    return testName.startsWith("dotnet");
  }

  /**
   * String notation of a qName.
   * 
   * @param variableName 
   *                      A full qualified name.
   * @return String value of qualified name.
   */
  private static String toClarkName(String variableName) {
    String retVal = null;

    if (variableName != null) {
      if (variableName.startsWith("local:")) {
        return "{http://www.w3.org/2005/xquery-local-functions}"
            + variableName.substring(6);
      } else {
        return variableName;
      }
    }

    return retVal;
  }

  /**
   * Construct source object. This method allows subclassing e.g. to build a DOM or XOM source.
   * @param xml
   */
  private DocumentInfo loadDocument(String xml) {
    return documentCache.get(xml);
  }

  /**
   * Process a static or dynamic error.
   */
  private void processError(
      final XPathException err,
      final NodeInfo testCase,
      final String testName,
      final String queryPath,
      final NameTest expectedErrorNT,
      final int specVersionAtt) throws java.io.IOException {
    final String actualError = err.getErrorCodeLocalPart();
    final AxisIterator expectedErrors =
        testCase.iterateAxis(Axis.CHILD, expectedErrorNT);
    final FastStringBuffer expected = new FastStringBuffer(20);
    while (true) {
      final NodeInfo expectedError = (NodeInfo) expectedErrors.next();
      if (expectedError == null) {
        break;
      }
      final String appliesTo = expectedError.getAttributeValue(specVersionAtt);
      if (appliesTo != null && !appliesTo.contains(specVersion)) {
        continue; // results apply to a different version
      }
      if (expectedError.getStringValue().equals(actualError)
          || expectedError.getStringValue().equals("*")) {
        results.write("  <test-case name='" + testName + "' result='pass'/>\n");
        return;
      }
      expected.append(expectedError.getStringValue());
      expected.append(" ");
    }
    if (expected.length() > 0) {
      results.write("  <test-case name='"
          + testName
          + "' result='pass'"
          + " comment='expected "
          + expected
          + ", got "
          + actualError
          + "'/>\n");
    } else {
      results.write("  <test-case name='"
          + testName
          + "' result='fail'"
          + " comment='expected success, got "
          + actualError
          + "'/>\n");
    }
    results.write("  <?file " + queryPath + " ?>\n");

  }

  private static CanonicalXML canon = new CanonicalXML();

  /**
   * Compare output with reference file.
   * 
   * @param outfile 
   *                Output from Treetank/Saxon.
   * @param reffile
   *                Reference file.
   * @param comparator
   *                How the files should be compared.
   * @param silent
   * @return True if both file contents don't differ from each other.
   */
  private boolean compare(
      final String outfile,
      final String reffile,
      final String comparator,
      final boolean silent) {
    if (reffile == null) {
      LOGGER.info("*** No reference results available");
      return false;
    }
    
    final File outfileFile = new File(outfile);
    final File reffileFile = new File(reffile);

    if (!reffileFile.exists()) {
      LOGGER.info("*** No reference results available");
      return false;
    }

    // try direct comparison first
    String refResult = null;
    String actResult = null;

    // This is decoding bytes assuming the platform default encoding
    try {
      final FileReader reader1 = new FileReader(outfileFile);
      final FileReader reader2 = new FileReader(reffileFile);
      final char[] contents1 = new char[(int) outfileFile.length()];
      final char[] contents2 = new char[(int) reffileFile.length()];
      final int size1 = reader1.read(contents1, 0, (int) outfileFile.length());
      final int size2 = reader2.read(contents2, 0, (int) reffileFile.length());
      int offset1 = 0;
      int offset2 = 0;
      if (contents1[0] == '\u00ef'
          && contents1[1] == '\u00bb'
          && contents1[2] == '\u00bf') {
        offset1 += 3;
      }
      if (contents2[0] == '\u00ef'
          && contents2[1] == '\u00bb'
          && contents2[2] == '\u00bf') {
        offset2 += 3;
      }
      actResult =
          (size1 == -1 ? "" : new String(contents1, offset1, size1 - offset1));
      refResult =
          (size2 == -1 ? "" : new String(contents2, offset2, size2 - offset2));
      actResult = normalizeNewlines(actResult);
      refResult = normalizeNewlines(refResult);
      if (actResult.equals(refResult)) {
        return true;
      }
      if (size1 == 0) {
        if (!silent) {
          LOGGER.info("** ACTUAL RESULTS EMPTY; REFERENCE RESULTS LENGTH "
              + size2);
        }
        return false;
      }
      if (size2 == 0) {
        if (!silent) {
          LOGGER.info("** REFERENCED RESULTS EMPTY; ACTUAL RESULTS LENGTH "
              + size2);
        }
        return false;
      }
    } catch (final IOException e) {
      LOGGER.error(e.getMessage(), e);
    }

    // HTML: can't do logical comparison
    if ("html-output".equals(comparator)) {
      // TODO: Tidy gets upset by byte-order-marks. Use the strings constructed above as input.
      try {
        final Tidy tidy = new Tidy();
        tidy.setXmlOut(true);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
        final InputStream in1 = new FileInputStream(outfile);
        final File xml1 = new File(outfile + ".xml");
        xml1.createNewFile();
        final OutputStream out1 = new FileOutputStream(xml1);
        tidy.parse(in1, out1);
        final InputStream in2 = new FileInputStream(reffile);
        File xml2 = new File(reffile + ".xml");
        xml2.createNewFile();
        final OutputStream out2 = new FileOutputStream(xml2);
        tidy.parse(in2, out2);
        return compare(xml1.toString(), xml2.toString(), "xml", silent);
      } catch (final IOException e) {
        LOGGER.error(e.getMessage(), e);
      }
    } else if ("xhtml-output".equals(comparator)) {
      refResult = canonizeXhtml(refResult);
      actResult = canonizeXhtml(actResult);
      return actResult.equals(refResult);
    } else if ("Fragment".equals(comparator) || "Text".equals(comparator)) {
      try {
        // try two comparison techniques hoping one will work...
        boolean b = false;
        try {
          b = compareFragments2(actResult, refResult, outfile, silent);
        } catch (final Exception e) {
          LOGGER.warn("XQTS: First comparison attempt failed "
              + e.getMessage()
              + ", trying again", e);
        }
        if (!b) {
          LOGGER.info("XQTS: First comparison attempt failed, trying again");
          b = compareFragments(outfileFile, reffileFile, outfile, silent);
        }
        return b;
      } catch (final Exception e) {
        LOGGER.error("Failed to compare results for "
            + outfile
            + ": "
            + e.getMessage(), e);
      }
    } else if (comparator.equals("Inspect")) {
      LOGGER.info("** Inspect results by hand");
      return true;
    } else {
      // convert both files to Canonical XML and compare them again
      try {
        final InputSource out = new InputSource(outfileFile.toURL().toString());
        final InputSource ref = new InputSource(reffileFile.toURL().toString());
        String outxml = canon.toCanonicalXML2(resultParser, out, false);
        String refxml = canon.toCanonicalXML2(resultParser, ref, false);
        //                String outxml = canon.toCanonicalXML3(factory, resultParser, actResult, false);
        //                String refxml = canon.toCanonicalXML3(factory, resultParser, refResult, false);
        if (!outxml.equals(refxml)) {
          // try comparing again, this time without whitespace nodes
          outxml = canon.toCanonicalXML2(resultParser, out, true);
          refxml = canon.toCanonicalXML2(resultParser, ref, true);
          //                    outxml = canon.toCanonicalXML3(factory, resultParser, actResult, true);
          //                    refxml = canon.toCanonicalXML3(factory, resultParser, refResult, true);
          if (outxml.equals(refxml)) {
            LOGGER.info("*** Match after stripping whitespace nodes: "
                + outfile);
            return false;
          } else {
            if (!silent) {
              LOGGER.info("Mismatch with reference results: " + outfile);
              LOGGER.info("REFERENCE RESULTS:");
              LOGGER.info(truncate(refxml));
              LOGGER.info("ACTUAL RESULTS:");
              LOGGER.info(truncate(outxml));
              findDiff(refxml, outxml);
            }
            return false;
          }
        } else {
          return true;
        }
      } catch (final Exception e) {
        try {
          LOGGER.warn("Failed to compare results for: "
              + outfile
              + ": "
              + e.getMessage());
          LOGGER.info("** Attempting XML Fragment comparison");
          //boolean b = compareFragments(outfileFile, reffileFile, outfile, silent);
          boolean b = compareFragments2(actResult, refResult, outfile, silent);
          LOGGER.info("** " + (b ? "Success" : "Still different"));
          return b;
        } catch (final Exception e1) {
          LOGGER.error("Again failed to compare results for "
              + outfile
              + ": "
              + e.getMessage(), e);
        }
        return false;
      }
    }
    
    // TODO: check.
    return false;
  }

  Templates xhtmlCanonizer;

  private String canonizeXhtml(final String input) {
    String retVal = "";
    try {
      final Templates canonizer = getXhtmlCanonizer();
      final Transformer t = canonizer.newTransformer();
      final StringWriter sw = new StringWriter();
      final StreamResult r = new StreamResult(sw);
      final InputSource is = new InputSource(new StringReader(input));
      final SAXSource ss = new SAXSource(resultParser, is);
      t.transform(ss, r);
      retVal = sw.toString();
    } catch (TransformerConfigurationException e) {
      LOGGER.error("*** Failed to compile XHTML canonicalizer stylesheet: "
          + e.getMessage(), e);
    } catch (TransformerException e) {
      LOGGER.error("*** Failed while running XHTML canonicalizer stylesheet: "
          + e.getMessage(), e);
    }
    return retVal;
  }

  private Templates getXhtmlCanonizer()
      throws TransformerConfigurationException {
    if (xhtmlCanonizer == null) {
      final Source source =
          new StreamSource(new File(saxonDir + "/canonizeXhtml.xsl"));
      xhtmlCanonizer = tfactory.newTemplates(source);
    }
    return xhtmlCanonizer;
  }

  /**
   * Compare fragments.
   * 
   * @param outfileFile
   *                    Output file from Treetank/Saxon.
   * @param reffileFile
   *                    Reference file.
   * @param outfile
   *                    
   * @param silent 
   *                    Logger infos verbose or not?
   * @return true if fragments match, false otherwise.
   */
  private boolean compareFragments(
      final File outfileFile,
      final File reffileFile,
      final String outfile,
      final boolean silent) {

    // if we can't parse the output as a document, try it as an external entity, with space stripping
    final String outurl = outfileFile.toURI().toString();
    final String refurl = reffileFile.toURI().toString();
    final String outdoc =
        "<?xml version='1.1'?><!DOCTYPE doc [ <!ENTITY e SYSTEM '"
            + outurl
            + "'>]><doc>&e;</doc>";
    final String refdoc =
        "<?xml version='1.1'?><!DOCTYPE doc [ <!ENTITY e SYSTEM '"
            + refurl
            + "'>]><doc>&e;</doc>";
    final InputSource out2 = new InputSource(new StringReader(outdoc));
    final InputSource ref2 = new InputSource(new StringReader(refdoc));
    final String outxml2 = canon.toCanonicalXML(fragmentParser, out2, true);
    final String refxml2 = canon.toCanonicalXML(fragmentParser, ref2, true);
    if (outxml2 != null && refxml2 != null && !outxml2.equals(refxml2)) {
      if (!silent) {
        LOGGER.info("Mismatch with reference results: " + outfile);
        LOGGER.info("REFERENCE RESULTS:");
        LOGGER.info(truncate(refxml2));
        LOGGER.info("ACTUAL RESULTS:");
        LOGGER.info(truncate(outxml2));
        findDiff(refxml2, outxml2);
      }
      return false;
    } else if (outxml2 == null) {
      LOGGER.warn("Cannot canonicalize actual results");
      return false;
    } else if (refxml2 == null) {
      LOGGER.warn("Cannot canonicalize reference results");
      return false;
    }
    return true;
  }

  /**
   * With this method of fragment comparison we build the wrapper document 
   * ourselves. This is mainly to circumvent a Java XML parsing bug.
   * 
   * @param outFragment
   * @param refFragment
   * @param outfile
   * @param silent
   * @return
   */
  private boolean compareFragments2(
      String outFragment,
      String refFragment,
      final String outfile,
      final boolean silent) {
    if (outFragment == null) {
      outFragment = "";
    }
    if (outFragment.startsWith("<?xml")) {
      int x = outFragment.indexOf("?>");
      outFragment = outFragment.substring(x + 2);
    }
    if (refFragment == null) {
      refFragment = "";
    }
    if (refFragment.startsWith("<?xml")) {
      int x = refFragment.indexOf("?>");
      refFragment = refFragment.substring(x + 2);
    }

    final String outdoc =
        "<?xml version='1.1'?><doc>" + outFragment.trim() + "</doc>";
    final String refdoc =
        "<?xml version='1.1'?><doc>" + refFragment.trim() + "</doc>";
    final InputSource out2 = new InputSource(new StringReader(outdoc));
    final InputSource ref2 = new InputSource(new StringReader(refdoc));
    final String outxml2 = canon.toCanonicalXML(fragmentParser, out2, false);
    final String refxml2 = canon.toCanonicalXML(fragmentParser, ref2, false);
    if (outxml2 != null && refxml2 != null && !outxml2.equals(refxml2)) {
      // Try again with whitespace stripping
      final InputSource out3 = new InputSource(new StringReader(outdoc));
      final InputSource ref3 = new InputSource(new StringReader(refdoc));
      final String outxml3 = canon.toCanonicalXML(fragmentParser, out3, true);
      final String refxml3 = canon.toCanonicalXML(fragmentParser, ref3, true);
      if (outxml3 != null && refxml3 != null && !outxml3.equals(refxml3)) {
        if (!silent) {
          LOGGER.info("Mismatch with reference results: " + outfile);
          LOGGER.info("REFERENCE RESULTS:");
          LOGGER.info(truncate(refxml2));
          LOGGER.info("ACTUAL RESULTS:");
          LOGGER.info(truncate(outxml2));
          findDiff(refxml2, outxml2);
        }
        return false;
      } else {
        LOGGER.info("Matches after stripping whitespace");
        return true;
      }
    } else if (outxml2 == null) {
      LOGGER.warn("Cannot canonicalize actual results");
      return false;
    } else if (refxml2 == null) {
      LOGGER.warn("Cannot canonicalize reference results");
      return false;
    }
    return true;
  }

  /**
   * Truncate string.
   * 
   * @param s
   *          String value to truncate.
   * @return Truncated string.
   */
  private static String truncate(final String s) {
    String str = s;
    if (s.length() > 200) {
      str = s.substring(0, 200);
    }
    return str;
  }

  /**
   * Find difference between two strings.
   * 
   * @param s1 
   *           First string.
   * @param s2
   *           Second string.
   */
  private void findDiff(final String s1, final String s2) {
    final FastStringBuffer sb1 = new FastStringBuffer(s1.length());
    sb1.append(s1);
    final FastStringBuffer sb2 = new FastStringBuffer(s2.length());
    sb2.append(s2);
    int i = 0;
    while (true) {
      if (s1.charAt(i) != s2.charAt(i)) {
        int j = (i < 50 ? 0 : i - 50);
        int k = (i + 50 > s1.length() || i + 50 > s2.length() ? i + 1 : i + 50);
        LOGGER.info("Different at char "
            + i
            + "\n+"
            + s1.substring(j, k)
            + "\n+"
            + s2.substring(j, k));
        break;
      }
      if (i >= s1.length())
        break;
      if (i >= s2.length())
        break;
      i++;
    }
  }

  private void outputDiscretionaryItems() throws IOException {
    results.write("  <discretionary-items/>\n");
  }

  /**
   * Normalize newlines in string.
   * 
   * @param in
   *           Input string.
   * @return Normalized string.
   */
  private String normalizeNewlines(String in) {
    return in.replace("\r\n", "\n");
  }
}

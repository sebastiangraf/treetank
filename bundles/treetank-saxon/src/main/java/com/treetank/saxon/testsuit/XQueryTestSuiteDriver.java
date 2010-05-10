package com.treetank.saxon.testsuit;

import net.sf.saxon.*;
import net.sf.saxon.om.*;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.tinytree.TinyBuilder;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;

import org.w3c.tidy.Tidy;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * This class runs the W3C XQuery Test Suite, driven from the test catalog. It includes options to
 * run the tests intepretively or by compilation.
 */
public final class XQueryTestSuiteDriver {
    /**
     * Run the testsuite using Saxon.
     *
     * @param args Array of parameters passed to the application
     * via the command line.
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args[0].equals("-?")) {
            System.err.println(
                    "XQueryTestSuiteDriver testsuiteDir saxonDir [testNamePattern] [-compile] [-runcomp] [-w] [-onwards] [-unfold] [-pull] [-indent:yes|no] [-spec:1.0|1.1]");
        }

        System.err.println("Testing Saxon " + Version.getProductVersion());
        new XQueryTestSuiteDriver().go(args);
    }

    private String testSuiteDir;
    private String saxonDir;
    private Configuration saConfig;
    private XMLReader resultParser;
    private XMLReader fragmentParser;
    private boolean usePull = false;
    private Pattern testPattern = null;
    private boolean showWarnings = false;
    private boolean compile = false;
    private boolean onwards = false;
    private boolean unfolded = false;
    private boolean runCompiled = false;
    private HashMap documentCache = new HashMap(50);

    private TransformerFactory tfactory = new TransformerFactoryImpl();

    private Writer results;
    private Writer compileScript = null;
    private HashSet directories = new HashSet(200);
    private PrintStream monitor = System.err;
    private PrintStream log;
    private String indent = "yes";
    private String specVersion = "1.1";

    /**
     * Some tests use schemas that conflict with others, so they can't use the common schema cache.
     * These tests are run in a Configuration of their own. (Ideally we would put this list in a
     * catalogue file of some kind).
     */

    static HashSet noCacheTests = new HashSet(30);
    static {
        noCacheTests.add("schemainline20_005_01");

    }

    private NameTest elementNameTest(NamePool pool, String local) {
        int nameFP = pool.allocate("", "http://www.w3.org/2005/02/query-test-XQTSCatalog", local) & NamePool.FP_MASK;
        return new NameTest(Type.ELEMENT, nameFP, pool);
    }

    private NodeInfo getChildElement(NodeInfo parent, NameTest child) {
        return (NodeInfo)parent.iterateAxis(Axis.CHILD, child).next();
    }

    public void go(String[] args) throws SAXException, ParserConfigurationException {


        testSuiteDir = args[0];
        saxonDir = args[1];
        HashSet exceptions = new HashSet();

        for (int i=2; i<args.length; i++) {
            if (args[i].startsWith("-")) {
                if (args[i].equals("-w")) {
                    showWarnings = true;
                } else if (args[i].equals("-compile")) {
                    compile = true;
                } else if (args[i].equals("-onwards")) {
                    onwards = true;
                } else if (args[i].equals("-runcomp")) {
                    runCompiled = true;
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
            NamePool pool = new NamePool();
            saConfig = new Configuration();
            saConfig.setNamePool(pool);
            saConfig.setHostLanguage(Configuration.XQUERY);
            //saConfig.setSourceParserClass("com.sun.org.apache.xerces.internal.parsers.SAXParser");
            XMLReader parser = saConfig.getSourceParser();

            boolean supports11 = false;
            try {
                supports11 = parser.getFeature("http://xml.org/sax/features/xml-1.1");
            } catch (Exception err) {}

            if (!supports11) {
                monitor.println("Warning: XML parser does not support XML 1.1 - " + parser.getClass());
            }
            resultParser = saConfig.getSourceParser();
            resultParser.setEntityResolver(
                    new EntityResolver() {
                        public InputSource resolveEntity(String publicId, String systemId) {
                            return new InputSource(new StringReader(""));
                        }
                    }
            );
            fragmentParser = saConfig.getSourceParser();

            //Configuration config11 = new Configuration();
            //config11.setXMLVersion(Configuration.XML11);
            //config11.setNamePool(pool);

            results = new FileWriter(
                    new File(saxonDir + "/results" + Version.getProductVersion() + ".xml"));
            log = new PrintStream(new FileOutputStream(
                    new File(saxonDir + "/results" + Version.getProductVersion() + ".log")));

            MyErrorListener errorListener = new MyErrorListener(log);
            saConfig.setErrorListener(errorListener);

            NameTest testCaseNT = elementNameTest(pool, "test-case");
            NameTest inputUriNT = elementNameTest(pool, "input-URI");
            NameTest inputFileNT = elementNameTest(pool, "input-file");
            NameTest queryNT = elementNameTest(pool, "query");
            NameTest inputQueryNT = elementNameTest(pool, "input-query");
            NameTest contextItemNT = elementNameTest(pool, "contextItem");
            NameTest outputFileNT = elementNameTest(pool, "output-file");
            NameTest sourceNT = elementNameTest(pool, "source");
            NameTest schemaNT = elementNameTest(pool, "schema");
            NameTest expectedErrorNT = elementNameTest(pool, "expected-error");
            NameTest collectionNT = elementNameTest(pool, "collection");
            NameTest defaultCollectionNT = elementNameTest(pool, "defaultCollection");
            NameTest optimizationNT = elementNameTest(pool, "optimization");


            int schemaAtt = pool.allocate("", "", "schema") & NamePool.FP_MASK;
            int nameAtt = pool.allocate("", "", "name") & NamePool.FP_MASK;
            int filePathAtt = pool.allocate("", "", "FilePath") & NamePool.FP_MASK;
            int fileNameAtt = pool.allocate("", "", "FileName") & NamePool.FP_MASK;
            int idAtt = pool.allocate("", "", "ID") & NamePool.FP_MASK;
            int compareAtt = pool.allocate("", "", "compare") & NamePool.FP_MASK;
            int variableAtt = pool.allocate("", "", "variable") & NamePool.FP_MASK;
            int scenarioAtt = pool.allocate("", "", "scenario") & NamePool.FP_MASK;
            int explainAtt = pool.allocate("", "", "explain") & NamePool.FP_MASK;
            int assertAtt = pool.allocate("", "", "assert") & NamePool.FP_MASK;
            int versionAtt = pool.allocate("", "", "version") & NamePool.FP_MASK;
            int specVersionAtt = pool.allocate("", "", "spec-version") & NamePool.FP_MASK;

            /**
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

            DocumentInfo exceptionsDoc = saConfig.buildDocument(
                    new StreamSource(new File(saxonDir + "/exceptions.xml"))
            );

            NameTest exceptionTestsNT = new NameTest(Type.ELEMENT, pool.allocate("", "", "tests"), pool);
            AxisIterator exceptionTestCases = exceptionsDoc.iterateAxis(Axis.DESCENDANT, exceptionTestsNT);
            while (true) {
                NodeInfo testCase = (NodeInfo)exceptionTestCases.next();
                if (testCase == null) {
                    break;
                }
                String name = testCase.getStringValue();
                StringTokenizer tok = new StringTokenizer(name);
                while (tok.hasMoreElements()) {
                    exceptions.add(tok.nextElement());
                }
            }


            DocumentInfo catalog = saConfig.buildDocument(
                    new StreamSource(new File(testSuiteDir + "/XQTScatalog.xml"))
            );



            /**
             * If compiling tests to Java, generate a batch script to perform the Java compilations
             */

            if (compile) {
                compileScript = new FileWriter(
                        new File(saxonDir + "compile.cmd"));
                compileScript.write("set JAVA=\"c:\\Program Files\\java\\jdk1.6.0_05\"\n");
                //compileScript.write("set CLASSPATH=\"C:\\Documents and Settings\\Mike\\IdeaProjects\\Saxon 8.x\\Saxon\\classes\";c:\\Javalib\\jsdk2.1\\servlet.jar\n");
                compileScript.write("set CLASSPATH=e:\\saxon-build\\9.2.0.1\\eej\\saxon9ee.jar;e:\\Javalib\\jsdk2.1\\servlet.jar\n");
            }

            String dateTime = DateTimeValue.getCurrentDateTime(null).getStringValue();
            Properties outputProps = new Properties();
            outputProps.setProperty("method", "xml");
            outputProps.setProperty("indent", indent);
            results.write("<test-suite-result xmlns='http://www.w3.org/2005/02/query-test-XQTSResult'>");
            results.write(" <implementation name='Saxon-EE' version='" + Version.getProductVersion() +
                    "' anonymous-result-column='false'>\n");
            results.write("  <organization name='Saxonica' website='http://www.saxonica.com/' anonymous='false'/>\n");
            results.write("  <submittor name='Michael Kay' title='Director' email='mike@saxonica.com'/>\n");
            outputDiscretionaryItems();
            results.write(" </implementation>\n");
            results.write(" <syntax>XQuery</syntax>");
            results.write(" <test-run dateRun='" + dateTime + "'>");
            results.write("   <test-suite version='1.0.4'/>");
            results.write("   <transformation/>");
            results.write("   <comparison/>");
            results.write("   <otherComments/>");
            results.write(" </test-run>");

            /**
             * Load all the schemas
             */

            AxisIterator schemas = catalog.iterateAxis(Axis.DESCENDANT, schemaNT);
            while (true) {
                NodeInfo schemaElement = (NodeInfo)schemas.next();
                if (schemaElement == null) {
                    break;
                }
                String fileName = schemaElement.getAttributeValue(fileNameAtt);
                monitor.println("Loading schema " + fileName);
                StreamSource ss = new StreamSource(new File(testSuiteDir + fileName));
                saConfig.addSchemaSource(ss);

            }

            /**
             * Unless we are compiling tests to Java, load the source documents
             */

            if (!compile) {
                AxisIterator sources = catalog.iterateAxis(Axis.DESCENDANT, sourceNT);
                while (true) {
                    NodeInfo sourceElement = (NodeInfo)sources.next();
                    if (sourceElement == null) {
                        break;
                    }
                    String schema = sourceElement.getAttributeValue(schemaAtt);
                    String id = sourceElement.getAttributeValue(idAtt);
                    String fileName = sourceElement.getAttributeValue(fileNameAtt);
                    monitor.println("Loading source " + fileName);
                    Source ss = new StreamSource(new File(testSuiteDir + fileName));
                    if (schema != null) {
                        ss = AugmentedSource.makeAugmentedSource(ss);
                        ((AugmentedSource)ss).setSchemaValidationMode(Validation.STRICT);
                    }
                    try {
                        DocumentInfo doc = saConfig.buildDocument(ss);
                        documentCache.put(id, doc) ;
                    } catch (XPathException e) {
                        monitor.println("** invalid source document");
                    }

                }
            }

            AxisIterator testCases = catalog.iterateAxis(Axis.DESCENDANT, testCaseNT);
            int groupsize = 0;
            String prevGroup = null;
            while (true) {
                NodeInfo testCase = (NodeInfo)testCases.next();
                if (testCase == null) {
                    break;
                }

                String testName = testCase.getAttributeValue(nameAtt);
                boolean optimizationOK = true;
                if (testPattern != null && !testPattern.matcher(testName).matches()) {
                    continue;
                }
                if (onwards) {
                    testPattern = null;
                }
                if (exceptions.contains(testName)) {
                    continue;
                }
                if (isExcluded(testName)) {
                    continue;
                }
                monitor.println("Test " + testName);
                log.println("Test " + testName);

                String filePath = testCase.getAttributeValue(filePathAtt);
                if (filePath.startsWith("StaticTyping")) {
                    continue;
                }
                //NodeInfo testInput = getChildElement(testCase, inputFileNT);

                NodeInfo query = getChildElement(testCase, queryNT);
                String queryName = query.getAttributeValue(nameAtt);
                String languageVersion = query.getAttributeValue(versionAtt);
                if (languageVersion == null) {
                    languageVersion = specVersion;
                }

                String absQueryName = testSuiteDir + "/Queries/" +
                        (unfolded ? "XQUnfolded/" : "XQuery/") +
                        filePath + queryName + ".xq";

                String outputFile = null;

                if (compile) {} else {
                    if (runCompiled) {} else {
                        StaticQueryContext env = saConfig.newStaticQueryContext();
                        env.setModuleURIResolver(new XQTSModuleURIResolver(testCase));
                        env.setBaseURI(new File(absQueryName).toURI().toString());
                        env.setLanguageVersion(languageVersion);
                        XQueryExpression xqe;

                        try {
                            xqe = env.compileQuery(new FileInputStream(absQueryName), "UTF-8");
                        } catch (XPathException err) {
                            processError(err, testCase, testName, filePath + queryName + ".xq", expectedErrorNT, specVersionAtt);
                            continue;
                        }

                        NodeInfo optElement = getChildElement(testCase, optimizationNT);
                        if (optElement != null) {
                            String explain = optElement.getAttributeValue(explainAtt);
                            if ("true".equals(explain) || "1".equals(explain)) {
                                ExpressionPresenter presenter = new ExpressionPresenter(saConfig);
                                xqe.explain(presenter);
                                presenter.close();
                            }
                            String assertion = optElement.getAttributeValue(assertAtt);
                            if (assertion != null) {
                                TinyBuilder builder = new TinyBuilder();
                                builder.setPipelineConfiguration(saConfig.makePipelineConfiguration());
                                ExpressionPresenter presenter = new ExpressionPresenter(saConfig, builder);
                                xqe.explain(presenter);
                                presenter.close();
                                NodeInfo expressionTree = builder.getCurrentRoot();
                                XPathEvaluator xpe = new XPathEvaluator(saConfig);
                                XPathExpression exp = xpe.createExpression(assertion);
                                try {
                                    Boolean bv = (Boolean)exp.evaluateSingle(expressionTree);
                                    if (bv == null || !bv.booleanValue()) {
                                        log.println("** Optimization assertion failed");
                                        optimizationOK = false;
                                    }
                                } catch (Exception e) {
                                    log.println("** Optimization assertion result is not a boolean: " + assertion +
                                            "(" + e.getMessage() + ")");

                                }
                            }
                        }

                        DynamicQueryContext dqc = new DynamicQueryContext(saConfig);

                        NodeInfo contextItemElement = getChildElement(testCase, contextItemNT);
                        if (contextItemElement != null) {
                            DocumentInfo contextNode = loadDocument(contextItemElement.getStringValue());
                            dqc.setContextItem(contextNode);
                        }

                        processInputQueries(testCase, inputQueryNT, variableAtt, nameAtt, filePath, dqc);

                        processInputDocuments(testCase, inputFileNT, variableAtt, dqc);

                        setQueryParameters(catalog, testCase, dqc, inputUriNT, variableAtt, collectionNT, idAtt);

                        NodeInfo defaultCollection = getChildElement(testCase, defaultCollectionNT);
                        if (defaultCollection != null) {
                            String docName = defaultCollection.getStringValue();
                            NodeInfo collectionElement = getCollectionElement(catalog, docName, collectionNT, idAtt);
                            CollectionURIResolver r =
                                            new XQTSCollectionURIResolver(catalog, collectionElement, true);
                            saConfig.setCollectionURIResolver(r);
                        }


                        // Run the query

                        String outputDir = saxonDir + "/results/" + filePath;
                        if (outputDir.endsWith("/")) {
                            outputDir = outputDir.substring(0, outputDir.length()-1);
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
                        } catch (XPathException err) {
                            processError(err, testCase, testName, filePath + queryName + ".xq", expectedErrorNT, specVersionAtt);
                            continue;
                        }
                    }
                }

                // Compare the results

                boolean resultsMatched = false;
                String possibleMatch = null;
                SequenceIterator expectedResults = testCase.iterateAxis(Axis.CHILD, outputFileNT);
                boolean multipleResults = false;
                SequenceIterator ccc = expectedResults.getAnother();
                ccc.next();
                if (ccc.next() != null) {
                    multipleResults = true;
                }
                while (true) {
                    NodeInfo outputFileElement = (NodeInfo)expectedResults.next();
                    if (outputFileElement == null) {
                        break;
                    }
                    String appliesTo = outputFileElement.getAttributeValue(specVersionAtt);
                    if (appliesTo != null && !appliesTo.contains(specVersion)) {
                        continue; // results apply to a different version
                    }
                    String resultsDir = testSuiteDir + "/ExpectedTestResults/" + filePath;
                    String resultsPath = resultsDir + outputFileElement.getStringValue();
                    String comparisonMethod = outputFileElement.getAttributeValue(compareAtt);
                    String comparisonResult;
                    if (comparisonMethod.equals("Ignore")) {
                        comparisonResult = "true";
                    } else {
                        comparisonResult = compare(outputFile, resultsPath, comparisonMethod, multipleResults);
                    }
                    if (comparisonResult.equals("true")) {
                        // exact match
                        results.write("  <test-case name='" + testName + "' result='pass'" +
                                (optimizationOK ? "" : " comment='check optimization'") +
                                "/>\n");
                        resultsMatched = true;
                        break;
                    } else if (comparisonResult.equals("false")) {
                        //continue;
                    } else {
                        possibleMatch = comparisonResult;
                        //continue;
                    }
                }

                if (!resultsMatched) {
                    if (multipleResults) {
                        log.println("*** Failed to match any of the permitted results");
                    }
                    NodeInfo expectedError = null;
                    SequenceIterator eit = testCase.iterateAxis(Axis.CHILD, expectedErrorNT);
                    while (true) {
                        NodeInfo e = (NodeInfo)eit.next();
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

                    if (possibleMatch != null) {
                        results.write("  <test-case name='" + testName + "' result='pass' comment='" +
                                possibleMatch + "'/>\n");
                    } else if (expectedError != null) {
                        results.write("  <test-case name='" + testName + "' result='fail' comment='" +
                                "expected " + expectedError.getStringValue() + ", got success'/>\n");
                    } else {
                        results.write("  <test-case name='" + testName + "' result='fail'/>\n");
                    }
                    results.write("  <?file " + filePath + queryName + ".xq ?>\n");
                }
            }

            results.write("</test-suite-result>");
            results.close();
            if (compile) {
                compileScript.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setQueryParameters(DocumentInfo catalog, NodeInfo testCase, DynamicQueryContext dqc, NameTest inputUriNT, int variableAtt, NameTest collectionNT, int idAtt) throws XPathException {
        SequenceIterator inputURIs = testCase.iterateAxis(Axis.CHILD, inputUriNT);
        while (true) {
            NodeInfo inputURI = (NodeInfo)inputURIs.next();
            if (inputURI == null) {
                break;
            }
            String variableName = inputURI.getAttributeValue(variableAtt);
            variableName = toClarkName(variableName);
            if (variableName != null) {
                String docName = inputURI.getStringValue();
                if (docName.startsWith("collection")) {
                    NodeInfo collectionElement = getCollectionElement(catalog, docName, collectionNT, idAtt);
                    CollectionURIResolver r =
                            new XQTSCollectionURIResolver(catalog, collectionElement, false);
                    saConfig.setCollectionURIResolver(r);
                    dqc.setParameterValue(variableName, new AnyURIValue(docName));
                } else {
                    DocumentInfo doc = loadDocument(docName);
                    if (doc == null) {
                        dqc.setParameterValue(variableName, new AnyURIValue("error-document" + docName));
                    } else {
                        String uri = doc.getSystemId();
                        dqc.setParameterValue(variableName, new AnyURIValue(uri));
                    }
                }
            }
        }
    }

    private void processInputDocuments(NodeInfo testCase, NameTest inputFileNT, int variableAtt, DynamicQueryContext dqc) throws XPathException {
        SequenceIterator inputFiles = testCase.iterateAxis(Axis.CHILD, inputFileNT);
        while (true) {
            NodeInfo inputFile = (NodeInfo)inputFiles.next();
            if (inputFile == null) {
                break;
            }
            String variableName = inputFile.getAttributeValue(variableAtt);
            variableName = toClarkName(variableName);
            if (variableName != null) {
                DocumentInfo inputDoc = loadDocument(inputFile.getStringValue());
                dqc.setParameterValue(variableName, inputDoc);
                //System.err.println("Set parameter " + variableName + " := " + inputDoc.getSystemId());
            }
        }
    }

    private void processInputQueries(NodeInfo testCase, NameTest inputQueryNT, int variableAtt, int nameAtt, String filePath, DynamicQueryContext dqc) throws XPathException, IOException {
        SequenceIterator inputQueries = testCase.iterateAxis(Axis.CHILD, inputQueryNT);
        while (true) {
            NodeInfo inputQuery = (NodeInfo)inputQueries.next();
            if (inputQuery == null) {
                break;
            }
            String variableName = inputQuery.getAttributeValue(variableAtt);
            variableName = toClarkName(variableName);
            if (variableName != null) {
                String preQueryName = inputQuery.getAttributeValue(nameAtt);
                String subQueryFile = testSuiteDir + "/Queries/XQuery/" + filePath + preQueryName + ".xq";
                StaticQueryContext sqc2 = saConfig.newStaticQueryContext();
                XQueryExpression subQuery = sqc2.compileQuery(new FileReader(subQueryFile));
                SequenceIterator subQueryResult = subQuery.iterator(new DynamicQueryContext(saConfig));
                dqc.setParameterValue(variableName, SequenceExtent.makeSequenceExtent(subQueryResult));
            }
        }
    }

    private NodeInfo getCollectionElement(DocumentInfo catalog, String docName, NameTest collectionNT, int idAtt) {
        NodeInfo collectionElement = null;
        AxisIterator colls = catalog.iterateAxis(Axis.DESCENDANT, collectionNT);
        while (true) {
            NodeInfo coll = (NodeInfo)colls.next();
            if (coll == null) {
                break;
            }
            if (docName.equals(coll.getAttributeValue(idAtt))) {
                collectionElement = coll;
            }
        }
        return collectionElement;
    }

//    protected String getResultDirectoryName() {
//        return "SaxonDriver";
//    }

    protected boolean isExcluded(String testName) {
        return testName.startsWith("dotnet");
    }

    private static String toClarkName(String variableName) {
        // Crude handling of QName-valued variables (there aren't many in the catalog!)
        if (variableName == null) {
            return null;
        }
        if (variableName.startsWith("local:")) {
            return "{http://www.w3.org/2005/xquery-local-functions}" + variableName.substring(6);
        } else {
            return variableName;
        }

    }


    /**
     * Construct source object. This method allows subclassing e.g. to build a DOM or XOM source.
     * @param xml
     */

    private DocumentInfo loadDocument(String xml) {
        return (DocumentInfo)documentCache.get(xml);
    }

    /**
     * Process a static or dynamic error
     */

    private void processError(XPathException err, NodeInfo testCase, String testName, String queryPath, NameTest expectedErrorNT, int specVersionAtt)
    throws java.io.IOException {
        String actualError = err.getErrorCodeLocalPart();
        AxisIterator expectedErrors = testCase.iterateAxis(Axis.CHILD, expectedErrorNT);
        FastStringBuffer expected = new FastStringBuffer(20);
        while (true) {
            NodeInfo expectedError = (NodeInfo)expectedErrors.next();
            if (expectedError == null) {
                break;
            }
            String appliesTo = expectedError.getAttributeValue(specVersionAtt);
            if (appliesTo != null && !appliesTo.contains(specVersion)) {
                continue; // results apply to a different version
            }
            if (expectedError.getStringValue().equals(actualError) ||
                    expectedError.getStringValue().equals("*")) {
                results.write("  <test-case name='" + testName + "' result='pass'/>\n");
                return;
            }
            expected.append(expectedError.getStringValue());
            expected.append(" ");
        }
        if (expected.length() > 0) {
            results.write("  <test-case name='" + testName + "' result='pass'" +
                    " comment='expected " + expected + ", got " + actualError + "'/>\n");
        } else {
            results.write("  <test-case name='" + testName + "' result='fail'" +
                    " comment='expected success, got " + actualError + "'/>\n");
        }
        results.write("  <?file " + queryPath + " ?>\n");

    }


    static CanonicalXML canon = new CanonicalXML();

    private String compare(String outfile, String reffile, String comparator, boolean silent) {
        if (reffile == null) {
            log.println("*** No reference results available");
            return "false";
        }
        File outfileFile = new File(outfile);
        File reffileFile = new File(reffile);

        if (!reffileFile.exists()) {
            log.println("*** No reference results available");
            return "false";
        }

        // try direct comparison first

        String refResult = null;
        String actResult = null;

        try {
            // This is decoding bytes assuming the platform default encoding
            FileReader reader1 = new FileReader(outfileFile);
            FileReader reader2 = new FileReader(reffileFile);
            char[] contents1 = new char[(int)outfileFile.length()];
            char[] contents2 = new char[(int)reffileFile.length()];
            int size1 = reader1.read(contents1, 0, (int)outfileFile.length());
            int size2 = reader2.read(contents2, 0, (int)reffileFile.length());
            int offset1 = 0;
            int offset2 = 0;
            if (contents1[0]=='\u00ef' && contents1[1]=='\u00bb' && contents1[2]=='\u00bf') {
                offset1 += 3;
            }
            if (contents2[0]=='\u00ef' && contents2[1]=='\u00bb' && contents2[2]=='\u00bf') {
                offset2 += 3;
            }
            actResult = (size1==-1 ? "" : new String(contents1, offset1, size1-offset1));
            refResult = (size2==-1 ? "" : new String(contents2, offset2, size2-offset2));

            actResult = normalizeNewlines(actResult);
            refResult = normalizeNewlines(refResult);
            if (actResult.equals(refResult)) {
                return "true";
            }
            if (size1 == 0) {
                if (!silent) {
                    log.println("** ACTUAL RESULTS EMPTY; REFERENCE RESULTS LENGTH " + size2);
                }
                return "false";
            }
            if (size2 == 0) {
                if (!silent) {
                    log.println("** REFERENCED RESULTS EMPTY; ACTUAL RESULTS LENGTH " + size2);
                }
                return "false";
            }
        } catch (Exception e) {
        }

        // HTML: can't do logical comparison

        if (comparator.equals("html-output")) {
            // TODO: Tidy gets upset by byte-order-marks. Use the strings constructed above as input.
            try {
                Tidy tidy = new Tidy();
                tidy.setXmlOut(true);
                tidy.setQuiet(true);
                tidy.setShowWarnings(false);
                tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
                InputStream in1 = new FileInputStream(outfile);
                File xml1 = new File(outfile + ".xml");
                xml1.createNewFile();
                OutputStream out1 = new FileOutputStream(xml1);
                tidy.parse(in1, out1);
                InputStream in2 = new FileInputStream(reffile);
                File xml2 = new File(reffile + ".xml");
                xml2.createNewFile();
                OutputStream out2 = new FileOutputStream(xml2);
                tidy.parse(in2, out2);
                return compare(xml1.toString(), xml2.toString(), "xml", silent);
            } catch (IOException e) {
                e.printStackTrace();
                return "false";
            }
        } else if (comparator.equals("xhtml-output")) {
            refResult = canonizeXhtml(refResult);
            actResult = canonizeXhtml(actResult);
            return Boolean.toString((actResult.equals(refResult)));

        } else if (comparator.equals("Fragment") || comparator.equals("Text")) {
            try {
                // try two comparison techniques hoping one will work...
                boolean b = false;
                try {
                    b = compareFragments2(actResult, refResult, outfile, silent);
                } catch (Exception err1) {
                    log.println("XQTS: First comparison attempt failed " + err1.getMessage() + ", trying again");
                }
                if (!b) {
                    log.println("XQTS: First comparison attempt failed, trying again");
                    b = compareFragments(outfileFile, reffileFile, outfile, silent);
                }
                return Boolean.toString(b);
            } catch (Exception err2) {
                log.println("Failed to compare results for: " + outfile);
                err2.printStackTrace();
                return "false";
            }
        } else if (comparator.equals("Inspect")) {
            log.println("** Inspect results by hand");
            return "true";
        } else {
            // convert both files to Canonical XML and compare them again
            try {
                InputSource out = new InputSource(outfileFile.toURL().toString());
                InputSource ref = new InputSource(reffileFile.toURL().toString());
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
                        log.println("*** Match after stripping whitespace nodes: " + outfile);
                        return "*** Match after stripping whitespace nodes";
                    } else {
                        if (!silent) {
                            log.println("Mismatch with reference results: " + outfile);
                            log.println("REFERENCE RESULTS:");
                            log.println(truncate(refxml));
                            log.println("ACTUAL RESULTS:");
                            log.println(truncate(outxml));
                            findDiff(refxml, outxml);
                        }
                        return "false";
                    }
                } else {
                    return "true";
                }
            } catch (Exception err) {
                try {
                    log.println("Failed to compare results for: " + outfile + ": " + err.getMessage());
                    log.println("** Attempting XML Fragment comparison");
                    //boolean b = compareFragments(outfileFile, reffileFile, outfile, silent);
                    boolean b = compareFragments2(actResult, refResult, outfile, silent);
                    log.println("** " + (b ? "Success" : "Still different"));
                    return Boolean.toString(b);
                } catch (Exception err2) {
                    log.println("Again failed to compare results for: " + outfile);
                    err2.printStackTrace();
                }
                return "false";
            }
        }
    }

    Templates xhtmlCanonizer;

    private String canonizeXhtml(String input) {
        try {
            Templates canonizer = getXhtmlCanonizer();
            Transformer t = canonizer.newTransformer();
            StringWriter sw = new StringWriter();
            StreamResult r = new StreamResult(sw);
            InputSource is = new InputSource(new StringReader(input));
            SAXSource ss = new SAXSource(resultParser, is);
            t.transform(ss, r);
            return sw.toString();
        } catch (TransformerConfigurationException err) {
            log.println("*** Failed to compile XHTML canonicalizer stylesheet");
        } catch (TransformerException err) {
            log.println("*** Failed while running XHTML canonicalizer stylesheet");
        }
        return "";
    }

    private Templates getXhtmlCanonizer() throws TransformerConfigurationException {
        if (xhtmlCanonizer == null) {
            Source source = new StreamSource(new File(saxonDir + "/canonizeXhtml.xsl"));
            xhtmlCanonizer = tfactory.newTemplates(source);
        }
        return xhtmlCanonizer;
    }

    private boolean compareFragments(File outfileFile, File reffileFile, String outfile, boolean silent) {
        // if we can't parse the output as a document, try it as an external entity, with space stripping
        String outurl = outfileFile.toURI().toString();
        String refurl = reffileFile.toURI().toString();
        String outdoc = "<?xml version='1.1'?><!DOCTYPE doc [ <!ENTITY e SYSTEM '" + outurl + "'>]><doc>&e;</doc>";
        String refdoc = "<?xml version='1.1'?><!DOCTYPE doc [ <!ENTITY e SYSTEM '" + refurl + "'>]><doc>&e;</doc>";
        InputSource out2 = new InputSource(new StringReader(outdoc));
        InputSource ref2 = new InputSource(new StringReader(refdoc));
        String outxml2 = canon.toCanonicalXML(fragmentParser, out2, true);
        String refxml2 = canon.toCanonicalXML(fragmentParser, ref2, true);
        if (outxml2 != null && refxml2 != null && !outxml2.equals(refxml2)) {
            if (!silent) {
                log.println("Mismatch with reference results: " + outfile);
                log.println("REFERENCE RESULTS:");
                log.println(truncate(refxml2));
                log.println("ACTUAL RESULTS:");
                log.println(truncate(outxml2));
                findDiff(refxml2, outxml2);
            }
            return false;
        } else if (outxml2 == null) {
            log.println("Cannot canonicalize actual results");
            return false;
        } else if (refxml2 == null) {
            log.println("Cannot canonicalize reference results");
            return false;
        }
        return true;
    }

    /**
     * With this method of fragment comparison we build the wrapper document ourselves. This is
     * mainly to circumvent a Java XML parsing bug
     * @param outFragment
     * @param refFragment
     * @param outfile
     * @param silent
     * @return
     */

    private boolean compareFragments2(String outFragment, String refFragment, String outfile, boolean silent) {
        if (outFragment == null) {
            outFragment = "";
        }
        if (outFragment.startsWith("<?xml")) {
            int x = outFragment.indexOf("?>");
            outFragment = outFragment.substring(x+2);
        }
        if (refFragment == null) {
            refFragment = "";
        }
        if (refFragment.startsWith("<?xml")) {
            int x = refFragment.indexOf("?>");
            refFragment = refFragment.substring(x+2);
        }

        String outdoc = "<?xml version='1.1'?><doc>" + outFragment.trim() + "</doc>";
        String refdoc = "<?xml version='1.1'?><doc>" + refFragment.trim() + "</doc>";
        InputSource out2 = new InputSource(new StringReader(outdoc));
        InputSource ref2 = new InputSource(new StringReader(refdoc));
        String outxml2 = canon.toCanonicalXML(fragmentParser, out2, false);
        String refxml2 = canon.toCanonicalXML(fragmentParser, ref2, false);
        if (outxml2 != null && refxml2 != null && !outxml2.equals(refxml2)) {
            // Try again with whitespace stripping
            InputSource out3 = new InputSource(new StringReader(outdoc));
            InputSource ref3 = new InputSource(new StringReader(refdoc));
            String outxml3 = canon.toCanonicalXML(fragmentParser, out3, true);
            String refxml3 = canon.toCanonicalXML(fragmentParser, ref3, true);
            if (outxml3 != null && refxml3 != null && !outxml3.equals(refxml3)) {
                if (!silent) {
                    log.println("Mismatch with reference results: " + outfile);
                    log.println("REFERENCE RESULTS:");
                    log.println(truncate(refxml2));
                    log.println("ACTUAL RESULTS:");
                    log.println(truncate(outxml2));
                    findDiff(refxml2, outxml2);
                }
                return false;
            } else {
                log.println("Matches after stripping whitespace");
                return true;
            }

        } else if (outxml2 == null) {
            log.println("Cannot canonicalize actual results");
            return false;
        } else if (refxml2 == null) {
            log.println("Cannot canonicalize reference results");
            return false;
        }
        return true;
    }


    private static String truncate(String s) {
        if (s.length() > 200) return s.substring(0, 200);
        return s;
    }

    private void findDiff(String s1, String s2) {
        FastStringBuffer sb1 = new FastStringBuffer(s1.length());
        sb1.append(s1);
        FastStringBuffer sb2 = new FastStringBuffer(s2.length());
        sb2.append(s2);
        int i = 0;
        while (true) {
            if (s1.charAt(i) != s2.charAt(i)) {
                int j = (i < 50 ? 0 : i - 50);
                int k = (i + 50 > s1.length() || i + 50 > s2.length() ? i + 1 : i + 50);
                log.println("Different at char " + i + "\n+" + s1.substring(j, k) +
                                   "\n+" + s2.substring(j, k));
                break;
            }
            if (i >= s1.length()) break;
            if (i >= s2.length()) break;
            i++;
        }
    }

    private void outputDiscretionaryItems() throws IOException {
        results.write("  <discretionary-items/>\n");
    }


    private class MyErrorListener extends StandardErrorListener {

        public String errorCode;

        public MyErrorListener(PrintStream log) {
            setErrorOutput(log);
        }

        /**
         * Receive notification of a recoverable error.
         */

        public void error(TransformerException exception) throws TransformerException {
            if (exception instanceof XPathException) {
                String code = ((XPathException)exception).getErrorCodeLocalPart();
                if (code != null) {
                    errorCode = code;
                }
                if ("FODC0005".equals(errorCode)) {
                    fatalError(exception);
                }
            }
            super.error(exception);
        }

        /**
         * Receive notification of a non-recoverable error.
         */

        public void fatalError(TransformerException exception) throws TransformerException {
            if (exception instanceof XPathException) {
                String code = ((XPathException)exception).getErrorCodeLocalPart();
                if (code != null) {
                    errorCode = code;
                }
            }
            super.fatalError(exception);
        }

        /**
         * Receive notification of a warning.
         */

        public void warning(TransformerException exception) throws TransformerException {
            if (showWarnings) {
                super.warning(exception);
            }
        }

        /**
         * Make a clean copy of this ErrorListener. This is necessary because the
         * standard error listener is stateful (it remembers how many errors there have been)
         */

        public StandardErrorListener makeAnother(int hostLanguage) {
            return new MyErrorListener(log);
        }

    }

    private String lowercase(String name) {
        FastStringBuffer sb = new FastStringBuffer(name.length());
        name = name.toLowerCase();
        for (int p=0; p<name.length(); p++) {
            char c = name.charAt(p);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String makeClassName(String groupName, String mainName) {
        FastStringBuffer sb = new FastStringBuffer(mainName.length());
        sb.append(groupName);
        sb.append(".");
        mainName = mainName.substring(0,1).toUpperCase() + mainName.substring(1).toLowerCase();
        sb.append(mainName);
        return sb.toString();
    }

    private String normalizeNewlines(String in) {
        return in.replace("\r\n", "\n");
    }
//        int cr = in.indexOf('\r');
//        if (cr >= 0 && in.charAt(cr + 1) == '\n') {
//            return in.substring(0, cr) + normalizeNewlines(in.substring(cr + 1));
//        } else {
//            return in;
//        }
//    }


}


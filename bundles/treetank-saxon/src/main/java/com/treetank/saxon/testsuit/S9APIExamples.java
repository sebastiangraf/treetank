package com.treetank.saxon.testsuit;

import net.sf.saxon.s9api.*;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

/**
 * Some examples to show how the Saxon XQuery API should be used
 */
public class S9APIExamples {

    /**
     * Class is not instantiated, so give it a private constructor
     */
    private S9APIExamples() {
    }

    /**
     * Method main
     * @param argv arguments supplied from the command line. If an argument is supplied, it should be the
     * name of a test, or "all" to run all tests, or "nonschema" to run all the tests that are not schema-aware.
     */
    public static void main(String[] argv) {

        List tests = new ArrayList();
        tests.add(new S9APIExamples.QueryA());
        tests.add(new S9APIExamples.QueryB());
        tests.add(new S9APIExamples.QueryC());
        tests.add(new S9APIExamples.QueryD());
        tests.add(new S9APIExamples.QueryE());
        tests.add(new S9APIExamples.TransformA());
        tests.add(new S9APIExamples.TransformB());
        tests.add(new S9APIExamples.TransformC());
        tests.add(new S9APIExamples.TransformD());
        tests.add(new S9APIExamples.TransformE());
        tests.add(new S9APIExamples.TransformF());
        tests.add(new S9APIExamples.XPathA());
        tests.add(new S9APIExamples.XPathB());
        tests.add(new S9APIExamples.XPathDOM());
        tests.add(new S9APIExamples.SerializeA());
        tests.add(new S9APIExamples.SerializeB());
        tests.add(new S9APIExamples.SerializeC());

        String test = "all";
        if (argv.length > 0) {
            test = argv[0];
        }


        boolean found = false;
        Iterator allTests = tests.iterator();
        while (allTests.hasNext()) {
            S9APIExamples.Test next = (S9APIExamples.Test)allTests.next();
            if (test.equals("all") ||
                    (test.equals("nonschema") && !next.needsSaxonEE()) ||
                    next.name().equals(test)) {
                found = true;
                try {
                    System.out.println("\n==== " + next.name() + "====\n");
                    next.run();
                } catch (SaxonApiException ex) {
                    handleException(ex);
                }
            }
        }

        if (!found) {
            System.err.println("Please supply a valid test name, or 'all' or 'nonschema' ('" + test + "' is invalid)");
        }


    }

    private interface Test {
        public String name();
        public boolean needsSaxonEE();
        public void run() throws SaxonApiException;
    }

    // PART 1: XQuery tests

    /**
     * Compile and execute a simple query taking no input,
     * producing a document as its result and serializing this directly to System.out
     */

    private static class QueryA implements S9APIExamples.Test {
        public String name() {
            return "QueryA";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XQueryCompiler comp = proc.newXQueryCompiler();
            XQueryExecutable exp = comp.compile("<a b='c'>{5+2}</a>");
            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "xml");
            out.setOutputProperty(Serializer.Property.INDENT, "yes");
            out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
            out.setOutputStream(System.out);
            exp.load().run(out);
        }
    }

    /**
     * Show a query compiled using a reusable XQExpression object, taking a parameter
     * (external variable), and returning a sequence of integers
     */

    private static class QueryB implements S9APIExamples.Test {
        public String name() {
            return "QueryB";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XQueryCompiler comp = proc.newXQueryCompiler();
            XQueryExecutable exp = comp.compile(
                    "declare variable $n external; for $i in 1 to $n return $i*$i");
            XQueryEvaluator qe = exp.load();
            qe.setExternalVariable(new QName("n"), new XdmAtomicValue(10));
            XdmValue result = qe.evaluate();
            int total = 0;
            for (XdmItem item: result)  {
                total += ((XdmAtomicValue)item).getLongValue();
            }
            System.out.println("Total: " + total);
        }
    }

    /**
     * Show a query taking input from the context item, reusing the compiled Expression object
     * to run more than one query in succession.
     */

    private static class QueryC implements S9APIExamples.Test {
        public String name() {
            return "QueryC";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XQueryCompiler comp = proc.newXQueryCompiler();
            XQueryExecutable exp = comp.compile("contains(., 'e')");

            XQueryEvaluator qe = exp.load();
            qe.setContextItem(new XdmAtomicValue("apple"));
            XdmValue result = qe.evaluate();
            System.out.println("apple: " + ((XdmAtomicValue)result).getBooleanValue());

            qe = exp.load();
            qe.setContextItem(new XdmAtomicValue("banana"));
            result = qe.evaluate();
            System.out.println("banana: " + ((XdmAtomicValue)result).getBooleanValue());

            qe = exp.load();
            qe.setContextItem(new XdmAtomicValue("cherry"));
            result = qe.evaluate();
            System.out.println("cherry: " + ((XdmAtomicValue)result).getBooleanValue());

         }
    }

    /**
     * Show a query producing a DOM document as its output, and passing the DOM as input to a second query.
     * Also demonstrates declaring a namespace for use in the query
     */

    private static class QueryD implements S9APIExamples.Test {
        public String name() {
            return "QueryD";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XQueryCompiler comp = proc.newXQueryCompiler();
            comp.declareNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
            XQueryExecutable exp = comp.compile("<temp>{for $i in 1 to 20 return <e>{$i}</e>}</temp>");

            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            dfactory.setNamespaceAware(true);
            Document dom;

            try {
                dom = dfactory.newDocumentBuilder().newDocument();
            } catch (ParserConfigurationException e) {
                throw new SaxonApiException(e);
            }

            exp.load().run(new DOMDestination(dom));

            XdmNode temp = proc.newDocumentBuilder().wrap(dom);

            exp = comp.compile("<out>{//e[xsd:integer(.) gt 10]}</out>");
            XQueryEvaluator qe = exp.load();
            qe.setContextItem(temp);

            Serializer out = new Serializer();
            out.setOutputStream(System.out);
            qe.run(out);

        }
    }

    /**
     * Show a query taking a SAX event stream as its input and producing a SAX event stream as
     * its output.
     */

    private static class QueryE implements S9APIExamples.Test {
        public String name() {
            return "QueryE";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XQueryCompiler comp = proc.newXQueryCompiler();
            comp.declareNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
            XQueryExecutable exp = comp.compile("<copy>{//ITEM[1]}</copy>");
            XQueryEvaluator qe = exp.load();

            File inputFile = new File("src/test/resources/data/books.xml");
            FileInputStream fis;
            try {
                fis = new FileInputStream(inputFile);
            } catch (FileNotFoundException e) {
                throw new SaxonApiException(
                        "Input file not found. The current directory should be the Saxon samples directory");
            }
            SAXSource source = new SAXSource(new InputSource(fis));
            source.setSystemId(inputFile.toURI().toString());

            qe.setSource(source);

            ContentHandler ch = new XMLFilterImpl() {

                public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                    System.out.println("Start element {" + uri + "}" + localName);
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    System.out.println("End element {" + uri + "}" + localName);
                }
            };

            qe.run(new SAXDestination(ch));

        }
    }

    // PART 2: XSLT tests

    /**
     * Compile and execute a simple transformation that applies a stylesheet to an input file,
     * and serializing the result to an output file
     */

    private static class TransformA implements S9APIExamples.Test {
        public String name() {
            return "TransformA";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XsltCompiler comp = proc.newXsltCompiler();
            XsltExecutable exp = comp.compile(new StreamSource(new File("src/test/resources/styles/books.xsl")));
            XdmNode source = proc.newDocumentBuilder().build(new StreamSource(new File("src/test/resources/data/books.xml")));
            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "html");
            out.setOutputProperty(Serializer.Property.INDENT, "yes");
            out.setOutputFile(new File("src/test/resources/output/books1.html"));
            XsltTransformer trans = exp.load();
            trans.setInitialContextNode(source);
            trans.setDestination(out);
            trans.transform();

            System.out.println("Output written to books.html");
        }
    }

    /**
     * Show a transformation that takes no input file, but accepts parameters
     */

    private static class TransformB implements S9APIExamples.Test {
        public String name() {
            return "TransformB";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XsltCompiler comp = proc.newXsltCompiler();
            XsltExecutable exp = comp.compile(new StreamSource(new File("src/test/resources/styles/tour.xsl")));
            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "html");
            out.setOutputProperty(Serializer.Property.INDENT, "yes");
            out.setOutputFile(new File("src/test/resources/output/tour.html"));
            XsltTransformer trans = exp.load();
            trans.setInitialTemplate(new QName("main"));
            trans.setDestination(out);
            trans.transform();

            System.out.println("Output written to tour.html");
        }
    }

    /**
     * Show a stylesheet being compiled once and then executed several times with different
     * source documents. The XsltTransformer object is serially reusable, but not thread-safe.
     */

    private static class TransformC implements S9APIExamples.Test {
        public String name() {
            return "TransformC";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XsltCompiler comp = proc.newXsltCompiler();
            String stylesheet =
                    "<xsl:transform version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>" +
                    "  <xsl:param name='in'/>" +
                    "  <xsl:template name='main'><xsl:value-of select=\"contains($in, 'e')\"/></xsl:template>" +
                    "</xsl:transform>";
            XsltExecutable exp = comp.compile(new StreamSource(new StringReader(stylesheet)));

            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "text");
            XsltTransformer t = exp.load();
            t.setInitialTemplate(new QName("main"));
            
            String[] fruit = {"apple", "banana", "cherry"};
            QName paramName = new QName("in");
            for (String s: fruit) {
                StringWriter sw = new StringWriter();
                out.setOutputWriter(sw);
                t.setParameter(paramName, new XdmAtomicValue(s));
                t.setDestination(out);
                t.transform();
                System.out.println(s + ": " + sw.toString());
            }

         }
    }

    /**
     * Show the output of one stylesheet being passed for processing to a second stylesheet
     */

    private static class TransformD implements S9APIExamples.Test {
        public String name() {
            return "TransformD";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XsltCompiler comp = proc.newXsltCompiler();
            XsltExecutable templates1 = comp.compile(new StreamSource(new File("src/test/resources/styles/books.xsl")));
            XdmNode source = proc.newDocumentBuilder().build(new StreamSource(new File("src/test/resources/data/books.xml")));


            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "html");
            out.setOutputProperty(Serializer.Property.INDENT, "yes");
            out.setOutputFile(new File("src/test/resources/books2.html"));
            XsltTransformer trans1 = templates1.load();
            trans1.setInitialContextNode(source);

            String stylesheet2 =
                    "<xsl:transform version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>" +
                    "  <xsl:template match='/'><xsl:value-of select=\"count(//*)\"/></xsl:template>" +
                    "</xsl:transform>";
            XsltExecutable templates2 = comp.compile(new StreamSource(new StringReader(stylesheet2)));
            XsltTransformer trans2 = templates2.load();
            XdmDestination resultTree = new XdmDestination();
            trans2.setDestination(resultTree);

            trans1.setDestination(trans2);
            trans1.transform();

            System.out.println(resultTree.getXdmNode());
        }
    }

    /**
     * Run a transformation by writing a document node to a Transformer
     */

    private static class TransformE implements S9APIExamples.Test {
        public String name() {
            return "TransformE";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XsltCompiler comp = proc.newXsltCompiler();
            XsltExecutable templates = comp.compile(new StreamSource(new File("src/test/resources/styles/books.xsl")));
            XsltTransformer transformer = templates.load();
            XdmNode source = proc.newDocumentBuilder().build(new StreamSource(new File("src/test/resources/data/books.xml")));
            Serializer out = new Serializer();
            out.setOutputStream(System.out);
            transformer.setDestination(out);
            proc.writeXdmValue(source, transformer);
        }
    }

    /**
     * Run a transformation that sends xsl:message output to a user-supplied MessageListener
     */

    private static class TransformF implements S9APIExamples.Test {
        public String name() {
            return "TransformF";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XsltCompiler comp = proc.newXsltCompiler();
            String stylesheet =
                    "<xsl:transform version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n" +
                    "  <xsl:template name='main'>\n" +
                    "    <xsl:message><msg>Reading http://www.w3.org/TR/xslt20/ ...</msg></xsl:message>/>\n" +
                    "    <exists>\n" +
                    "      <xsl:value-of select=\"doc-available('http://www.w3.org/TR/xslt20/')\"/>\n" +
                    "    </exists>\n" +
                    "    <xsl:message><msg>finishing</msg></xsl:message>\n" +
                    "  </xsl:template>\n" +
                    "</xsl:transform>";
            StringReader reader = new StringReader(stylesheet);
            StreamSource styleSource = new StreamSource(reader, "http://localhost/string");
            XsltExecutable templates = comp.compile(styleSource);
            XsltTransformer transformer = templates.load();
            transformer.setInitialTemplate(new QName("main"));
            transformer.setMessageListener(
                    new MessageListener() {
                        public void message(XdmNode content, boolean terminate, SourceLocator locator) {
                            System.err.println("MESSAGE terminate=" + (terminate?"yes":"no") + " at " + new Date());
                            System.err.println("From instruction at line " + locator.getLineNumber() +
                                    " of " + locator.getSystemId());
                            System.err.println(">>" + content.getStringValue());
                        }
                    }
            );
            Serializer out = new Serializer();
            out.setOutputStream(System.out);
            transformer.setDestination(out);
            transformer.transform();
            System.err.println("Finished TransformF");
        }
    }

    /**
     * Demonstrate navigation of an input document using XPath and native methods.
     */

    private static class XPathA implements S9APIExamples.Test {
        public String name() {
            return "XPathA";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XPathCompiler xpath = proc.newXPathCompiler();
            xpath.declareNamespace("saxon", "http://saxon.sf.net/"); // not actually used, just for demonstration

            DocumentBuilder builder = proc.newDocumentBuilder();
            builder.setLineNumbering(true);
            builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
            XdmNode booksDoc = builder.build(new File("src/test/resources/data/books.xml"));

            // find all the ITEM elements, and for each one display the TITLE child

            XPathSelector selector = xpath.compile("//ITEM").load();
            selector.setContextItem(booksDoc);
            QName titleName = new QName("TITLE");
            for (XdmItem item: selector) {
                XdmNode title = getChild((XdmNode)item, titleName);
                System.out.println(title.getNodeName() +
                        "(" + title.getLineNumber() + "): " +
                        title.getStringValue());
            }
        }

        // Helper method to get the first child of an element having a given name.
        // If there is no child with the given name it returns null

        private static XdmNode getChild(XdmNode parent, QName childName) {
            XdmSequenceIterator iter = parent.axisIterator(Axis.CHILD, childName);
            if (iter.hasNext()) {
                return (XdmNode)iter.next();
            } else {
                return null;
            }
        }
    }

    /**
     * Demonstrate use of an XPath expression with variables.
     */

    private static class XPathB implements S9APIExamples.Test {
        public String name() {
            return "XPathB";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            ItemTypeFactory itf = new ItemTypeFactory(proc);
            ItemType decimalType = itf.getAtomicType(new QName("http://www.w3.org/2001/XMLSchema", "decimal"));
            XPathCompiler xpath = proc.newXPathCompiler();
            xpath.declareVariable(new QName("http://www.example.com/", "x"), decimalType, OccurrenceIndicator.ONE);
            xpath.declareVariable(new QName("http://www.example.com/", "y"), decimalType, OccurrenceIndicator.ONE);
            xpath.declareNamespace("e", "http://www.example.com/");
            XPathExecutable xx = xpath.compile("$e:x + $e:y");
            XPathSelector selector = xx.load();
            selector.setVariable(new QName("http://www.example.com/", "x"), new XdmAtomicValue(new BigDecimal("2.5")));
            selector.setVariable(new QName("http://www.example.com/", "y"), new XdmAtomicValue(new BigDecimal("3.61")));
            XdmAtomicValue result = (XdmAtomicValue)selector.evaluateSingle();
            System.err.println("Result: " + result.getDecimalValue());
        }

    }

    /**
      * Demonstrate use of an XPath expression against a DOM source document. For this
      * sample, saxon9-dom.jar must be on the classpath
      */

     private static class XPathDOM implements S9APIExamples.Test {
         public String name() {
             return "XPathDOM";
         }
         public boolean needsSaxonEE() {
             return false;
         }
         public void run() throws SaxonApiException {
             // Build the DOM document
             File file = new File("src/test/resources/data/books.xml");
             DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
             dfactory.setNamespaceAware(true);
             javax.xml.parsers.DocumentBuilder docBuilder;
             try {
                 docBuilder = dfactory.newDocumentBuilder();
             } catch (ParserConfigurationException e) {
                 throw new SaxonApiException(e);
             }
             Document doc;
             try {
                 doc = docBuilder.parse(new InputSource(file.toURI().toString()));
             } catch (SAXException e) {
                 throw new SaxonApiException(e);
             } catch (IOException e) {
                 throw new SaxonApiException(e);
             }
             // Compile the XPath Expression
             Processor proc = new Processor(false);
             DocumentBuilder db = proc.newDocumentBuilder();
             XdmNode xdmDoc = db.wrap(doc);
             XPathCompiler xpath = proc.newXPathCompiler();
             XPathExecutable xx = xpath.compile("//ITEM/TITLE");
             // Run the XPath Expression
             XPathSelector selector = xx.load();
             selector.setContextItem(xdmDoc);
             for(XdmItem item : selector) {
                 XdmNode node = (XdmNode)item;
                 org.w3c.dom.Node element = (org.w3c.dom.Node)node.getExternalNode();
                 System.out.println(element.getTextContent());
             }
         }

     }






    /**
     * Serialize a document
     */

    private static class SerializeA implements S9APIExamples.Test {
        public String name() {
            return "SerializeA";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            DocumentBuilder builder = proc.newDocumentBuilder();
            StringReader reader = new StringReader("<a xmlns='http://a.com/' b='c'><z xmlns=''/></a>");
            XdmNode doc = builder.build(new StreamSource(reader));
            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "xml");
            out.setOutputProperty(Serializer.Property.INDENT, "yes");
            out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
            out.setOutputStream(System.out);
            proc.writeXdmValue(doc, out);
        }
    }

    /**
     * Serialize a sequence of elements
     */

    private static class SerializeB implements S9APIExamples.Test {
        public String name() {
            return "SerializeB";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            DocumentBuilder builder = proc.newDocumentBuilder();
            StringReader reader = new StringReader("<a><b>2</b><c>3</c><d>4</d></a>");
            XdmNode doc = builder.build(new StreamSource(reader));
            XPathCompiler xpath = proc.newXPathCompiler();
            XPathSelector exp = xpath.compile("reverse(*/*)").load();
            exp.setContextItem(doc);
            XdmValue children = exp.evaluate();
            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "xml");
            out.setOutputProperty(Serializer.Property.INDENT, "yes");
            out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
            out.setOutputStream(System.out);
            proc.writeXdmValue(children, out);
        }
    }

    /**
     * Serialize a sequence of atomic values
     */

    private static class SerializeC implements S9APIExamples.Test {
        public String name() {
            return "SerializeC";
        }
        public boolean needsSaxonEE() {
            return false;
        }
        public void run() throws SaxonApiException {
            Processor proc = new Processor(false);
            XPathCompiler xpath = proc.newXPathCompiler();
            XPathSelector exp = xpath.compile("5 to 10, '&', 20").load();
            XdmValue children = exp.evaluate();
            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "text");
            StringWriter sw = new StringWriter();
            out.setOutputWriter(sw);
            proc.writeXdmValue(children, out);
            System.out.println("Serialized result: " + sw.toString());
        }
    }

    /**
     * Handle an exception thrown while running one of the examples
     *
     * @param ex the exception
     */
    private static void handleException(Exception ex) {
        System.out.println("EXCEPTION: " + ex);
        ex.printStackTrace();
    }

}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Contributor(s):
//


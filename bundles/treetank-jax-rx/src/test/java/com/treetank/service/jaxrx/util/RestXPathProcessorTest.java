/**
 * 
 */
package com.treetank.service.jaxrx.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import com.treetank.exception.AbsTTException;
import com.treetank.service.jaxrx.implementation.DatabaseRepresentation;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This class tests the class {@link RestXPathProcessor}.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public class RestXPathProcessorTest {

    /**
     * The rxProcessor reference;
     */
    private final transient RestXPathProcessor rxProcessor = new RestXPathProcessor();

    /**
     * The resource name.
     */
    private static final transient String RESOURCENAME = "books";

    /**
     * Instances param books static variable.
     */
    public static final transient String PARAMBOOKS = "book";
    /**
     * Instances param author static variable.
     */
    public static final transient String PARAMAUTHOR = "author";
    /**
     * Instances param jaxrx result static variable.
     */
    public static final transient String PARAMJAXRES = "jaxrx:result";
    /**
     * Instances param rest sequence static variable.
     */
    public static final transient String PARAMJRESTSEQ = "rest:sequence";

    @BeforeClass
    public static void setUpGlobal() throws AbsTTException {
        final File dir = new File(RESTProps.STOREDBPATH);
        deleteDirectory(dir);
        final InputStream xmlInput = RestXPathProcessorTest.class.getResourceAsStream("/books.xml");
        new DatabaseRepresentation().shred(xmlInput, RESOURCENAME);
    }

    /**
     * Test method for
     * {@link org.treetank.rest.util.RestXPathProcessor#RestXPathProcessor(org.treetank.rest.util.WorkerHelper)}
     * .
     */
    @Test
    public final void testRestXPathProcessor() {
        final RestXPathProcessor reference = new RestXPathProcessor();
        assertNotNull("checks if the reference is not null and constructor works", reference);
    }

    /**
     * Test method for
     * {@link org.treetank.rest.util.RestXPathProcessor#getXpathResource(java.lang.String, java.lang.String, boolean, java.lang.Long, java.io.OutputStream, boolean)}
     * .
     * 
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TreetankException
     */
    @Test
    public final void testGetXpathResourceStringStringBooleanLongOutputStreamBoolean() throws IOException,
        SAXException, ParserConfigurationException, AbsTTException {
        String xPath = "//book";
        boolean withNodeIds = true;
        OutputStream output = new ByteArrayOutputStream();
        rxProcessor.getXpathResource(RESOURCENAME, xPath, withNodeIds, 0L, output, true);
        InputStream xmlInput = new ByteArrayInputStream(((ByteArrayOutputStream)output).toByteArray());
        Document resultDoc = xmlDocument(xmlInput);
        final NodeList bNodes = resultDoc.getElementsByTagName(PARAMBOOKS);
        assertEquals("test for items size of books is 6", 6, bNodes.getLength());
        final NodeList aNodes = resultDoc.getElementsByTagName(PARAMAUTHOR);
        assertEquals("test for items size of authors is 12", 12, aNodes.getLength());
        final NodeList rNodes = resultDoc.getElementsByTagName(PARAMJAXRES);
        assertEquals("test for existence of result element is 1", 1, rNodes.getLength());
        final NodeList iNodes = resultDoc.getElementsByTagName(PARAMJRESTSEQ);
        assertEquals("test for existence of node ids", 6, iNodes.getLength());
        xPath = "//author";
        withNodeIds = false;
        output = new ByteArrayOutputStream();
        rxProcessor.getXpathResource(RESOURCENAME, xPath, withNodeIds, null, output, true);
        xmlInput = new ByteArrayInputStream(((ByteArrayOutputStream)output).toByteArray());
        resultDoc = xmlDocument(xmlInput);
        final NodeList b2Nodes = resultDoc.getElementsByTagName(PARAMBOOKS);
        assertEquals("test for items size of books is 0", 0, b2Nodes.getLength());
        final NodeList a2Nodes = resultDoc.getElementsByTagName(PARAMAUTHOR);
        assertEquals("test for items size of authors", 12, a2Nodes.getLength());
        final NodeList r2Nodes = resultDoc.getElementsByTagName(PARAMJAXRES);
        assertEquals("test for existence of result element", 1, r2Nodes.getLength());
        final NodeList i2Nodes = resultDoc.getElementsByTagName(PARAMJRESTSEQ);
        assertEquals("test for existence of node ids", 0, i2Nodes.getLength());

    }

    /**
     * Test method for
     * {@link org.treetank.rest.util.RestXPathProcessor#getXpathResource(java.io.File, long, java.lang.String, boolean, java.lang.Long, java.io.OutputStream, boolean)}
     * .
     * 
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws TreetankException
     */
    @Test
    public final void testGetXpathResourceFileLongStringBooleanLongOutputStreamBoolean() throws SAXException,
        IOException, ParserConfigurationException, AbsTTException {
        String xPath = "//author";
        boolean withNodeIds = true;
        OutputStream output = new ByteArrayOutputStream();
        final File tnkFile = new File(RESTProps.STOREDBPATH + File.separatorChar + RESOURCENAME + ".tnk");
        rxProcessor.getXpathResource(tnkFile, 10L, xPath, withNodeIds, 0L, output, true);
        InputStream xmlInput = new ByteArrayInputStream(((ByteArrayOutputStream)output).toByteArray());
        Document resultDoc = xmlDocument(xmlInput);
        final NodeList bNodes = resultDoc.getElementsByTagName(PARAMBOOKS);
        assertEquals("test for items size of books", 0, bNodes.getLength());
        final NodeList aNodes = resultDoc.getElementsByTagName(PARAMAUTHOR);
        assertEquals("test for items size of authors is 2", 2, aNodes.getLength());
        final NodeList rNodes = resultDoc.getElementsByTagName(PARAMJAXRES);
        assertEquals("test for item of result element is 1", 1, rNodes.getLength());
        final NodeList iNodes = resultDoc.getElementsByTagName(PARAMJRESTSEQ);
        assertEquals("test for existence of node ids", 2, iNodes.getLength());
        xPath = "//author";
        withNodeIds = false;
        output = new ByteArrayOutputStream();
        rxProcessor.getXpathResource(tnkFile, 10L, xPath, withNodeIds, null, output, true);
        xmlInput = new ByteArrayInputStream(((ByteArrayOutputStream)output).toByteArray());
        resultDoc = xmlDocument(xmlInput);
        final NodeList b2Nodes = resultDoc.getElementsByTagName(PARAMBOOKS);
        assertEquals("test for item size of books", 0, b2Nodes.getLength());
        final NodeList a2Nodes = resultDoc.getElementsByTagName(PARAMAUTHOR);
        assertEquals("test for item size of authors", 2, a2Nodes.getLength());
        final NodeList r2Nodes = resultDoc.getElementsByTagName(PARAMJAXRES);
        assertEquals("test of result element", 1, r2Nodes.getLength());
        final NodeList i2Nodes = resultDoc.getElementsByTagName(PARAMJRESTSEQ);
        assertEquals("test existence of node ids", 0, i2Nodes.getLength());
    }

    /**
     * This method deletes a not empty directory.
     * 
     * @param path
     *            The director that has to be deleted.
     * @return <code>true</code> if the deletion process has been successful. <code>false</code> otherwise.
     */
    private static boolean deleteDirectory(final File path) {
        if (path.exists()) {
            final File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return path.delete();
    }

    /**
     * This method creates of an input stream an XML document.
     * 
     * @param input
     *            The input stream.
     * @return The packed XML document.
     * @throws SAXException
     *             Exception occurred.
     * @throws IOException
     *             Exception occurred.
     * @throws ParserConfigurationException
     *             Exception occurred.
     */
    private Document xmlDocument(final InputStream input) throws SAXException, IOException,
        ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
    }

}

/**
 * 
 */
package com.treetank.service.jaxrx.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class gets a stream and builds of it a Document object to perform tests
 * for an expected streaming result.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public final class DOMHelper {

    /**
     * private contructor.
     */
    private DOMHelper() {
        // private no instantiation
    }

    /**
     * This method gets an output stream from the streaming output and converts
     * it to a Document type to perform test cases.
     * 
     * @param output
     *            The output stream that has to be packed into the document.
     * @return The parsed document.
     * @throws ParserConfigurationException
     *             The error occurred.
     * @throws SAXException
     *             XML parsing exception.
     * @throws IOException
     *             An exception occurred.
     */
    public static Document buildDocument(final ByteArrayOutputStream output)
        throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final ByteArrayInputStream bais = new ByteArrayInputStream(output.toByteArray());
        final Document document = docBuilder.parse(bais);

        return document;
    }

}

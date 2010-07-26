/**
 * 
 */
package com.treetank.service.jaxrx.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.treetank.exception.TreetankException;
import com.treetank.service.jaxrx.implementation.DatabaseRepresentation;
import com.treetank.service.jaxrx.implementation.NodeIdRepresentationTest;

/**
 * This class tests {@link RESTResponseHelper}.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public class RESTResponseHelperTest {
    /**
     * name constant.
     */
    private static final String NAME = "name";

    /**
     * last revision constant.
     */
    private static final String LREV = "lastRevision";

    /**
     * shake constant.
     */
    private static final String SHAKE = "shakespeare";
    /**
     * book constant.
     */
    private static final String BOOK = "books";
    /**
     * fact constant.
     */
    private static final String FACT = "factbook";
    /**
     * ebay constant.
     */
    private static final String EBAY = "ebay";
    /**
     * resource path constant.
     */
    private static final String RESPATH = "/factbook.xml";
    /**
     * collection ending constant.
     */
    private static final String COLEND = ".col";
    /**
     * tnk ending constant.
     */
    private static final String TNKEND = ".tnk";
    /**
     * slash constant.
     */
    private static final String SLASH = "/";

    /**
     * Test method for {@link org.treetank.rest.util.RESTResponseHelper#buildResponseOfDomLR(java.util.Map)} .
     * 
     * @throws IOException
     * @throws WebApplicationException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws TreetankException
     * @throws InterruptedException
     */
    @Test
    public final void testBuildResponseOfDomLR() throws WebApplicationException, IOException,
        ParserConfigurationException, SAXException, TreetankException, InterruptedException {

        final Map<String, String> availResources = new HashMap<String, String>();
        availResources.put(FACT + TNKEND, "resource");
        availResources.put(BOOK + COLEND, "collection");
        availResources.put(EBAY + TNKEND, "resource");
        availResources.put(SHAKE + COLEND, "collection");
        final DatabaseRepresentation treeTank = new DatabaseRepresentation();
        InputStream input = NodeIdRepresentationTest.class.getClass().getResourceAsStream(RESPATH);
        treeTank.shred(input, FACT);
        input.close();
        input = NodeIdRepresentationTest.class.getClass().getResourceAsStream(RESPATH);
        treeTank.shred(input, EBAY);
        input.close();
        input = NodeIdRepresentationTest.class.getClass().getResourceAsStream(RESPATH);
        treeTank.shred(input, BOOK);
        input.close();
        input = NodeIdRepresentationTest.class.getClass().getResourceAsStream(RESPATH);
        treeTank.add(input, BOOK);
        input.close();
        input = NodeIdRepresentationTest.class.getClass().getResourceAsStream(RESPATH);
        treeTank.shred(input, SHAKE);
        input.close();
        input = NodeIdRepresentationTest.class.getClass().getResourceAsStream(RESPATH);
        treeTank.add(input, SHAKE);
        input.close();

        Node node;
        Attr attribute;

        final StreamingOutput result = RESTResponseHelper.buildResponseOfDomLR(availResources);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        result.write(outputStream);
        final Document doc = DOMHelper.buildDocument(outputStream);
        final NodeList listRes = doc.getElementsByTagName("resource");
        assertEquals("Test for the length of resource", 2, listRes.getLength());
        final NodeList listCol = doc.getElementsByTagName("collection");
        assertEquals("Test for the length of collection", 2, listCol.getLength());

        node = listRes.item(0);
        attribute = (Attr)node.getAttributes().getNamedItem(NAME);
        assertEquals("test for name factbook", SLASH + FACT, attribute.getTextContent());
        attribute = (Attr)node.getAttributes().getNamedItem(LREV);
        assertNotNull("test for existence of revision attribute", attribute);
        node = listRes.item(1);
        attribute = (Attr)node.getAttributes().getNamedItem(NAME);
        assertEquals("test for name ebay", SLASH + EBAY, attribute.getTextContent());
        attribute = (Attr)node.getAttributes().getNamedItem(LREV);
        assertNotNull("test for existence of revision attribute", attribute);

        node = listCol.item(0);
        attribute = (Attr)node.getAttributes().getNamedItem(NAME);
        assertEquals("test for name books", SLASH + SHAKE + COLEND, attribute.getTextContent());
        attribute = (Attr)node.getAttributes().getNamedItem(LREV);
        assertNull("test for existence of revision attribute in collection", attribute);
        node = listCol.item(1);
        attribute = (Attr)node.getAttributes().getNamedItem(NAME);
        assertEquals("test for name shakespeare", SLASH + BOOK + COLEND, attribute.getTextContent());
        attribute = (Attr)node.getAttributes().getNamedItem(LREV);
        assertNull("test for existence of revision attribute in collection", attribute);

        outputStream.close();

        treeTank.deleteResource(EBAY);
        treeTank.deleteResource(FACT);
        treeTank.deleteResource(BOOK + COLEND);
        treeTank.deleteResource(SHAKE + COLEND);

    }
}

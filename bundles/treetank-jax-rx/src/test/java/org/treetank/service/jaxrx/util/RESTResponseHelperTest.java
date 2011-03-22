/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * 
 */
package org.treetank.service.jaxrx.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import com.treetank.exception.AbsTTException;

import org.treetank.service.jaxrx.implementation.DatabaseRepresentation;
import org.treetank.service.jaxrx.implementation.NodeIdRepresentationTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
     * @throws AbsTTException
     * @throws InterruptedException
     */
    @Test
    public final void testBuildResponseOfDomLR() throws WebApplicationException, IOException,
        ParserConfigurationException, SAXException, AbsTTException, InterruptedException {

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

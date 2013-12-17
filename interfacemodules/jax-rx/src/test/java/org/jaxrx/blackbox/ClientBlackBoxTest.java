/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
package org.jaxrx.blackbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jaxrx.JettyServer;
import org.jaxrx.core.JaxRxConstants;
import org.jaxrx.resource.XMLResource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is a client app test for this JAX-RX layer.
 * 
 * @author Sebastian Graf, Christian Gruen, Lukas Lewandowski, University of
 *         Konstanz
 * 
 */
public class ClientBlackBoxTest {

    /**
     * The XMLResource field.
     */
    private transient XMLResource xmlResource;

    /**
     * This field contains the user specified system name.
     */
    private final transient String SYSPROPERTY = System.getProperty(JaxRxConstants.PATHPROP);

    /**
     * This field specifies the system name. Either the new specified by the
     * underlying implementation or DOM.
     */
    private final transient String SYSTEMNAME = SYSPROPERTY == null ? "dom" : SYSPROPERTY;

    /**
     * This field specifies the requested URL resource. This example requests a
     * light version of factbook.
     */
    private final transient String RESURI = "factbookLite";

    /**
     * This field specifies the server port.
     */
    private final transient static int PORT = 8091;

    /**
     * Name of JAX-RX resource.
     */
    private final transient String JAXRX = "jax-rx";

    /**
     * This field specifies the XML tag name country for test evaluation.
     */
    private final transient String COUNTRY = "country";

    /**
     * This field specifies the XML tag name jaxrx-result (result node) for test
     * evaluation.
     */
    private final transient String RESULTNODE = "jaxrx:result";

    /**
     * This field specifies the request URL to the server.
     */
    private final transient URL REQURL;

    /**
     * This field specifies the connection to the URL.
     */
    private transient HttpURLConnection connection;

    /**
     * This method
     * 
     * @throws MalformedURLException
     *             URL exception
     */
    public ClientBlackBoxTest() throws MalformedURLException {
        REQURL = new URL("http://localhost:" + PORT + "/" + SYSTEMNAME + "/" + JAXRX + "/" + RESURI);
    }

    /**
     * Global set up to start the server.
     * 
     * @throws Exception
     *             any exception
     */
    @BeforeClass
    public static void globSetUp() throws Exception {
        new JettyServer(PORT);
    }

    /**
     * Simple setUp method.
     * 
     * @throws Exception
     *             any exception
     */
    @Before
    public void setUp() throws Exception {
        xmlResource = new XMLResource();
        final InputStream testXML = ClientBlackBoxTest.class.getResourceAsStream("/factbook.xml");
        xmlResource.putResource(SYSTEMNAME, RESURI, null, testXML);
        testXML.close();
    }

    /**
     * Simple tearDown method.
     */
    @After
    public void tearDown() {
        xmlResource.deleteResource(SYSTEMNAME, RESURI, null);
    }

    /**
     * This method tests the simple get method without additional query
     * parameters.
     * 
     * @throws IOException
     *             I/O exception
     * @throws ParserConfigurationException
     *             parser exception
     * @throws SAXException
     *             SAX exception
     */
    @Test
    public void testGetRes() throws IOException, SAXException, ParserConfigurationException {
        connection = (HttpURLConnection)REQURL.openConnection();

        // Print the HTTP response code.
        int code = connection.getResponseCode();

        // Check if request was successful.
        if (code == HttpURLConnection.HTTP_OK) {
            final Document doc = xmlDocument(connection.getInputStream());
            NodeList nodes = doc.getElementsByTagName(COUNTRY);
            assertTrue("Check if more than one result", nodes.getLength() > 1);
            nodes = doc.getElementsByTagName(RESULTNODE);
            assertTrue("Check if result node is not in result: ", nodes.getLength() == 0);
        }
        assertEquals("Test if get request has been performed successful", HttpURLConnection.HTTP_OK, code);

        final URL overviewURL = new URL("http://localhost:" + PORT + "/" + SYSTEMNAME + "/" + JAXRX);
        connection = (HttpURLConnection)overviewURL.openConnection();
        code = connection.getResponseCode();

        if (code == HttpURLConnection.HTTP_OK) {
            final Document document = xmlDocument(connection.getInputStream());
            final NodeList nodes = document.getElementsByTagName("jax-rx:resource");
            assertEquals("Test if 1 resource is available", 1, nodes.getLength());
        }
        assertEquals("Test if the request has been successful", HttpURLConnection.HTTP_OK, code);
        // Close connection.
        connection.disconnect();

    }

    /**
     * This method tests a GET request with additional query parameters, such
     * like query and wrap.
     * 
     * @throws IOException
     *             I/O exception
     * @throws ParserConfigurationException
     *             parser exception
     * @throws SAXException
     *             SAX exception
     */
    @Test
    public void testGetResParams() throws IOException, SAXException, ParserConfigurationException {
        Document doc;
        Node resultNode;
        NodeList results;
        Attr attribute;

        // Simple query
        final String query = "/descendant::country[attribute::name='Austria']";

        // wrap parameter
        String wrap = "wrap=no";

        // url for a GET query
        URL queryUrl = new URL(REQURL.toString() + "?query=" + query + "&" + wrap);

        // perform query
        connection = (HttpURLConnection)queryUrl.openConnection();

        // Print the HTTP response code.
        int code = connection.getResponseCode();
        // Check if request was successful.
        if (code == HttpURLConnection.HTTP_OK) {

            doc = xmlDocument(connection.getInputStream());
            results = doc.getElementsByTagName(COUNTRY);
            assertEquals("Test if only 1 country has been received: ", 1, results.getLength());
            attribute = (Attr)results.item(0).getAttributes().getNamedItem("name");
            assertEquals("Test if expected country is Austria: ", "Austria", attribute.getTextContent());
            resultNode = doc.getElementsByTagName(RESULTNODE).item(0);
            assertNull("Test if result node does not exists: ", resultNode);

        }
        // Close connection.
        connection.disconnect();

        assertEquals("Test if get request has been performed successful", HttpURLConnection.HTTP_OK, code);

        wrap = "wrap=yes";
        queryUrl = new URL(REQURL.toString() + "?query=" + query + "&" + wrap);
        final HttpURLConnection connection2 = (HttpURLConnection)queryUrl.openConnection();
        code = connection2.getResponseCode();
        if (code == HttpURLConnection.HTTP_OK) {
            doc = xmlDocument(connection2.getInputStream());
            results = doc.getElementsByTagName(COUNTRY);
            assertEquals("Test if only 1 country has been received again: ", 1, results.getLength());
            attribute = (Attr)results.item(0).getAttributes().getNamedItem("name");
            assertEquals("Test if expected country is Austria again: ", "Austria", attribute.getTextContent());
            resultNode = doc.getElementsByTagName(RESULTNODE).item(0);
            assertNotNull("Test if result node exists: ", resultNode);
        }

        // Close connection.
        connection2.disconnect();

        assertEquals("Test if get request has been performed successful", HttpURLConnection.HTTP_OK, code);

    }

    /**
     * This method tests a put HTTP request to create a new resource.
     * 
     * @throws IOException
     *             I/O exception
     */
    @Test
    public void testPut() throws IOException {
        final String reqURL = "theNewResource";
        final URL theURL =
            new URL("http://localhost:" + PORT + "/" + SYSTEMNAME + "/" + JAXRX + "/" + reqURL);
        connection = (HttpURLConnection)theURL.openConnection();
        int code = connection.getResponseCode();
        assertEquals("Test if the resource is exists before creation: ", HttpURLConnection.HTTP_NOT_FOUND,
            code);

        connection = (HttpURLConnection)theURL.openConnection();
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        final OutputStream output = new BufferedOutputStream(connection.getOutputStream());
        final InputStream input =
            new BufferedInputStream(ClientBlackBoxTest.class.getResourceAsStream("/factbook.xml"));
        int read;
        while ((read = input.read()) != -1) {
            output.write(read);
        }
        output.flush();
        output.close();
        input.close();

        code = connection.getResponseCode();
        assertEquals("Test if the resource has been successful created", HttpURLConnection.HTTP_CREATED, code);
        connection = (HttpURLConnection)theURL.openConnection();
        code = connection.getResponseCode();
        assertEquals("Test if the resource is now available ", HttpURLConnection.HTTP_OK, code);

        final InputStream is = connection.getInputStream();
        while (is.read() != -1)
            ;

        connection.disconnect();
    }

    /**
     * This method tests the query support for the HTTP POST method.
     * 
     * @throws IOException
     *             I/O exception
     * @throws ParserConfigurationException
     *             parser exception
     * @throws SAXException
     *             SAX exception
     */
    @Test
    public void testPostQuery() throws IOException, SAXException, ParserConfigurationException {
        final String postRequest =
            "<query xmlns:jax-rx='http://jax-rx.sourceforge.net'>\n"
                + "  <text>//continent<![CDATA[[position()<3]]]></text>\n"
                + "  <parameter name='wrap' value='yes'/>\n</query>";
        connection = (HttpURLConnection)REQURL.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/query+xml");
        final BufferedOutputStream output = new BufferedOutputStream(connection.getOutputStream());
        output.write(postRequest.getBytes("UTF-8"));
        output.flush();
        output.close();
        final int code = connection.getResponseCode();
        final InputStream input = connection.getInputStream();
        final Document document = xmlDocument(input);
        final NodeList nodes = document.getElementsByTagName("continent");
        assertEquals("Test if query result is confirmed", HttpURLConnection.HTTP_OK, code);
        assertEquals("Test if the expected continent nodes are correct", 2, nodes.getLength());
        input.close();
        connection.disconnect();

    }

    /**
     * This method tests a delete request.d
     * 
     * @throws Exception
     *             any exception
     */
    @Test
    public void testDeleteRes() throws Exception {

        connection = (HttpURLConnection)REQURL.openConnection();
        int code = connection.getResponseCode();
        assertEquals("Test if the resource is existing: ", HttpURLConnection.HTTP_OK, code);
        connection = (HttpURLConnection)REQURL.openConnection();
        connection.setRequestMethod("DELETE");
        code = connection.getResponseCode();
        assertEquals("Test if deleting has been successful", HttpURLConnection.HTTP_OK, code);
        connection = (HttpURLConnection)REQURL.openConnection();
        code = connection.getResponseCode();
        assertEquals("Test if the resource is no longerexisting: ", HttpURLConnection.HTTP_NOT_FOUND, code);
        connection.disconnect();
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
